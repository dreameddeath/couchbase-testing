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

package com.dreameddeath.core.service.registrar;

import com.dreameddeath.core.service.AbstractExposableService;
import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.service.annotation.ServiceDefTag;
import com.dreameddeath.core.service.model.common.CuratorDiscoveryServiceDescription;
import com.dreameddeath.core.service.utils.ServiceInstanceSerializerImpl;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 03/09/2016.
 */
public abstract class AbstractServiceRegistrar<T extends CuratorDiscoveryServiceDescription> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractServiceRegistrar.class);
    private final CuratorFramework curatorClient;
    private final String domain;
    private final String serviceType;
    private boolean isStarted=false;
    private Set<AbstractExposableService> services = new CopyOnWriteArraySet<>();
    private ServiceDiscovery<T> serviceDiscovery;


    protected abstract Class<T> getDescriptionClass();

    public AbstractServiceRegistrar(CuratorFramework curatorClient, String domain,String serviceType){
        this.curatorClient = curatorClient;
        this.domain = domain;
        this.serviceType = serviceType;
    }



    public synchronized void start() throws Exception{
        curatorClient.blockUntilConnected(10, TimeUnit.SECONDS);
        String fullPath = ServiceNamingUtils.buildServiceDiscovererDomain(curatorClient,domain,serviceType);

        serviceDiscovery = ServiceDiscoveryBuilder.builder(getDescriptionClass())
                .serializer((InstanceSerializer<T>)new ServiceInstanceSerializerImpl())
                .client(curatorClient)
                .basePath(fullPath).build();

        serviceDiscovery.start();

        for(AbstractExposableService foundService:services) {
            registerService(foundService);
        }

        isStarted=true;
    }

    public synchronized void stop() throws IOException {
        isStarted=false;
        serviceDiscovery.close();
        services.clear();
    }

    public List<ServiceInstance<T>> getServicesInstanceDescription(){
        return services.stream()
                .map(this::buildServiceInstanceDescription)
                .collect(Collectors.toList());
    }

    protected abstract ServiceInstance<T> buildServiceInstanceDescription(AbstractExposableService service);


    protected T initServiceInstanceDescription(AbstractExposableService service,T serviceDescr) {

        ServiceDef annotDef = service.getClass().getAnnotation(ServiceDef.class);
        Path pathAnnot = service.getClass().getAnnotation(Path.class);

        serviceDescr.setDomain(annotDef.domain());
        serviceDescr.setName(annotDef.name());
        serviceDescr.setState(annotDef.status().name());
        serviceDescr.setVersion(annotDef.version());
        for(ServiceDefTag tag : annotDef.tags()){
            serviceDescr.addTag(tag.value());
        }
        return serviceDescr;
    }

    public Set<AbstractExposableService> getServices(){
        return Collections.unmodifiableSet(services);
    }

    public void addService(AbstractExposableService service){
        services.add(service);
        if(isStarted){
            try {
                registerService(service);
            }
            catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    protected void registerService(AbstractExposableService service)throws Exception{
        ServiceInstance<T> newServiceDef = buildServiceInstanceDescription(service);
        String servicePath= ServiceNamingUtils.buildServerServicePath(curatorClient,domain, newServiceDef.getPayload());
        serviceDiscovery.registerService(newServiceDef);
        LOG.info("Service {} registred with id {} within domain {} in path {}", newServiceDef.getName(), newServiceDef.getId(), domain,servicePath);
    }

}
