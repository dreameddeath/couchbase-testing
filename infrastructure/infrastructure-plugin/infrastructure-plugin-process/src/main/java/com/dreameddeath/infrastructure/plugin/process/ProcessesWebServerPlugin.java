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

package com.dreameddeath.infrastructure.plugin.process;

import com.dreameddeath.core.process.service.factory.impl.ExecutorClientFactory;
import com.dreameddeath.core.process.service.factory.impl.ExecutorServiceFactory;
import com.dreameddeath.core.process.service.factory.impl.ProcessingServiceFactory;
import com.dreameddeath.infrastructure.daemon.plugin.AbstractWebServerPlugin;
import com.dreameddeath.infrastructure.daemon.plugin.IWebServerPluginBuilder;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import com.dreameddeath.infrastructure.plugin.couchbase.CouchbaseWebServerPlugin;
import com.google.common.base.Preconditions;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * Created by Christophe Jeunesse on 31/12/2015.
 */
public class ProcessesWebServerPlugin extends AbstractWebServerPlugin {
    public static final String GLOBAL_EXECUTOR_FACTORY_PARAM_NAME = "executorServiceFactory";
    public static final String GLOBAL_PROCESSING_FACTORY_PARAM_NAME = "processingServiceFactory";
        public static final String GLOBAL_EXECUTOR_CLIENT_FACTORY_PARAM_NAME = "executorClientFactory";

    private ExecutorClientFactory executorClientFactory;
    private ExecutorServiceFactory executorServiceFactory;
    private ProcessingServiceFactory processingServiceFactory;

    public ProcessesWebServerPlugin(AbstractWebServer server,Builder builder) {
        super(server);
        CouchbaseWebServerPlugin couchbasePlugin = server.getPlugin(CouchbaseWebServerPlugin.class);
        Preconditions.checkNotNull(couchbasePlugin,"The couchbase Plugin must be define to create the Process Plugin");
        executorServiceFactory = new ExecutorServiceFactory();
        processingServiceFactory = new ProcessingServiceFactory();
        executorClientFactory = new ExecutorClientFactory(
                couchbasePlugin.getSessionFactory(),executorServiceFactory,processingServiceFactory,server.getMetricRegistry());
    }


    @Override
    public void enrich(ServletContextHandler handler) {
        super.enrich(handler);
        handler.setAttribute(GLOBAL_EXECUTOR_FACTORY_PARAM_NAME,executorServiceFactory);
        handler.setAttribute(GLOBAL_PROCESSING_FACTORY_PARAM_NAME,processingServiceFactory);
        handler.setAttribute(GLOBAL_EXECUTOR_CLIENT_FACTORY_PARAM_NAME, executorClientFactory);
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder implements IWebServerPluginBuilder<ProcessesWebServerPlugin> {
        @Override
        public ProcessesWebServerPlugin build(AbstractWebServer parent) {
            return new ProcessesWebServerPlugin(parent,this);
        }
    }

    public ExecutorClientFactory getExecutorClientFactory() {
        return executorClientFactory;
    }

    public ExecutorServiceFactory getExecutorServiceFactory() {
        return executorServiceFactory;
    }

    public ProcessingServiceFactory getProcessingServiceFactory() {
        return processingServiceFactory;
    }
}
