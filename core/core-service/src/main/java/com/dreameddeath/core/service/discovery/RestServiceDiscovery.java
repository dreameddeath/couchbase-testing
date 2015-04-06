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

package com.dreameddeath.core.service.discovery;

import com.dreameddeath.core.service.model.ServicesInstanceDescription;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by CEAJ8230 on 18/01/2015.
 */
@Path("/apis")
public class RestServiceDiscovery {
    private ServiceDiscoverer _serviceDiscoverer;

    public void setServiceDiscoverer(ServiceDiscoverer serviceDiscoverer){
        _serviceDiscoverer = serviceDiscoverer;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ServicesInstanceDescription getServices() throws Exception{
        return _serviceDiscoverer.getInstancesDescription();
    }
}
