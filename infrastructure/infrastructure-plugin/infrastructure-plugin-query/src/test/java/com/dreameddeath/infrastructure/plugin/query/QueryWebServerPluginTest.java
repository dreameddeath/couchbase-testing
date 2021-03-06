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

import com.dreameddeath.core.config.ConfigManagerFactory;
import com.dreameddeath.core.dao.config.CouchbaseDaoConfigProperties;
import com.dreameddeath.core.user.StandardMockUserFactory;
import com.dreameddeath.infrastructure.common.CommonConfigProperties;
import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.lifecycle.IDaemonLifeCycle;
import com.dreameddeath.infrastructure.daemon.webserver.RestWebServer;
import com.dreameddeath.infrastructure.plugin.couchbase.CouchbaseDaemonPlugin;
import com.dreameddeath.infrastructure.plugin.couchbase.CouchbaseWebServerPlugin;
import com.dreameddeath.infrastructure.plugin.query.config.InfrastructureQueryPluginConfigProperties;
import com.dreameddeath.testing.couchbase.CouchbaseBucketFactorySimulator;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * Created by christophe jeunesse on 21/06/2017.
 */
public class QueryWebServerPluginTest {
    private final static Logger LOG = LoggerFactory.getLogger(QueryWebServerPluginTest.class);
    private CuratorTestUtils testUtils;
    private CouchbaseBucketFactorySimulator couchbaseBucketFactory;

    @Before
    public void setup() throws Exception {
        testUtils = new CuratorTestUtils();
        testUtils.prepare(1);
        couchbaseBucketFactory = new CouchbaseBucketFactorySimulator();
    }

    @Test
    public void testDaemon() throws Exception {
        final AtomicInteger nbErrors = new AtomicInteger(0);
        String connectionString = testUtils.getCluster().getConnectString();

        ConfigManagerFactory.addPersistentConfigurationEntry(CommonConfigProperties.ZOOKEEPER_CLUSTER_ADDREES.getName(), connectionString);
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("test", "testmodel").getName(), "testBucketName");
        ConfigManagerFactory.addPersistentConfigurationEntry(InfrastructureQueryPluginConfigProperties.REMOTE_SERVICE_FOR_DOMAIN.getProperty("test").getName(),"test");
        final AbstractDaemon daemon = AbstractDaemon.builder()
                .withName("testing Daemon")
                .withUserFactory(new StandardMockUserFactory())
                .withPlugin(CouchbaseDaemonPlugin.builder().withBucketFactory(couchbaseBucketFactory))
                .build();

        daemon.addWebServer(RestWebServer.builder().withName("tests")
                .withPlugin(CouchbaseWebServerPlugin.builder())
                .withPlugin(QueryWebServerPlugin.builder())
                .withApplicationContextConfig("applicationContextLocal.xml"));
        daemon.addWebServer(RestWebServer.builder().withName("testRemote")
                .withPlugin(CouchbaseWebServerPlugin.builder())
                .withPlugin(QueryWebServerPlugin.builder())
                .withApplicationContextConfig("applicationContextLocal.xml"));


        Thread stopping_thread = new Thread(() -> {

            try {
                QueryWebServerPlugin plugin=daemon.getAdditionalWebServers().get(0).getPlugin(QueryWebServerPlugin.class);
                CouchbaseWebServerPlugin cbPlugin=daemon.getAdditionalWebServers().get(0).getPlugin(CouchbaseWebServerPlugin.class);


            } catch (Throwable e) {
                nbErrors.incrementAndGet();
                LOG.error("!!!!! ERROR !!!!!Error during status read", e);
            }
            try {
                daemon.getDaemonLifeCycle().stop();
            }
            catch(Exception e){

            }
        });
        daemon.getDaemonLifeCycle().addLifeCycleListener(new IDaemonLifeCycle.DefaultListener(1000000) {
            @Override
            public void lifeCycleStarted(final IDaemonLifeCycle lifeCycle) {
                stopping_thread.start();
            }
        });
        daemon.startAndJoin();
        stopping_thread.join();
        assertEquals(0L, nbErrors.get());
    }

    @After
    public void close() throws Exception {
        if (testUtils != null) testUtils.stop();
    }

}