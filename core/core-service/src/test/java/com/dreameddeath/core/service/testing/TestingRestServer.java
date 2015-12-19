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
import com.dreameddeath.core.service.annotation.processor.ServiceExpositionDef;
import com.dreameddeath.core.service.client.ServiceClientFactory;
import com.dreameddeath.core.service.discovery.ClientDiscoverer;
import com.dreameddeath.core.service.discovery.ProxyClientDiscoverer;
import com.dreameddeath.core.service.discovery.ServiceDiscoverer;
import com.dreameddeath.core.service.registrar.ClientRegistrar;
import com.dreameddeath.core.service.registrar.IRestEndPointDescription;
import com.dreameddeath.core.service.registrar.ServiceRegistrar;
import com.dreameddeath.core.service.utils.ServiceObjectMapperConfigurator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.ContextLoaderListener;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Christophe Jeunesse on 10/04/2015.
 */
public class TestingRestServer {
    public static final String DOMAIN = "services";
    private Server server;
    private final UUID daemonUid = UUID.randomUUID();
    private final UUID serverUid = UUID.randomUUID();

    private CuratorFramework curatorClient;
    private ServiceDiscoverer serviceDiscoverer;
    private ServiceClientFactory serviceClientFactory;
    private ServiceRegistrar serviceRegistrar;
    private ClientRegistrar clientRegistrar;
    private ClientDiscoverer clientDiscoverer;
    private ProxyClientDiscoverer proxyClientDiscoverer;

    private ServerConnector connector;
    private Map<String,Class> beanClassNameMap = new HashMap<>();
    private Map<String,Object> beanObjMap = new HashMap<>();


    public TestingRestServer(String testName,CuratorFramework curatorClient,ObjectMapper jacksonMapper) throws Exception{
        this.curatorClient = curatorClient;
        server = new Server();
        connector = new ServerConnector(server);
        server.addConnector(connector);
        ServletContextHandler contextHandler = new ServletContextHandler();
        server.setHandler(contextHandler);
        ServletHolder cxfHolder = new ServletHolder("CXF",CXFServlet.class);
        cxfHolder.setInitOrder(1);
        contextHandler.addServlet(cxfHolder, "/*");
        serviceDiscoverer = new ServiceDiscoverer(curatorClient, DOMAIN);
        serviceRegistrar = new ServiceRegistrar(curatorClient, DOMAIN);
        clientRegistrar = new ClientRegistrar(curatorClient, DOMAIN,daemonUid.toString(),serverUid.toString());
        clientDiscoverer = new ClientDiscoverer(curatorClient, DOMAIN);
        proxyClientDiscoverer = new ProxyClientDiscoverer(curatorClient,DOMAIN);

        server.addLifeCycleListener(new LifeCycleListener(serviceRegistrar, serviceDiscoverer));
        contextHandler.setInitParameter("contextConfigLocation", "classpath:rest.applicationContext.xml");
        contextHandler.setAttribute("jacksonObjectMapper", jacksonMapper);
        contextHandler.setAttribute("serviceRegistrar", serviceRegistrar);
        contextHandler.setAttribute("serviceDiscoverer", serviceDiscoverer);
        contextHandler.setAttribute("clientRegistrar", clientRegistrar);
        contextHandler.setAttribute("clientDiscoverer", clientDiscoverer);
        contextHandler.setAttribute("proxyClientDiscoverer", proxyClientDiscoverer);

        contextHandler.setAttribute("curatorClient", curatorClient);
        contextHandler.setAttribute("endPointInfo", new IRestEndPointDescription() {

            @Override
            public String daemonUid() {
                return daemonUid.toString();
            }

            @Override
            public String webserverUid() {
                return serverUid.toString();
            }

            @Override
            public int port() {
                return connector.getLocalPort();
            }

            @Override
            public String path() {
                return "";
            }

            @Override
            public String host() {
                try {
                    return InetAddress.getLocalHost().getHostAddress();
                } catch (Exception e) {
                    return "localhost";
                }
            }

            @Override
            public String buildInstanceUid() {
                return UUID.randomUUID().toString();
            }

        });

        contextHandler.setAttribute("beanClassNameMap", beanClassNameMap);
        contextHandler.setAttribute("beanObjMap",beanObjMap);
        contextHandler.addEventListener(new ContextLoaderListener());

        serviceClientFactory = new ServiceClientFactory(serviceDiscoverer,clientRegistrar);
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


    public ServiceClientFactory getClientFactory(){
        return serviceClientFactory;
    }


    public void start() throws Exception{
        if(!curatorClient.getState().equals(CuratorFrameworkState.STARTED)) {
            curatorClient.start();
        }
        curatorClient.blockUntilConnected(1, TimeUnit.MINUTES);
        server.start();
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

    public ServiceDiscoverer getServiceDiscoverer() {
        return serviceDiscoverer;
    }

    public ClientDiscoverer getClientDiscoverer() {
        return clientDiscoverer;
    }
}

