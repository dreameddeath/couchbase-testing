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

import com.dreameddeath.core.couchbase.exception.DocumentNotFoundException;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.notification.bus.EventFireResult;
import com.dreameddeath.core.notification.model.v1.EventLink;
import com.dreameddeath.core.process.exception.IExecutionExceptionNoLog;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import com.dreameddeath.core.process.model.v1.base.ProcessState.State;
import com.dreameddeath.core.process.model.v1.notification.AbstractTaskEvent;
import com.dreameddeath.core.process.service.ITaskExecutorService;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.context.TaskNotificationBuildResult;
import com.dreameddeath.core.process.service.context.TaskProcessingResult;
import com.google.common.base.Preconditions;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public class BasicTaskExecutorServiceImpl<TJOB extends AbstractJob,T extends AbstractTask> implements ITaskExecutorService<TJOB,T> {
    private final static Logger LOG = LoggerFactory.getLogger(BasicTaskExecutorServiceImpl.class);
    public Single<TaskContext<TJOB,T>> onSave(TaskContext<TJOB,T> ctxt, State state){
        return Single.just(ctxt);
    }
    public Single<TaskContext<TJOB,T>> onEndProcessing(TaskContext<TJOB,T> ctxt, State state){
        return Single.just(ctxt);
    }

    public Single<TaskContext<TJOB,T>> manageStateProcessing(final TaskContext<TJOB,T> ctxt, Function<? super TaskContext<TJOB,T>,? extends Single<? extends TaskProcessingResult<TJOB,T>>> processingFunc, State state, Predicate<TaskContext<TJOB,T>> checkPredicate){
        try{
            if(!checkPredicate.test(ctxt)){
                return Single.just(ctxt).flatMap(processingFunc)
                        .flatMap(res->{
                            res.getContext().setState(state);
                            if(res.isNeedSave()){
                                return onSave(res.getContext(),state)
                                        .flatMap(TaskContext::asyncSave);
                            }
                            return Single.just(res.getContext());
                        })
                        .flatMap(newCtxt -> onEndProcessing(newCtxt,state) );
            }
            else{
                return Single.just(ctxt);
            }
        }
        catch (Throwable e){
            return Single.error(new TaskExecutionException(ctxt,"Unexpected exception",e));
        }
    }


    @Override
    public Single<TaskContext<TJOB,T>> execute(TaskContext<TJOB,T> origCtxt){
        try {
            return Single.just(origCtxt)
                    .flatMap(ctxt->
                            manageStateProcessing(ctxt,
                                    (inCtxt)->inCtxt.getProcessingService().init(inCtxt),
                                    State.INITIALIZED,
                                    (inCtxt)->inCtxt.getTaskState().isInitialized()
                            )
                    )
                    .flatMap(ctxt->
                            manageStateProcessing(ctxt,
                                    (inCtxt)->inCtxt.getProcessingService().preprocess(inCtxt),
                                    State.PREPROCESSED,
                                    (inCtxt)->inCtxt.getTaskState().isPrepared()
                            )
                    )
                    .flatMap(ctxt->
                            manageStateProcessing(ctxt,
                                    this::runTask,
                                    State.PROCESSED,
                                    (inCtxt)->inCtxt.getTaskState().isProcessed()
                            )
                    )
                    .flatMap(ctxt->
                            manageStateProcessing(ctxt,
                                    (inCtxt)->inCtxt.getProcessingService().postprocess(inCtxt),
                                    State.POSTPROCESSED,
                                    (inCtxt)->inCtxt.getTaskState().isFinalized()
                            )
                    )
                    .flatMap(ctxt ->
                            manageStateProcessing(ctxt,
                                    this::manageNotifications,
                                    State.NOTIFIED,
                                    (inCtxt) -> inCtxt.getTaskState().isNotified()
                            )
                    )
                    .flatMap(ctxt->
                            manageStateProcessing(ctxt,
                                    (inCtxt)->inCtxt.getProcessingService().cleanup(inCtxt),
                                    State.DONE,
                                    (inCtxt)->inCtxt.getTaskState().isDone()
                            )
                    )
                    .flatMap(TaskContext::asyncSave)
                    .doOnError(throwable -> this.logError(origCtxt,throwable));

        }
        catch (Throwable e){
            return Single.error(new TaskExecutionException(origCtxt,"Unexpected Error",e));
        }
    }

    private void logError(TaskContext<TJOB, T> origCtxt, Throwable orig) {
        Throwable e=orig;
        while(e!=null) {
            Throwable eCause = e.getCause();
            if(e instanceof IExecutionExceptionNoLog) {
                return;
            }
            else if (e instanceof TaskExecutionException && eCause!=null) {
                e=eCause;
            }
            else if(e instanceof DaoException && eCause!=null){
                e = eCause;
            }
            else if(e instanceof StorageException && eCause!=null) {
                e = eCause;
            }
            else {
                break;
            }
        }

        LOG.error("An error occurs during task <"+origCtxt.toString()+">",orig);
    }

    private Single<TaskProcessingResult<TJOB,T>> runTask(final TaskContext<TJOB,T> ctxt){
        if(ctxt.isSubJobTask()) {
            Single<? extends AbstractJob> subJobObs = ctxt.getSubJob();
            Preconditions.checkNotNull(subJobObs,"Cannot get sub job observable for task {}",ctxt.getId());
            return subJobObs
                    .flatMap(subJob -> ctxt.getJobContext().getClientFactory().buildJobClient((Class<AbstractJob>) subJob.getClass()).executeJob(subJob, ctxt.getUser()))
                    .flatMap(subJobCtxt -> TaskProcessingResult.build(ctxt, true));
        }
        else{
            return ctxt.getProcessingService().process(ctxt);
        }
    }

    private Single<? extends TaskProcessingResult<TJOB,T>> manageNotifications(final TaskContext<TJOB,T> origContext) {
        final TaskContext<TJOB,T> readOnlyContext=origContext.getTemporaryReadOnlySessionContext();
        return readOnlyContext
                .getProcessingService().buildNotifications(readOnlyContext)
                .flatMap(taskNotificationBuildResult -> TaskNotificationBuildResult.build(taskNotificationBuildResult.getContext().getStandardSessionContext(),taskNotificationBuildResult.getEventMap().values()))
                .flatMap(this::manageNotificationsRetry)
                .flatMap(this::attachEvents)
                .flatMap(this::submitNotifications)
                .onErrorResumeNext(throwable->this.manageError(throwable,origContext));
    }

    private Single<TaskNotificationBuildResult<TJOB, T>> attachEvents(final TaskNotificationBuildResult<TJOB, T> origTaskNotificationBuildRes) {
        return Observable.fromIterable(origTaskNotificationBuildRes.getEventMap().values())
                .flatMapSingle(event->{
                    if(event.getBaseMeta().getKey()!=null){
                        return Single.just(event);
                    }
                    else{
                        return origTaskNotificationBuildRes.getContext().getSession().asyncBuildKey(event);
                    }
                })
                .toList()
                .flatMap(events->{
                    final List<EventLink> newListToSubmit = events.stream().map(EventLink::new).collect(Collectors.toList());
                    final List<EventLink> listExistingAttachedElements = origTaskNotificationBuildRes.getContext().getInternalTask().getNotifications();
                    final List<EventLink> listNotAttached = newListToSubmit.stream().filter(newEventLink->!listExistingAttachedElements.contains(newEventLink)).collect(Collectors.toList());
                    if(listNotAttached.size()>0) {
                        origTaskNotificationBuildRes.getContext().getInternalTask().setNotifications(newListToSubmit);
                        return origTaskNotificationBuildRes.getContext().asyncSave()
                                .flatMap(savedCtxt->TaskNotificationBuildResult.build(savedCtxt,events));
                    }
                    else {
                        return TaskNotificationBuildResult.build(origTaskNotificationBuildRes.getContext(), events);
                    }
                });
    }

    private Single<TaskProcessingResult<TJOB,T>> submitNotifications(TaskNotificationBuildResult<TJOB,T> taskNotifBuildRes) {
        return Observable.fromIterable(taskNotifBuildRes.getEventMap().values())
                .flatMapSingle(event->taskNotifBuildRes.getContext().getEventBus().asyncFireEvent(event,taskNotifBuildRes.getContext().getSession()))
                .toList()
                .flatMap(eventFireResults->{
                    List<EventFireResult<?>> failedNotifs=eventFireResults.stream().filter(res->!res.isSuccess()).collect(Collectors.toList());
                    if(failedNotifs.size()>0){
                        return Single.error(new TaskExecutionException(taskNotifBuildRes.getContext(),"Errors during notifications",failedNotifs));
                    }
                    return TaskProcessingResult.build(taskNotifBuildRes.getContext(),true);
                });
    }

    private Single<TaskNotificationBuildResult<TJOB,T>> manageNotificationsRetry(final TaskNotificationBuildResult<TJOB,T> origTaskNotificationBuildResult) {
        return Observable.fromIterable(origTaskNotificationBuildResult.getContext().getInternalTask().getNotifications())
                .flatMapMaybe(eventLink -> manageNotificationRead(eventLink,origTaskNotificationBuildResult.getContext().getSession()))
                .toList()
                .map(events->new TaskNotificationBuildResult<>(origTaskNotificationBuildResult,events, TaskNotificationBuildResult.DuplicateMode.REPLACE));
    }

    private Maybe<AbstractTaskEvent> manageNotificationRead(EventLink eventLink, ICouchbaseSession session) {
        return eventLink.<AbstractTaskEvent>getEvent(session)
                .toMaybe()
                .onErrorResumeNext(throwable-> {
                    if(throwable instanceof DocumentNotFoundException){
                        return Maybe.empty();
                    }
                    return Maybe.error(throwable);
                });
    }

    private Single<TaskProcessingResult<TJOB,T>> manageError(Throwable throwable, TaskContext<TJOB,T> inCtxt) {
        inCtxt.getInternalTask().getBaseMeta().unfreeze();
        return Single.error(throwable);
    }
}
