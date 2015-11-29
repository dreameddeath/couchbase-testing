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

import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.service.annotation.ServiceDefTag;
import com.dreameddeath.core.service.model.AbstractExposableService;
import com.dreameddeath.core.service.model.ServiceDescription;
import com.dreameddeath.core.service.utils.ServiceInstanceSerializerImpl;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;
import io.swagger.jaxrs.Reader;
import io.swagger.models.Swagger;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
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
 * Created by Christophe Jeunesse on 13/01/2015.
 */
public class ServiceRegistrar {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceRegistrar.class);
    private final CuratorFramework curatorClient;
    private final String domain;
    private boolean isStarted=false;
    private Set<AbstractExposableService> services = new CopyOnWriteArraySet<>();
    private ServiceDiscovery<ServiceDescription> serviceDiscovery;


    public ServiceRegistrar(CuratorFramework curatorClient,String domain){
        this.curatorClient = curatorClient;
        this.domain = domain;
    }

    public synchronized void start() throws Exception{
        curatorClient.blockUntilConnected(10, TimeUnit.SECONDS);
        String fullPath = ServiceNamingUtils.buildServiceDomain(curatorClient, domain);

        serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceDescription.class)
                .serializer(new ServiceInstanceSerializerImpl())
                .client(curatorClient)
                .basePath(fullPath).build();

        serviceDiscovery.start();

        for(AbstractExposableService foundService:services) {
            ServiceInstance<ServiceDescription> newServiceDef = buildServiceInstanceDescription(foundService);
            serviceDiscovery.registerService(newServiceDef);
            LOG.info("Service {} registred with id {} within domain {}", newServiceDef.getName(),newServiceDef.getId(),domain);
        }

        isStarted=true;
    }

    public synchronized void stop() throws IOException{
        isStarted=false;
        serviceDiscovery.close();
        services.clear();
    }

    public List<ServiceInstance<ServiceDescription>> getServicesInstanceDescription(){
        return services.stream()
                .map(this::buildServiceInstanceDescription)
                .collect(Collectors.toList());
    }

    protected ServiceInstance<ServiceDescription> buildServiceInstanceDescription(AbstractExposableService service) {
        ServiceDef annotDef = service.getClass().getAnnotation(ServiceDef.class);
        Path pathAnnot = service.getClass().getAnnotation(Path.class);

        Swagger swagger = new Swagger();
        swagger.setBasePath((service.getEndPoint().path()+"/"+service.getAddress()+"/"+pathAnnot.value()).replaceAll("/{2,}","/"));
        swagger.setHost(service.getEndPoint().host());

        Reader reader=new Reader(swagger);
        reader.read(service.getClass());

        ServiceDescription serviceDescr = new ServiceDescription();
        serviceDescr.setDomain(annotDef.domain());
        serviceDescr.setState(annotDef.status().name());
        serviceDescr.setVersion(annotDef.version());
        serviceDescr.setSwagger(reader.getSwagger());
        for(ServiceDefTag tag : annotDef.tags()){
            serviceDescr.addTag(tag.value());
        }

        String uriStr = "{scheme}://{address}:{port}"+("/"+swagger.getBasePath()).replaceAll("/{2,}","/");
        UriSpec uriSpec = new UriSpec(uriStr);

        return new ServiceInstance<>(
                ServiceNamingUtils.buildServiceFullName(annotDef.name(),annotDef.version()),
                service.getId(),
                service.getEndPoint().host(),
                service.getEndPoint().port(),
                null,
                serviceDescr,
                System.currentTimeMillis(),
                null,
                uriSpec
                );
    }

    public Set<AbstractExposableService> getServices(){
        return Collections.unmodifiableSet(services);
    }

    public void addService(AbstractExposableService service){
        services.add(service);
    }

}
