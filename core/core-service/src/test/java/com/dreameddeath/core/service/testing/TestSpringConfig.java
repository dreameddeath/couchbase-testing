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

package com.dreameddeath.core.service.testing;

import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.service.context.IGlobalContextFactory;
import com.dreameddeath.core.service.context.provider.GlobalContextProvider;
import com.dreameddeath.core.service.context.provider.UserContextProvider;
import com.dreameddeath.core.service.discovery.ClientDiscoverer;
import com.dreameddeath.core.service.discovery.ProxyClientDiscoverer;
import com.dreameddeath.core.service.discovery.ServiceDiscoverer;
import com.dreameddeath.core.service.model.AbstractExposableService;
import com.dreameddeath.core.service.registrar.ClientRegistrar;
import com.dreameddeath.core.service.registrar.IRestEndPointDescription;
import com.dreameddeath.core.service.registrar.ServiceRegistrar;
import com.dreameddeath.core.service.utils.ServiceObjectMapperConfigurator;
import com.dreameddeath.core.user.IUserFactory;
import com.dreameddeath.core.user.StandardMockUserFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.curator.framework.CuratorFramework;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.jaxrs.spring.JAXRSServerFactoryBeanDefinitionParser.SpringJAXRSServerFactoryBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 24/03/2015.
 */
@Configuration
@ImportResource({"classpath:META-INF/cxf/cxf.xml"})
public class TestSpringConfig implements ServletContextAware {
    private static String JAXRS_PATH="/apis";

    @Autowired
    private ConfigurableApplicationContext ctxt;
    private ServletContext servletContext;

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setCtxt(ConfigurableApplicationContext ctxt) {
        this.ctxt = ctxt;
    }


    @Bean(name="curatorClient")
    public CuratorFramework getClient() throws Exception{
        return (CuratorFramework)servletContext.getAttribute("curatorClient");
    }


    @Bean(name="serviceDiscoverer")
    public ServiceDiscoverer getDiscoverer(){
        return (ServiceDiscoverer)servletContext.getAttribute("serviceDiscoverer");
    }

    @Bean(name="serviceRegistrar")
    public ServiceRegistrar getServiceRegistrar(){
        return (ServiceRegistrar) servletContext.getAttribute("serviceRegistrar");
    }


    @Bean(name="clientRegistrar")
    public ClientRegistrar getClientRegistrar(){
        return (ClientRegistrar) servletContext.getAttribute("clientRegistrar");
    }


    @Bean(name="clientDiscoverer")
    public ClientDiscoverer getClientDiscoverer(){
        return (ClientDiscoverer) servletContext.getAttribute("clientDiscoverer");
    }

    @Bean(name="proxyClientDiscoverer")
    public ProxyClientDiscoverer getProxyClientDiscoverer(){
        return (ProxyClientDiscoverer) servletContext.getAttribute("proxyClientDiscoverer");
    }

    @Bean(name="globalContextTranscoder")
    public IGlobalContextFactory getContextTranscoder(){
        return new DummyContextFactory();
    }

    @Bean(name="userFactory")
    public IUserFactory getUserFactory(){
        return new StandardMockUserFactory();
    }


    @Bean(name="endPointDescription")
    public IRestEndPointDescription getEndPointDescr(){
        return (IRestEndPointDescription)servletContext.getAttribute("endPointInfo");
    }

    @Bean(name="globalContextProvider")
    public GlobalContextProvider globalContextProvider(){
        return new GlobalContextProvider();
    }

    @Bean(name="userContextProvider")
    public UserContextProvider userContextProvider(){
        return new UserContextProvider();
    }



    @Bean(name="testingJaxRsServer")
    public Server buildJaxRsServer() {
        SpringJAXRSServerFactoryBean factory = new SpringJAXRSServerFactoryBean();
        factory.setTransportId("http://cxf.apache.org/transports/http");
        factory.setAddress(JAXRS_PATH);
        ObjectMapper mapper =(ObjectMapper)servletContext.getAttribute("jacksonObjectMapper");
        if(mapper==null){
            mapper = ObjectMapperFactory.BASE_INSTANCE.getMapper(ServiceObjectMapperConfigurator.SERVICE_MAPPER_CONFIGURATOR);
        }
        factory.setProviders(Arrays.asList(
                new JacksonJsonProvider(mapper),
                ctxt.getBeanFactory().getBean("userContextProvider"),
                ctxt.getBeanFactory().getBean("globalContextProvider")
        ));

        Map<String,Object> beanObjMap = (Map<String,Object>)servletContext.getAttribute("beanObjMap");
        for(Map.Entry<String,Object> beanObjEntry:beanObjMap.entrySet()){
            ctxt.getBeanFactory().registerSingleton(beanObjEntry.getKey(),beanObjEntry.getValue());
        }

        List<ResourceProvider> resourceProviders = new LinkedList<>();
        Map<String,Class> servicesClassNameMap = (Map<String,Class>)servletContext.getAttribute("beanClassNameMap");
        for(Map.Entry<String,Class> serviceDef:servicesClassNameMap.entrySet()){
            try {
                GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                beanDefinition.setBeanClass(serviceDef.getValue());
                beanDefinition.setLazyInit(false);
                beanDefinition.setAbstract(false);
                //beanDefinition.isSingleton()
                beanDefinition.setScope(ConfigurableBeanFactory.SCOPE_SINGLETON);
                beanDefinition.setAutowireCandidate(true);
                if(AbstractExposableService.class.isAssignableFrom(serviceDef.getValue())) {
                    MutablePropertyValues values = new MutablePropertyValues();
                    values.add("address", factory.getAddress());
                    beanDefinition.setPropertyValues(values);
                }
                ((BeanDefinitionRegistry)ctxt.getBeanFactory()).registerBeanDefinition(serviceDef.getKey(),beanDefinition);
            }
            catch(Exception e){
                throw new RuntimeException(e);
            }
        }

        for(Map.Entry<String,Class> serviceDef:servicesClassNameMap.entrySet()){
            Object bean=ctxt.getBean(serviceDef.getKey(),serviceDef.getValue());
            if(AbstractExposableService.class.isAssignableFrom(serviceDef.getValue())){
                ResourceProvider provider = new SingletonResourceProvider(bean);
                resourceProviders.add(provider);
            }
        }

        factory.setResourceProviders(resourceProviders);
        factory.setApplicationContext(ctxt);
        return factory.create();
    }

}
