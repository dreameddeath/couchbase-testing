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

package com.dreameddeath.core.process.service.impl.processor;

import com.dreameddeath.core.couchbase.exception.DocumentNotFoundException;
import com.dreameddeath.core.couchbase.exception.StorageObservableException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.process.exception.AlreadyCreatedSubJobObservableException;
import com.dreameddeath.core.process.exception.TaskObservableExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.ProcessState;
import com.dreameddeath.core.process.model.v1.tasks.SubJobProcessTask;
import com.dreameddeath.core.process.service.ITaskProcessingService;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.context.TaskNotificationBuildResult;
import com.dreameddeath.core.process.service.context.TaskProcessingResult;
import com.dreameddeath.core.process.service.context.UpdateJobTaskProcessingResult;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 25/11/2014.
 */
public abstract class StandardSubJobProcessTaskProcessingService<TPARENTJOB extends AbstractJob,TJOB extends AbstractJob,TTASK extends SubJobProcessTask<TJOB>> implements ITaskProcessingService<TPARENTJOB,TTASK> {
    @Override
    public Observable<TaskProcessingResult<TPARENTJOB,TTASK>>  init(TaskContext<TPARENTJOB,TTASK> origCtxt){
        try {
            return Observable.just(origCtxt)
                    .flatMap(this::manageRetry)
                    .map(this::toReadOnlyCtxt)
                    .flatMap(this::buildSubJob)
                    .map(this::toReadWriteCtxt)
                    .flatMap(this::setSubJobIdAndSaveCtxt)
                    .flatMap(this::saveSubJob)
                    .map(result -> new TaskProcessingResult<>(result.getCtxt(), false))
                    .onErrorResumeNext(throwable -> this.manageError(throwable, origCtxt));
        }
        catch(TaskObservableExecutionException e){
            return Observable.error(e);
        }
        catch(Throwable e){
            return Observable.error(new TaskObservableExecutionException(origCtxt,"Unexpected error",e));
        }
    }

    private Observable<TaskProcessingResult<TPARENTJOB, TTASK>> manageError(Throwable throwable, TaskContext<TPARENTJOB, TTASK> origCtxt) {
        if(throwable instanceof AlreadyCreatedSubJobObservableException){
            AlreadyCreatedSubJobObservableException processedObservableException = (AlreadyCreatedSubJobObservableException) throwable;
            return Observable.just(new TaskProcessingResult(processedObservableException.getCtxt((Class<TaskContext<TPARENTJOB,TTASK>>)origCtxt.getClass()),false));
        }
        else if(throwable instanceof TaskObservableExecutionException) {
            return Observable.error(throwable);
        }
        else{
            return Observable.error(new TaskObservableExecutionException(origCtxt,"Error during execution",throwable));
        }
    }

    private BuildSubJobResult toReadWriteCtxt(BuildSubJobResult buildSubJobResult) {
        return new BuildSubJobResult(buildSubJobResult.getCtxt().getStandardSessionContext(),buildSubJobResult.getSubJob());
    }

    private TaskContext<TPARENTJOB, TTASK> toReadOnlyCtxt(TaskContext<TPARENTJOB, TTASK> context) {
        return context.getTemporaryReadOnlySessionContext();
    }

    private Observable<BuildSubJobResult> saveSubJob(BuildSubJobResult buildSubJobResult) {
        return buildSubJobResult.getCtxt().getSession().asyncSave(buildSubJobResult.getSubJob())
                .map(savedSubJob->new BuildSubJobResult(buildSubJobResult.getCtxt(),savedSubJob));
    }

    private Observable<BuildSubJobResult> setSubJobIdAndSaveCtxt(BuildSubJobResult buildSubJobResult) {
        buildSubJobResult.getCtxt().getInternalTask().setSubJobId(buildSubJobResult.getSubJob().getUid());
        return buildSubJobResult.getCtxt().asyncSave()
                .map(newCtxt->new BuildSubJobResult(newCtxt,buildSubJobResult.getSubJob()));
    }

