/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

package com.dreameddeath.core.process.testing;

import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.notification.bus.EventFireResult;
import com.dreameddeath.core.notification.bus.PublishedResult;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import com.dreameddeath.core.process.model.v1.base.ProcessState;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.impl.executor.BasicTaskExecutorServiceImpl;
import com.dreameddeath.core.process.testing.exception.FakeStorageException;
import com.dreameddeath.core.session.impl.CouchbaseSession;
import com.dreameddeath.testing.couchbase.CouchbaseBucketSimulator;
import com.dreameddeath.testing.couchbase.ICouchbaseOnWriteListener;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 24/11/2016.
 */
public class TestingTaskExecutionServiceImpl<TJOB extends AbstractJob,T extends AbstractTask>  extends BasicTaskExecutorServiceImpl<TJOB,T> {
    @Override
    public Single<TaskContext<TJOB, T>> execute(TaskContext<TJOB, T> origCtxt) {
        final ICouchbaseOnWriteListener listener = new TestingFailureGeneratorWriteListener(origCtxt);
        ICouchbaseBucket bucket = ((CouchbaseSession)(origCtxt.getSession())).getClientForClass(origCtxt.getTaskClass());
        if(bucket instanceof CouchbaseBucketSimulator){
            ((CouchbaseBucketSimulator) bucket).addOnWriteListener(listener);
        }
        return manageExecute(origCtxt)
                .doFinally(()->{if(bucket instanceof CouchbaseBucketSimulator){
                    ((CouchbaseBucketSimulator) bucket).removeOnWriteListener(listener);
                }});
    }


    private Single<TaskContext<TJOB,T>> manageExecute(TaskContext<TJOB,T> origCtxt) {
        return super.execute(origCtxt).
                onErrorResumeNext(throwable -> this.manageResume(throwable,origCtxt));
    }

    private Single<TaskContext<TJOB,T>> manageResume(final Throwable origThrowable, TaskContext<TJOB,T> origCtxt) {
        Throwable throwable = origThrowable;
        boolean retry = false;
        if(throwable instanceof TaskExecutionException){
            Throwable eCause = throwable.getCause();
            if(((TaskExecutionException) throwable).getFailedNotifications().size()>0){
                List<EventFireResult> errorEventSavedFaked = ((TaskExecutionException) throwable).getFailedNotifications()
                        .stream()
                        .filter(eventFireResult -> eventFireResult.getSaveError()!=null && eventFireResult.getSaveError().getCause() instanceof FakeStorageException)
                        .collect(Collectors.toList());

                List<PublishedResult> errorPublishedFaked = ((TaskExecutionException) throwable).getFailedNotifications()
                        .stream()
                        .flatMap(eventFireResult -> eventFireResult.getResults().stream())
                        .filter(publishedResult->publishedResult.getThrowable() instanceof FakeStorageException)
                        .collect(Collectors.toList());

                if(errorEventSavedFaked.size()>0 || errorPublishedFaked.size()>0){
                    retry=true;
                    try {
                        Thread.sleep(10);
                    }
                    catch (Exception e){
                        //ignore
                    }
                }
            }
            else if(eCause!=null){
                throwable=eCause;
            }
        }
        if(throwable instanceof FakeStorageException) {
            retry=true;
        }
        if(retry){
            T effectiveTask;
            try {
                effectiveTask = origCtxt.getSession().toBlocking().blockingRefresh(origCtxt.getInternalTask());
            }
            catch(StorageException |DaoException e) {
                effectiveTask = origCtxt.getInternalTask();
            }
            TaskContext<TJOB,T> newContext=new TaskContext.Builder<>(origCtxt,effectiveTask).build();
            return Single.just(newContext)
                    .subscribeOn(Schedulers.computation())
                    .flatMap(this::manageExecute);
        }
        else{
            return Single.error(origThrowable);
        }
    }

    private class TestingFailureGeneratorWriteListener implements ICouchbaseOnWriteListener {
        private final TaskContext<TJOB, T> origContext;
        private TaskErrorState nextErrorState =TaskErrorState.ERROR_BEFORE_SAVE_TASK;
        private ProcessState.State lastStateStartErrorLoop=null;
        private ProcessState.State maxStateReached=null;
        private int currStateNbErrorLoop=0;
        private int nbMultipleConsecutiveSaves=0;
        private int nbMaxReachedReattempt=0;

        public TestingFailureGeneratorWriteListener(TaskContext<TJOB, T> origCtxt) {
            this.origContext =origCtxt;
        }

        private void manageErrorThrow(CouchbaseDocument doc, boolean isBefore) throws StorageException{
            final AbstractTask task;
            if(! (doc instanceof AbstractTask)) {
                return;
            }
            else{
                task = ((AbstractTask)doc);
                if(
                        !task.getJobUid().equals(origContext.getInternalTask().getJobUid()) ||
                        !task.getId().equals(origContext.getInternalTask().getId())
                    )
                {
                    return;
                }
            }
            final ProcessState.State currTaskState = task.getStateInfo().getState();
            if(isBefore){
                if(nextErrorState==TaskErrorState.ERROR_BEFORE_SAVE_TASK){
                    if(currTaskState==lastStateStartErrorLoop){
                        currStateNbErrorLoop++;
                        if(currStateNbErrorLoop>5*5){
                            throw new IllegalStateException("too many attempt");
                        }
                        if(currStateNbErrorLoop%5==0){
                            nbMultipleConsecutiveSaves+=currStateNbErrorLoop/5;
                        }
                        if(nbMultipleConsecutiveSaves>0){
                            nbMultipleConsecutiveSaves--;
                            return;
                        }
                    }
                    else{
                        lastStateStartErrorLoop=currTaskState;
                        if(maxStateReached==null || lastStateStartErrorLoop.ordinal()>maxStateReached.ordinal()){
                            maxStateReached=currTaskState;
                            nbMaxReachedReattempt=0;
                        }
                        else{
                            nbMaxReachedReattempt++;
                        }
                        currStateNbErrorLoop=0;
                        nbMultipleConsecutiveSaves=0;
                        if(nbMaxReachedReattempt>10){
                            throw new IllegalStateException("too many attempt");
                        }
                        if(nbMaxReachedReattempt>2){
                            return;
                        }

                    }
                    nextErrorState = TaskErrorState.ERROR_AFTER_SAVE_TASK;
                    throw new FakeStorageException();
                }
            }
            else{
                if(nextErrorState ==TaskErrorState.ERROR_AFTER_SAVE_TASK){
                    nextErrorState=TaskErrorState.NO_ERROR;
                    throw new FakeStorageException();
                }
                else if(nextErrorState==TaskErrorState.NO_ERROR){
                    nextErrorState=TaskErrorState.ERROR_BEFORE_SAVE_TASK;
                }
            }
        }

        @Override
        public <TDOC extends CouchbaseDocument> void onBeforeWrite(CouchbaseBucketSimulator.ImpactMode mode, TDOC inputDoc) throws StorageException {
            manageErrorThrow(inputDoc,true);
        }

        @Override
        public <TDOC extends CouchbaseDocument> void onAfterWrite(CouchbaseBucketSimulator.ImpactMode mode, TDOC newDoc) throws StorageException{
            manageErrorThrow(newDoc,false);
        }
    }

    private enum TaskErrorState{
        ERROR_BEFORE_SAVE_TASK,
        ERROR_AFTER_SAVE_TASK,
        NO_ERROR
    }

    private enum TaskErrorDocState{
        ERROR_BEFORE_SAVE_TASK,
        ERROR_AFTER_SAVE_TASK,
        NO_ERROR
    }
}
