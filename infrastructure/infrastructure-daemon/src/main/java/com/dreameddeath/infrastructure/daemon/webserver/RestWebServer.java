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
import com.dreameddeath.infrastructure.daemon.servlet.RestServicesServletContextHandler;

/**
 * Created by Christophe Jeunesse on 18/08/2015.
 */
public class RestWebServer extends AbstractWebServer {
    private ServiceDiscoveryManager _serviceDiscoveryManager;

    public RestWebServer(Builder builder){
        super(builder);
        _serviceDiscoveryManager = new ServiceDiscoveryManager(getParentDaemon().getCuratorClient());
        getWebServer().addLifeCycleListener(new ServiceDiscoveryLifeCycleManager(_serviceDiscoveryManager));

        String path = builder._apiPath;
        if(path ==null){
            path = DaemonConfigProperties.DAEMON_WEBSERVER_API_PATH_PREFIX.get();
        }
        getWebServer().setHandler(new RestServicesServletContextHandler(this,builder._applicationContextConfig,path,_serviceDiscoveryManager));
    }


    public ServiceDiscoveryManager getServiceDiscoveryManager() {
        return _serviceDiscoveryManager;
    }


    public static class Builder extends AbstractWebServer.Builder<Builder>{
        private String _apiPath=null;
        private String _applicationContextConfig;

        public Builder withApplicationContextConfig(String configName){
            _applicationContextConfig = configName;
            return this;
        }

        public Builder withPath(String path){
            _apiPath = path;
            return this;
        }
    }

    public static Builder builder(){
        return new Builder();
    }
}