    private Observable<TaskContext<TPARENTJOB, TTASK>> manageRetry(TaskContext<TPARENTJOB, TTASK> ctxt) {
        if(ctxt.getInternalTask().getSubJobId()!=null){
            return ctxt.getInternalTask().getJob(ctxt.getSession())
                    .flatMap(foundJob->Observable.<TaskContext<TPARENTJOB,TTASK>>error(new AlreadyCreatedSubJobObservableException(ctxt,(TJOB)foundJob)))
                    .onErrorResumeNext(throwable -> {
                        if(!(throwable instanceof StorageObservableException) || !(((StorageObservableException)throwable).getCause() instanceof DocumentNotFoundException)){
                            return Observable.error(throwable);
                        }
                        return manageCleanup(ctxt);
                    });
        }
        return Observable.just(ctxt);
    }

    private Observable<TaskContext<TPARENTJOB, TTASK>> manageCleanup(TaskContext<TPARENTJOB, TTASK> origCtxt) {
        return cleanupBeforeRetryBuildDocument(origCtxt)
                .map(newCtxt->{
                    newCtxt.getInternalTask().setSubJobId(null);
                    return newCtxt;
                })
                .flatMap(TaskContext::asyncSave);
    }

    protected Observable<TaskContext<TPARENTJOB, TTASK>> cleanupBeforeRetryBuildDocument(TaskContext<TPARENTJOB, TTASK> origCtxt) {
        return Observable.just(origCtxt);
    }


    @Override
    public Observable<TaskProcessingResult<TPARENTJOB,TTASK>> preprocess(TaskContext<TPARENTJOB,TTASK> ctxt){
        return Observable.just(new TaskProcessingResult<>(ctxt,false));
    }

    @Override
    public Observable<TaskProcessingResult<TPARENTJOB,TTASK>> process(TaskContext<TPARENTJOB,TTASK>  ctxt) {
        return Observable.error(new TaskObservableExecutionException(ctxt.getInternalTask(), ProcessState.State.PROCESSED,"Cannot process at this level this type of task : must be handled at TaskProcessService level"));
    }

    @Override
    public Observable<TaskProcessingResult<TPARENTJOB,TTASK>> postprocess(TaskContext<TPARENTJOB,TTASK>  ctxt){
        return Observable.just(new TaskProcessingResult<>(ctxt,false));
    }

    @Override
    public Observable<TaskNotificationBuildResult<TPARENTJOB, TTASK>> buildNotifications(TaskContext<TPARENTJOB, TTASK> ctxt) {
        return TaskNotificationBuildResult.build(ctxt);
    }

    @Override
    public Observable<TaskProcessingResult<TPARENTJOB,TTASK>> cleanup(TaskContext<TPARENTJOB,TTASK>  ctxt){
        return Observable.just(new TaskProcessingResult<>(ctxt,false));
    }

    @Override
    public Observable<UpdateJobTaskProcessingResult<TPARENTJOB, TTASK>> updatejob(TPARENTJOB job, TTASK task, ICouchbaseSession session) {
        return Observable.just(new UpdateJobTaskProcessingResult<>(job,task,false));
    }

    protected abstract Observable<BuildSubJobResult> buildSubJob(TaskContext<TPARENTJOB,TTASK> ctxt);

    protected class BuildSubJobResult{
        private final TaskContext<TPARENTJOB,TTASK> ctxt;
        private final TJOB subJob;

        public BuildSubJobResult(TaskContext<TPARENTJOB, TTASK> ctxt, TJOB subJob) {
            this.ctxt = ctxt;
            this.subJob = subJob;
        }

        public TaskContext<TPARENTJOB, TTASK> getCtxt() {
            return ctxt;
        }

        public TJOB getSubJob() {
            return subJob;
        }
    }
}
