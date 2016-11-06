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

package com.dreameddeath.couchbase.core.process.remote;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.process.exception.TaskObservableExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.service.IHasServiceClient;
import com.dreameddeath.core.process.service.ITaskProcessingService;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.context.TaskProcessingResult;
import com.dreameddeath.core.process.service.context.UpdateJobTaskProcessingResult;
import com.dreameddeath.core.service.client.IServiceClient;
import com.dreameddeath.core.service.client.rest.IRestServiceClient;
import com.dreameddeath.couchbase.core.process.remote.factory.IRemoteClientFactory;
import com.dreameddeath.couchbase.core.process.remote.model.RemoteJobInfo;
import com.dreameddeath.couchbase.core.process.remote.model.RemoteJobProcessTask;
import com.dreameddeath.couchbase.core.process.remote.model.rest.AlreadyExistingJob;
import com.dreameddeath.couchbase.core.process.remote.model.rest.RemoteJobResultWrapper;
import com.dreameddeath.couchbase.core.process.remote.model.rest.StateInfo;
import com.dreameddeath.couchbase.core.process.remote.service.AbstractRemoteJobRestService;
import rx.Observable;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 14/01/2016.
 */
public abstract class RemoteJobTaskProcessing<TREQ,TRESP,TJOB extends AbstractJob,TTASK extends RemoteJobProcessTask<TREQ,TRESP>> implements ITaskProcessingService<TJOB,TTASK>, IHasServiceClient{
    private IRemoteClientFactory remoteClientFactory;
    private IRestServiceClient remoteJobProcessingClient;

    @Inject
    public void setRemoteJobClientFactory(IRemoteClientFactory remoteClientFactory) {
        synchronized (this) {
            this.remoteClientFactory = remoteClientFactory;
            this.remoteJobProcessingClient = null;
        }
    }

    public IRestServiceClient getRemoteJobProcessingClient(){
        IRestServiceClient result = this.remoteJobProcessingClient;
        if(result==null){
            synchronized (this){
                if(this.remoteJobProcessingClient==null){
                    this.remoteJobProcessingClient = remoteClientFactory.getClient(this);
                }
                result = this.remoteJobProcessingClient;
            }
        }
        return result;
    }
    @Override
    public Observable<TaskProcessingResult<TJOB,TTASK>> init(TaskContext<TJOB, TTASK> ctxt) {
        return TaskProcessingResult.build(ctxt,false);
    }

    @Override
    public Observable<TaskProcessingResult<TJOB,TTASK>> preprocess(TaskContext<TJOB, TTASK> ctxt){
        return TaskProcessingResult.build(ctxt,false);
    }

    protected abstract <T extends RemoteJobResultWrapper<TRESP>> Class<T> getResponseClass();
    protected abstract Observable<TREQ> getRequest(TaskContext<TJOB,TTASK> ctxt);
    protected void updateTaskWithResponse(TTASK task, TRESP resp){}
    protected void onResponseReceived(TaskContext<TJOB,TTASK> ctxt, TRESP resp){}

    @Override
    public final Observable<TaskProcessingResult<TJOB,TTASK>> process(TaskContext<TJOB, TTASK> ctxt){
        try{
            if(!ctxt.getInternalTask().getRemoteJobInfo().getIsDone()){
                return this.manageInitialSave(ctxt)
                    .flatMap(this::buildAndSendRequest)
                    .flatMap(this::manageDuplicateResponse)
                    .flatMap(this::manageResponse)
                    .onErrorResumeNext(throwable -> this.manageError(throwable,ctxt));
            }
            else{
                return TaskProcessingResult.build(ctxt,true);
            }
        }
        catch (TaskObservableExecutionException e) {
            return Observable.error(e);
        }
        catch(Throwable e) {
            return Observable.error(new TaskObservableExecutionException(ctxt, "Unexpected error", e));
        }
    }

    private Observable<TaskProcessingResult<TJOB, TTASK>> manageError(Throwable throwable, TaskContext<TJOB, TTASK> ctxt) {
        if(throwable instanceof TaskObservableExecutionException){
            return Observable.error(throwable);
        }
        else{
            return Observable.error(new TaskObservableExecutionException(ctxt,"Unknown error",throwable));
        }
    }

