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

package com.dreameddeath.couchbase.core.process.rest;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.session.ICouchbaseSessionFactory;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.utils.ProcessUtils;
import com.dreameddeath.core.service.model.AbstractExposableService;
import com.dreameddeath.couchbase.core.process.rest.model.ActionRequest;
import com.dreameddeath.couchbase.core.process.rest.model.TestDocJobCreate;
import com.dreameddeath.couchbase.core.process.rest.model.TestDocJobUpdate;
import com.dreameddeath.couchbase.core.process.rest.model.rest.TestDocJobCreateRequest;
import com.dreameddeath.couchbase.core.process.rest.model.rest.TestDocJobCreateResponse;

import javax.ws.rs.*;

/**
 * Created by Christophe Jeunesse on 04/01/2016.
 */
public class RestTestDocJobService extends AbstractExposableService {
    ICouchbaseSessionFactory sessionFactory;
    IJobExecutorClient<TestDocJobCreate> testDocJobCreateClient;
    IJobExecutorClient<TestDocJobUpdate> testDocJobUpdateClient;

    @POST
    @Path("/create")
    public TestDocJobCreateResponse runJobCreate(TestDocJobCreateRequest request, @QueryParam("submitOnly")Boolean submitOnly, @QueryParam("details") Boolean details){
        try {
            JobContext<TestDocJobCreate> result;
            if (submitOnly) {
                result =testDocJobCreateClient.submitJob(request.buildJob(), null);
            }
            else {
                result = testDocJobCreateClient.executeJob(request.buildJob(), null);
            }
            return new TestDocJobCreateResponse(result.getJob());
        }
        catch(JobExecutionException e){
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/create/{uid}")
    public TestDocJobCreateResponse getJobCreate(@PathParam("uid") String uid, @QueryParam("details") Boolean withTasks){
        ICouchbaseSession session = sessionFactory.newSession(ICouchbaseSession.SessionType.READ_ONLY,null);
        try{
            TestDocJobCreate createJob = ProcessUtils.loadJob(session, uid, TestDocJobCreate.class);
            return new TestDocJobCreateResponse(createJob);
        }
        catch(StorageException|DaoException e){
            throw new RuntimeException(e);
        }
    }

    @PUT
    @Path("/create/{uid}")
    public TestDocJobCreateResponse updateJobCreate(@PathParam("uid")String uid,ActionRequest resumeRequest){
        //if()
        return null;
    }

}
