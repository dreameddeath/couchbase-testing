/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.service.testing;

import com.dreameddeath.core.context.impl.GlobalContextFactoryImpl;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.service.annotation.processor.ServiceExpositionDef;
import com.dreameddeath.core.service.client.rest.RestServiceClientFactory;
import com.dreameddeath.core.service.discovery.ClientDiscoverer;
import com.dreameddeath.core.service.discovery.ProxyClientDiscoverer;
import com.dreameddeath.core.service.discovery.rest.RestServiceDiscoverer;
import com.dreameddeath.core.service.interceptor.rest.feature.ClientFeatureFactory;
import com.dreameddeath.core.service.interceptor.rest.feature.ContextClientFeature;
import com.dreameddeath.core.service.interceptor.rest.feature.LogClientFeature;
import com.dreameddeath.core.service.interceptor.rest.feature.UserClientFeature;
import com.dreameddeath.core.service.registrar.ClientRegistrar;
import com.dreameddeath.core.service.registrar.IEndPointDescription;
import com.dreameddeath.core.service.registrar.RestServiceRegistrar;
import com.dreameddeath.core.service.utils.ServiceObjectMapperConfigurator;
import com.dreameddeath.core.service.utils.rest.RestServiceTypeHelper;
import com.dreameddeath.core.user.StandardMockUserFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.LifeCycle;
import org.springframework.web.context.ContextLoaderListener;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Christophe Jeunesse on 10/04/2015.
 */
public class TestingRestServer {
    public static final String DOMAIN = "services";
    private Server server;
    private final UUID daemonUid = UUID.randomUUID();
    private final UUID serverUid = UUID.randomUUID();

    private CuratorFramework curatorClient;
    private RestServiceDiscoverer serviceDiscoverer;
    private RestServiceClientFactory serviceClientFactory;
    private RestServiceRegistrar serviceRegistrar;
    private ClientRegistrar clientRegistrar;
    private ClientDiscoverer clientDiscoverer;
    private ProxyClientDiscoverer proxyClientDiscoverer;

    private ServerConnector connector;
    private Map<String,Class> beanClassNameMap = new HashMap<>();
    private Map<String,Object> beanObjMap = new HashMap<>();


    public TestingRestServer(String testName,CuratorFramework curatorClient,ObjectMapper jacksonMapper) throws Exception{
        this.curatorClient = curatorClient;
        server = new Server();
        HttpConfiguration httpConfiguration=new HttpConfiguration();
        connector = new ServerConnector(server,-1,-1,new HttpConnectionFactory(httpConfiguration),new HTTP2CServerConnectionFactory(httpConfiguration));
        server.addConnector(connector);
        ServletContextHandler contextHandler = new ServletContextHandler();
        server.setHandler(contextHandler);
        ServletHolder cxfHolder = new ServletHolder("CXF",CXFServlet.class);
        cxfHolder.setInitOrder(1);
        contextHandler.addServlet(cxfHolder, "/*");
        serviceDiscoverer = new RestServiceDiscoverer(curatorClient, DOMAIN);
        serviceRegistrar = new RestServiceRegistrar(curatorClient, DOMAIN);
        clientRegistrar = new ClientRegistrar(curatorClient, RestServiceTypeHelper.SERVICE_TECH_TYPE, DOMAIN,daemonUid.toString(),serverUid.toString());
        clientDiscoverer = new ClientDiscoverer(curatorClient, DOMAIN,RestServiceTypeHelper.SERVICE_TECH_TYPE);
        proxyClientDiscoverer = new ProxyClientDiscoverer(curatorClient,DOMAIN,RestServiceTypeHelper.SERVICE_TECH_TYPE);

        server.addLifeCycleListener(new LifeCycleListener(serviceRegistrar, serviceDiscoverer));
        contextHandler.setInitParameter("contextConfigLocation", "classpath:rest.applicationContext.xml");
        contextHandler.setAttribute("jacksonObjectMapper", jacksonMapper);
        contextHandler.setAttribute("serviceRegistrar", serviceRegistrar);
        contextHandler.setAttribute("serviceDiscoverer", serviceDiscoverer);
        contextHandler.setAttribute("clientRegistrar", clientRegistrar);
        contextHandler.setAttribute("clientDiscoverer", clientDiscoverer);
        contextHandler.setAttribute("proxyClientDiscoverer", proxyClientDiscoverer);
        contextHandler.setAttribute("clientFactory",getClientFactory());
        contextHandler.setAttribute("curatorClient", curatorClient);
        contextHandler.setAttribute("endPointInfo", new IEndPointDescription() {
            @Override public Integer securedPort() {return null;}
            @Override public Set<Protocol> protocols() {return ImmutableSet.of(Protocol.HTTP_1,Protocol.HTTP_2);}
            @Override public String daemonUid() {
                return daemonUid.toString();
            }
            @Override public String webserverUid() {
                return serverUid.toString();
            }
            @Override public int port() {
                return connector.getLocalPort();
            }
            @Override public String path() {
                return "";
            }
            @Override public String host() {
                try { return InetAddress.getLocalHost().getHostAddress();
                } catch (Exception e) {return "localhost";}
            }
            @Override public String buildInstanceUid() {
                return UUID.randomUUID().toString();
            }
        });

        contextHandler.setAttribute("beanClassNameMap", beanClassNameMap);
        contextHandler.setAttribute("beanObjMap",beanObjMap);
        contextHandler.addEventListener(new ContextLoaderListener());

        serviceClientFactory = new RestServiceClientFactory(serviceDiscoverer,clientRegistrar);
        GlobalContextFactoryImpl globalContextFactory = new GlobalContextFactoryImpl();
        globalContextFactory.setUserFactory(new StandardMockUserFactory());
        ClientFeatureFactory featureFactory = new ClientFeatureFactory();
        featureFactory.addFeature(new ContextClientFeature(globalContextFactory));
        featureFactory.addFeature(new UserClientFeature(new StandardMockUserFactory()));
        featureFactory.addFeature(new LogClientFeature());
        serviceClientFactory.setFeatureFactory(featureFactory);
        contextHandler.setAttribute("serviceClientFactory", serviceClientFactory);
    }

