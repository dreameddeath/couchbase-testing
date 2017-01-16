/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.infrastructure.daemon.webserver;

import com.dreameddeath.core.service.utils.rest.RestServiceTypeHelper;
import com.dreameddeath.infrastructure.daemon.config.DaemonConfigProperties;
import com.dreameddeath.infrastructure.daemon.servlet.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 28/08/2015.
 */
public class WebAppWebServer extends AbstractWebServer<WebAppWebServer.Builder>{
    public WebAppWebServer(Builder builder){
        super(builder);
    }

    @Override
    protected List<ServletContextHandler> buildContextHandlers(Builder builder) {
        List<ServletContextHandler> handlersList = super.buildContextHandlers(builder);
        WebAppServletContextHandler webAppHandler = new WebAppServletContextHandler(this,builder.path,builder.resourcePath);
        handlersList.add(webAppHandler);
        WebJarsServletContextHandler webJarHandler= new WebJarsServletContextHandler(this,builder.getLibsPath(),builder.webJarsSubPath,builder.forTesting);
        handlersList.add(webJarHandler);

        if(builder.withProxy) {
            for (String serviceType : builder.perServiceTypeDiscoverDomainsForProxy.keys()) {
                Collection<String> domains = builder.perServiceTypeDiscoverDomainsForProxy.get(serviceType);
                Preconditions.checkNotNull(builder.perServiceTypePathForProxy.get(serviceType), "The service type %d should have a path defined", serviceType);
                handlersList.add(new ProxyServletContextHandler(this, domains, serviceType, builder.perServiceTypePathForProxy.get(serviceType)));
            }
        }

        if(builder.withApis){
            String path = builder.apiPath;

            if(path ==null){
                path = DaemonConfigProperties.DAEMON_WEBSERVER_API_PATH_PREFIX.get();
            }
            handlersList.add(new RestServicesServletContextHandler(this,builder.applicationContextConfig,path,getServiceDiscoveryManager()));
        }
        return handlersList;
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
        private final Multimap<String,String> perServiceTypeDiscoverDomainsForProxy = ArrayListMultimap.create();
        private final Map<String,String> perServiceTypePathForProxy = new HashMap<>();
        private boolean withApis=false;
        private String apiPath=null;
        private String applicationContextConfig;

        public Builder(){
            perServiceTypePathForProxy.put(RestServiceTypeHelper.SERVICE_TECH_TYPE,"");
        }


        public Builder withApplicationContextConfig(String applicationContextConfig) {
            this.applicationContextConfig = applicationContextConfig;
            return this;
        }

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

        public Builder withForTesting(boolean forTesting) {
            this.forTesting = forTesting;
            return this;
        }

        public String getLibsPath(){
            return ServletUtils.normalizePath(new String[]{path,libSubPath},false);
        }

        public Builder withApiPath(String apiPath) {
            withApis = (apiPath!=null);
            super.withServiceDiscoveryManager(withApis);
            this.apiPath = apiPath;
            return this;
        }

        /*
        *
        * Proxy Config
        *
        */
        public Builder withProxyDiscoverDomain(String domain){
            return withProxyDiscoverDomain(RestServiceTypeHelper.SERVICE_TECH_TYPE,domain);
        }

        public Builder withProxyDiscoverDomain(String serviceType, String domain){
            this.withProxy=true;
            perServiceTypeDiscoverDomainsForProxy.put(serviceType,domain);
            return this;
        }

        public Builder withProxyDiscoverDomains( final Collection<String> domains){
            return withProxyDiscoverDomains(RestServiceTypeHelper.SERVICE_TECH_TYPE,domains);
        }
        public Builder withProxyDiscoverDomains(final String serviceType, final Collection<String> domains){
            domains.forEach(domain->withProxyDiscoverDomain(serviceType,domain));
            return this;
        }
        public Builder withProxyPath(String serviceType, String path){
            perServiceTypePathForProxy.put(serviceType,path);
            return this;
        }

    }
}
