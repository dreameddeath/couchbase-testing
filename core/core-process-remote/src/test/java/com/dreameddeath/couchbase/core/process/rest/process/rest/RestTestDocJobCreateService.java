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

package com.dreameddeath.couchbase.core.process.rest.process.rest;

import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.couchbase.core.process.remote.model.TestDocJobCreate;
import com.dreameddeath.couchbase.core.process.remote.model.rest.ActionRequest;
import com.dreameddeath.couchbase.core.process.remote.model.rest.RemoteJobResultWrapper;
import com.dreameddeath.couchbase.core.process.remote.model.rest.TestDocJobCreateRequest;
import com.dreameddeath.couchbase.core.process.remote.model.rest.TestDocJobCreateResult;
import com.dreameddeath.couchbase.core.process.remote.service.AbstractRemoteJobRestService;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * Created by Christophe Jeunesse on 04/01/2016.
 */
@ServiceDef(domain = "tests",type = AbstractRemoteJobRestService.SERVICE_TYPE,name="testdocjobcreate",version = "1.0.0")
@Path("testdocjobs/create")
public class RestTestDocJobCreateService extends AbstractRemoteJobRestService<TestDocJobCreate,TestDocJobCreateRequest,TestDocJobCreateResult> {
    @Override
    protected TestDocJobCreate buildJobFromRequest(TestDocJobCreateRequest request) {
        return request.buildJob();
    }

    @Override
    protected TestDocJobCreateResult buildResponse(TestDocJobCreate response) {
        return new TestDocJobCreateResult(response);
    }

    public static class CartResponse extends RemoteJobResultWrapper<TestDocJobCreateResult> {
        public CartResponse(TestDocJobCreateResult result) {
            super(result);
        }
    }

    @Override
    protected final Class<CartResponse> getResponseClass(){
        return CartResponse.class;
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public void runJobCreate(@Context IUser user,
                             @QueryParam(SUBMIT_ONLY_QUERY_PARAM) Boolean submitOnly,
                             @QueryParam(REQUEST_UID_QUERY_PARAM) String requestUid,
                             TestDocJobCreateRequest request,
                             @Suspended final AsyncResponse asyncResponse){
        super.doRunJobCreate(user,submitOnly,requestUid,request,asyncResponse);
    }

    @GET
    @Path("/{uid}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getJob(@Context IUser user,
                       @PathParam("uid") String uid,
                       @Suspended AsyncResponse asyncResponse) {
        super.doGetJob(user,uid,asyncResponse);
    }

    @PUT
    @Path("/{uid}/{action:cancel|resume}")
    @Produces(MediaType.APPLICATION_JSON)
    public void updateJob(  @Context final IUser user,
                            @PathParam("uid") final String uid,
                            @QueryParam(REQUEST_UID_QUERY_PARAM) final String requestUid,
                            @PathParam("action") final ActionRequest actionRequest,
                            @Suspended final AsyncResponse asyncResponse)
    {
        super.doUpdateJob(user,uid,requestUid,actionRequest,asyncResponse);
    }

}
