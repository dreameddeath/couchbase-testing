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
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;



/**
 * Created by Christophe Jeunesse on 13/01/2015.
 */
public class ServiceRegistrar {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceRegistrar.class);
    private final CuratorFramework curatorClient;
    private final String basePath;
    private Set<AbstractExposableService> services = new CopyOnWriteArraySet<>();
    private ServiceDiscovery<ServiceDescription> serviceDiscovery;


    public ServiceRegistrar(CuratorFramework curatorClient,String basePath){
        this.curatorClient = curatorClient;
        if(!basePath.startsWith("/")){
            basePath="/"+basePath;
        }
        this.basePath = basePath;
    }

    public void start() throws Exception{
        curatorClient.blockUntilConnected(10, TimeUnit.SECONDS);
        ServiceNamingUtils.createBaseServiceName(curatorClient, basePath);

        serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceDescription.class)
                .serializer(new ServiceInstanceSerializerImpl())
                .client(curatorClient)
                .basePath(basePath).build();

        serviceDiscovery.start();

        for(AbstractExposableService foundService:services) {
            ServiceDef annotDef = foundService.getClass().getAnnotation(ServiceDef.class);
            Swagger swagger = new Swagger();
            Path pathAnnot = foundService.getClass().getAnnotation(Path.class);
            swagger.setBasePath((foundService.getEndPoint().path()+"/"+foundService.getAddress()+"/"+pathAnnot.value()).replaceAll("/{2,}","/"));
            swagger.setHost(foundService.getEndPoint().host());

            Reader reader=new Reader(swagger);

            reader.read(foundService.getClass());

            ServiceDescription serviceDescr = new ServiceDescription();
            serviceDescr.setState(annotDef.status().name());
            serviceDescr.setVersion(annotDef.version());
            serviceDescr.setSwagger(reader.getSwagger());

            String uriStr = "{scheme}://{address}:{port}"+("/"+swagger.getBasePath()).replaceAll("/{2,}","/");
            UriSpec uriSpec = new UriSpec(uriStr);

            ServiceInstance<ServiceDescription> newServiceDef = ServiceInstance.<ServiceDescription>builder().name(ServiceNamingUtils.buildServiceFullName(annotDef.name(),annotDef.version()))
                    .id(foundService.getId())
                    .uriSpec(uriSpec)
                    .address(foundService.getEndPoint().host())
                    .port(foundService.getEndPoint().port())
                    .payload(serviceDescr)
                    .build();

            serviceDiscovery.registerService(newServiceDef);
            LOG.info("Service {} registred with id {} within domain {}", newServiceDef.getName(),newServiceDef.getId(),basePath);
        }

        //LOG.info("Services Regi: " + serviceDiscovery.queryForNames().toString());
    }

    public void stop() throws IOException{
        serviceDiscovery.close();
    }

    public Set<AbstractExposableService> getServices(){
        return Collections.unmodifiableSet(services);
    }

    public void addService(AbstractExposableService service){
        services.add(service);
    }

}
