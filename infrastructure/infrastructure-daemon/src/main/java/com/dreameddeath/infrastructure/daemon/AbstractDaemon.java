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

package com.dreameddeath.infrastructure.daemon;

import com.dreameddeath.core.config.exception.ConfigPropertyValueNotFoundException;
import com.dreameddeath.core.curator.CuratorFrameworkFactory;
import com.dreameddeath.core.service.registrar.IRestEndPointDescription;
import com.dreameddeath.infrastructure.common.CommonConfigProperties;
import com.dreameddeath.infrastructure.daemon.config.DaemonConfigProperties;
import com.dreameddeath.infrastructure.daemon.lifecycle.DaemonLifeCycle;
import com.dreameddeath.infrastructure.daemon.manager.ServiceDiscoveryLifeCycleManager;
import com.dreameddeath.infrastructure.daemon.manager.ServiceDiscoveryManager;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.ContextLoaderListener;

import java.net.InetAddress;

/**
 * Created by Christophe Jeunesse on 05/02/2015.
 */
public class AbstractDaemon {
    public static final String GLOBAL_CURATOR_CLIENT_SERVLET_PARAM_NAME = "globalCuratorClient";
    public static final String SERVICE_DISCOVERER_MANAGER_PARAM_NAME = "serviceDiscovererManager";
    public static final String END_POINT_INFO_SERVLET_PARAM_NAME = "endPointInfo";
    public static final String GLOBAL_DAEMON_LIFE_CYCLE_PARAM_NAME = "daemonLifeCycle";
    public static final String GLOBAL_DAEMON_PARAM_NAME = "daemon";

    private Status _status=Status.STOPPED;
    private final DaemonLifeCycle _daemonLifeCycle=new DaemonLifeCycle(AbstractDaemon.this);
    private final CuratorFramework _curatorClient;
    private final Server _webServer;
    private ServiceDiscoveryManager _serviceDiscoveryManager;

    protected static CuratorFramework setupDefaultCuratorClient(){
        try {
            String addressProp = CommonConfigProperties.ZOOKEEPER_CLUSTER_ADDREES.getMandatoryValue("The zookeeper cluster address must be defined");
            int sleepTime = CommonConfigProperties.ZOOKEEPER_CLUSTER_SLEEP_TIME.getMandatoryValue("The sleep time is not set");
            int maxRetries = CommonConfigProperties.ZOOKEEPER_CLUSTER_MAX_RECONNECTION_ATTEMPTS.getMandatoryValue("The max connection time must be set");
            CuratorFramework client = CuratorFrameworkFactory.newClient(addressProp, new ExponentialBackoffRetry(sleepTime, maxRetries));
            client.start();
            return client;
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    protected Server setupDefaultWebServer() throws ConfigPropertyValueNotFoundException {
        Server server = new Server();
        final ServerConnector connector = new ServerConnector(server);
        String addressProp = DaemonConfigProperties.DAEMON_WEBSERVER_ADDRESS.get();
        if(addressProp!=null){
            connector.setHost(addressProp);
        }
        int port = DaemonConfigProperties.DAEMON_WEBSERVER_PORT.get();
        if(port!=0){
            connector.setPort(port);
        }

        server.addConnector(connector);

        //Create the contextHandler and at it to the server
        ServletContextHandler contextHandler = new ServletContextHandler();
        server.setHandler(contextHandler);
        contextHandler.setInitParameter("contextConfigLocation", "classpath:applicationContext.xml");
        contextHandler.addEventListener(new ContextLoaderListener());

        //Init Cxf context handler
        ServletHolder cxfHolder = new ServletHolder("CXF",CXFServlet.class);
        cxfHolder.setInitOrder(1);
        contextHandler.addServlet(cxfHolder, "/*");

        contextHandler.setAttribute(GLOBAL_CURATOR_CLIENT_SERVLET_PARAM_NAME, _curatorClient);
        contextHandler.setAttribute(GLOBAL_DAEMON_PARAM_NAME, this);
        contextHandler.setAttribute(GLOBAL_DAEMON_LIFE_CYCLE_PARAM_NAME, _daemonLifeCycle);
        contextHandler.setAttribute(SERVICE_DISCOVERER_MANAGER_PARAM_NAME, _serviceDiscoveryManager);
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
                    if(connector.getHost()!=null) {
                        return connector.getHost();
                    }
                    else{
                        return InetAddress.getLocalHost().getHostAddress();
                    }
                } catch (Exception e) {
                    return "localhost";
                }
            }
        });
        return server;
    }

    public AbstractDaemon(){
        this(setupDefaultCuratorClient());
    }

    public AbstractDaemon(CuratorFramework curatorClient){
        _curatorClient = curatorClient;
        _serviceDiscoveryManager = new ServiceDiscoveryManager(_curatorClient);
        _daemonLifeCycle.addLifeCycleListener(new ServiceDiscoveryLifeCycleManager(_serviceDiscoveryManager));
        try {
            _webServer = setupDefaultWebServer();
        }
        catch(ConfigPropertyValueNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    public CuratorFramework getCuratorClient(){
        return _curatorClient;
    }


    public Server getWebServer() {
        return _webServer;
    }

    public ServiceDiscoveryManager getServiceDiscoveryManager() {
        return _serviceDiscoveryManager;
    }

    public DaemonLifeCycle getDaemonLifeCycle() {
        return _daemonLifeCycle;
    }

    public void setStatus(Status status){
        _status = status;
    }

    public Status getStatus() {
        return _status;
    }

    public enum Status{
        STOPPED,
        STARTING,
        STARTED,
        STOPPING
    }

    public void startAndJoin() throws Exception{
        //Starting using the status manager
        _daemonLifeCycle.start();
        _webServer.join();
    }
}
