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

import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.model.AbstractJob;
import com.dreameddeath.core.process.service.ITaskProcessingService;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.service.client.IServiceClient;
import com.dreameddeath.couchbase.core.process.remote.model.RemoteJobProcessTask;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Christophe Jeunesse on 14/01/2016.
 */
public abstract class RemoteJobTaskProcessing<TREQ,TRESP,TJOB extends AbstractJob,TTASK extends RemoteJobProcessTask<TREQ,TRESP>> implements ITaskProcessingService<TJOB,TTASK> {
    private IServiceClient remoteJobProcessingClient;

    @Inject
    public void setRemoteJobProcessingClient(IServiceClient remoteJobProcessingClient) {
        this.remoteJobProcessingClient = remoteJobProcessingClient;
    }

    @Override
    public boolean init(TaskContext<TJOB, TTASK> ctxt) throws TaskExecutionException {
        return false;
    }


    @Override
    public boolean preprocess(TaskContext<TJOB, TTASK> ctxt) throws TaskExecutionException {
        return false;
    }

    protected abstract TREQ getRequest(TaskContext<TJOB,TTASK> ctxt);

    @Override
    public final boolean process(TaskContext<TJOB, TTASK> ctxt) throws TaskExecutionException {
        TTASK task = ctxt.getTask();
        if(!task.getRemoteJobInfo().getIsDone()){
            TREQ request = getRequest(ctxt);
            try {
                Response response = remoteJobProcessingClient
                        .getInstance()
                        .property(IServiceClient.USER_PROPERTY,ctxt.getUser())
                        .request()
                        .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

            }
            catch(Exception e){

            }
        }
        return false;
    }

    @Override
    public boolean postprocess(TaskContext<TJOB, TTASK> ctxt) throws TaskExecutionException {
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
}
