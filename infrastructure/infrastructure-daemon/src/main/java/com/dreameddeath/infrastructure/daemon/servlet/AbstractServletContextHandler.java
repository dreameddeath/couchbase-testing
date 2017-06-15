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

package com.dreameddeath.infrastructure.daemon.servlet;

import com.dreameddeath.infrastructure.daemon.plugin.AbstractDaemonPlugin;
import com.dreameddeath.infrastructure.daemon.plugin.AbstractWebServerPlugin;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * Created by Christophe Jeunesse on 10/10/2015.
 */
public class AbstractServletContextHandler extends ServletContextHandler {
    public static final String GLOBAL_WEBSERVER_PARAM_NAME = "parentWebServer";

    private final AbstractWebServer webServer;

    public AbstractServletContextHandler(AbstractWebServer<?> parentServer){
        super(parentServer.getWebServer(),null);
        webServer = parentServer;
        this.setAttribute(GLOBAL_WEBSERVER_PARAM_NAME,webServer);
        this.setAttribute(AbstractWebServer.GLOBAL_CURATOR_CLIENT_SERVLET_PARAM_NAME, parentServer.getParentDaemon().getCuratorClient());
        this.setAttribute(AbstractWebServer.GLOBAL_DAEMON_PARAM_NAME, parentServer.getParentDaemon());
        this.setAttribute(AbstractWebServer.GLOBAL_DAEMON_LIFE_CYCLE_PARAM_NAME, parentServer.getParentDaemon().getDaemonLifeCycle());
        this.setAttribute(AbstractWebServer.GLOBAL_DAEMON_PROPERTY_SOURCE_PARAM_NAME,parentServer.getPropertySources());
        this.setAttribute(AbstractWebServer.GLOBAL_USER_FACTORY_PARAM_NAME,parentServer.getParentDaemon().getUserFactory());
        this.setAttribute(AbstractWebServer.GLOBAL_METRICS_REGISTRY_PARAM_NAME,parentServer.getMetricRegistry());
        this.setAttribute(AbstractWebServer.GLOBAL_DATETIME_FACTORY_PARAM_NAME,parentServer.getDateTimeServiceFactory());
        this.setAttribute(AbstractWebServer.GLOBAL_DTO_CONVERTER_FACTORY_PARAM_NAME,parentServer.getDtoConverterFactory());
        for(AbstractDaemonPlugin plugin:parentServer.getParentDaemon().getPlugins()){
            plugin.enrich(this);
        }

        for(AbstractWebServerPlugin plugin:parentServer.getPlugins()){
            plugin.enrich(this);
        }
    }

    public AbstractWebServer getWebServer(){
        return webServer;
    }
}
