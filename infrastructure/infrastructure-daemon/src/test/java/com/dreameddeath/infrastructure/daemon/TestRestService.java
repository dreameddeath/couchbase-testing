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

package com.dreameddeath.infrastructure.daemon;

import com.dreameddeath.core.service.AbstractRestExposableService;
import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.service.annotation.VersionStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by Christophe Jeunesse on 20/08/2015.
 */
@Path("/")
@ServiceDef(domain = "test",type="tests",name="tests#tests#tests",version="1.0",status = VersionStatus.STABLE)
@Api(value = "/", description = "testing services")
public class TestRestService extends AbstractRestExposableService {
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "return 12",
            response = Integer.class,
            position = 0)
    public Integer get(){
        return 12;
    }


    @GET
    @Path("/{nb}")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "return 12 + nb + qbn",
            response = Integer.class,
            position = 0)
    public Integer getWithParams(@PathParam("nb")int nb,@QueryParam("qnb") int qnb){
        return 12+nb+qnb;
    }

}
