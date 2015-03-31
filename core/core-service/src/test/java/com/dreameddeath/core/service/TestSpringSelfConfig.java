package com.dreameddeath.core.service;

import com.dreameddeath.core.service.discovery.ServiceDiscoverer;
import com.dreameddeath.core.service.model.AbstractExposableService;
import com.dreameddeath.core.service.registrar.IRestEndPointDescription;
import com.dreameddeath.core.service.utils.ServiceJacksonObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.curator.framework.CuratorFramework;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.spring.JAXRSServerFactoryBeanDefinitionParser.SpringJAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.spring.SpringResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import java.util.*;

/**
 * Created by CEAJ8230 on 24/03/2015.
 */
@Configuration
@ImportResource({"classpath:META-INF/cxf/cxf.xml"})
public class TestSpringSelfConfig
    //implements BeanFactoryPostProcessor
{
    private static Map<String,AbstractExposableService> _services = new HashMap<>();
    private static CuratorFramework _curatorClient=null;
    private static ServiceDiscoverer _serviceDiscoverer=null;
    private static IRestEndPointDescription _endPointDescr=null;


    public static void setServiceDiscoverer(ServiceDiscoverer serviceDiscoverer){
        _serviceDiscoverer = serviceDiscoverer;
    }
    public static void setEndPointDescr(IRestEndPointDescription descr){
        _endPointDescr = descr;
    }

    public static void registerService(String name, AbstractExposableService service){
        _services.put(name,service);
    }

    public static void setCuratorClient(CuratorFramework client){
        _curatorClient = client;
    }

    @Autowired
    private ConfigurableApplicationContext ctxt;


    @Bean(name="curatorClient")
    public CuratorFramework getClient() throws Exception{
        return _curatorClient;
    }


    @Bean(name="serviceDiscoverer")
    public ServiceDiscoverer getDiscoverer(){
        return _serviceDiscoverer;
    }

    //<bean id="serviceDiscoverer" class="com.dreameddeath.core.service.discovery.ServiceDiscoverer" init-method="start" depends-on="testingJaxRsServer">
    //<constructor-arg><ref bean="curatorClient" /></constructor-arg>
    //<constructor-arg><value>${serviceBasePath}</value></constructor-arg>
    //</bean>


    @Bean(name="testingJaxRsServer")
    public Server buildJaxRsServer() {
        SpringJAXRSServerFactoryBean factory = new SpringJAXRSServerFactoryBean();
        factory.setTransportId("http://cxf.apache.org/transports/http");
        factory.setAddress("/apis");
        factory.setProviders(Arrays.asList(new JacksonJsonProvider(ServiceJacksonObjectMapper.getInstance())));

        List<ResourceProvider> resourceProviders = new LinkedList<>();
        for(Map.Entry<String,AbstractExposableService> serviceDef:_services.entrySet()){
            serviceDef.getValue().setEndPoint(new IRestEndPointDescription() {
                @Override
                public int port() {
                    return _endPointDescr.port();
                }

                @Override
                public String path() {
                    return (_endPointDescr.path()+factory.getAddress()).replaceAll("//{2,}","/");
                }

                @Override
                public String host() {
                    return _endPointDescr.host();
                }
            });
            ctxt.getBeanFactory().registerSingleton(serviceDef.getKey(), serviceDef.getValue());
            SpringResourceFactory factoryResource = new SpringResourceFactory(serviceDef.getKey());
            factoryResource.setApplicationContext(ctxt);
            resourceProviders.add(factoryResource);
        }

        factory.setResourceProviders(resourceProviders);
        factory.setApplicationContext(ctxt);
        return factory.create();
    }

    /*@Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }*/
}
