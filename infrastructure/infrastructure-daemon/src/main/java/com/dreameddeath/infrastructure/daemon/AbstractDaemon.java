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
import com.dreameddeath.core.couchbase.impl.CouchbaseBucketFactory;
import com.dreameddeath.core.couchbase.impl.CouchbaseClusterFactory;
import com.dreameddeath.core.curator.CuratorFrameworkFactory;
import com.dreameddeath.core.user.IUserFactory;
import com.dreameddeath.infrastructure.common.CommonConfigProperties;
import com.dreameddeath.infrastructure.daemon.config.DaemonConfigProperties;
import com.dreameddeath.infrastructure.daemon.couchbase.CouchbaseDaemonLifeCycle;
import com.dreameddeath.infrastructure.daemon.couchbase.DaemonCouchbaseFactories;
import com.dreameddeath.infrastructure.daemon.discovery.DaemonRegisterLifeCycleListener;
import com.dreameddeath.infrastructure.daemon.lifecycle.DaemonLifeCycle;
import com.dreameddeath.infrastructure.daemon.lifecycle.IDaemonLifeCycle;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import com.dreameddeath.infrastructure.daemon.webserver.ProxyWebServer;
import com.dreameddeath.infrastructure.daemon.webserver.RestWebServer;
import com.dreameddeath.infrastructure.daemon.webserver.WebAppWebServer;
import com.google.common.base.Preconditions;
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

    private final String name;
    private final UUID uuid = UUID.randomUUID();
    private final IDaemonLifeCycle daemonLifeCycle=new DaemonLifeCycle(AbstractDaemon.this);
    private final CuratorFramework curatorClient;
    private final IUserFactory userFactory;
    private final DaemonCouchbaseFactories daemonCouchbaseFactories;
    private final RestWebServer adminWebServer;
    private final List<AbstractWebServer> additionalWebServers = new ArrayList<>();

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
        //TODO improve user management
        Preconditions.checkNotNull(builder.userFactory,"Please give a user factory");
        userFactory = builder.userFactory;

        if(builder.getWithCouchbase()){
            if(builder.getWithCouchbaseClusterFactory()==null){
                builder.withWithCouchbaseClusterFactory(new CouchbaseClusterFactory());
            }
            if(builder.getWithCouchbaseBucketFactory()==null){
                builder.withWithCouchbaseBucketFactory(CouchbaseBucketFactory.builder().withClusterFactory(builder.getWithCouchbaseClusterFactory()).build());
            }
            daemonCouchbaseFactories = new DaemonCouchbaseFactories(builder.getWithCouchbaseClusterFactory(),builder.getWithCouchbaseBucketFactory());
            daemonLifeCycle.addLifeCycleListener(new CouchbaseDaemonLifeCycle(daemonCouchbaseFactories));
        }
        else {
            daemonCouchbaseFactories = null;
        }

        if(builder.getName()==null){
            try {
                builder.withName(DaemonConfigProperties.DAEMON_NAME.getMandatoryValue("The name must be given"));
            }
            catch(ConfigPropertyValueNotFoundException e){
                throw new RuntimeException(e);
            }
        }
        name = builder.getName();
        curatorClient = builder.getCuratorFramework();
        adminWebServer = new RestWebServer(
                    RestWebServer.builder().withDaemon(this)
                            .withName(builder.getAdminWebServerName())
                            .withApplicationContextConfig(builder.getAdminApplicationContextName())
                            .withIsRoot(true)
                );

        Runtime.getRuntime().addShutdownHook(new Thread(this::stopForShutdownHook));
        if(builder.getRegisterDaemon()){
            daemonLifeCycle.addLifeCycleListener(new DaemonRegisterLifeCycleListener(curatorClient));
        }
    }

    public String getName() {
        return name;
    }

    public CuratorFramework getCuratorClient(){
        return curatorClient;
    }

    public RestWebServer getAdminWebServer() {
        return adminWebServer;
    }

    public List<AbstractWebServer> getAdditionalWebServers(){
        return Collections.unmodifiableList(additionalWebServers);
    }

    public UUID getUuid() {
        return uuid;
    }

    synchronized public RestWebServer addWebServer(RestWebServer.Builder builder){
        RestWebServer newWebServer  = new RestWebServer(builder.withDaemon(this));
        additionalWebServers.add(newWebServer);
        return newWebServer;
    }

    synchronized public ProxyWebServer addWebServer(ProxyWebServer.Builder builder){
        ProxyWebServer newWebServer  = new ProxyWebServer(builder.withDaemon(this));
        additionalWebServers.add(newWebServer);
        return newWebServer;
    }

    synchronized public WebAppWebServer addWebServer(WebAppWebServer.Builder builder){
        WebAppWebServer newWebServer  = new WebAppWebServer(builder.withDaemon(this));
        additionalWebServers.add(newWebServer);
        return newWebServer;
    }

    public DaemonCouchbaseFactories getDaemonCouchbaseFactories() {
        return daemonCouchbaseFactories;
    }

    public IDaemonLifeCycle getDaemonLifeCycle() {
        return daemonLifeCycle;
    }

    public IDaemonLifeCycle.Status getStatus() {
        return daemonLifeCycle.getStatus();
    }


    public IUserFactory getUserFactory() {
        return userFactory;
    }

    public void startAndJoin() throws Exception{
        //Starting using the status manager
        daemonLifeCycle.start();
        daemonLifeCycle.join();
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
        private IUserFactory userFactory=null;
        private String name=null;
        private boolean registerDaemon=true;
        private CuratorFramework curatorFramework=null;
        private String adminApplicationContextName="META-INF/spring/daemon.admin.applicationContext.xml";
        private String adminWebServerName="admin";
        private boolean withCouchbase=false;
        private CouchbaseClusterFactory withCouchbaseClusterFactory=null;
        private CouchbaseBucketFactory withCouchbaseBucketFactory=null;

        public String getName() {
            return name;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public String getAdminApplicationContextName() {
            return adminApplicationContextName;
        }

        public Builder withAdminApplicationContextName(String adminApplicationContextName) {
            this.adminApplicationContextName = adminApplicationContextName;
            return this;
        }

        public CuratorFramework getCuratorFramework() {
            return curatorFramework;
        }

        public Builder withCuratorFramework(CuratorFramework curatorFramework) {
            this.curatorFramework = curatorFramework;
            return this;
        }

        public String getAdminWebServerName() {
            return adminWebServerName;
        }

        public Builder withAdminWebServerName(String adminWebServerName) {
            this.adminWebServerName = adminWebServerName;
            return this;
        }

        public boolean getRegisterDaemon() {
            return registerDaemon;
        }

        public Builder withRegisterDaemon(Boolean registerDaemon) {
            this.registerDaemon = registerDaemon;
            return this;
        }

        public boolean getWithCouchbase() {
            return withCouchbase;
        }

        public Builder withWithCouchbase(boolean withCouchbase) {
            this.withCouchbase = withCouchbase;
            return this;
        }

        public CouchbaseClusterFactory getWithCouchbaseClusterFactory() {
            return withCouchbaseClusterFactory;
        }

        public Builder withWithCouchbaseClusterFactory(CouchbaseClusterFactory withCouchbaseClusterFactory) {
            this.withCouchbaseClusterFactory = withCouchbaseClusterFactory;
            return this;
        }

        public CouchbaseBucketFactory getWithCouchbaseBucketFactory() {
            return withCouchbaseBucketFactory;
        }

        public Builder withWithCouchbaseBucketFactory(CouchbaseBucketFactory withCouchbaseBucketFactory) {
            this.withCouchbaseBucketFactory = withCouchbaseBucketFactory;
            return this;
        }

        public Builder withUserFactory(IUserFactory userFactory) {
            this.userFactory = userFactory;
            return this;
        }

        public AbstractDaemon build(){
            return new AbstractDaemon(this);
        }
    }
}
