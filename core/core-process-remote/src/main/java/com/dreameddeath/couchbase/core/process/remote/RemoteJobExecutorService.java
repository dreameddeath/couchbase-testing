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

import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.exception.JobObservableExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.service.IHasServiceClient;
import com.dreameddeath.core.process.service.IJobExecutorService;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.service.client.IServiceClient;
import com.dreameddeath.core.service.client.rest.IRestServiceClient;
import rx.Observable;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 10/01/2016.
 */
public class RemoteJobExecutorService<T extends AbstractJob> implements IJobExecutorService<T>,IHasServiceClient{
    private final IRestServiceClient client;

    public RemoteJobExecutorService(IRestServiceClient client) {
        this.client = client;
    }

    @Override
    public Observable<JobContext<T>> execute(JobContext<T> origCtxt)  {
        try {
            return this.manageInitialSave(origCtxt)
                    .flatMap(this::callRemoteProcessing)
                    .flatMap(this::manageResult)
                    .onErrorResumeNext(throwable -> this.manageError(throwable, origCtxt));
        }
        catch(JobObservableExecutionException e){
            return Observable.error(e);
        }
        catch(Throwable e){
            return Observable.error(new JobObservableExecutionException(origCtxt,"Unexpected error",e));
        }
    }

    private Observable<JobContext<T>> manageError(Throwable throwable, JobContext<T> origCtxt) {
        if(throwable instanceof JobObservableExecutionException){
            return Observable.error(throwable);
        }
        else{
            return Observable.error(new JobObservableExecutionException(origCtxt,throwable));
        }
    }

    private Observable<JobContext<T>> manageResult(CallResponseWithContext callResponseWithContext) {
        if(callResponseWithContext.response.getStatus()== Response.Status.OK.getStatusCode()) {
            return callResponseWithContext.jobContext.getSession()
                    .asyncGet(callResponseWithContext.jobContext.getInternalJob().getBaseMeta().getKey(), callResponseWithContext.jobContext.getJobClass())
                    .map(newJob -> new JobContext.Builder<>(newJob, callResponseWithContext.jobContext).build());
        }
        else {
            return Observable.error(new JobExecutionException(callResponseWithContext.jobContext,"An error occurs during distant call"));
        }
    }

    private Observable<CallResponseWithContext> callRemoteProcessing(final JobContext<T> context) {
            return client.getInstance()
                .path("/{uid}/resume")
                .resolveTemplate("uid",context.getInternalJob().getUid().toString())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .property(IServiceClient.USER_PROPERTY,context.getUser())
                .put(Entity.entity(context.getInternalJob(),MediaType.APPLICATION_JSON_TYPE))
                .map(response -> new CallResponseWithContext(context,response));
    }

    private Observable<JobContext<T>> manageInitialSave(JobContext<T> context) {
        if(context.isNew()){
            return context.asyncSave();
        }
        else {
            return Observable.just(context);
        }
    }

    @Override
    public UUID getClientUUID() {
        return client.getUuid();
    }

    @Override
    public String getServiceName() {
        return client.getFullName();
    }

    private class CallResponseWithContext {
        private final JobContext<T> jobContext;
        private final Response response;

        public CallResponseWithContext(JobContext<T> jobContext, Response response) {
            this.jobContext = jobContext;
            this.response = response;
        }
    }
}
