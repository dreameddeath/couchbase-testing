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

package com.dreameddeath.core.service.discovery;

import com.dreameddeath.core.service.model.ServiceInfoDescription;
import com.dreameddeath.core.service.model.ServiceInstanceDescription;
import com.dreameddeath.core.service.model.ServicesByNameInstanceDescription;
import com.dreameddeath.core.service.model.ServicesListInstanceDescription;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 18/01/2015.
 */
@Path("/services")
public class RestServiceDiscovery {
    private ServiceDiscoverer _serviceDiscoverer;

    public void setServiceDiscoverer(ServiceDiscoverer serviceDiscoverer){
        _serviceDiscoverer = serviceDiscoverer;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/instances")
    public ServicesByNameInstanceDescription getServices() throws Exception{
        return _serviceDiscoverer.getInstancesDescription();
    }

    @GET
    @Path("/instances/{fullname}")
    @Produces(MediaType.APPLICATION_JSON)
    public ServicesListInstanceDescription getService(@PathParam("fullname") String fullName) throws Exception{
        return _serviceDiscoverer.getInstancesDescriptionByFullName(fullName);
    }

    @GET
    @Path("/instances/{fullname}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceInstanceDescription getService(@PathParam("fullname") String fullName,@PathParam("id") String id) throws Exception{
        return new ServiceInstanceDescription(_serviceDiscoverer.getInstance(fullName,id));
    }

    @GET
    @Path("/infos/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceInfoDescription getServicesInfosByName(@PathParam("name") String name) throws Exception{
        Collection<ServiceInfoDescription> filteredList = _serviceDiscoverer.getInstancesInfo(name);
        if(filteredList.size()==0){
            return null;
        }
        return filteredList.iterator().next();
    }


    @GET
    @Path("/infos")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ServiceInfoDescription> getServicesInfos() throws Exception{
        Collection<ServiceInfoDescription> services= _serviceDiscoverer.getInstancesInfo(null);
        List<ServiceInfoDescription> result = new ArrayList<>(services.size());
        result.addAll(services);
        return result;
    }


}
