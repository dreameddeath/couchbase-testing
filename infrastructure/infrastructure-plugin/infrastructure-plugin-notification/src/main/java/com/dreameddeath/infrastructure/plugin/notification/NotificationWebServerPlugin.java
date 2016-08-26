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

package com.dreameddeath.infrastructure.plugin.notification;

import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.notification.bus.impl.EventBusImpl;
import com.dreameddeath.core.notification.discoverer.ListenerAutoSubscribe;
import com.dreameddeath.core.notification.discoverer.ListenerDiscoverer;
import com.dreameddeath.core.notification.listener.impl.EventListenerFactory;
import com.dreameddeath.core.notification.registrar.ListenerRegistrar;
import com.dreameddeath.core.notification.utils.ListenerInfoManager;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.infrastructure.daemon.plugin.AbstractWebServerPlugin;
import com.dreameddeath.infrastructure.daemon.plugin.IWebServerPluginBuilder;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import com.dreameddeath.infrastructure.plugin.couchbase.CouchbaseWebServerPlugin;
import com.dreameddeath.infrastructure.plugin.notification.config.InfrastructureNotificationPluginConfigProperties;
import com.google.common.base.Preconditions;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * Created by Christophe Jeunesse on 20/08/2016.
 */
public class NotificationWebServerPlugin extends AbstractWebServerPlugin {
    public static final String GLOBAL_EVENTBUS_PARAM_NAME="notificationsEventBus";
    public static final String GLOBAL_LISTENER_REGISTRAR_PARAM_NAME="notificationsListenerRegistrar";
    public static final String GLOBAL_EVENT_LISTENER_FACTORY_PARAM_NAME="notificationsListenerFactory";

    private final IEventBus eventBus;
    private final ListenerDiscoverer listenerDiscoverer;
    private final ListenerRegistrar listenerRegistrar;
    private final EventListenerFactory eventListenerFactory;

    public NotificationWebServerPlugin(AbstractWebServer server,Builder builder) {
        super(server);
        CouchbaseWebServerPlugin couchbasePlugin = server.getPlugin(CouchbaseWebServerPlugin.class);
        Preconditions.checkNotNull(couchbasePlugin,"The couchbase Plugin must be define to create the Process Plugin");

        eventBus = new EventBusImpl(server.getMetricRegistry());
        listenerDiscoverer = new ListenerDiscoverer(server.getParentDaemon().getCuratorClient(), builder.basePath);
        ListenerInfoManager listenerInfoManager = new ListenerInfoManager();
        eventListenerFactory = new EventListenerFactory();
        eventListenerFactory.setListenerInfoManager(listenerInfoManager);
        ListenerAutoSubscribe listenerAutoSubscribe = new ListenerAutoSubscribe(eventBus,eventListenerFactory);
        listenerAutoSubscribe.setSessionFactory(couchbasePlugin.getSessionFactory());
        listenerDiscoverer.addListener(listenerAutoSubscribe);

        this.listenerRegistrar = new ListenerRegistrar(server.getParentDaemon().getCuratorClient(),builder.basePath);

        server.getLifeCycle().addLifeCycleListener(new NotificationsWebServerLifeCycle(this));
    }

    public ListenerDiscoverer getListenerDiscoverer(){
        return listenerDiscoverer;
    }


    public ListenerRegistrar getListenerRegistrar() {
        return listenerRegistrar;
    }

    @Override
    public void enrich(ServletContextHandler handler) {
        super.enrich(handler);
        handler.setAttribute(GLOBAL_EVENTBUS_PARAM_NAME,eventBus);
        handler.setAttribute(GLOBAL_LISTENER_REGISTRAR_PARAM_NAME,listenerRegistrar);
        handler.setAttribute(GLOBAL_EVENT_LISTENER_FACTORY_PARAM_NAME,eventListenerFactory);
    }

    public IEventBus getEventBus() {
        return eventBus;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements IWebServerPluginBuilder<NotificationWebServerPlugin> {
        private String basePath = InfrastructureNotificationPluginConfigProperties.LISTENERS_PATH.get();
        private IUser defaultUser = AnonymousUser.INSTANCE;

        public Builder withBasePath(String basePath){
            this.basePath = basePath;
            return this;
        }

        public Builder withDefaultUser(IUser defaultUser){
            this.defaultUser=defaultUser;
            return this;
        }


        @Override
        public NotificationWebServerPlugin build(AbstractWebServer parent) {
            return new NotificationWebServerPlugin(parent,this);
        }
    }
}
