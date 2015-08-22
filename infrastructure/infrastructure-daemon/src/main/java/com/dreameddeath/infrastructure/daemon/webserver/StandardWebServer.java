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

import com.dreameddeath.infrastructure.daemon.manager.ServiceDiscoveryLifeCycleManager;
import com.dreameddeath.infrastructure.daemon.manager.ServiceDiscoveryManager;
import com.dreameddeath.infrastructure.daemon.services.StandardDaemonRestEndPointDescription;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.ContextLoaderListener;

/**
 * Created by Christophe Jeunesse on 18/08/2015.
 */
public class StandardWebServer extends AbstractWebServer {
    public static final String SERVICE_DISCOVERER_MANAGER_PARAM_NAME = "serviceDiscovererManager";
    public static final String END_POINT_INFO_SERVLET_PARAM_NAME = "endPointInfo";


    private ServiceDiscoveryManager _serviceDiscoveryManager;

    public StandardWebServer(Builder builder){
        super(builder);
        _serviceDiscoveryManager = new ServiceDiscoveryManager(getParentDaemon().getCuratorClient());
        getWebServer().addLifeCycleListener(new ServiceDiscoveryLifeCycleManager(_serviceDiscoveryManager));

        //Create the contextHandler and at it to the server
        ServletContextHandler contextHandler = new ServletContextHandler();
        getWebServer().setHandler(contextHandler);
        contextHandler.setInitParameter("contextConfigLocation", "classpath:" + builder._applicationContextConfig);
        contextHandler.addEventListener(new ContextLoaderListener());

        //Init Cxf context handler
        ServletHolder cxfHolder = new ServletHolder("CXF",CXFServlet.class);
        cxfHolder.setInitOrder(1);
        contextHandler.addServlet(cxfHolder, "/*");

        //Setup standardized elements
        contextHandler.setAttribute(GLOBAL_CURATOR_CLIENT_SERVLET_PARAM_NAME, getParentDaemon().getCuratorClient());
        contextHandler.setAttribute(GLOBAL_DAEMON_PARAM_NAME, this);
        contextHandler.setAttribute(GLOBAL_DAEMON_LIFE_CYCLE_PARAM_NAME, getParentDaemon().getDaemonLifeCycle());
        contextHandler.setAttribute(SERVICE_DISCOVERER_MANAGER_PARAM_NAME, _serviceDiscoveryManager);
        contextHandler.setAttribute(END_POINT_INFO_SERVLET_PARAM_NAME, new StandardDaemonRestEndPointDescription(getServerConnector()));
    }


    public ServiceDiscoveryManager getServiceDiscoveryManager() {
        return _serviceDiscoveryManager;
    }


    public static class Builder extends AbstractWebServer.Builder<Builder>{
        private String _applicationContextConfig;

        public Builder withApplicationContextConfig(String configName){
            _applicationContextConfig = configName;
            return this;
        }
    }

    public static Builder builder(){
        return new Builder();
    }
}
