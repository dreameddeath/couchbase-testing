package com.dreameddeath.core.service.registrar;

import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.service.model.AbstractExposableService;
import com.dreameddeath.core.service.model.ServiceDescription;
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
    private final IRestEndPointDescription _restEndPointDescription;
    private ServiceDiscovery<ServiceDescription> _serviceDiscovery;

    public ServiceRegistrar(CuratorFramework client,IRestEndPointDescription restEndPointDescription){
        _client = client;
        _restEndPointDescription = restEndPointDescription;
    }

    public void start() throws InterruptedException, UnknownHostException, Exception{
        _client.blockUntilConnected(10, TimeUnit.SECONDS);
        _serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceDescription.class)
                .client(_client)
                .basePath(_restEndPointDescription.path()).build();

        _serviceDiscovery.start();

        JaxrsApiReader reader =new DefaultJaxrsApiReader();
        for(AbstractExposableService foundService:_services) {
            ServiceDef annotDef = foundService.getClass().getAnnotation(ServiceDef.class);
            SwaggerConfig cfg = new SwaggerConfig();
            cfg.setBasePath(_restEndPointDescription.host());
            cfg.setApiVersion(annotDef.version());

            ApiListing parsedResult = reader.read(/*endPoint.getAddress()*/_restEndPointDescription.host(), foundService.getClass(),cfg).get();

            ServiceDescription serviceDescr = new ServiceDescription();
            serviceDescr.setState(annotDef.status());
            serviceDescr.setVersion(annotDef.version());
            serviceDescr.setSwagger(JsonSerializer.asJson(parsedResult).toString());

            UriSpec uriSpec = new UriSpec("{scheme}://{address}:{port}");
            ServiceInstance<ServiceDescription> newServiceDef = ServiceInstance.<ServiceDescription>builder().name(annotDef.name())
                    .uriSpec(uriSpec)
                    .address(_restEndPointDescription.host())
                    .port(_restEndPointDescription.port())
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
