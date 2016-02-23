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
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.model.AbstractJob;
import com.dreameddeath.core.process.service.IJobExecutorService;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.service.client.IServiceClient;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Christophe Jeunesse on 10/01/2016.
 */
public class RemoteJobExecutorService<T extends AbstractJob> implements IJobExecutorService<T>{
    private final IServiceClient client;

    public RemoteJobExecutorService(IServiceClient client) {
        this.client = client;
    }

    @Override
    public JobContext<T> execute(JobContext<T> context) throws JobExecutionException {
        try {
            if (context.getJob().getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.NEW)) {
                context.save();
            }
        }
        catch(StorageException|DaoException|ValidationException e){
            throw new JobExecutionException(context,"Cannot save job prior to it's remote execution",e);
        }
        Response response =client.getInstance()
                .path("/{uid}/resume")
                .resolveTemplate("uid",context.getJob().getUid().toString())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .property(IServiceClient.USER_PROPERTY,context.getUser())
                .put(Entity.entity(context.getJob(),MediaType.APPLICATION_JSON_TYPE));
        if(response.getStatus()== Response.Status.OK.getStatusCode()){
            try {
                T updatedJob = context.getSession().refresh(context.getJob());
                return JobContext.newContext(new JobContext.Builder<>(updatedJob).fromJobContext(context));
            }
            catch (DaoException|StorageException e){
                throw new JobExecutionException(context,"Cannot read again the job result",e);
            }
        }
        else{
            throw new JobExecutionException(context,"An error occurs during distant call");
        }
    }
}
