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

import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.process.exception.TaskObservableExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import com.dreameddeath.core.process.model.v1.base.ProcessState.State;
import com.dreameddeath.core.process.service.ITaskExecutorService;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.context.TaskNotificationBuildResult;
import com.dreameddeath.core.process.service.context.TaskProcessingResult;
import rx.Observable;
import rx.functions.Func1;

import java.util.function.Predicate;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public class BasicTaskExecutorServiceImpl<TJOB extends AbstractJob,T extends AbstractTask> implements ITaskExecutorService<TJOB,T> {
    public Observable<TaskContext<TJOB,T>> onSave(TaskContext<TJOB,T> ctxt, State state){
        return Observable.just(ctxt);
    }
    public Observable<TaskContext<TJOB,T>> onEndProcessing(TaskContext<TJOB,T> ctxt, State state){
        return Observable.just(ctxt);
    }

    public Observable<TaskContext<TJOB,T>> manageStateProcessing(final TaskContext<TJOB,T> ctxt, Func1<? super TaskContext<TJOB,T>,? extends Observable<? extends TaskProcessingResult<TJOB,T>>> processingFunc, State state, Predicate<TaskContext<TJOB,T>> checkPredicate){
        try{
            if(!checkPredicate.test(ctxt)){
                return Observable.just(ctxt).flatMap(processingFunc)
                        .flatMap(res->{
                            res.getContext().setState(state);
                            if(res.isNeedSave()){
                                return onSave(res.getContext(),state).flatMap(TaskContext::asyncSave);
                            }
                            return Observable.just(res.getContext());
                        })
                        .flatMap(newCtxt -> onEndProcessing(newCtxt,state) );
            }
            else{
                return Observable.just(ctxt);
            }
        }
        catch (TaskObservableExecutionException e){
            return Observable.error(e);
        }
        catch (Throwable e){
            return Observable.error(new TaskObservableExecutionException(ctxt,"Unexpected exception",e));
        }
    }


    @Override
    public Observable<TaskContext<TJOB,T>> execute(TaskContext<TJOB,T> origCtxt){
        try {
            return Observable.just(origCtxt)
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
                    .flatMap(TaskContext::asyncSave);

        }
        catch(TaskObservableExecutionException e){
            throw e;
        }
        catch (Throwable e){
            throw new TaskObservableExecutionException(origCtxt,"Unexpected Error",e);
        }
    }

    private Observable<TaskProcessingResult<TJOB,T>> runTask(final TaskContext<TJOB,T> ctxt){
        if(ctxt.isSubJobTask()) {
            return ctxt.getSubJob()
                    .flatMap(subJob -> ctxt.getJobContext().getClientFactory().buildJobClient((Class<AbstractJob>) subJob.getClass()).executeJob(subJob, ctxt.getUser()))
                    .flatMap(subJobCtxt -> TaskProcessingResult.build(ctxt, true));
        }
        else{
            return ctxt.getProcessingService().process(ctxt);
        }
    }

    private Observable<? extends TaskProcessingResult<TJOB,T>> manageNotifications(final TaskContext<TJOB,T> origContext) {
        final TaskContext<TJOB,T> readOnlyContext=origContext.getTemporaryReadOnlySessionContext();
        return readOnlyContext
                .getProcessingService().buildNotifications(readOnlyContext)
                .flatMap(taskNotificationBuildResult -> TaskNotificationBuildResult.build(taskNotificationBuildResult.getContext().getStandardSessionContext(),taskNotificationBuildResult.getEventMap().values()))
                .flatMap(this::manageNotificationsRetry)
                .flatMap(this::submitNotifications)
                .onErrorResumeNext(throwable->this.manageError(throwable,origContext));
    }

    private Observable<TaskProcessingResult<TJOB,T>> submitNotifications(TaskNotificationBuildResult<TJOB,T> jobNotifBuildRes) {
        return Observable.from(jobNotifBuildRes.getEventMap().values())
                .flatMap(event->jobNotifBuildRes.getContext().getEventBus().asyncFireEvent(event,jobNotifBuildRes.getContext().getSession()))
                .toList()
                .flatMap(eventFireResults ->{
                    if(eventFireResults.stream().filter(res->!res.isSuccess()).count()>0){
                        return Observable.error(new TaskObservableExecutionException(jobNotifBuildRes.getContext(),"Errors duering notifications"));
                    }
                    return TaskProcessingResult.build(jobNotifBuildRes.getContext(),true);
                });
    }

    private Observable<TaskNotificationBuildResult<TJOB,T>> manageNotificationsRetry(final TaskNotificationBuildResult<TJOB,T> origTaskNotificationBuildResult) {
        return Observable.from(origTaskNotificationBuildResult.getContext().getInternalTask().getNotifications())
                .flatMap(eventLink -> eventLink.<Event>getEvent(origTaskNotificationBuildResult.getContext().getSession()))
                .toList()
                .map(events->new TaskNotificationBuildResult<>(origTaskNotificationBuildResult,events, TaskNotificationBuildResult.DuplicateMode.REPLACE));
    }

    private Observable<TaskProcessingResult<TJOB,T>> manageError(Throwable throwable, TaskContext<TJOB,T> inCtxt) {
        inCtxt.getInternalTask().getBaseMeta().unfreeze();
        if(throwable instanceof TaskObservableExecutionException){
            return Observable.error(throwable);
        }
        else{
            return Observable.error(new TaskObservableExecutionException(inCtxt,"Unexpected error",throwable));
        }
    }

}
