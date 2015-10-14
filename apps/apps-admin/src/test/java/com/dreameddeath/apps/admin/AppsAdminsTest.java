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

package com.dreameddeath.apps.admin;

import com.dreameddeath.core.config.ConfigManagerFactory;
import com.dreameddeath.infrastructure.common.CommonConfigProperties;
import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.webserver.RestWebServer;
import com.dreameddeath.infrastructure.daemon.webserver.WebAppWebServer;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.apache.curator.framework.CuratorFramework;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 29/09/2015.
 */
public class AppsAdminsTest {
    public final static boolean MANUAL_TEST_MODE =false;
    private List<AbstractDaemon> daemons=new ArrayList<>();
    private CuratorTestUtils testUtils;

    @Before
    public void runServer() throws Exception{
        testUtils = new CuratorTestUtils();
        testUtils.prepare(1);
        String connectionString = testUtils.getCluster().getConnectString();
        ConfigManagerFactory.addConfigurationEntry(CommonConfigProperties.ZOOKEEPER_CLUSTER_ADDREES.getName(), connectionString);
        CuratorFramework client = testUtils.getClient("testingDaemons");
        AbstractDaemon daemon = AbstractDaemon.builder().withName("testing Daemon 1").withCuratorFramework(client).build();
        daemon.addWebServer(WebAppWebServer.builder().withName("apps-admin-tests").withApplicationContextConfig("testadmin.applicationContext.xml").withApiPath("/apis").withForTesting(MANUAL_TEST_MODE));
        daemons.add(daemon);
        daemon.getDaemonLifeCycle().start();
        final AbstractDaemon daemon2=AbstractDaemon.builder().withName("testing Daemon 2").withCuratorFramework(client).build();
        daemon2.addWebServer(RestWebServer.builder().withName("testing-rest").withApplicationContextConfig("test.secondarywebserver.applicationContext.xml").withPath("/apis"));
        daemons.add(daemon2);
        daemon2.getDaemonLifeCycle().start();
    }

    @Test @Ignore
    public void runTest(){
        try {
            System.out.println(">>> STARTING webserver : http://localhost:"+daemons.get(0).getAdditionalWebServers().get(0).getServerConnector().getLocalPort()+"/webapp/");
            System.out.println(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
            while (System.in.available() == 0) {
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(100);
        }
    }


    @After
    public void close() throws Exception{
        for(AbstractDaemon daemon:daemons){
            daemon.getDaemonLifeCycle().stop();
            daemon.getDaemonLifeCycle().join();
        }
        testUtils.stop();
    }
}
