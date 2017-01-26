/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.service.api;

import com.dreameddeath.core.curator.discovery.standard.ICuratorDiscoveryListener;
import com.dreameddeath.core.service.discovery.ServiceDomainDiscovery;
import com.dreameddeath.core.service.discovery.ServiceTypeDiscovery;
import com.dreameddeath.core.service.model.common.ServiceDomainDefinition;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;
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
public class RestApiServiceDomainDiscovery {
    private static final Logger LOG= LoggerFactory.getLogger(RestApiServiceDomainDiscovery.class);

    private ServiceDomainDiscovery discovery=null;
    final private Map<String,RestApiServiceTypeDiscovery> serviceTypeDomainsMap=new ConcurrentHashMap<>();


    @Autowired
    public void setDiscovery(ServiceDomainDiscovery discovery){
        this.discovery=discovery;
        this.discovery.addListener(new ICuratorDiscoveryListener<ServiceDomainDefinition>() {
            @Override
            public void onRegister(String uid, ServiceDomainDefinition obj) {
                try {

                    RestApiServiceTypeDiscovery restApiServiceTypeDiscovery = new RestApiServiceTypeDiscovery(obj.getName());
                    restApiServiceTypeDiscovery.setDiscovery(new ServiceTypeDiscovery(discovery.getClient(), ServiceNamingUtils.buildServiceDomain(discovery.getClient(),obj.getName())));
                    serviceTypeDomainsMap.put(uid,restApiServiceTypeDiscovery);
                    restApiServiceTypeDiscovery.getDiscovery().start();
                    LOG.info("Registering service domain <{}>",uid);
                }
                catch(Exception e){
                    LOG.error("Error while registering service domain <"+uid+">",e);
                }
            }

            @Override
            public void onUnregister(String uid, ServiceDomainDefinition oldObj) {
                LOG.info("Removing service domain <{}>",uid);
                RestApiServiceTypeDiscovery removedEntry = serviceTypeDomainsMap.remove(uid);
                if(removedEntry!=null){
                    try {
                        removedEntry.getDiscovery().stop();
                    }
                    catch(Exception e){
                        LOG.error("Error while stopping client discoverer service for domain <"+uid+">",e);
                    }
                    try {
                        removedEntry.getDiscovery().stop();
                    }
                    catch(Exception e){
                        LOG.error("Error while stopping service discoverer service for domain <"+uid+">",e);
                    }
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
    public List<String> getNames(){
        List<String> result = new ArrayList<>(serviceTypeDomainsMap.keySet());
        Collections.sort(result);
        return result;
    }

    @Path("/{name}")
    public RestApiServiceTypeDiscovery getDomainInstance(@PathParam("name")String serviceDomainName){
        RestApiServiceTypeDiscovery service = serviceTypeDomainsMap.get(serviceDomainName);
        if(service==null){
            throw new NotFoundException("Cannot find service domain name <"+serviceDomainName+">");
        }
        return service;
    }
}
