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

package com.dreameddeath.core.service;

import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.service.annotation.VersionStatus;
import com.dreameddeath.core.service.context.IGlobalContext;
import com.dreameddeath.core.service.context.IGlobalContextTranscoder;
import com.dreameddeath.core.service.model.AbstractExposableService;
import com.dreameddeath.core.service.swagger.TestingDocument;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by Christophe Jeunesse on 17/03/2015.
 */
@Path("/TestService")
@ServiceDef(domain = "test",name="testService",version="1.0",status = VersionStatus.STABLE)
@Api(value = "/TestService", description = "Basic resource")
public class TestServiceRestService extends AbstractExposableService {
    private TestServiceImpl testService=new TestServiceImpl();
    private IGlobalContextTranscoder transcoder;

    public void setGlobalContextTranscoder(IGlobalContextTranscoder transcoder){
        this.transcoder = transcoder;
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
    public ITestService.Result runWithRes(@HeaderParam("X-CONTEXT") String contextParam,@PathParam("rootId") String rootId,@PathParam("id") String id, ITestService.Input input){
        IGlobalContext context = transcoder.decode(contextParam);
        /*ITestService.Input input = new ITestService.Input();
        input.rootId = rootId;
        input.id = id;*/
        return testService.runWithRes(context,input).toBlocking().first();
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
    public ITestService.Result getWithRes(@PathParam("rootId") String rootId,@PathParam("id") String id){

        /*ITestService.Input input = new ITestService.Input();
        input.rootId = rootId;
        input.id = id;*/
        return testService.getWithRes(rootId, id).toBlocking().first();
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
    public ITestService.Result putWithQuery(@PathParam("rootId") String rootId,@QueryParam("id") String id){

        /*ITestService.Input input = new ITestService.Input();
        input.rootId = rootId;
        input.id = id;*/
        return testService.putWithQuery(rootId, id).toBlocking().first();
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

    public TestingDocument initDocument(){
        return testService.initDocument(null).toBlocking().first();
    }
}
