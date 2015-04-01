package com.dreameddeath.core.service.registrar;

import com.dreameddeath.core.annotation.service.ServiceDef;
import com.dreameddeath.core.model.ServiceDescription;
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
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * Created by CEAJ8230 on 13/01/2015.
 */
public class ServiceRegistrar {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceRegistrar.class);
    private static Set<AbstractExposableService> _services = new ConcurrentHashSet<>();

    public static Set<AbstractExposableService> getServices(){ return Collections.unmodifiableSet(_services);}
    public static void addService(AbstractExposableService service){_services.add(service);}

    private final String _basePath;
    private final CuratorFramework _client;
    private final String _host;
    private final int _port;
    private final ServerConnector _connector;
    private ServiceDiscovery<ServiceDescription> _serviceDiscovery;

    public ServiceRegistrar(CuratorFramework client,String basePath,String host,int port){
        _client = client;
        _basePath = basePath;
        _host=host;
        _port = port;
        _connector=null;
    }

    public ServiceRegistrar(CuratorFramework client,String basePath,String host,ServerConnector connector){
        _client = client;
        _basePath = basePath;
        _host=host;
        _port = 0;
        _connector=connector;
    }

    public void start() throws InterruptedException, UnknownHostException, Exception{
        int port = _port;
        if(_connector!=null){
            port = _connector.getLocalPort();
        }
        _client.blockUntilConnected(10, TimeUnit.SECONDS);
        _serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceDescription.class)
                .client(_client)
                .basePath(_basePath).build();
        _serviceDiscovery.start();

        JaxrsApiReader reader =new DefaultJaxrsApiReader();
        for(AbstractExposableService foundService:_services) {
            ServiceDef annotDef = foundService.getClass().getAnnotation(ServiceDef.class);
            SwaggerConfig cfg = new SwaggerConfig();
            JAXRSServerFactoryBean endPoint = (JAXRSServerFactoryBean)foundService.getEndPoint();
            cfg.setBasePath(endPoint.getAddress());
            cfg.setApiVersion(annotDef.version());
            ApiListing parsedResult = reader.read(endPoint.getAddress(), foundService.getClass(),cfg).get();

            ServiceDescription serviceDescr = new ServiceDescription();
            serviceDescr.setState(annotDef.status());
            serviceDescr.setVersion(annotDef.version());
            serviceDescr.setSwagger(JsonSerializer.asJson(parsedResult));

            UriSpec uriSpec = new UriSpec("{scheme}://{address}:{port}/"+endPoint.getAddress()+"/"+parsedResult.resourcePath());

            ServiceInstance<ServiceDescription> newServiceDef = ServiceInstance.<ServiceDescription>builder().name(annotDef.name())
                    .uriSpec(uriSpec)
                    .address(_host)
                    .port(port)
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
