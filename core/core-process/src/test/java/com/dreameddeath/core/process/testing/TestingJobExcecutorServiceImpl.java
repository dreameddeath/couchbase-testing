/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.process.testing;

import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.couchbase.exception.StorageObservableException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.exception.JobObservableExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.ProcessState;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.impl.executor.BasicJobExecutorServiceImpl;
import com.dreameddeath.core.process.testing.exception.FakeStorageException;
import com.dreameddeath.core.session.impl.CouchbaseSession;
import com.dreameddeath.testing.couchbase.CouchbaseBucketSimulator;
import com.dreameddeath.testing.couchbase.ICouchbaseOnWriteListener;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 23/11/2016.
 */
public class TestingJobExcecutorServiceImpl<T extends AbstractJob> extends BasicJobExecutorServiceImpl<T> {
    @Override
    public Observable<JobContext<T>> execute(JobContext<T> origCtxt) {
        final ICouchbaseOnWriteListener listener = new TestingFailureGeneratorWriteListener(origCtxt);
        ICouchbaseBucket bucket = ((CouchbaseSession)(origCtxt.getSession())).getClientForClass(origCtxt.getJobClass());
        if(bucket instanceof CouchbaseBucketSimulator){
            ((CouchbaseBucketSimulator) bucket).addOnWriteListener(listener);
        }
        return manageExecute(origCtxt)
                .doAfterTerminate(()->{if(bucket instanceof CouchbaseBucketSimulator){
                    ((CouchbaseBucketSimulator) bucket).removeOnWriteListener(listener);
                }});
    }

    private Observable<JobContext<T>> manageExecute(JobContext<T> origCtxt) {
        return super.execute(origCtxt).
                onErrorResumeNext(throwable -> this.manageResume(throwable,origCtxt));
    }

    private Observable<JobContext<T>> manageResume(final Throwable origThrowable, JobContext<T> origCtxt) {
        Throwable throwable = origThrowable;
        if(throwable instanceof JobObservableExecutionException){
            throwable = throwable.getCause();
        }
        if(throwable instanceof JobExecutionException && throwable.getCause()!=null){
            throwable = throwable.getCause();
        }

        if(throwable instanceof StorageObservableException){
            throwable = throwable.getCause();
        }

        if(throwable instanceof FakeStorageException){
            T effectiveJob;
            try {
                effectiveJob = origCtxt.getSession().toBlocking().blockingRefresh(origCtxt.getInternalJob());
            }
            catch(StorageException|DaoException e) {
                effectiveJob = origCtxt.getInternalJob();
            }
            JobContext<T> newContext=JobContext.newContext(new JobContext.Builder<>(effectiveJob)
                    .withSession(origCtxt.getSession())
                    .withClientFactory(origCtxt.getClientFactory())
                    .withJobExecutorService(origCtxt.getExecutorService())
                    .withJobProcessingService(origCtxt.getProcessingService())
                    .withEventBus(origCtxt.getEventBus())
            );
            return manageExecute(newContext);
        }
        else{
            return Observable.error(origThrowable);
        }
    }


    private class TestingFailureGeneratorWriteListener implements ICouchbaseOnWriteListener {
        private final JobContext<T> origContext;
        private JobErrorState nextErrorState =JobErrorState.ERROR_BEFORE_SAVE;
        private ProcessState.State lastStateStartErrorLoop=null;
        private int currStateNbErrorLoop=0;

        public TestingFailureGeneratorWriteListener(JobContext<T> origContext) {
            this.origContext = origContext;
        }

        private void manageErrorThrow(CouchbaseDocument doc, boolean isBefore) throws StorageException{
            final AbstractJob job;
            if(! (doc instanceof AbstractJob)) {
                return;
            }
            else{
                job = ((AbstractJob)doc);
                if(!job.getUid().equals(origContext.getInternalJob().getUid())){
                    return;
                }
            }
            final ProcessState.State currJobState = job.getStateInfo().getState();
            if(isBefore){
                if(nextErrorState==JobErrorState.ERROR_BEFORE_SAVE){
                    if(currJobState==lastStateStartErrorLoop){
                        currStateNbErrorLoop++;
                        if(currStateNbErrorLoop>10){
                            currStateNbErrorLoop=0;
                            return;
                        }
                    }
                    else{
                        lastStateStartErrorLoop=currJobState;
                        currStateNbErrorLoop=0;
                    }
                    nextErrorState = JobErrorState.ERROR_AFTER_SAVE;
                    throw new FakeStorageException();
                }
            }
            else{
                if(nextErrorState ==JobErrorState.ERROR_AFTER_SAVE){
                    nextErrorState=JobErrorState.NO_ERROR;
                    throw new FakeStorageException();
                }
                else if(nextErrorState==JobErrorState.NO_ERROR){
                    nextErrorState=JobErrorState.ERROR_BEFORE_SAVE;
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

    private enum JobErrorState{
        ERROR_BEFORE_SAVE,
        ERROR_AFTER_SAVE,
        NO_ERROR
    }
}
