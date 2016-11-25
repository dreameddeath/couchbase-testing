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

package com.dreameddeath.core.process.service.impl.executor;

import com.dreameddeath.core.couchbase.exception.StorageObservableException;
import com.dreameddeath.core.dao.exception.DaoObservableException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.process.exception.IExecutionExceptionNoLog;
import com.dreameddeath.core.process.exception.JobObservableExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import com.dreameddeath.core.process.model.v1.base.ProcessState;
import com.dreameddeath.core.process.model.v1.base.ProcessState.State;
import com.dreameddeath.core.process.service.IJobExecutorService;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.JobNotificationBuildResult;
import com.dreameddeath.core.process.service.context.JobProcessingResult;
import com.dreameddeath.core.process.service.context.TaskContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;


/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public class BasicJobExecutorServiceImpl<T extends AbstractJob> implements IJobExecutorService<T> {
    private final static Logger LOG = LoggerFactory.getLogger(BasicJobExecutorServiceImpl.class);
    public Observable<JobContext<T>> onSave(JobContext<T> ctxt, ProcessState.State state) {
        return Observable.just(ctxt);
    }

    public Observable<JobContext<T>> onEndProcessing(JobContext<T> ctxt, ProcessState.State state) {
        return Observable.just(ctxt);
    }

    public Observable<JobContext<T>> manageStateProcessing(final JobContext<T> ctxt, Func1<? super JobContext<T>, ? extends Observable<? extends JobProcessingResult<T>>> processingFunc, State state, Predicate<JobContext<T>> checkPredicate) {
        try {
            if (!checkPredicate.test(ctxt)) {
                return Observable.just(ctxt).flatMap(processingFunc)
                        .flatMap(res -> {
                            res.getContext().setState(state);
                            if (res.isNeedSave()) {
                                return onSave(res.getContext(),state)
                                        .flatMap(JobContext::asyncSave);
                            }
                            return Observable.just(res.getContext());
                        })
                        .flatMap(newCtxt -> onEndProcessing(newCtxt,state));
            } else {
                return Observable.just(ctxt);
            }
        } catch (JobObservableExecutionException e) {
            return Observable.error(e);
        } catch (Throwable e) {
            return Observable.error(new JobObservableExecutionException(ctxt, "Unexpected exception", e));
        }
    }


    @Override
    public Observable<JobContext<T>> execute(JobContext<T> origCtxt) {
        try {
            return Observable.just(origCtxt)
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

            } catch (JobObservableExecutionException e) {
                this.logError(origCtxt,e);
                return Observable.error(e);
            } catch (Throwable e) {
                this.logError(origCtxt,e);
                return Observable.error(new JobObservableExecutionException(origCtxt,e));
            }

    }

    private void logError(JobContext<T> origCtxt, final Throwable orig) {
        Throwable e=orig;
        while(e!=null) {
            if(e instanceof IExecutionExceptionNoLog) {
                return;
            }
            else if (e instanceof JobObservableExecutionException) {
                e = e.getCause();
                //Skip JobExecutionException
                if (e != null && e.getCause() != null) {
                    e = e.getCause();
                }
            }
            else if(e instanceof DaoObservableException && e.getCause()!=null){
                e = e.getCause();
            }
            else if(e instanceof StorageObservableException && e.getCause()!=null) {
                e = e.getCause();
            }
            else {
                break;
            }
        }

        LOG.error("An error occurs during Job <"+origCtxt.toString()+">",e);
    }

    private Observable<? extends JobProcessingResult<T>> manageNotifications(JobContext<T> context) {
        return context.getProcessingService().buildNotifications(context)
                .flatMap(this::manageNotificationsRetry)
                .flatMap(this::submitNotifications)
                .onErrorResumeNext(throwable->this.manageError(throwable,context));
    }

    private Observable<JobProcessingResult<T>> submitNotifications(JobNotificationBuildResult<T> jobNotifBuildRes) {
        return Observable.from(jobNotifBuildRes.getEventMap().values())
                .flatMap(event->jobNotifBuildRes.getContext().getEventBus().asyncFireEvent(event,jobNotifBuildRes.getContext().getSession()))
                .toList()
                .flatMap(eventFireResults ->{
                        if(eventFireResults.stream().filter(res->!res.isSuccess()).count()>0){
                            return Observable.error(new JobObservableExecutionException(jobNotifBuildRes.getContext(),"Errors duering notifications"));
                        }
                        return JobProcessingResult.build(jobNotifBuildRes.getContext(),true);
                });
    }

    private Observable<JobNotificationBuildResult<T>> manageNotificationsRetry(final JobNotificationBuildResult<T> origJobNotificationBuildResult) {
       return Observable.from(origJobNotificationBuildResult.getContext().getInternalJob().getNotifications())
                .flatMap(eventLink -> eventLink.<Event>getEvent(origJobNotificationBuildResult.getContext().getSession()))
                .toList()
                .map(events->new JobNotificationBuildResult<>(origJobNotificationBuildResult,events, JobNotificationBuildResult.DuplicateMode.REPLACE));
    }

    private Observable<? extends JobProcessingResult<T>> executeTasks(JobContext<T> inCtxt) {
        return inCtxt.asyncSave()
                .flatMap(this::manageJobUpdatesRetry)
                .flatMap(this::executePendingTasks)
                .flatMap(this::manageResult)
                .onErrorResumeNext(throwable->this.manageError(throwable,inCtxt));
    }

    private Observable<JobProcessingResult<T>> manageError(Throwable throwable, JobContext<T> inCtxt) {
        inCtxt.getInternalJob().getBaseMeta().unfreeze();
        if(throwable instanceof JobObservableExecutionException){
            return Observable.error(throwable);
        }
        else{
            return Observable.error(new JobObservableExecutionException(inCtxt,throwable));
        }
    }

    private Observable<JobProcessingResult<T>> manageResult(JobTasksResult jobTasksResult) {
        final JobContext<T> jobCtxt=jobTasksResult.job;
        return jobCtxt.getPendingTasks()
                .toList()
                .map(taskList->{
                    if(taskList.size()>0){
                        throw new JobObservableExecutionException(jobCtxt,"Pending tasks existing");
                    }
                    return new JobProcessingResult<>(jobCtxt,true);
                });
    }

    private Observable<JobContext<T>> manageJobUpdatesRetry(final JobContext<T> inCtxt) {
        final ICouchbaseSession readOnlySession=inCtxt.getSession().getTemporaryReadOnlySession();
        inCtxt.getInternalJob().getBaseMeta().unfreeze();

        final Set<String> taskIdsUpdatedForTask= new TreeSet<>(inCtxt.getInternalJob().getJobUpdatedForTasks());
        return inCtxt.getExecutedTasks()
                .filter(task->!taskIdsUpdatedForTask.contains(task.getId()))
                .toList()
                .flatMap(listTasks->manageJobUpdateFromFilteredTasks(inCtxt,listTasks));

    }

    private Observable<JobTasksResult> executePendingTasks(JobContext<T> inCtxt){
        inCtxt.getInternalJob().getBaseMeta().freeze();
        return inCtxt.getNextExecutableTasks()
                .flatMap(this::runTask)
                .toList()
                .switchIfEmpty(Observable.just(Collections.emptyList()))
                .flatMap(listExecutedTasks->manageJobUpdate(inCtxt,listExecutedTasks))
                .flatMap(jobUpdateRes->{
                    if(jobUpdateRes.listTasks.size()>0) {
                        return executePendingTasks(jobUpdateRes.job);
                    }
                    else {
                        return Observable.just(jobUpdateRes);
                    }
                })
                ;
    }

    private Observable<TaskContext<T,AbstractTask>> runTask(TaskContext<T,AbstractTask> origTask){
       return Observable.just(origTask)
                .flatMap(taskCtxt->{
                    if(taskCtxt.isNew()){return taskCtxt.asyncSave();}
                    else{return Observable.just(taskCtxt);}
                })
                .subscribeOn(Schedulers.computation())
                .flatMap(TaskContext::execute);
    }

    private Observable<JobContext<T>> manageJobUpdateFromFilteredTasks(JobContext<T> inCtxt,List<TaskContext<T, AbstractTask>> listExecutedTasks){
        final ICouchbaseSession readOnlySession=inCtxt.getSession().getTemporaryReadOnlySession();
        final T job = inCtxt.getInternalJob();
        job.getBaseMeta().unfreeze();
        return Observable.from(listExecutedTasks)
                .flatMap(taskCtxt->{
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
                        return Observable.just(jobContext);
                    }
                });
    }

    private Observable<JobTasksResult> manageJobUpdate(JobContext<T> inCtxt, List<TaskContext<T, AbstractTask>> listExecutedTasks) {
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