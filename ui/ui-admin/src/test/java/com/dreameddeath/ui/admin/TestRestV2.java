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

package com.dreameddeath.ui.admin;

import com.dreameddeath.core.config.ConfigPropertyFactory;
import com.dreameddeath.core.config.impl.StringConfigProperty;
import com.dreameddeath.core.process.service.factory.impl.ExecutorClientFactory;
import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.service.annotation.VersionStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 27/11/2015.
 */
@Path("/tests/v2")
@ServiceDef(domain = "test",type="test",name="test",version="2.0",status = VersionStatus.TESTING)
@Api
public class TestRestV2 extends TestRest {
    private final StringConfigProperty property = ConfigPropertyFactory.getStringProperty("test.message.prefix","a warm welcome");


    public ExecutorClientFactory clientFactory;

    @Autowired
    public void setClientFactory(ExecutorClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    @GET
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation("get enhanced generated message")
    public Map<String, Object> genericGet(@PathParam("id") String id){
        Map<String,Object> result = new HashMap<>();

        result.put("message",property.get()+" to you : "+id);
        return result;
    }



}
