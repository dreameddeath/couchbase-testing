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

import com.dreameddeath.core.curator.CuratorFrameworkFactory;
import com.dreameddeath.infrastructure.common.CommonConfigProperties;
import com.dreameddeath.infrastructure.daemon.lifecycle.DaemonLifeCycle;
import com.dreameddeath.infrastructure.daemon.webserver.StandardWebServer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 05/02/2015.
 */
public class AbstractDaemon {
    public static final String GLOBAL_CURATOR_CLIENT_SERVLET_PARAM_NAME = "globalCuratorClient";
    public static final String SERVICE_DISCOVERER_MANAGER_PARAM_NAME = "serviceDiscovererManager";
    public static final String END_POINT_INFO_SERVLET_PARAM_NAME = "endPointInfo";
    public static final String GLOBAL_DAEMON_LIFE_CYCLE_PARAM_NAME = "daemonLifeCycle";
    public static final String GLOBAL_DAEMON_PARAM_NAME = "daemon";

    private Status _status=Status.STOPPED;
    private final DaemonLifeCycle _daemonLifeCycle=new DaemonLifeCycle(AbstractDaemon.this);
    private final CuratorFramework _curatorClient;
    private final StandardWebServer _adminWebServer;
    private final List<StandardWebServer> _standardWebServers=new ArrayList<>();

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
        this(setupDefaultCuratorClient());
    }

    public AbstractDaemon(CuratorFramework curatorClient){
        _curatorClient = curatorClient;
        _adminWebServer = new StandardWebServer(this,"admin","admin.applicationContext.xml",true);
    }

    public CuratorFramework getCuratorClient(){
        return _curatorClient;
    }


    public StandardWebServer getAdminWebServer() {
        return _adminWebServer;
    }


    public List<StandardWebServer> getStandardWebServers(){
        return Collections.unmodifiableList(_standardWebServers);
    }

    synchronized public StandardWebServer addStandardWebServer(String name,String applicationContextFile){
        StandardWebServer newWebServer  = new StandardWebServer(this,name,applicationContextFile,false);
        _standardWebServers.add(newWebServer);
        return newWebServer;
    }


    public DaemonLifeCycle getDaemonLifeCycle() {
        return _daemonLifeCycle;
    }

    public void setStatus(Status status){
        _status = status;
    }

    public Status getStatus() {
        return _status;
    }

    public enum Status{
        STOPPED,
        STARTING,
        STARTED,
        HALTED,
        STOPPING
    }

    public void startAndJoin() throws Exception{
        //Starting using the status manager
        _daemonLifeCycle.start();
        _adminWebServer.join();
    }
}
