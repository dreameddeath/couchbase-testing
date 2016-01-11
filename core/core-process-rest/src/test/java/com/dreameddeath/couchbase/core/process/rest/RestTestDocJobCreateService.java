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
import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.service.model.AbstractExposableService;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.couchbase.core.process.rest.model.ActionRequest;
import com.dreameddeath.couchbase.core.process.rest.model.TestDocJobCreate;
import com.dreameddeath.couchbase.core.process.rest.model.rest.TestDocJobCreateRequest;
import com.dreameddeath.couchbase.core.process.rest.model.rest.TestDocJobCreateResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * Created by Christophe Jeunesse on 04/01/2016.
 */
@ServiceDef(domain = "tests",name="testdocjobcreate",version = "1.0.0")
@Path("testdocjobs/create")
public class RestTestDocJobCreateService extends AbstractExposableService {
    ICouchbaseSessionFactory sessionFactory;
    IJobExecutorClient<TestDocJobCreate> testDocJobCreateClient;

    @POST
    public TestDocJobCreateResponse runJobCreate(@Context IUser user,
                                                 @QueryParam("submitOnly")Boolean submitOnly,
                                                 TestDocJobCreateRequest request){
        try {
            JobContext<TestDocJobCreate> result;
            if (submitOnly) {
                result =testDocJobCreateClient.submitJob(request.buildJob(), user);
            }
            else {
                result = testDocJobCreateClient.executeJob(request.buildJob(), user);
            }
            return new TestDocJobCreateResponse(result.getJob());
        }
        catch(JobExecutionException e){
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/{uid}")
    public TestDocJobCreateResponse getJobCreate(@Context IUser user,
                                                 @PathParam("uid") String uid){
        ICouchbaseSession session = sessionFactory.newSession(ICouchbaseSession.SessionType.READ_ONLY,user);
        try{
            TestDocJobCreate createJob = ProcessUtils.loadJob(session, uid, TestDocJobCreate.class);
            return new TestDocJobCreateResponse(createJob);
        }
        catch(StorageException|DaoException e){
            throw new RuntimeException(e);
        }
    }

    @PUT
    @Path("/{uid}/{action:cancel|resume}")
    public TestDocJobCreateResponse updateJobCreate(@Context IUser user,
                                                    @PathParam("uid")String uid,
                                                    @PathParam("action") ActionRequest actionRequest){
        if(actionRequest==null){
            throw new BadRequestException("The action is iconsistent");
        }
        try {
            ICouchbaseSession session = sessionFactory.newSession(ICouchbaseSession.SessionType.READ_ONLY, user);
            TestDocJobCreate createJob = ProcessUtils.loadJob(session, uid, TestDocJobCreate.class);
            JobContext<TestDocJobCreate> result;
            switch (actionRequest) {
                case RESUME:
                    result = testDocJobCreateClient.resumeJob(createJob, user);
                    break;
                case CANCEL:
                    result = testDocJobCreateClient.cancelJob(createJob, user);
                    break;
                default:
                    throw new NotSupportedException("Not managed action :"+actionRequest+" on job "+uid);
            }
            return new TestDocJobCreateResponse(result.getJob());
        }
        catch(StorageException|DaoException|JobExecutionException e){
            throw new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR,e);
        }
    }

}
