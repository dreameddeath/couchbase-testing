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

package com.dreameddeath.core.service.testing;

import com.dreameddeath.core.context.IContextFactory;
import com.dreameddeath.core.context.impl.GlobalContextFactoryImpl;
import com.dreameddeath.core.json.EnhancedJacksonJsonProvider;
import com.dreameddeath.core.json.JsonProviderFactory;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.service.AbstractRestExposableService;
import com.dreameddeath.core.service.client.rest.RestServiceClientFactory;
import com.dreameddeath.core.service.discovery.AbstractServiceDiscoverer;
import com.dreameddeath.core.service.discovery.ClientDiscoverer;
import com.dreameddeath.core.service.discovery.ProxyClientDiscoverer;
import com.dreameddeath.core.service.interceptor.rest.filter.ContextServerFilter;
import com.dreameddeath.core.service.interceptor.rest.filter.LogServerFilter;
import com.dreameddeath.core.service.interceptor.rest.filter.UserServerFilter;
import com.dreameddeath.core.service.interceptor.rest.provider.GlobalContextProvider;
import com.dreameddeath.core.service.interceptor.rest.provider.UserContextProvider;
import com.dreameddeath.core.service.registrar.ClientRegistrar;
import com.dreameddeath.core.service.registrar.IEndPointDescription;
import com.dreameddeath.core.service.registrar.RestServiceRegistrar;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentObjectMapperConfigurator;
import com.dreameddeath.core.user.IUserFactory;
import com.dreameddeath.core.user.StandardMockUserFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public AbstractServiceDiscoverer getDiscoverer(){
        return (AbstractServiceDiscoverer)servletContext.getAttribute("serviceDiscoverer");
    }

    @Bean(name="serviceRegistrar")
    public RestServiceRegistrar getServiceRegistrar(){
        return (RestServiceRegistrar) servletContext.getAttribute("serviceRegistrar");
    }

    @Bean(name="clientRegistrar")
    public ClientRegistrar getClientRegistrar(){
        return (ClientRegistrar) servletContext.getAttribute("clientRegistrar");
    }

    @Bean(name="clientDiscoverer")
    public ClientDiscoverer getClientDiscoverer(){
        return (ClientDiscoverer) servletContext.getAttribute("clientDiscoverer");
    }

    @Bean(name="clientFactory")
    public RestServiceClientFactory getRestClientFactory(){
        return (RestServiceClientFactory) servletContext.getAttribute("serviceClientFactory");
    }


    @Bean(name="proxyClientDiscoverer")
    public ProxyClientDiscoverer getProxyClientDiscoverer(){
        return (ProxyClientDiscoverer) servletContext.getAttribute("proxyClientDiscoverer");
    }

    @Bean(name="globalContextTranscoder")
    public IContextFactory getContextTranscoder(){
        return new GlobalContextFactoryImpl();
    }

    @Bean(name="userFactory")
    public IUserFactory getUserFactory(){
        return new StandardMockUserFactory();
    }

    @Bean(name="endPointDescription")
    public IEndPointDescription getEndPointDescr(){
        return (IEndPointDescription)servletContext.getAttribute("endPointInfo");
    }

    @Bean(name="globalContextProvider")
    public GlobalContextProvider globalContextProvider(){
        return new GlobalContextProvider();
    }

    @Bean(name="userContextProvider")
    public UserContextProvider userContextProvider(){
        return new UserContextProvider();
    }

    @Bean(name="userServerFilter")
    public UserServerFilter userServerFilter(){
        UserServerFilter filter = new UserServerFilter();
        filter.setSetupDefaultUser(true);
        return filter;
    }


    @Bean(name="contextServerFilter")
    public ContextServerFilter contextServerFilter(){
        return new ContextServerFilter();
    }

    @Bean(name="logServerFilter")
    public LogServerFilter logServerFilter(){
        return new LogServerFilter();
    }


    @Bean(name="testingJaxRsServer")
    public Server buildJaxRsServer() {
        SpringJAXRSServerFactoryBean factory = new SpringJAXRSServerFactoryBean();
        factory.setTransportId("http://cxf.apache.org/transports/http");
        factory.setAddress(JAXRS_PATH);

        ObjectMapper mapper =(ObjectMapper)servletContext.getAttribute("jacksonObjectMapper");
        if(mapper==null){
            mapper = ObjectMapperFactory.BASE_INSTANCE.getMapper(CouchbaseDocumentObjectMapperConfigurator.BASE_COUCHBASE_PUBLIC);
        }

        factory.setProviders(Arrays.asList(
                new EnhancedJacksonJsonProvider(mapper, JsonProviderFactory.getProviderInterceptorList()),
                ctxt.getBeanFactory().getBean("userContextProvider"),
                ctxt.getBeanFactory().getBean("globalContextProvider"),
                ctxt.getBeanFactory().getBean("userServerFilter"),
                ctxt.getBeanFactory().getBean("contextServerFilter"),
                ctxt.getBeanFactory().getBean("logServerFilter")
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
                if(AbstractRestExposableService.class.isAssignableFrom(serviceDef.getValue())) {
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
            if(AbstractRestExposableService.class.isAssignableFrom(serviceDef.getValue())){
                ResourceProvider provider = new SingletonResourceProvider(bean);
                resourceProviders.add(provider);
            }
        }

        factory.setResourceProviders(resourceProviders);
        factory.setApplicationContext(ctxt);
        return factory.create();
    }

}
