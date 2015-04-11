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

package com.dreameddeath.testing.service;

import com.dreameddeath.core.service.client.ServiceClientFactory;
import com.dreameddeath.core.service.discovery.ServiceDiscoverer;
import com.dreameddeath.core.service.model.AbstractExposableService;
import com.dreameddeath.core.service.registrar.IRestEndPointDescription;
import com.dreameddeath.core.service.registrar.ServiceRegistrar;
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
 * Created by CEAJ8230 on 10/04/2015.
 */
public class TestingRestServer {
    private static final String BASE_PATH = "/services";
    private Server _server;
    private CuratorFramework _curatorClient;
    private ServiceDiscoverer _serviceDiscoverer;
    private ServiceClientFactory _serviceClientFactory;
    private ServiceRegistrar _serviceRegistrar;
    private ServerConnector _connector;
    private Map<String,AbstractExposableService> _servicesMap = new HashMap<>();

    public TestingRestServer(String testName,CuratorFramework curatorClient) throws Exception{
        _curatorClient = curatorClient;
        _server = new Server();
        _connector = new ServerConnector(_server);
        _server.addConnector(_connector);
        ServletContextHandler contextHandler = new ServletContextHandler();
        _server.setHandler(contextHandler);
        ServletHolder cxfHolder = new ServletHolder("CXF",CXFServlet.class);
        cxfHolder.setInitOrder(1);
        contextHandler.addServlet(cxfHolder, "/*");
        _serviceDiscoverer = new ServiceDiscoverer(_curatorClient, BASE_PATH);
        _serviceRegistrar = new ServiceRegistrar(_curatorClient, BASE_PATH);
        _server.addLifeCycleListener(new LifeCycleListener(_serviceRegistrar, _serviceDiscoverer));
        contextHandler.setInitParameter("contextConfigLocation", "classpath:rest.test.applicationContext.xml");

        contextHandler.setAttribute("serviceRegistrar", _serviceRegistrar);
        contextHandler.setAttribute("serviceDiscoverer", _serviceDiscoverer);
        contextHandler.setAttribute("curatorClient", _curatorClient);
        contextHandler.setAttribute("endPointInfo", new IRestEndPointDescription() {
            @Override
            public int port() {
                return _connector.getLocalPort();
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

        contextHandler.setAttribute("servicesMap",_servicesMap);
        contextHandler.addEventListener(new ContextLoaderListener());

        _serviceClientFactory = new ServiceClientFactory(_serviceDiscoverer);
    }

    public void registerService(String name,AbstractExposableService service){
        _servicesMap.put(name,service);
    }


    public ServiceClientFactory getClientFactory(){
        return _serviceClientFactory;
    }


    public void start() throws Exception{
        _server.start();
    }

    public void stop()throws Exception{
        if((_server!=null) && !_server.isStopped()) {
            _server.stop();
        }
    }
}
