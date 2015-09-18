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

package com.dreameddeath.infrastructure.daemon;

import com.dreameddeath.core.config.exception.ConfigPropertyValueNotFoundException;
import com.dreameddeath.core.curator.CuratorFrameworkFactory;
import com.dreameddeath.infrastructure.common.CommonConfigProperties;
import com.dreameddeath.infrastructure.daemon.config.DaemonConfigProperties;
import com.dreameddeath.infrastructure.daemon.discovery.DaemonRegisterLifeCycleListener;
import com.dreameddeath.infrastructure.daemon.lifecycle.DaemonLifeCycle;
import com.dreameddeath.infrastructure.daemon.lifecycle.IDaemonLifeCycle;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import com.dreameddeath.infrastructure.daemon.webserver.ProxyWebServer;
import com.dreameddeath.infrastructure.daemon.webserver.RestWebServer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 05/02/2015.
 */
public class AbstractDaemon {
    public static final String GLOBAL_CURATOR_CLIENT_SERVLET_PARAM_NAME = "globalCuratorClient";
    public static final String SERVICE_DISCOVERER_MANAGER_PARAM_NAME = "serviceDiscovererManager";
    public static final String END_POINT_INFO_SERVLET_PARAM_NAME = "endPointInfo";
    public static final String GLOBAL_DAEMON_LIFE_CYCLE_PARAM_NAME = "daemonLifeCycle";
    public static final String GLOBAL_DAEMON_PARAM_NAME = "daemon";

    private final String _name;
    private final UUID _uuid = UUID.randomUUID();
    private final IDaemonLifeCycle _daemonLifeCycle=new DaemonLifeCycle(AbstractDaemon.this);
    private final CuratorFramework _curatorClient;
    private final RestWebServer _adminWebServer;
    private final List<AbstractWebServer> _additionnalWebServers=new ArrayList<>();

    protected static CuratorFramework setupDefaultCuratorClient(){
        try {
            String addressProp = CommonConfigProperties.ZOOKEEPER_CLUSTER_ADDREES.getMandatoryValue("The zookeeper cluster address must be defined");
            int sleepTime = CommonConfigProperties.ZOOKEEPER_CLUSTER_SLEEP_TIME.getMandatoryValue("The sleep time is not set");
            int maxRetries = CommonConfigProperties.ZOOKEEPER_CLUSTER_MAX_RECONNECTION_ATTEMPTS.getMandatoryValue("The max connection time must be set");
            CuratorFramework client = CuratorFrameworkFactory.newClient(addressProp, new ExponentialBackoffRetry(sleepTime, maxRetries));
            client.start();
            return client;
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public AbstractDaemon(){
        this(new Builder());
    }

    public AbstractDaemon(CuratorFramework curatorClient){
        this(new Builder().withCuratorFramework(curatorClient));
    }

    public AbstractDaemon(Builder builder){
        if(builder.getCuratorFramework()==null){
            builder.withCuratorFramework(setupDefaultCuratorClient());
        }
        if(builder.getName()==null){
            try {
                builder.withName(DaemonConfigProperties.DAEMON_NAME.getMandatoryValue("The name must be given"));
            }
            catch(ConfigPropertyValueNotFoundException e){
                throw new RuntimeException(e);
            }
        }
        _name = builder.getName();
        _curatorClient = builder.getCuratorFramework();
        _adminWebServer = new RestWebServer(
                    RestWebServer.builder().withDaemon(this)
                            .withName(builder.getAdminWebServerName())
                            .withApplicationContextConfig(builder.getAdminApplicationContextName())
                            .withIsRoot(true)
                );

        Runtime.getRuntime().addShutdownHook(new Thread(this::stopForShutdownHook));
        if(builder.getRegisterDaemon()){
            _daemonLifeCycle.addLifeCycleListener(new DaemonRegisterLifeCycleListener(_curatorClient));
        }
    }

    public String getName() {
        return _name;
    }

    public CuratorFramework getCuratorClient(){
        return _curatorClient;
    }

    public RestWebServer getAdminWebServer() {
        return _adminWebServer;
    }

    public List<AbstractWebServer> getAdditionnalWebServers(){
        return Collections.unmodifiableList(_additionnalWebServers);
    }

    public UUID getUuid() {
        return _uuid;
    }

    synchronized public RestWebServer addStandardWebServer(RestWebServer.Builder builder){
        RestWebServer newWebServer  = new RestWebServer(builder.withDaemon(this));
        _additionnalWebServers.add(newWebServer);
        return newWebServer;
    }

    synchronized public ProxyWebServer addProxyWebServer(ProxyWebServer.Builder builder){
        ProxyWebServer newWebServer  = new ProxyWebServer(builder.withDaemon(this));
        _additionnalWebServers.add(newWebServer);
        return newWebServer;
    }

    public IDaemonLifeCycle getDaemonLifeCycle() {
        return _daemonLifeCycle;
    }


    public IDaemonLifeCycle.Status getStatus() {
        return _daemonLifeCycle.getStatus();
    }

    public void startAndJoin() throws Exception{
        //Starting using the status manager
        _daemonLifeCycle.start();
        _daemonLifeCycle.join();
    }

    private void stopForShutdownHook(){
        try{
            getDaemonLifeCycle().stop();
            getDaemonLifeCycle().join();
        }
        catch (Exception e){
            //TODO log something
        }
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder{
        private String _name=null;
        private Boolean _registerDaemon=true;
        private CuratorFramework _curatorFramework=null;
        private String _adminApplicationContextName="admin.applicationContext.xml";
        private String _adminWebServerName="admin";

        public String getName() {
            return _name;
        }

        public Builder withName(String name) {
            _name = name;
            return this;
        }

        public String getAdminApplicationContextName() {
            return _adminApplicationContextName;
        }

        public Builder withAdminApplicationContextName(String adminApplicationContextName) {
            _adminApplicationContextName = adminApplicationContextName;
            return this;
        }

        public CuratorFramework getCuratorFramework() {
            return _curatorFramework;
        }

        public Builder withCuratorFramework(CuratorFramework curatorFramework) {
            _curatorFramework = curatorFramework;
            return this;
        }

        public String getAdminWebServerName() {
            return _adminWebServerName;
        }

        public Builder withAdminWebServerName(String adminWebServerName) {
            _adminWebServerName = adminWebServerName;
            return this;
        }

        public Boolean getRegisterDaemon() {
            return _registerDaemon;
        }

        public Builder withRegisterDaemon(Boolean registerDaemon) {
            _registerDaemon = registerDaemon;
            return this;
        }

        public AbstractDaemon build(){
            return new AbstractDaemon(this);
        }
    }
}
