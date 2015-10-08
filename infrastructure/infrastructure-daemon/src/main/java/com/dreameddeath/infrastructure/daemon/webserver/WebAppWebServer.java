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
    private ServiceDiscoveryManager _serviceDiscoveryManager;

    public WebAppWebServer(Builder builder){
        super(builder);
        List<ServletContextHandler> handlersList = new ArrayList<>();

        WebAppServletContextHandler webAppHandler = new WebAppServletContextHandler(this,builder._path,builder._resourcePath);
        handlersList.add(webAppHandler);
        WebJarsServletContextHandler webJarHandler= new WebJarsServletContextHandler(this,builder.getLibsPath(),builder._webJarsSubPath,builder._forTesting);
        handlersList.add(webJarHandler);
        if(builder._withProxy){
            handlersList.add(new ProxyServletContextHandler(this,builder._discoverPaths));
        }

        if(builder._withApis){
            String path = builder._apiPath;
            _serviceDiscoveryManager = new ServiceDiscoveryManager(getParentDaemon().getCuratorClient());
            getWebServer().addLifeCycleListener(new ServiceDiscoveryLifeCycleManager(_serviceDiscoveryManager));

            if(path ==null){
                path = DaemonConfigProperties.DAEMON_WEBSERVER_API_PATH_PREFIX.get();
            }
            handlersList.add(new RestServicesServletContextHandler(this,builder._applicationContextConfig,path,_serviceDiscoveryManager));
        }

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        ServletContextHandler[] handlersArray =  new ServletContextHandler[handlersList.size()];
        for(int handlerPos=0;handlerPos<handlersArray.length;++handlerPos){
            handlersArray[handlerPos] = handlersList.get(handlerPos);
        }
        contexts.setHandlers(handlersArray);

        getWebServer().setHandler(contexts);
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder extends AbstractWebServer.Builder<Builder>{
        private String _path="webapp";
        private String _libSubPath = "libs";
        private String _webJarsSubPath = "webjars";
        private String _resourcePath="classpath:META-INF/resources/webapp";
        private boolean _forTesting = false;
        private boolean _withProxy = false;
        private List<String> _discoverPaths=new ArrayList<>();
        private boolean _withApis=false;
        private String _apiPath=null;
        private String _applicationContextConfig;


        public Builder withPath(String path){
            _path = path;
            return this;
        }

        public Builder withResourcePath(String resourcePath) {
            _resourcePath = resourcePath;
            return this;
        }

        public Builder withLibSubPath(String libSubPath) {
            _libSubPath = libSubPath;
            return this;
        }

        public Builder withWebJarsSubPath(String webJarsSubPath) {
            _webJarsSubPath = webJarsSubPath;
            return this;
        }

        public Builder withDiscoverPaths(List<String> discoverPaths) {
            _discoverPaths = discoverPaths;
            return this;
        }

        public Builder withForTesting(boolean forTesting) {
            _forTesting = forTesting;
            return this;
        }

        public String getLibsPath(){
            return ServletUtils.normalizePath(new String[]{_path,_libSubPath},false);
        }

        public Builder withApiPath(String apiPath) {
            _withApis = (apiPath!=null);
            _apiPath = apiPath;
            return this;
        }

        public Builder withApplicationContextConfig(String applicationContextConfig) {
            _applicationContextConfig = applicationContextConfig;
            return this;
        }

    }
}
