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

package com.dreameddeath.core.service.client;

import com.dreameddeath.core.service.discovery.AbstractServiceDiscoverer;
import com.dreameddeath.core.service.exception.ServiceDiscoveryException;
import com.dreameddeath.core.service.model.common.ClientInstanceInfo;
import com.dreameddeath.core.service.model.common.CuratorDiscoveryServiceDescription;
import com.dreameddeath.core.service.registrar.ClientRegistrar;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;
import org.apache.curator.x.discovery.ServiceProvider;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Christophe Jeunesse on 04/03/2015.
 */
public abstract class AbstractServiceClientFactory<TCLIENT extends IServiceClient<?>,TSPEC,TDESCR extends CuratorDiscoveryServiceDescription<TSPEC>> {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractServiceClientFactory.class);
    private final AbstractServiceDiscoverer<TSPEC,TDESCR> serviceDiscoverer;
    private final ClientRegistrar clientRegistrar;
    private final ConcurrentMap<String,TCLIENT> serviceClientMap = new ConcurrentHashMap<>();

    public AbstractServiceClientFactory(AbstractServiceDiscoverer serviceDiscoverer){
        this(serviceDiscoverer,null);
    }

    public AbstractServiceClientFactory(AbstractServiceDiscoverer serviceDiscoverer, ClientRegistrar registrar){
        this.serviceDiscoverer = serviceDiscoverer;
        this.clientRegistrar = registrar;
    }

    public void registarClient(TCLIENT client){
        ClientInstanceInfo instanceInfo = new ClientInstanceInfo();
        clientRegistrar.enrich(instanceInfo);
        instanceInfo.setCreationDate(DateTime.now());
        instanceInfo.setServiceName(client.getFullName());
        instanceInfo.setUid(client.getUuid().toString());
        instanceInfo.setServiceType(serviceDiscoverer.getServiceType());
        try {
            String path=clientRegistrar.register(instanceInfo);
            LOG.info("Registering client for service {} on path {}",client.getFullName(),path);
        }
        catch(Exception e){
            LOG.error("Cannot register client for service "+client.getFullName(),e);
        }
    }

    protected abstract TCLIENT buildClient(ServiceProvider<TDESCR> provider, String serviceFullName);

    public TCLIENT getClient(final String serviceName, final String serviceVersion){
        return serviceClientMap.computeIfAbsent(ServiceNamingUtils.buildServiceFullName(serviceName, serviceVersion),
                serviceFullName -> {
                    try {
                        TCLIENT client = buildClient(serviceDiscoverer.getServiceProvider(serviceFullName),serviceFullName);// new ServiceClientImpl(serviceDiscoverer.getServiceProvider(serviceFullName),serviceFullName,this);
                        if(clientRegistrar!=null){
                            registarClient(client);
                        }
                        return client;
                    }
                    catch(ServiceDiscoveryException e){
                        LOG.error("Cannot build/find service "+serviceName+" "+serviceVersion,e);
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    public void stop() throws Exception{
        if(clientRegistrar!=null){
            clientRegistrar.close();
        }
    }


}
