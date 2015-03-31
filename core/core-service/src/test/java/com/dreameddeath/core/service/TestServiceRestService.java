package com.dreameddeath.core.service;

import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.service.context.IGlobalContext;
import com.dreameddeath.core.service.context.IGlobalContextTranscoder;
import com.dreameddeath.core.service.model.AbstractExposableService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by CEAJ8230 on 17/03/2015.
 */
@Path("/TestService")
@ServiceDef(name="testService",version="1.0",status = "stable")
@Api(value = "/TestService", description = "Basic resource")
public class TestServiceRestService extends AbstractExposableService {
    TestServiceImpl _testService=new TestServiceImpl();
    IGlobalContextTranscoder _transcoder;

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
    public ITestService.Result runWithRes(@HeaderParam("X-CONTEXT") String contextParam,@PathParam("rootId") String rootId,@PathParam("id") String id){
        IGlobalContext context = _transcoder.decode(contextParam);
        ITestService.Input input = new ITestService.Input();
        input.rootId = rootId;
        input.id = id;
        return _testService.runWithRes(context,input).toBlocking().first();
    }
}
