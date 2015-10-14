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

package com.dreameddeath.testing.service;

import com.dreameddeath.core.service.client.ServiceClientFactory;
import com.dreameddeath.core.service.discovery.ServiceDiscoverer;
import com.dreameddeath.core.service.model.AbstractExposableService;
import com.dreameddeath.core.service.registrar.IRestEndPointDescription;
import com.dreameddeath.core.service.registrar.ServiceRegistrar;
import com.dreameddeath.core.service.utils.ServiceJacksonObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.ContextLoaderListener;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 10/04/2015.
 */
public class TestingRestServer {
    private static final String BASE_PATH = "/services";
    private Server server;
    private CuratorFramework curatorClient;
    private ServiceDiscoverer serviceDiscoverer;
    private ServiceClientFactory serviceClientFactory;
    private ServiceRegistrar serviceRegistrar;
    private ServerConnector connector;
    private Map<String,AbstractExposableService> servicesMap = new HashMap<>();

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
        serviceDiscoverer = new ServiceDiscoverer(curatorClient, BASE_PATH);
        serviceRegistrar = new ServiceRegistrar(curatorClient, BASE_PATH);
        server.addLifeCycleListener(new LifeCycleListener(serviceRegistrar, serviceDiscoverer));
        contextHandler.setInitParameter("contextConfigLocation", "classpath:rest.test.applicationContext.xml");
        contextHandler.setAttribute("jacksonObjectMapper", jacksonMapper);
        contextHandler.setAttribute("serviceRegistrar", serviceRegistrar);
        contextHandler.setAttribute("serviceDiscoverer", serviceDiscoverer);
        contextHandler.setAttribute("curatorClient", curatorClient);
        contextHandler.setAttribute("endPointInfo", new IRestEndPointDescription() {
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
        });

        contextHandler.setAttribute("servicesMap",servicesMap);
        contextHandler.addEventListener(new ContextLoaderListener());

        serviceClientFactory = new ServiceClientFactory(serviceDiscoverer);
    }



    public TestingRestServer(String testName,CuratorFramework curatorClient) throws Exception{
        this(testName,curatorClient,ServiceJacksonObjectMapper.getInstance());
    }

    public void registerService(String name,AbstractExposableService service){
        servicesMap.put(name,service);
    }


    public ServiceClientFactory getClientFactory(){
        return serviceClientFactory;
    }


    public void start() throws Exception{
        server.start();
    }

    public void stop()throws Exception{
        if((server!=null) && !server.isStopped()) {
            server.stop();
        }
    }
}
