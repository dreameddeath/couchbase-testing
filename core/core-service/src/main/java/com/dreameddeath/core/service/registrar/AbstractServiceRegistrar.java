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

package com.dreameddeath.core.service.registrar;

import com.dreameddeath.core.service.AbstractExposableService;
import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.service.annotation.ServiceDefTag;
import com.dreameddeath.core.service.model.common.CuratorDiscoveryServiceDescription;
import com.dreameddeath.core.service.utils.ServiceInstanceSerializerImpl;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;
import com.google.common.base.Preconditions;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Christophe Jeunesse on 03/09/2016.
 */
public abstract class AbstractServiceRegistrar<T extends CuratorDiscoveryServiceDescription> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractServiceRegistrar.class);
    private final CuratorFramework curatorClient;
    private final String domain;
    private final String serviceType;
    private final ServiceDiscovery<T> serviceDiscovery;
    private final List<AbstractExposableService> pendingRegistrar = new ArrayList<>();
    private final Map<AbstractExposableService,ServiceInstance<T>> services = new HashMap<>();
    private boolean isStarted=false;


    protected abstract Class<T> getDescriptionClass();

    public AbstractServiceRegistrar(CuratorFramework curatorClient, String domain,String serviceType){
        this.curatorClient = curatorClient;
        this.domain = domain;
        this.serviceType = serviceType;
        String fullPath = ServiceNamingUtils.buildServiceDiscovererDomain(curatorClient,domain,serviceType);
        serviceDiscovery = ServiceDiscoveryBuilder.builder(getDescriptionClass())
                .serializer((InstanceSerializer<T>)new ServiceInstanceSerializerImpl())
                .client(curatorClient)
                .basePath(fullPath).build();
    }



    public synchronized void start() throws Exception{
        curatorClient.blockUntilConnected(10, TimeUnit.SECONDS);
        serviceDiscovery.start();

        for(AbstractExposableService pendingService:pendingRegistrar){
            registerService(pendingService);
        }
        pendingRegistrar.clear();
        isStarted=true;
    }

    public synchronized void stop() throws IOException {
        for(Map.Entry<AbstractExposableService,ServiceInstance<T>> foundService:services.entrySet()) {
            try {
                unregisterService(foundService.getKey(), foundService.getValue());
            }
            catch (Throwable e){
                LOG.error("Error during Service unregister",e);
            }
        }
        services.clear();
        isStarted=false;
        if(serviceDiscovery!=null)
        serviceDiscovery.close();
    }

    public Collection<ServiceInstance<T>> getServicesInstanceDescription(){
        return services.values();
    }

    protected abstract ServiceInstance<T> buildServiceInstanceDescription(AbstractExposableService service);


    protected T initServiceInstanceDescription(AbstractExposableService service, T serviceDescr) {
        ServiceDef annotDef = service.getClass().getAnnotation(ServiceDef.class);
        Preconditions.checkNotNull(annotDef,"The service %s should have the annotation %s",service.getClass(),ServiceDef.class.getSimpleName());
        serviceDescr.setDomain(annotDef.domain());
        serviceDescr.setName(annotDef.name());
        serviceDescr.setState(annotDef.status().name());
        serviceDescr.setVersion(annotDef.version());
        serviceDescr.setProtocols(service.getEndPoint().protocols());
        for(ServiceDefTag tag : annotDef.tags()){
            serviceDescr.addTag(tag.value());
        }
        return serviceDescr;
    }

    public Set<AbstractExposableService> getServices(){
        return Collections.unmodifiableSet(services.keySet());
    }

    public synchronized void addService(AbstractExposableService service){
        if(isStarted){
            try {
                registerService(service);
            }
            catch(Exception e){
                throw new RuntimeException(e);
            }
        }
        else{
            pendingRegistrar.add(service);
        }
    }

    private String buildServicePath(ServiceInstance<T> newServiceDef){
        //TODO refactor to manage domain/payload data update
        return ServiceNamingUtils.buildServerServicePath(curatorClient,domain, newServiceDef.getPayload());
    }

    private void registerService(AbstractExposableService service)throws Exception{
        ServiceInstance<T> newServiceDef = buildServiceInstanceDescription(service);
        String servicePath = buildServicePath(newServiceDef);//Needed to precreate the path
        services.put(service,newServiceDef);
        serviceDiscovery.registerService(newServiceDef);
        LOG.info("Service {} registered with id {} within domain {} in path {}", newServiceDef.getName(), newServiceDef.getId(), domain, servicePath);
    }

    private void unregisterService(AbstractExposableService service,ServiceInstance<T> newServiceDef)throws Exception{
        serviceDiscovery.unregisterService(newServiceDef);
        LOG.info("Service {} unregistered with id {} within domain {} in path {}", newServiceDef.getName(), newServiceDef.getId(), domain,buildServicePath(newServiceDef));
    }

}
