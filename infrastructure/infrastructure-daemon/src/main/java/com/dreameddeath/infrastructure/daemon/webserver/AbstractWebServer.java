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

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jetty9.InstrumentedHandler;
import com.codahale.metrics.jetty9.InstrumentedQueuedThreadPool;
import com.dreameddeath.core.config.spring.ConfigMutablePropertySources;
import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.config.DaemonConfigProperties;
import com.dreameddeath.infrastructure.daemon.metrics.InstrumentedConnectionFactory;
import com.dreameddeath.infrastructure.daemon.plugin.AbstractWebServerPlugin;
import com.dreameddeath.infrastructure.daemon.plugin.IWebServerPluginBuilder;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.component.LifeCycle;
import org.springframework.core.env.PropertySources;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 21/08/2015.
 */
public abstract class AbstractWebServer {
    public static final String GLOBAL_CURATOR_CLIENT_SERVLET_PARAM_NAME = "globalCuratorClient";
    public static final String GLOBAL_DAEMON_LIFE_CYCLE_PARAM_NAME = "daemonLifeCycle";
    public static final String GLOBAL_DAEMON_PARAM_NAME = "daemon";
    public static final String GLOBAL_DAEMON_PROPERTY_SOURCE_PARAM_NAME = "propertySources";
    public static final String GLOBAL_USER_FACTORY_PARAM_NAME = "userFactory";
    public static final String GLOBAL_METRICS_REGISTRY_PARAM_NAME = "metricsRegistry";

    private final AbstractDaemon parentDaemon;
    private final MetricRegistry metricRegistry = new MetricRegistry();
    private final String name;
    private final UUID uuid;
    private final Server webServer;
    private final ServerConnector serverConnector;
    private final PropertySources propertySources;
    private final List<AbstractWebServerPlugin> plugins = new ArrayList<>();

    private String getAddress(String address,String networkInterfaceName){
        if(address!=null){
            return address;
        }
        else if(networkInterfaceName!=null){
            try {
                NetworkInterface networkInterface = NetworkInterface.getByName(networkInterfaceName);
                InetAddress inetAddress = networkInterface.getInetAddresses().nextElement();
                if(inetAddress!=null){
                    return inetAddress.getHostAddress();
                }
            }
            catch(Exception e){
                //Ignore
            }
        }
        return null;
    }

    public AbstractWebServer(Builder<?> builder) {
        uuid = UUID.randomUUID();
        parentDaemon = builder.daemon;
        name = builder.name;
        parentDaemon.getDaemonLifeCycle().addLifeCycleListener(new WebServerDaemonLifeCycleListener(this,builder.isRoot));
        webServer = new Server(new
                InstrumentedQueuedThreadPool(metricRegistry)
        );
        serverConnector = new ServerConnector(webServer,
                    new InstrumentedConnectionFactory(new HttpConnectionFactory(), metricRegistry.timer("http.connections"))
                );

        String address = getAddress(builder.address, builder.interfaceName);
        if(address==null){
            address = getAddress(DaemonConfigProperties.DAEMON_WEBSERVER_ADDRESS.get(),DaemonConfigProperties.DAEMON_WEBSERVER_INTERFACE.get());
        }
        if(address!=null){
            serverConnector.setHost(address);
        }

        int port = builder.port;
        if(port==0){
            port = DaemonConfigProperties.DAEMON_WEBSERVER_PORT.get();
        }
        if(port!=0){
            serverConnector.setPort(port);
        }

        webServer.addConnector(serverConnector);
        PropertySources propertySources = builder.propertySources;
        if(propertySources==null){
            propertySources = new ConfigMutablePropertySources();
        }
        this.propertySources = propertySources;

        for(IWebServerPluginBuilder pluginBuilder : builder.pluginBuilders){
            AbstractWebServerPlugin plugin = pluginBuilder.build(this);
            this.plugins.add(plugin);
        }
    }


    public List<AbstractWebServerPlugin> getPlugins(){
        return Collections.unmodifiableList(plugins);
    }


    public AbstractDaemon getParentDaemon() {
        return parentDaemon;
    }

    public Server getWebServer() {
        return webServer;
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public ServerConnector getServerConnector() {
        return serverConnector;
    }

    public void start() throws Exception{
        getMetricRegistry().removeMatching(MetricFilter.ALL);
        webServer.start();
    }

    public void stop() throws Exception{
        webServer.stop();
    }

    public void join() throws Exception{
        webServer.join();
    }

    public Status getStatus(){
        return Status.fromStateString(webServer.getState());
    }

    public PropertySources getPropertySources() {
        return propertySources;
    }

    public void setHandler(Handler handler){
        InstrumentedHandler monitoredHandler=new InstrumentedHandler(getMetricRegistry());
        monitoredHandler.setHandler(handler);
        webServer.setHandler(monitoredHandler);
    }

    public MetricRegistry getMetricRegistry(){
        return metricRegistry;
    }
    public enum Status{
        UNKNOWN,
        FAILED,
        STOPPED,
        STARTING,
        STARTED,
        RUNNING,
        STOPPING;

        public static Status fromStateString(String stateStr){
            switch (stateStr){
                case "RUNNING":return RUNNING;
                case "STOPPED":return STOPPED;
                case "FAILED":return FAILED;
                case "STARTING":return STARTING;
                case "STARTED":return STARTED;
                case "STOPPING":return STOPPING;
                default:return UNKNOWN;
            }
        }
    }

    public LifeCycle getLifeCycle(){
        return webServer;
    }


    public static Builder builder(){
        return new Builder();
    }

    public static class Builder<T extends AbstractWebServer.Builder> {
        private AbstractDaemon daemon;
        private PropertySources propertySources=null;
        private String name;
        private String address=null;
        private String interfaceName=null;
        private boolean isRoot=false;
        private int port=0;
        private List<IWebServerPluginBuilder> pluginBuilders = new ArrayList<>();

        public T withAddress(String address) {
            this.address = address;
            return (T)this;
        }

        public T withInterfaceName(String interfaceName) {
            this.interfaceName = interfaceName;
            return (T)this;
        }

        public T withIsRoot(boolean isRoot) {
            this.isRoot = isRoot;
            return (T) this;
        }

        public T withName(String name) {
            this.name = name;
            return (T)this;
        }

        public T withDaemon(AbstractDaemon daemon) {
            this.daemon = daemon;
            return (T)this;
        }

        public T withPort(int port) {
            this.port = port;
            return (T)this;
        }

        public T withPropertySources(PropertySources propertySources) {
            this.propertySources = propertySources;
            return (T)this;
        }

        public T withPlugin(IWebServerPluginBuilder pluginBuilder){
            this.pluginBuilders.add(pluginBuilder);
            return (T)this;
        }
    }
}
