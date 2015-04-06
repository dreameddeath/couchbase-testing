/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.curator.server;

import com.dreameddeath.core.annotation.service.ServiceDef;
import com.dreameddeath.core.service.registrar.AbstractExposableService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by CEAJ8230 on 13/01/2015.
 */
@Path("/v1/test")
@ServiceDef(name = "test",version = "1.0")
@Api(value = "/basic", description = "Basic resource")
public class ServiceTest extends AbstractExposableService{
    private static Logger LOG = LoggerFactory.getLogger(ServiceTest.class);

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Get object by ID",
            notes = "No details provided",
            response = Result.class,
            position = 0)
    @ApiResponses({
             @ApiResponse(code = 400, message = "Invalid ID"),
             @ApiResponse(code = 404, message = "object not found")
    })
    public Result test(){
        Result res = new Result();
        res.value = "An simple test";
        return res;
    }

    public static class Result{
        public String value;
    }

}
