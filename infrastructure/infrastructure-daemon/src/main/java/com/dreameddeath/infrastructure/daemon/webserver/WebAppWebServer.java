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
import com.dreameddeath.infrastructure.daemon.manager.ServiceDiscoveryLifeCycleManager;
import com.dreameddeath.infrastructure.daemon.manager.ServiceDiscoveryManager;
import com.dreameddeath.infrastructure.daemon.servlet.*;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 28/08/2015.
 */
public class WebAppWebServer extends AbstractWebServer {
    private ServiceDiscoveryManager serviceDiscoveryManager;

    public WebAppWebServer(Builder builder){
        super(builder);
        List<ServletContextHandler> handlersList = new ArrayList<>();

        WebAppServletContextHandler webAppHandler = new WebAppServletContextHandler(this,builder.path,builder.resourcePath);
        handlersList.add(webAppHandler);
        WebJarsServletContextHandler webJarHandler= new WebJarsServletContextHandler(this,builder.getLibsPath(),builder.webJarsSubPath,builder.forTesting);
        handlersList.add(webJarHandler);
        if(builder.withProxy){
            handlersList.add(new ProxyServletContextHandler(this,builder.discoverPaths));
        }

        if(builder.withApis){
            String path = builder.apiPath;
            serviceDiscoveryManager = new ServiceDiscoveryManager(getParentDaemon().getCuratorClient());
            getWebServer().addLifeCycleListener(new ServiceDiscoveryLifeCycleManager(serviceDiscoveryManager));

            if(path ==null){
                path = DaemonConfigProperties.DAEMON_WEBSERVER_API_PATH_PREFIX.get();
            }
            handlersList.add(new RestServicesServletContextHandler(this,builder.applicationContextConfig,path,serviceDiscoveryManager));
        }

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        ServletContextHandler[] handlersArray =  new ServletContextHandler[handlersList.size()];
        for(int handlerPos=0;handlerPos<handlersArray.length;++handlerPos){
            handlersArray[handlerPos] = handlersList.get(handlerPos);
        }
        contexts.setHandlers(handlersArray);

        setHandler(contexts);
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder extends AbstractWebServer.Builder<Builder>{
        private String path="webapp";
        private String libSubPath = "libs";
        private String webJarsSubPath = "webjars";
        private String resourcePath="classpath:META-INF/resources/webapp";
        private boolean forTesting = false;
        private boolean withProxy = false;
        private List<String> discoverPaths=new ArrayList<>();
        private boolean withApis=false;
        private String apiPath=null;
        private String applicationContextConfig;


        public Builder withPath(String path){
            this.path = path;
            return this;
        }

        public Builder withResourcePath(String resourcePath) {
            this.resourcePath = resourcePath;
            return this;
        }

        public Builder withLibSubPath(String libSubPath) {
            this.libSubPath = libSubPath;
            return this;
        }

        public Builder withWebJarsSubPath(String webJarsSubPath) {
            this.webJarsSubPath = webJarsSubPath;
            return this;
        }

        public Builder withDiscoverPaths(List<String> discoverPaths) {
            this.discoverPaths = discoverPaths;
            return this;
        }

        public Builder withForTesting(boolean forTesting) {
            this.forTesting = forTesting;
            return this;
        }

        public String getLibsPath(){
            return ServletUtils.normalizePath(new String[]{path,libSubPath},false);
        }

        public Builder withApiPath(String apiPath) {
            withApis = (apiPath!=null);
            this.apiPath = apiPath;
            return this;
        }

        public Builder withApplicationContextConfig(String applicationContextConfig) {
            this.applicationContextConfig = applicationContextConfig;
            return this;
        }

    }
}
