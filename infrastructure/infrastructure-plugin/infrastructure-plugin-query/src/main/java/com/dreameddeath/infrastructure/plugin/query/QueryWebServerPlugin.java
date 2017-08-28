/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.infrastructure.plugin.query;

import com.dreameddeath.core.query.factory.IRemoteQueryClientFactory;
import com.dreameddeath.core.query.factory.QueryServiceFactory;
import com.dreameddeath.infrastructure.daemon.plugin.AbstractWebServerPlugin;
import com.dreameddeath.infrastructure.daemon.plugin.IWebServerPluginBuilder;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * Created by Christophe Jeunesse on 31/12/2015.
 */
public class QueryWebServerPlugin extends AbstractWebServerPlugin {
    public static final String GLOBAL_QUERY_FACTORY_PARAM_NAME = "queryServiceFactory";
    public static final String GLOBAL_REMOTE_CLIENT_FACTORY_PARAM_NAME = "remoteQueryClientFactory";

    private final QueryServiceFactory queryServiceFactory;
    private final IRemoteQueryClientFactory remoteQueryClientFactory;

    public QueryWebServerPlugin(AbstractWebServer<?> server, Builder builder) {
        super(server);

        queryServiceFactory = new QueryServiceFactory();
        if(this.getParentWebServer().getServiceDiscoveryManager()!=null){
            remoteQueryClientFactory=new RemoteQueryServiceQueryClientFactoryWithManager(getParentWebServer().getServiceDiscoveryManager());
        }
        else{
            remoteQueryClientFactory=null;
        }
        getParentWebServer().getLifeCycle().addLifeCycleListener(new QueryWebServerLifeCycle(this));
    }


    @Override
    public void enrich(ServletContextHandler handler) {
        super.enrich(handler);
        handler.setAttribute(GLOBAL_QUERY_FACTORY_PARAM_NAME,queryServiceFactory);
        handler.setAttribute(GLOBAL_REMOTE_CLIENT_FACTORY_PARAM_NAME, remoteQueryClientFactory);
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder implements IWebServerPluginBuilder<QueryWebServerPlugin> {
        @Override
        public QueryWebServerPlugin build(AbstractWebServer parent) {
            return new QueryWebServerPlugin(parent,this);
        }
    }

    public QueryServiceFactory getQueryServiceFactory() {
        return queryServiceFactory;
    }

    public IRemoteQueryClientFactory getRemoteQueryClientFactory() {
        return remoteQueryClientFactory;
    }
}
