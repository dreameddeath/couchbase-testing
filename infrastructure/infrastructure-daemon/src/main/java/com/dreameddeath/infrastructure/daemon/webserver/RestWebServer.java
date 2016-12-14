/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.infrastructure.daemon.webserver;

import com.dreameddeath.infrastructure.daemon.config.DaemonConfigProperties;
import com.dreameddeath.infrastructure.daemon.servlet.RestServicesServletContextHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.List;

/**
 * Created by Christophe Jeunesse on 18/08/2015.
 */
public class RestWebServer extends AbstractWebServer<RestWebServer.Builder> {
    public RestWebServer(Builder builder){
        super(builder);
    }

    @Override
    protected List<ServletContextHandler> buildContextHandlers(Builder builder){
        String path = builder.apiPath;
        if(path ==null){
            path = DaemonConfigProperties.DAEMON_WEBSERVER_API_PATH_PREFIX.get();
        }
        List<ServletContextHandler> handlersList = super.buildContextHandlers(builder);
        handlersList.add(new RestServicesServletContextHandler(this,builder.applicationContextConfig,path,getServiceDiscoveryManager()));
        return handlersList;
    }




    public static class Builder extends AbstractWebServer.Builder<Builder>{
        private String apiPath=null;
        private String applicationContextConfig;

        public Builder(){
            super.withServiceDiscoveryManager(true);
        }

        @Override
        public Builder withServiceDiscoveryManager(boolean withServiceDiscoveryManager) {
            throw new RuntimeException("Shouldn't override the parameter");
        }

        public Builder withApplicationContextConfig(String configName){
            applicationContextConfig = configName;
            return this;
        }

        public Builder withPath(String path){
            apiPath = path;
            return this;
        }
    }

    public static Builder builder(){
        return new Builder();
    }
}
