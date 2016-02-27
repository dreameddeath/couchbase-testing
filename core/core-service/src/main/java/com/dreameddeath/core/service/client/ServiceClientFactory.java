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

package com.dreameddeath.core.service.client;

import com.dreameddeath.core.service.context.feature.ClientFeatureFactory;
import com.dreameddeath.core.service.discovery.ServiceDiscoverer;
import com.dreameddeath.core.service.exception.ServiceDiscoveryException;
import com.dreameddeath.core.service.model.ClientInstanceInfo;
import com.dreameddeath.core.service.registrar.ClientRegistrar;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Christophe Jeunesse on 04/03/2015.
 */
public class ServiceClientFactory {
    private final static Logger LOG = LoggerFactory.getLogger(ServiceClientFactory.class);
    private final ServiceDiscoverer serviceDiscoverer;
    private final ClientRegistrar clientRegistrar;
    private final ConcurrentMap<String,IServiceClient> serviceClientMap = new ConcurrentHashMap<>();
    private ClientFeatureFactory featureFactory=null;


    @Autowired(required = false)
    public void setFeatureFactory(ClientFeatureFactory featureFactory){
        this.featureFactory=featureFactory;
    }

    public ServiceClientFactory(ServiceDiscoverer serviceDiscoverer){
        this(serviceDiscoverer,null);
    }

    public ServiceClientFactory(ServiceDiscoverer serviceDiscoverer, ClientRegistrar registrar){
        this.serviceDiscoverer = serviceDiscoverer;
        this.clientRegistrar = registrar;
    }

    public void registarClient(IServiceClient client){
        ClientInstanceInfo instanceInfo = new ClientInstanceInfo();
        clientRegistrar.enrich(instanceInfo);
        instanceInfo.setCreationDate(DateTime.now());
        instanceInfo.setServiceName(client.getFullName());
        instanceInfo.setUid(client.getUuid().toString());
        try {
            clientRegistrar.register(instanceInfo);
        }
        catch(Exception e){
            LOG.error("Cannot register client for service "+client.getFullName(),e);
        }
    }

    public ClientFeatureFactory getFeatureFactory() {
        return featureFactory;
    }

    public IServiceClient getClient(final String serviceName, final String serviceVersion){
        return serviceClientMap.computeIfAbsent(ServiceNamingUtils.buildServiceFullName(serviceName, serviceVersion),
                serviceFullName -> {
                    try {
                        IServiceClient client = new ServiceClientImpl(serviceDiscoverer.getServiceProvider(serviceFullName),serviceFullName,this);
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
