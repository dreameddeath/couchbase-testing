/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.service.registrar;

import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.service.model.AbstractExposableService;
import com.dreameddeath.core.service.model.ServiceDescription;
import com.dreameddeath.core.service.utils.ServiceInstanceSerializerImpl;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.core.util.JsonSerializer;
import com.wordnik.swagger.jaxrs.JaxrsApiReader;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.model.ApiListing;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

/*import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ConcurrentHashSet;*/


/**
 * Created by CEAJ8230 on 13/01/2015.
 */
public class ServiceRegistrar {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceRegistrar.class);
    private static Set<AbstractExposableService> _services = new CopyOnWriteArraySet<>();

    public static Set<AbstractExposableService> getServices(){ return Collections.unmodifiableSet(_services);}
    public static void addService(AbstractExposableService service){_services.add(service);}

    private final CuratorFramework _client;
    private final String _basePath;
    //private final IRestEndPointDescription _restEndPointDescription;
    private ServiceDiscovery<ServiceDescription> _serviceDiscovery;


    public ServiceRegistrar(CuratorFramework client,String basePath){
        _client = client;
        _basePath = basePath;
    }

    public void start() throws InterruptedException, UnknownHostException, Exception{
        _client.blockUntilConnected(10, TimeUnit.SECONDS);
        ServiceNamingUtils.createBaseServiceName(_client, _basePath);

        _serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceDescription.class)
                .serializer(new ServiceInstanceSerializerImpl())
                .client(_client)
                .basePath(_basePath).build();

        _serviceDiscovery.start();

        JaxrsApiReader reader =new DefaultJaxrsApiReader();
        for(AbstractExposableService foundService:_services) {
            ServiceDef annotDef = foundService.getClass().getAnnotation(ServiceDef.class);
            SwaggerConfig cfg = new SwaggerConfig();
            cfg.setBasePath(foundService.getEndPoint().path());
            cfg.setApiVersion(annotDef.version());

            ApiListing parsedResult = reader.read(foundService.getEndPoint().path(), foundService.getClass(),cfg).get();

            ServiceDescription serviceDescr = new ServiceDescription();
            serviceDescr.setState(annotDef.status().name());
            serviceDescr.setVersion(annotDef.version());
            serviceDescr.setSwagger(JsonSerializer.asJson(parsedResult).toString());

            String uriStr = "{scheme}://{address}:{port}"+("/"+parsedResult.basePath()+parsedResult.resourcePath()).replaceAll("//","/");
            UriSpec uriSpec = new UriSpec(uriStr);

            ServiceInstance<ServiceDescription> newServiceDef = ServiceInstance.<ServiceDescription>builder().name(ServiceNamingUtils.buildServiceFullName(annotDef.name(),annotDef.version()))
                    .uriSpec(uriSpec)
                    .address(foundService.getEndPoint().host())
                    .port(foundService.getEndPoint().port())
                    .payload(serviceDescr)
                    .build();

            _serviceDiscovery.registerService(newServiceDef);
        }

        LOG.info("Services :" + _serviceDiscovery.queryForNames().toString());
    }

    public void stop() throws IOException{
        _serviceDiscovery.close();
    }
}
