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

package com.dreameddeath.testing.plugin.proxy.service;

import com.dreameddeath.infrastructure.daemon.plugin.AbstractWebServerPlugin;
import com.dreameddeath.infrastructure.daemon.plugin.IWebServerPluginBuilder;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 09/12/2016.
 */
public class ProxyTestWebServerPlugin extends AbstractWebServerPlugin {
    private final String path;
    public ProxyTestWebServerPlugin(Builder builder,AbstractWebServer server) {
        super(server);
        this.path = builder.path;
    }

    @Override
    public List<ServletContextHandler> buildAdditionnalContextHandlers() {
        return Collections.singletonList(new ProxyTestServletContextHandler(this.getParentWebServer(),path));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements IWebServerPluginBuilder<ProxyTestWebServerPlugin> {
        private String path=null;

        public Builder withPath(String path){
            this.path = path;
            return this;
        }

        @Override
        public ProxyTestWebServerPlugin build(AbstractWebServer parent) {
            return new ProxyTestWebServerPlugin(this,parent);
        }
    }
}
