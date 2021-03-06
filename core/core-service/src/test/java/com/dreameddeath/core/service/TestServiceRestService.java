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

package com.dreameddeath.core.service;

import com.dreameddeath.core.context.IGlobalContext;
import com.dreameddeath.core.service.annotation.DataAccessType;
import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.service.annotation.VersionStatus;
import com.dreameddeath.core.service.client.rest.IRestServiceClient;
import com.dreameddeath.core.service.client.rest.RestServiceClientFactory;
import com.dreameddeath.core.service.http.HttpHeaderUtils;
import com.dreameddeath.core.service.swagger.TestingDocument;
import com.google.common.base.Preconditions;
import io.reactivex.Single;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Christophe Jeunesse on 17/03/2015.
 */
@Path("/TestService")
@ServiceDef(domain = "test",type="other",name="testService",version="1.0",status = VersionStatus.STABLE,access = DataAccessType.READ_WRITE)
@Api(value = "/TestService", description = "Basic resource")
public class TestServiceRestService extends AbstractRestExposableService {
    private RestServiceClientFactory clientFactory;
    private TestServiceImpl testService=new TestServiceImpl();

    @Autowired
    public void setClientFactory(RestServiceClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Path("toto/{rootId}/tuto/{id}")
    @ApiOperation(value = "testing label",
            notes = "No details provided",
            response = ITestService.Result.class,
            position = 0)
    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid ID"),
            @ApiResponse(code = 404, message = "object not found")
    })
    public void runWithRes(
            @Context IGlobalContext context,
            @PathParam("rootId") String rootId,@PathParam("id") String id, ITestService.Input input,
            final @Suspended AsyncResponse response
    ){
        //IGlobalContext context = transcoder.decode(contextParam);
        /*ITestService.Input input = new ITestService.Input();
        input.rootId = rootId;
        input.id = id;*/
        testService.runWithRes(context,input)
                .subscribe(response::resume,response::resume);
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    //@Consumes({ MediaType.APPLICATION_OCTET_STREAM })
    @Path("toto/{rootId}/tuto/{id}")
    @ApiOperation(value = "testing label",
            notes = "No details provided",
            response = ITestService.Result.class,
            position = 0)
    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid ID"),
            @ApiResponse(code = 404, message = "object not found")
    })
    public void getWithRes(@PathParam("rootId") String rootId,@PathParam("id") String id,final @Suspended  AsyncResponse asyncResponse){

        /*ITestService.Input input = new ITestService.Input();
        input.rootId = rootId;
        input.id = id;*/
        testService.getWithRes(rootId, id).subscribe(asyncResponse::resume,asyncResponse::resume);
    }

    @PUT
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("toto/{rootId}")
    @ApiOperation(value = "testing label",
            notes = "No details provided",
            response = ITestService.Result.class,
            position = 0)
    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid ID"),
            @ApiResponse(code = 404, message = "object not found")
    })
    public void putWithQuery(@PathParam("rootId") String rootId,@QueryParam("id") String id
            , final @Suspended AsyncResponse asyncResponse){

        /*ITestService.Input input = new ITestService.Input();
        input.rootId = rootId;
        input.id = id;*/
        testService.putWithQuery(rootId, id).subscribe(asyncResponse::resume,asyncResponse::resume);
    }


    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("testingDoc")
    @ApiOperation(value = "testing label",
            notes = "No details provided",
            response = TestingDocument.class,
            position = 0)
    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid ID"),
            @ApiResponse(code = 404, message = "object not found")
    })

    public void initDocument(final @Suspended AsyncResponse asyncResponse){
        testService.initDocument((IGlobalContext)null).subscribe(asyncResponse::resume,asyncResponse::resume);
    }

    private volatile IRestServiceClient client = null;
    private final IRestServiceClient getClient(){
        if(client==null){
            synchronized (this){
                if(client==null) {
                    client=clientFactory.getClient("other","testService", "1.0");
                }
            }
        }
        return client;
    }

    @GET
    @Path("traceId")
    @Produces(MediaType.TEXT_PLAIN)
    public String traceId(@QueryParam("withId") final String traceId, @QueryParam("withGlobalId") final String globalId, @Context final IGlobalContext context){
        Preconditions.checkNotNull(context);
        if(traceId!=null){
            Preconditions.checkNotNull(context.callerCtxt());
            Preconditions.checkNotNull(globalId);
            Preconditions.checkArgument(traceId.equals(context.callerCtxt().traceId()));
            Preconditions.checkArgument(globalId.equals(context.globalTraceId()));
        }
        else{
            Response result= getClient().getInstance()
                    .path("traceId")
                    .queryParam("withId",context.currentTraceId())
                    .queryParam("withGlobalId",context.globalTraceId())
                    .request(MediaType.TEXT_PLAIN)
                    .sync()
                    .get();

            String calleeTraceId = result.getHeaderString(HttpHeaderUtils.HTTP_CALLEE_TRACE_ID);
            Preconditions.checkNotNull(calleeTraceId);
            Preconditions.checkArgument(calleeTraceId.equals(result.readEntity(String.class)));

        }
        return context.currentTraceId();
        //resultObservable.subscribe(resultCalledTraceId->asyncResponse.resume(context.currentTraceId()));
    }

    @GET
    @Path("asyncTraceId")
    @Produces(MediaType.TEXT_PLAIN)
    public void traceId(@QueryParam("withId") final String traceId, @QueryParam("withGlobalId") final String globalId, @Context final IGlobalContext context, @Suspended final AsyncResponse asyncResponse){
        Preconditions.checkNotNull(context);
        Single<String> resultObservable=null;
        if(traceId!=null){
            Preconditions.checkNotNull(context.callerCtxt());
            Preconditions.checkNotNull(globalId);
            Preconditions.checkArgument(traceId.equals(context.callerCtxt().traceId()));
            Preconditions.checkArgument(globalId.equals(context.globalTraceId()));
            resultObservable = Single.just(context.currentTraceId());
        }
        else{
            resultObservable = getClient().getInstance()
                    .path("asyncTraceId")
                    .queryParam("withId",context.currentTraceId())
                    .queryParam("withGlobalId",context.globalTraceId())
                    .request(MediaType.TEXT_PLAIN)
                    .get()
                    .doOnSuccess(result->{
                        String calleeTraceId = result.getHeaderString(HttpHeaderUtils.HTTP_CALLEE_TRACE_ID);
                        Preconditions.checkNotNull(calleeTraceId);
                        Preconditions.checkArgument(calleeTraceId.equals(result.readEntity(String.class)));
                    })
                    .map(result->result.readEntity(String.class));

        }
        resultObservable.subscribe(resultCalledTraceId->asyncResponse.resume(context.currentTraceId()),asyncResponse::resume);
    }
}
