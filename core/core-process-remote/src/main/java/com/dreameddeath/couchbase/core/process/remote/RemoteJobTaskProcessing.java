/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.couchbase.core.process.remote;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.service.IHasServiceClient;
import com.dreameddeath.core.process.service.ITaskProcessingService;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.service.client.IServiceClient;
import com.dreameddeath.core.service.client.rest.IRestServiceClient;
import com.dreameddeath.couchbase.core.process.remote.factory.IRemoteClientFactory;
import com.dreameddeath.couchbase.core.process.remote.model.RemoteJobInfo;
import com.dreameddeath.couchbase.core.process.remote.model.RemoteJobProcessTask;
import com.dreameddeath.couchbase.core.process.remote.model.rest.AlreadyExistingJob;
import com.dreameddeath.couchbase.core.process.remote.model.rest.RemoteJobResultWrapper;
import com.dreameddeath.couchbase.core.process.remote.model.rest.StateInfo;
import com.dreameddeath.couchbase.core.process.remote.service.AbstractRemoteJobRestService;

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
    public boolean init(TaskContext<TJOB, TTASK> ctxt) throws TaskExecutionException {
        return false;
    }

    @Override
    public boolean preprocess(TaskContext<TJOB, TTASK> ctxt) throws TaskExecutionException {
        return false;
    }

    protected abstract <T extends RemoteJobResultWrapper<TRESP>> Class<T> getResponseClass();
    protected abstract TREQ getRequest(TaskContext<TJOB,TTASK> ctxt) throws TaskExecutionException;
    protected void updateTaskWithResponse(TTASK task, TRESP resp){}
    protected void onResponseReceived(TaskContext<TJOB,TTASK> ctxt, TRESP resp){}

    @Override
    public final boolean process(TaskContext<TJOB, TTASK> ctxt) throws TaskExecutionException {
        TTASK task = ctxt.getTask();
        if(!task.getRemoteJobInfo().getIsDone()){
            if(task.getBaseMeta().getState()== CouchbaseDocument.DocumentState.NEW){
                try {
                    ctxt.save();
                }
                catch(ValidationException |DaoException|StorageException e){
                    throw new TaskExecutionException(ctxt,"Cannot save task before processing",e);
                }
            }
            String remoteRequestUid = ctxt.getParentJob().getUid()+"/"+task.getId();
            TREQ request = getRequest(ctxt);
            try {
                Response response = getRemoteJobProcessingClient()
                        .getInstance()
                        .property(IServiceClient.USER_PROPERTY,ctxt.getUser())
                        .queryParam(AbstractRemoteJobRestService.REQUEST_UID_QUERY_PARAM,remoteRequestUid)
                        .request()
                        .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
                //Cas of duplicate
                if(response.getStatus()== Response.Status.CONFLICT.getStatusCode()){
                    AlreadyExistingJob alreadyExistingJob=response.readEntity(AlreadyExistingJob.class);
                    if(!alreadyExistingJob.requestUid.equals(remoteRequestUid)){
                        throw new TaskExecutionException(ctxt,"Conflicting Duplicate remote job "+alreadyExistingJob.key);
                    }
                    response = getRemoteJobProcessingClient()
                            .getInstance()
                            .property(IServiceClient.USER_PROPERTY,ctxt.getUser())
                            .path("{uid}/{action}")
                            .resolveTemplate("uid",alreadyExistingJob.uid)
                            .resolveTemplate("action","resume")
                            .queryParam(AbstractRemoteJobRestService.REQUEST_UID_QUERY_PARAM,ctxt.getParentJob().getUid()+"/"+task.getId())
                            .queryParam(AbstractRemoteJobRestService.REQUEST_UID_QUERY_PARAM,ctxt.getParentJob().getUid()+"/"+task.getId())
                            .request()
                            .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
                }
                if(response.getStatus()== Response.Status.OK.getStatusCode()) {
                    RemoteJobResultWrapper<TRESP> parsedResponse = response.readEntity(getResponseClass());
                    RemoteJobInfo info=task.getRemoteJobInfo();
                    info.setRemoteJobId(parsedResponse.getJobId());

                    if(parsedResponse.getJobStateInfo().state.equals(StateInfo.State.done)){
                        info.setIsDone(true);
                    }
                    else{
                        info.setIsDone(false);
                    }
                    updateTaskWithResponse(task,parsedResponse.getResult());
                    onResponseReceived(ctxt,parsedResponse.getResult());
                    return true;
                }
                else{
                    throw new TaskExecutionException(ctxt,"Error from message "+response);
                }
            }
            catch(Exception e){
                throw new TaskExecutionException(ctxt,"Error during remote execution",e);
            }
        }
        return false;
    }

    @Override
    public boolean postprocess(TaskContext<TJOB, TTASK> ctxt) throws TaskExecutionException {
        return false;
    }

    @Override
    public boolean updatejob(TaskContext<TJOB, TTASK> ctxt) throws TaskExecutionException {
        return false;
    }

    @Override
    public boolean finish(TaskContext<TJOB, TTASK> ctxt) throws TaskExecutionException {
        return false;
    }

    @Override
    public boolean cleanup(TaskContext<TJOB, TTASK> ctxt) throws TaskExecutionException {
        return false;
    }

    @Override
    public UUID getClientUUID() {
        return getRemoteJobProcessingClient().getUuid();
    }

    @Override
    public String getServiceName() {
        return getRemoteJobProcessingClient().getFullName();
    }
}
