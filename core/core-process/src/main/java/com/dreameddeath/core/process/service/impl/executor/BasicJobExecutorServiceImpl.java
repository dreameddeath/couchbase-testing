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

package com.dreameddeath.core.process.service.impl.executor;


import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.notification.bus.EventFireResult;
import com.dreameddeath.core.process.exception.IExecutionExceptionNoLog;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import com.dreameddeath.core.process.model.v1.base.ProcessState;
import com.dreameddeath.core.process.model.v1.base.ProcessState.State;
import com.dreameddeath.core.process.service.IJobExecutorService;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.JobNotificationBuildResult;
import com.dreameddeath.core.process.service.context.JobProcessingResult;
import com.dreameddeath.core.process.service.context.TaskContext;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public class BasicJobExecutorServiceImpl<T extends AbstractJob> implements IJobExecutorService<T> {
    private final static Logger LOG = LoggerFactory.getLogger(BasicJobExecutorServiceImpl.class);
    public Single<JobContext<T>> onSave(JobContext<T> ctxt, ProcessState.State state) {
        return Single.just(ctxt);
    }

    public Single<JobContext<T>> onEndProcessing(JobContext<T> ctxt, ProcessState.State state) {
        return Single.just(ctxt);
    }

    public Single<JobContext<T>> manageStateProcessing(final JobContext<T> ctxt, Function<? super JobContext<T>, ? extends Single<? extends JobProcessingResult<T>>> processingFunc, State state, Predicate<JobContext<T>> checkPredicate) {
        try {
            if (!checkPredicate.test(ctxt)) {
                return Single.just(ctxt).flatMap(processingFunc)
                        .flatMap(res -> {
                            res.getContext().setState(state);
                            if (res.isNeedSave()) {
                                return onSave(res.getContext(),state)
                                        .flatMap(JobContext::asyncSave);
                            }
                            return Single.just(res.getContext());
                        })
                        .flatMap(newCtxt -> onEndProcessing(newCtxt,state));
            } else {
                return Single.just(ctxt);
            }
        } catch (Throwable e) {
            return Single.error(new JobExecutionException(ctxt, "Unexpected exception", e));
        }
    }


    @Override
    public Single<JobContext<T>> execute(JobContext<T> origCtxt) {
        try {
            return Single.just(origCtxt)
                    .flatMap(ctxt ->
                            manageStateProcessing(ctxt,
                                    (inCtxt) -> inCtxt.getProcessingService().init(inCtxt),
                                    State.INITIALIZED,
                                    (inCtxt) -> inCtxt.getJobState().isInitialized()
                            )
                    )
                    .flatMap(ctxt ->
                            manageStateProcessing(ctxt,
                                    (inCtxt) -> inCtxt.getProcessingService().preprocess(inCtxt),
                                    State.PREPROCESSED,
                                    (inCtxt) -> inCtxt.getJobState().isPrepared()
                            )
                    )
                    .flatMap(ctxt ->
                            manageStateProcessing(ctxt,
                                    this::executeTasks,
                                    State.PROCESSED,
                                    (inCtxt) -> inCtxt.getJobState().isProcessed()
                            )
                    )
                    .flatMap(ctxt ->
                            manageStateProcessing(ctxt,
                                    (inCtxt) -> inCtxt.getProcessingService().postprocess(inCtxt),
                                    State.POSTPROCESSED,
                                    (inCtxt) -> inCtxt.getJobState().isFinalized()
                            )
                    )
                    .flatMap(ctxt ->
                            manageStateProcessing(ctxt,
                                    (inCtxt) -> inCtxt.getProcessingService().postprocess(inCtxt),
                                    State.POSTPROCESSED,
                                    (inCtxt) -> inCtxt.getJobState().isFinalized()
                            )
                    )
                    .flatMap(ctxt ->
                            manageStateProcessing(ctxt,
                                    this::manageNotifications,
                                    State.NOTIFIED,
                                    (inCtxt) -> inCtxt.getJobState().isNotified()
                            )
                    )
                    .flatMap(ctxt ->
                            manageStateProcessing(ctxt,
                                    (inCtxt) -> inCtxt.getProcessingService().cleanup(inCtxt),
                                    State.DONE,
                                    (inCtxt) -> inCtxt.getJobState().isDone()
                            )
                    )
                    .flatMap(JobContext::asyncSave)
                    .doOnError(throwable -> this.logError(origCtxt,throwable));

            } catch (Throwable e) {
                this.logError(origCtxt,e);
                return Single.error(new JobExecutionException(origCtxt,e));
            }

    }

    private void logError(JobContext<T> origCtxt, final Throwable orig) {
        Throwable e=orig;
        while(e!=null) {
            Throwable eCause = e.getCause();
            if(e instanceof IExecutionExceptionNoLog) {
                return;
            }
            else if (e instanceof JobExecutionException && eCause!=null) {
                e=eCause;
            }
            else {
                break;
            }
        }

        LOG.error("An error occurs during Job <"+origCtxt.toString()+">",e);
    }

    private Single<? extends JobProcessingResult<T>> manageNotifications(JobContext<T> context) {
        return context.getProcessingService().buildNotifications(context)
                .flatMap(this::manageNotificationsRetry)
                .flatMap(this::submitNotifications)
                .onErrorResumeNext(throwable->this.manageError(throwable,context));
    }

    private Single<JobProcessingResult<T>> submitNotifications(JobNotificationBuildResult<T> jobNotifBuildRes) {
        return Observable.fromIterable(jobNotifBuildRes.getEventMap().values())
                .flatMap(event->jobNotifBuildRes.getContext().getEventBus().asyncFireEvent(event,jobNotifBuildRes.getContext().getSession()).toObservable())
                .toList()
                .flatMap(eventFireResults ->{
                    List<EventFireResult<?>> failedNotifs=eventFireResults.stream().filter(res->!res.isSuccess()).collect(Collectors.toList());

                    if(failedNotifs.size()>0){
                        return Single.error(new JobExecutionException(jobNotifBuildRes.getContext(),"Errors during notifications",failedNotifs));
                    }
                    return JobProcessingResult.build(jobNotifBuildRes.getContext(),true);
                });
    }

    private Single<JobNotificationBuildResult<T>> manageNotificationsRetry(final JobNotificationBuildResult<T> origJobNotificationBuildResult) {
       return Observable.fromIterable(origJobNotificationBuildResult.getContext().getInternalJob().getNotifications())
                .flatMap(eventLink -> eventLink.getEvent(origJobNotificationBuildResult.getContext().getSession()).toObservable())
                .toList()
                .map(events->new JobNotificationBuildResult<>(origJobNotificationBuildResult,events, JobNotificationBuildResult.DuplicateMode.REPLACE));
    }

    private Single<? extends JobProcessingResult<T>> executeTasks(JobContext<T> inCtxt) {
        return inCtxt.asyncSave()
                .flatMap(this::manageJobUpdatesRetry)
                .flatMap(this::executePendingTasks)
                .flatMap(this::manageResult)
                .onErrorResumeNext(throwable->this.manageError(throwable,inCtxt));
    }

    private Single<JobProcessingResult<T>> manageError(Throwable throwable, JobContext<T> inCtxt) {
        inCtxt.getInternalJob().getBaseMeta().unfreeze();
        if(throwable instanceof JobExecutionException){
            return Single.error(throwable);
        }
        else{
            return Single.error(new JobExecutionException(inCtxt,throwable));
        }
    }

    private Single<JobProcessingResult<T>> manageResult(JobTasksResult jobTasksResult) {
        final JobContext<T> jobCtxt=jobTasksResult.job;
        return jobCtxt.getPendingTasks()
                .toList()
                .map(taskList->{
                    if(taskList.size()>0){
                        throw new JobExecutionException(jobCtxt,"Pending tasks existing");
                    }
                    return new JobProcessingResult<>(jobCtxt,true);
                });
    }

    private Single<JobContext<T>> manageJobUpdatesRetry(final JobContext<T> inCtxt) {
        inCtxt.getInternalJob().getBaseMeta().unfreeze();
        final Set<String> taskIdsUpdatedForTask= new TreeSet<>(inCtxt.getInternalJob().getJobUpdatedForTasks());
        return inCtxt.getExecutedTasks()
                .filter(task->!taskIdsUpdatedForTask.contains(task.getId()))
                .toList()
                .flatMap(listTasks->manageJobUpdateFromFilteredTasks(inCtxt,listTasks));

    }

    private Single<JobTasksResult> executePendingTasks(JobContext<T> inCtxt){
        inCtxt.getInternalJob().getBaseMeta().freeze();
        return inCtxt.getNextExecutableTasks()
                .flatMapSingle(this::runTask)
                .toList()
                .flatMap(listExecutedTasks->manageJobUpdate(inCtxt,listExecutedTasks))
                .flatMap(jobUpdateRes->{
                    if(jobUpdateRes.listTasks.size()>0) {
                        return executePendingTasks(jobUpdateRes.job);
                    }
                    else {
                        return Single.just(jobUpdateRes);
                    }
                })
                ;
    }

    private Single<TaskContext<T,AbstractTask>> runTask(TaskContext<T,AbstractTask> origTask){
       return Single.just(origTask)
                .flatMap(taskCtxt->{
                    if(taskCtxt.isNew()){return taskCtxt.asyncSave();}
                    else{return Single.just(taskCtxt);}
                })
                .subscribeOn(Schedulers.computation())
                .flatMap(TaskContext::execute);
    }

    private Single<JobContext<T>> manageJobUpdateFromFilteredTasks(JobContext<T> inCtxt,List<TaskContext<T, AbstractTask>> listExecutedTasks){
        final ICouchbaseSession readOnlySession=inCtxt.getSession().getTemporaryReadOnlySession();
        final T job = inCtxt.getInternalJob();
        job.getBaseMeta().unfreeze();
        return Observable.fromIterable(listExecutedTasks)
                .flatMapSingle(taskCtxt->{
                    synchronized (job){
                        return taskCtxt.getProcessingService().updatejob(job,taskCtxt.getInternalTask(),readOnlySession);
                    }
                })
                .map(resultUpdate->{
                    synchronized (job){
                        return job.addJobUpdatedForTask(resultUpdate.getTask().getId());
                    }
                })
                .toList()
                .flatMap(listJobUpdatedResult->{
                    JobContext<T> jobContext = JobContext.newContext(new JobContext.Builder<>(job,inCtxt));
                    if(listJobUpdatedResult.stream().filter(b->b).count()>0) {
                        return jobContext.asyncSave();
                    }
                    else{
                        return Single.just(jobContext);
                    }
                });
    }

    private Single<JobTasksResult> manageJobUpdate(JobContext<T> inCtxt, List<TaskContext<T, AbstractTask>> listExecutedTasks) {
        return manageJobUpdateFromFilteredTasks(inCtxt,listExecutedTasks)
                .map(savedJobContext->new JobTasksResult(savedJobContext,listExecutedTasks));
    }

    public class JobTasksResult {
        private final JobContext<T> job;
        private final List<TaskContext<T,AbstractTask>> listTasks;

        public JobTasksResult(JobContext<T> job, List<TaskContext<T, AbstractTask>> listTasks) {
            this.job = job;
            this.listTasks = listTasks;
        }
    }
}