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

package com.dreameddeath.infrastructure.daemon;

import com.dreameddeath.core.config.ConfigPropertyFactory;
import com.dreameddeath.core.curator.CuratorFrameworkFactory;
import com.dreameddeath.core.service.discovery.ServiceDiscoverer;
import com.dreameddeath.core.service.registrar.IRestEndPointDescription;
import com.dreameddeath.core.service.registrar.ServiceRegistrar;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.ContextLoaderListener;

/**
 * Created by CEAJ8230 on 05/02/2015.
 */
public class AbstractDaemon {
    public static final String GLOBAL_CURATOR_CLIENT_SERVLET_PARAM_NAME = "globalCuratorClient";
    public static final String SERVICE_REGISTRAR_SERVLET_PARAM_NAME = "serviceRegistrar";
    public static final String SERVICE_DISCOVERER_SERVLET_PARAM_NAME = "serviceDiscoverer";
    public static final String END_POINT_INFO_SERVLET_PARAM_NAME = "endPointInfo";
    private final CuratorFramework _curatorClient;
    private final Server _webServer;

    protected static CuratorFramework setupDefaultCuratorClient(){
        try {
            String addressProp = ConfigPropertyFactory.getStringProperty("zookeeper.cluster.addresses", (String)null).getMandatoryValue("The zookeeper cluster address must be defined");
            int sleepTime = ConfigPropertyFactory.getIntProperty("zookeeper.retry.sleepTime", 1000).get();
            int maxRetries = ConfigPropertyFactory.getIntProperty("zookeeper.retry.maxRetries", 3).get();
            CuratorFramework client = CuratorFrameworkFactory.newClient(addressProp, new ExponentialBackoffRetry(sleepTime, maxRetries));
            client.start();
            return client;
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    protected Server setupDefaultWebServer(){
        String selfDiscoveryBasePath = ConfigPropertyFactory.getStringProperty("zookeeper.services.self-discovery.basepath", "services").get();
        Server server = new Server();
        final ServerConnector connector = new ServerConnector(server);
        String addressProp = ConfigPropertyFactory.getStringProperty("deamon.webserver.address", (String)null).get();
        if(addressProp!=null){
            connector.setHost(addressProp);
        }
        int port = ConfigPropertyFactory.getIntProperty("deamon.webserver.port", 0).get();
        if(port!=0){
            connector.setPort(port);
        }

        server.addConnector(connector);
        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setAttribute(GLOBAL_CURATOR_CLIENT_SERVLET_PARAM_NAME, _curatorClient);
        server.setHandler(contextHandler);
        ServletHolder cxfHolder = new ServletHolder("CXF",CXFServlet.class);
        cxfHolder.setInitOrder(1);
        contextHandler.addServlet(cxfHolder, "/*");

        ServiceDiscoverer serviceDiscoverer = new ServiceDiscoverer(_curatorClient, selfDiscoveryBasePath);
        ServiceRegistrar serviceRegistrar = new ServiceRegistrar(_curatorClient, selfDiscoveryBasePath);
        server.addLifeCycleListener(new DaemonServletLifeCycleListener(serviceRegistrar, serviceDiscoverer));
        contextHandler.setInitParameter("contextConfigLocation", "classpath:rest.test.applicationContext.xml");
        contextHandler.setAttribute(SERVICE_REGISTRAR_SERVLET_PARAM_NAME, serviceRegistrar);
        contextHandler.setAttribute(SERVICE_DISCOVERER_SERVLET_PARAM_NAME, serviceDiscoverer);

        contextHandler.setAttribute(END_POINT_INFO_SERVLET_PARAM_NAME, new IRestEndPointDescription() {
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
                    return connector.getHost();
                } catch (Exception e) {
                    return "localhost";
                }
            }
        });

        contextHandler.addEventListener(new ContextLoaderListener());
        return server;
    }

    public AbstractDaemon(){
        this(setupDefaultCuratorClient());
    }

    public AbstractDaemon(CuratorFramework curatorClient){
        _curatorClient = curatorClient;
        _webServer = setupDefaultWebServer();
    }

    public CuratorFramework getCuratorClient(){
        return _curatorClient;
    }



}
