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

package com.dreameddeath.core.service.api;

import com.dreameddeath.core.curator.discovery.standard.ICuratorDiscoveryListener;
import com.dreameddeath.core.service.discovery.AbstractServiceDiscoverer;
import com.dreameddeath.core.service.discovery.ClientDiscoverer;
import com.dreameddeath.core.service.discovery.ProxyClientDiscoverer;
import com.dreameddeath.core.service.discovery.ServiceTypeDiscovery;
import com.dreameddeath.core.service.model.common.ServiceTypeDefinition;
import com.dreameddeath.core.service.utils.IServiceTypeHelper;
import com.dreameddeath.core.service.utils.ServiceTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 04/09/2016.
 */
@Path("/")
public class RestApiServiceTypeDiscovery {
    private static final Logger LOG= LoggerFactory.getLogger(RestApiServiceTypeDiscovery.class);
    private final String domain;
    final private Map<String,RestApiServiceDiscovery> serviceTypesMap =new ConcurrentHashMap<>();

    private ServiceTypeDiscovery discovery;

    public RestApiServiceTypeDiscovery(String domain) {
        this.domain = domain;
    }

    public void setDiscovery(ServiceTypeDiscovery discovery){
        this.discovery = discovery;
        this.discovery.addListener(new ICuratorDiscoveryListener<ServiceTypeDefinition>() {
            @Override
            public void onRegister(String uid, ServiceTypeDefinition obj) {
                try {
                    //discovery.getClient().
                    //new CuratorDiscoveryImpl<ServiceTypeDefinition>(discovery.getClient());
                    RestApiServiceDiscovery service = new RestApiServiceDiscovery();
                    final IServiceTypeHelper serviceTypeDefinition = ServiceTypeUtils.getDefinition(obj.getType());
                    AbstractServiceDiscoverer serviceDiscoverer =serviceTypeDefinition.buildDiscoverer(discovery.getClient(),domain);// new AbstractServiceDiscoverer(discovery.getClient(),obj.getName());
                    serviceDiscoverer.start();
                    ClientDiscoverer clientDiscoverer = new ClientDiscoverer(discovery.getClient(),domain,obj.getType());
                    clientDiscoverer.start();
                    ProxyClientDiscoverer proxyClientDiscoverer = new ProxyClientDiscoverer(discovery.getClient(),domain,obj.getType());
                    proxyClientDiscoverer.start();
                    service.setServiceDiscoverer(serviceDiscoverer);
                    service.setClientDiscoverer(clientDiscoverer);
                    service.setProxyClientDiscoverer(proxyClientDiscoverer);
                    serviceTypesMap.put(uid,service);
                    LOG.info("Registering service type <{}> on domain <{}>",uid,domain);
                }
                catch(Exception e){
                    LOG.error("Error while registering service type <"+uid+"> on domain <"+domain+">",e);
                }
            }

            @Override
            public void onUnregister(String uid, ServiceTypeDefinition oldObj) {
                LOG.info("Removing service type <{}> on domain <{}>",uid,domain);
                RestApiServiceDiscovery removedEntry = serviceTypesMap.remove(uid);
                if(removedEntry!=null){
                    try {
                        removedEntry.getClientDiscoverer().stop();
                    }
                    catch(Exception e){
                        LOG.error("Error while stopping client discoverer service type <"+uid+"> for domain <"+domain+">",e);
                    }
                    try {
                        removedEntry.getServiceDiscoverer().stop();
                    }
                    catch(Exception e){
                        LOG.error("Error while stopping service discoverer service for type <"+uid+"> for domain <"+domain+">",e);
                    }
                }
            }

            @Override
            public void onUpdate(String uid, ServiceTypeDefinition obj, ServiceTypeDefinition newObj) {
                //Nothing to do
            }
        });
    }

    public ServiceTypeDiscovery getDiscovery() {
        return discovery;
    }


    @Path("/")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public List<String> getTypes(){
        List<String> result = new ArrayList<>(serviceTypesMap.keySet());
        Collections.sort(result);
        return result;
    }

    @Path("/{techType}")
    public RestApiServiceDiscovery getTypeInstance(@PathParam("techType")String serviceType){
        RestApiServiceDiscovery service = serviceTypesMap.get(serviceType);
        if(service==null){
            throw new NotFoundException("Cannot find service type <"+serviceType+"> on domain <"+domain+">");
        }
        return service;
    }
}
