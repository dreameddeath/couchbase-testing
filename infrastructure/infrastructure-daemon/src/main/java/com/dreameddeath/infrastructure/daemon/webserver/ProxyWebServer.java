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

import com.dreameddeath.infrastructure.daemon.config.DaemonConfigProperties;
import com.dreameddeath.infrastructure.daemon.servlet.ProxyServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 21/08/2015.
 */
public class ProxyWebServer extends AbstractWebServer {
    public ProxyWebServer(Builder builder){
        super(builder);

        //Create the contextHandler and at it to the server
        ServletContextHandler contextHandler = new ServletContextHandler();
        getWebServer().setHandler(contextHandler);

        String proxyPath = DaemonConfigProperties.DAEMON_WEBSERVER_PROXY_API_PATH_PREFIX.get();
        proxyPath = proxyPath.replaceAll("^/+","");
        proxyPath = proxyPath.replaceAll("/+$","");
        //Init Cxf context handler
        ServletHolder proxyHolder = new ServletHolder("proxy", ProxyServlet.class);
        proxyHolder.setInitOrder(1);
        contextHandler.addServlet(proxyHolder, "/"+proxyPath+"/*");

        //Setup standardized elements
        contextHandler.setAttribute(GLOBAL_CURATOR_CLIENT_SERVLET_PARAM_NAME, getParentDaemon().getCuratorClient());
        contextHandler.setAttribute(GLOBAL_DAEMON_PARAM_NAME, this);
        contextHandler.setAttribute(GLOBAL_DAEMON_LIFE_CYCLE_PARAM_NAME, getParentDaemon().getDaemonLifeCycle());
        contextHandler.setAttribute(ProxyServlet.PROXY_PREFIX_PARAM_NAME, proxyPath);
        contextHandler.setAttribute(ProxyServlet.SERVICE_DISCOVERER_PATHES_PARAM_NAME,builder._discoverPaths);
    }



    public static Builder builder(){
        return new Builder();
    }

    public static class Builder extends AbstractWebServer.Builder<Builder>{
        List<String> _discoverPaths=new ArrayList<>();

        public Builder withDiscoverPath(String path){
            _discoverPaths.add(path);
            return this;
        }
    }
}
