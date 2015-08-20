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

package com.dreameddeath.infrastructure.daemon.webserver;

import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.config.DaemonConfigProperties;
import com.dreameddeath.infrastructure.daemon.manager.ServiceDiscoveryLifeCycleManager;
import com.dreameddeath.infrastructure.daemon.manager.ServiceDiscoveryManager;
import com.dreameddeath.infrastructure.daemon.services.StandardDaemonRestEndPointDescription;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.LifeCycle;
import org.springframework.web.context.ContextLoaderListener;

/**
 * Created by Christophe Jeunesse on 18/08/2015.
 */
public class StandardWebServer {
    public static final String GLOBAL_CURATOR_CLIENT_SERVLET_PARAM_NAME = "globalCuratorClient";
    public static final String SERVICE_DISCOVERER_MANAGER_PARAM_NAME = "serviceDiscovererManager";
    public static final String END_POINT_INFO_SERVLET_PARAM_NAME = "endPointInfo";
    public static final String GLOBAL_DAEMON_LIFE_CYCLE_PARAM_NAME = "daemonLifeCycle";
    public static final String GLOBAL_DAEMON_PARAM_NAME = "daemon";


    private final AbstractDaemon _parentDaemon;
    private final String _name;
    private final Server _webServer;
    private ServiceDiscoveryManager _serviceDiscoveryManager;

    public StandardWebServer(AbstractDaemon daemon,String name,String applicationContextConfig,boolean isRoot){
        _parentDaemon = daemon;
        _name = name;
        _parentDaemon.getDaemonLifeCycle().addLifeCycleListener(new WebServerDaemonLifeCycleListner(this,isRoot));
        _serviceDiscoveryManager = new ServiceDiscoveryManager(_parentDaemon.getCuratorClient());
        _webServer = new Server();
        final ServerConnector connector = new ServerConnector(_webServer);
        String addressProp = DaemonConfigProperties.DAEMON_WEBSERVER_ADDRESS.get();
        if(addressProp!=null){
            connector.setHost(addressProp);
        }
        int port = DaemonConfigProperties.DAEMON_WEBSERVER_PORT.get();
        if(port!=0){
            connector.setPort(port);
        }

        _webServer.addConnector(connector);
        _webServer.addLifeCycleListener(new ServiceDiscoveryLifeCycleManager(_serviceDiscoveryManager));

        //Create the contextHandler and at it to the server
        ServletContextHandler contextHandler = new ServletContextHandler();
        _webServer.setHandler(contextHandler);
        contextHandler.setInitParameter("contextConfigLocation", "classpath:" + applicationContextConfig);
        contextHandler.addEventListener(new ContextLoaderListener());

        //Init Cxf context handler
        ServletHolder cxfHolder = new ServletHolder("CXF",CXFServlet.class);
        cxfHolder.setInitOrder(1);
        contextHandler.addServlet(cxfHolder, "/*");

        //Setup standardized elements
        contextHandler.setAttribute(GLOBAL_CURATOR_CLIENT_SERVLET_PARAM_NAME, _parentDaemon.getCuratorClient());
        contextHandler.setAttribute(GLOBAL_DAEMON_PARAM_NAME, this);
        contextHandler.setAttribute(GLOBAL_DAEMON_LIFE_CYCLE_PARAM_NAME, _parentDaemon.getDaemonLifeCycle());
        contextHandler.setAttribute(SERVICE_DISCOVERER_MANAGER_PARAM_NAME, _serviceDiscoveryManager);
        contextHandler.setAttribute(END_POINT_INFO_SERVLET_PARAM_NAME, new StandardDaemonRestEndPointDescription(connector));
    }

    public AbstractDaemon getParentDaemon() {
        return _parentDaemon;
    }

    public ServiceDiscoveryManager getServiceDiscoveryManager() {
        return _serviceDiscoveryManager;
    }

    public Server getWebServer() {
        return _webServer;
    }

    public String getName() {
        return _name;
    }

    public void start() throws Exception{
        _webServer.start();
    }

    public void stop() throws Exception{
        _webServer.stop();
    }

    public void join() throws Exception{
        _webServer.join();
    }

    public LifeCycle getLifeCycle(){
        return _webServer;
    }
}
