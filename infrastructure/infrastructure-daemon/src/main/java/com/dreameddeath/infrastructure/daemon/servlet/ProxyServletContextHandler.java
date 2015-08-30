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

package com.dreameddeath.infrastructure.daemon.servlet;

import com.dreameddeath.infrastructure.daemon.config.DaemonConfigProperties;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.List;

/**
 * Created by Christophe Jeunesse on 28/08/2015.
 */
public class ProxyServletContextHandler extends ServletContextHandler {
    private final AbstractWebServer _parentServer;

    public ProxyServletContextHandler(AbstractWebServer parentServer,List<String> pathsToSelfDiscover){
        _parentServer = parentServer;

        String proxyPath = DaemonConfigProperties.DAEMON_WEBSERVER_PROXY_API_PATH_PREFIX.get();
        proxyPath = ServletUtils.normalizePath(proxyPath,false);
        this.setContextPath(proxyPath);
        this.setDisplayName("Proxy for Apis");

        //Init Cxf context handler
        ServletHolder proxyHolder = new ServletHolder("proxy-api", ProxyServlet.class);
        proxyHolder.setName("Proxy for Apis Proxy Holder");
        proxyHolder.setInitOrder(1);
        this.addServlet(proxyHolder, "/*");

        //Setup standardized elements
        this.setAttribute(AbstractWebServer.GLOBAL_CURATOR_CLIENT_SERVLET_PARAM_NAME, _parentServer.getParentDaemon().getCuratorClient());
        this.setAttribute(AbstractWebServer.GLOBAL_DAEMON_PARAM_NAME, _parentServer.getParentDaemon());
        this.setAttribute(AbstractWebServer.GLOBAL_DAEMON_LIFE_CYCLE_PARAM_NAME, _parentServer.getParentDaemon().getDaemonLifeCycle());
        this.setAttribute(ProxyServlet.PROXY_PREFIX_PARAM_NAME, proxyPath);
        this.setAttribute(ProxyServlet.SERVICE_DISCOVERER_PATHES_PARAM_NAME, pathsToSelfDiscover);
    }
}
