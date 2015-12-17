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

import com.dreameddeath.core.curator.discovery.ICuratorDiscoveryListener;
import com.dreameddeath.core.service.model.ServiceDomainDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 26/11/2015.
 */
@Path("/domains")
public class RestServiceDomainDiscovery {
    private static final Logger LOG= LoggerFactory.getLogger(RestServiceDomainDiscovery.class);

    private ServiceDomainDiscovery discovery;
    final private Map<String,RestServiceDiscovery> serviceDomainsMap =new ConcurrentHashMap<>();

    @Autowired
    public void setDiscovery(ServiceDomainDiscovery discovery){
        this.discovery=discovery;
        this.discovery.addListener(new ICuratorDiscoveryListener<ServiceDomainDefinition>() {
            @Override
            public void onRegister(String uid, ServiceDomainDefinition obj) {
                try {
                    RestServiceDiscovery service = new RestServiceDiscovery();
                    ServiceDiscoverer serviceDiscoverer = new ServiceDiscoverer(discovery.getClient(),obj.getName());
                    serviceDiscoverer.start();
                    ClientDiscoverer clientDiscoverer = new ClientDiscoverer(discovery.getClient(),obj.getName());
                    clientDiscoverer.start();
                    ProxyClientDiscoverer proxyClientDiscoverer = new ProxyClientDiscoverer(discovery.getClient(),obj.getName());
                    proxyClientDiscoverer.start();
                    service.setServiceDiscoverer(serviceDiscoverer);
                    service.setClientDiscoverer(clientDiscoverer);
                    service.setProxyClientDiscoverer(proxyClientDiscoverer);
                    serviceDomainsMap.put(uid,service);
                    LOG.info("Registering service domain <{}>",uid);
                }
                catch(Exception e){
                    LOG.error("Error while registering service domain <"+uid+">",e);
                    ///TODO log error
                }
            }

            @Override
            public void onUnregister(String uid, ServiceDomainDefinition oldObj) {
                LOG.info("Removing service domain <{}>",uid);
                RestServiceDiscovery removedEntry = serviceDomainsMap.remove(uid);
                if(removedEntry!=null){
                    try {
                        removedEntry.getClientDiscoverer().stop();
                    }
                    catch(Exception e){}
                    try {
                        removedEntry.getServiceDiscoverer().stop();
                    }
                    catch(Exception e){}
                }
            }

            @Override
            public void onUpdate(String uid, ServiceDomainDefinition obj, ServiceDomainDefinition newObj) {
                //Nothing to do
            }
        });
    }

    @Path("/")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public List<String> get(){
        List<String> result = new ArrayList<>(serviceDomainsMap.keySet());
        Collections.sort(result);
        return result;
    }

   @Path("/{name}")
    public RestServiceDiscovery getDomainInstance(@PathParam("name")String serviceDomainName){
        RestServiceDiscovery service = serviceDomainsMap.get(serviceDomainName);
        if(service==null){
            throw new NotFoundException("Cannot find service domain name <"+serviceDomainName+">");
        }
        return service;
    }

}