    private Observable<TaskProcessingResult<TJOB,TTASK>> manageResponse(TaskContextAndResponse taskContextAndResponse) {
        final Response response = taskContextAndResponse.response;

        if(response.getStatus()== Response.Status.OK.getStatusCode()) {
            RemoteJobResultWrapper<TRESP> parsedResponse = response.readEntity(getResponseClass());
            RemoteJobInfo info=taskContextAndResponse.ctxt.getInternalTask().getRemoteJobInfo();
            info.setRemoteJobId(parsedResponse.getJobId());

            if(parsedResponse.getJobStateInfo().state.equals(StateInfo.State.done)){
                info.setIsDone(true);
            }
            else{
                info.setIsDone(false);
            }
            TRESP responseContent = parsedResponse.getResult();
            updateTaskWithResponse(taskContextAndResponse.ctxt.getInternalTask(),responseContent);
            onResponseReceived(taskContextAndResponse.ctxt,responseContent);
            return TaskProcessingResult.build(taskContextAndResponse.ctxt,true);
        }
        else{
            return Observable.error(new TaskObservableExecutionException(taskContextAndResponse.ctxt,"Error from message "+response));
        }
    }

    private Observable<TaskContextAndResponse> manageDuplicateResponse(TaskContextAndResponse taskContextAndResponse) {
        final Response origResponse = taskContextAndResponse.response;
        //Cas of duplicate
        if(origResponse.getStatus()== Response.Status.CONFLICT.getStatusCode()){
            AlreadyExistingJob alreadyExistingJob=origResponse.readEntity(AlreadyExistingJob.class);
            if(!alreadyExistingJob.requestUid.equals(taskContextAndResponse.remoteRequestUid)){
                return Observable.error(new TaskObservableExecutionException(taskContextAndResponse.ctxt,"Conflicting Duplicate remote job "+alreadyExistingJob.key));
            }
            return getRemoteJobProcessingClient()
                    .getInstance()
                    .property(IServiceClient.USER_PROPERTY,taskContextAndResponse.ctxt.getUser())
                    .path("{uid}/{action}")
                    .resolveTemplate("uid",alreadyExistingJob.uid)
                    .resolveTemplate("action","resume")
                    .queryParam(AbstractRemoteJobRestService.REQUEST_UID_QUERY_PARAM,taskContextAndResponse.remoteRequestUid)
                    .request()
                    .put(Entity.json(""))
                    .map(response -> new TaskContextAndResponse(taskContextAndResponse.ctxt,response,taskContextAndResponse.remoteRequestUid));
        }
        else{
            return Observable.just(taskContextAndResponse);
        }
    }

    private Observable<TaskContextAndResponse> buildAndSendRequest(TaskContext<TJOB, TTASK> context) {
        final String remoteRequestUid = context.getJobUid()+"/"+context.getInternalTask().getId();
        return getRequest(context)
                .switchIfEmpty(Observable.error(new TaskObservableExecutionException(context,"Error",new IllegalStateException("The request build is empty"))))
                .flatMap(request->
                getRemoteJobProcessingClient()
                .getInstance()
                .property(IServiceClient.USER_PROPERTY, context.getUser())
                .queryParam(AbstractRemoteJobRestService.REQUEST_UID_QUERY_PARAM, remoteRequestUid)
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
                .map(response -> new TaskContextAndResponse(context, response,remoteRequestUid))
        );
    }

    private Observable<TaskContext<TJOB, TTASK>> manageInitialSave(TaskContext<TJOB, TTASK> ctxt) {
        if(ctxt.isNew()) {
            return ctxt.asyncSave();
        }
        else {
            return Observable.just(ctxt);
        }
    }

    @Override
    public Observable<TaskProcessingResult<TJOB,TTASK>> postprocess(TaskContext<TJOB, TTASK> ctxt){
        return TaskProcessingResult.build(ctxt,false);
    }

    @Override
    public Observable<UpdateJobTaskProcessingResult<TJOB, TTASK>> updatejob(TJOB job, TTASK task, ICouchbaseSession session) {
        return new UpdateJobTaskProcessingResult<>(job,task,false).toObservable();
    }

    @Override
    public Observable<TaskProcessingResult<TJOB,TTASK>> notify(TaskContext<TJOB, TTASK> ctxt) {
        return TaskProcessingResult.build(ctxt,false);
    }

    @Override
    public Observable<TaskProcessingResult<TJOB,TTASK>> finish(TaskContext<TJOB, TTASK> ctxt){
        return TaskProcessingResult.build(ctxt,false);
    }

    @Override
    public Observable<TaskProcessingResult<TJOB,TTASK>> cleanup(TaskContext<TJOB, TTASK> ctxt){
        return TaskProcessingResult.build(ctxt,false);
    }

    @Override
    public UUID getClientUUID() {
        return getRemoteJobProcessingClient().getUuid();
    }

    @Override
    public String getServiceName() {
        return getRemoteJobProcessingClient().getFullName();
    }

    private class TaskContextAndResponse {
        private final TaskContext<TJOB,TTASK> ctxt;
        private final Response response;
        private final String remoteRequestUid;

        public TaskContextAndResponse(TaskContext<TJOB, TTASK> ctxt, Response response,String remoteUid) {
            this.ctxt = ctxt;
            this.response = response;
            this.remoteRequestUid = remoteUid;
        }
    }
}