    public TestingRestServer(String testName,CuratorFramework curatorClient) throws Exception{
        this(testName,curatorClient, ObjectMapperFactory.BASE_INSTANCE.getMapper(ServiceObjectMapperConfigurator.SERVICE_MAPPER_CONFIGURATOR));
    }

    public void registerBeanClass(String name, Class beanClass){
        beanClassNameMap.put(name,beanClass);
    }

    public void registerBeanObject(String name,Object beanClass){
        beanObjMap.put(name,beanClass);
    }


    public void registerWrapperServerBean(String name, Class beanClass){
        beanClassNameMap.put(name+"Impl",beanClass);
        try{
            beanClassNameMap.put(name,ServiceExpositionDef.getRestServerClass(beanClass));
        }
        catch(ClassNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    public RestServiceClientFactory getClientFactory(){
        return serviceClientFactory;
    }

    public void start() throws Exception{
        if(!curatorClient.getState().equals(CuratorFrameworkState.STARTED)) {
            curatorClient.start();
        }
        curatorClient.blockUntilConnected(1, TimeUnit.MINUTES);
        CountDownLatch startingWait=new CountDownLatch(1);
        AtomicInteger errorCounter = new AtomicInteger(0);
        server.addLifeCycleListener(new LifeCycle.Listener(){
            @Override public void lifeCycleStarting(LifeCycle lifeCycle) {}
            @Override public void lifeCycleStarted(LifeCycle lifeCycle) {startingWait.countDown();}
            @Override public void lifeCycleFailure(LifeCycle lifeCycle, Throwable throwable) {
                errorCounter.incrementAndGet();
            }
            @Override public void lifeCycleStopping(LifeCycle lifeCycle) {}
            @Override public void lifeCycleStopped(LifeCycle lifeCycle) {}
        });
        server.start();
        Preconditions.checkArgument(startingWait.await(1,TimeUnit.MINUTES),"Starting failed");
        Preconditions.checkArgument(errorCounter.get()==0,"Error occurs");
    }

    public void stop()throws Exception{
        if((server!=null) && !server.isStopped()) {
            server.stop();
        }
        if(curatorClient!=null){
            curatorClient.close();
        }
    }

    public int getLocalPort(){
        return connector.getLocalPort();
    }

    public CuratorFramework getCuratorClient() {
        return curatorClient;
    }

    public UUID getDaemonUid() {
        return daemonUid;
    }

    public UUID getServerUid() {
        return serverUid;
    }

    public RestServiceDiscoverer getServiceDiscoverer() {
        return serviceDiscoverer;
    }


    public RestServiceRegistrar getServiceRegistrar() {
        return serviceRegistrar;
    }

    public ClientDiscoverer getClientDiscoverer() {
        return clientDiscoverer;
    }
}

