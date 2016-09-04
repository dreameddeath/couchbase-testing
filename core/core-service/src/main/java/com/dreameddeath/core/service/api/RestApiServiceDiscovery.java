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

package com.dreameddeath.core.service.api;

import com.dreameddeath.core.service.discovery.AbstractServiceDiscoverer;
import com.dreameddeath.core.service.discovery.ClientDiscoverer;
import com.dreameddeath.core.service.discovery.ProxyClientDiscoverer;
import com.dreameddeath.core.service.model.common.*;
import com.dreameddeath.core.service.utils.ServiceTypeUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 18/01/2015.
 */
@Path("/services")
public class RestApiServiceDiscovery {
    @Autowired
    private AbstractServiceDiscoverer serviceDiscoverer;

    @Autowired
    private ClientDiscoverer clientDiscoverer;

    @Autowired
    private ProxyClientDiscoverer proxyClientDiscoverer;


    public void setServiceDiscoverer(AbstractServiceDiscoverer serviceDiscoverer){
        this.serviceDiscoverer = serviceDiscoverer;
    }

    public void setClientDiscoverer(ClientDiscoverer clientDiscoverer) {
        this.clientDiscoverer = clientDiscoverer;
    }

    public void setProxyClientDiscoverer(ProxyClientDiscoverer proxyClientDiscoverer) {
        this.proxyClientDiscoverer = proxyClientDiscoverer;
    }

    public ClientDiscoverer getClientDiscoverer() {
        return clientDiscoverer;
    }

    public AbstractServiceDiscoverer getServiceDiscoverer() {
        return serviceDiscoverer;
    }

    public ProxyClientDiscoverer getProxyClientDiscoverer() {
        return proxyClientDiscoverer;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/instances")
    public ServicesByNameInstanceDescription getServices() throws Exception{
        return serviceDiscoverer.getInstancesDescription();
    }

    @GET
    @Path("/instances/{fullname}")
    @Produces(MediaType.APPLICATION_JSON)
    public ServicesListInstanceDescription getService(@PathParam("fullname") String fullName) throws Exception{
        return serviceDiscoverer.getInstancesDescriptionByFullName(fullName);
    }

    @GET
    @Path("/instances/{fullname}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceInstanceDescription getService(@PathParam("fullname") String fullName,@PathParam("id") String id) throws Exception{
        return serviceDiscoverer.getInstanceDescription(fullName,id);
    }

    @GET
    @Path("/infos/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceInfoDescription getServicesInfosByName(@PathParam("name") String name) throws Exception{
        Collection<ServiceInfoDescription> filteredList = serviceDiscoverer.getInstancesInfo(name);
        if(filteredList.size()==0){
            return null;
        }
        return filteredList.iterator().next();
    }

    @GET
    @Path("/specifications/{fullName}")
    public Response getDoc(@PathParam("fullName") String fullName) throws Exception{
        ServicesListInstanceDescription instances = serviceDiscoverer.getInstancesDescriptionByFullName(fullName);
        if(instances.getServiceInstanceList().size()>0){
            ServiceInstanceDescription<?> instanceDescription = instances.getServiceInstanceList().get(0);
            return Response.ok(instanceDescription.getSpec(), ServiceTypeUtils.getDefinition(instanceDescription.getServiceType()).getSpecificationMediaType()).build();
        }
        else{
            throw new NotFoundException("Cannot retrieve documentation for <"+fullName+">");
        }
    }

    @GET
    @Path("/infos")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ServiceInfoDescription> getServicesInfos() throws Exception{
        Collection<ServiceInfoDescription> services= serviceDiscoverer.getInstancesInfo(null);
        List<ServiceInfoDescription> result = new ArrayList<>(services.size());
        result.addAll(services);
        return result;
    }


    @GET
    @Path("/clients/{fullName}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ClientInstanceInfo> getClientsInfosByFullName(@PathParam("fullName") String fullName) throws Exception{
        if(clientDiscoverer!=null){
            return clientDiscoverer.getInstances(fullName);
        }
        return Collections.emptyList();
    }


    @GET
    @Path("/clients")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ClientInstanceInfo> getClientsInfos() throws Exception{
        if(clientDiscoverer!=null){
            return clientDiscoverer.getInstances();
        }
        return Collections.emptyList();
    }

    @GET
    @Path("/proxies/{fullName}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProxyClientInstanceInfo> getProxyClientsInfosByFullName(@PathParam("fullName") String fullName) throws Exception{
        if(proxyClientDiscoverer!=null){
            return proxyClientDiscoverer.getInstances(fullName);
        }
        return Collections.emptyList();
    }


    @GET
    @Path("/proxies")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProxyClientInstanceInfo> getProxyClientsInfos() throws Exception{
        if(proxyClientDiscoverer!=null){
            return proxyClientDiscoverer.getInstances();
        }
        return Collections.emptyList();
    }
}
