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

package com.dreameddeath.infrastructure.plugin.couchbase;

import com.dreameddeath.core.config.ConfigManagerFactory;
import com.dreameddeath.core.dao.config.CouchbaseDaoConfigProperties;
import com.dreameddeath.core.dao.discovery.DaoDiscovery;
import com.dreameddeath.core.dao.model.discovery.DaoInstanceInfo;
import com.dreameddeath.core.helper.config.DaoHelperConfigProperties;
import com.dreameddeath.core.service.client.ServiceClientFactory;
import com.dreameddeath.core.user.StandardMockUserFactory;
import com.dreameddeath.infrastructure.common.CommonConfigProperties;
import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.lifecycle.IDaemonLifeCycle;
import com.dreameddeath.infrastructure.daemon.webserver.RestWebServer;
import com.dreameddeath.testing.couchbase.CouchbaseBucketFactorySimulator;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Christophe Jeunesse on 20/12/2015.
 */
public class CouchbasePluginTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(CouchbasePluginTest.class);
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
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("test", "testdoc").getName(), "testBucketName");
        final AbstractDaemon daemon = AbstractDaemon.builder()
                .withName("testing Daemon")
                .withUserFactory(new StandardMockUserFactory())
                .withPlugin(CouchbaseDaemonPlugin.builder().withBucketFactory(couchbaseBucketFactory))
                .build();

        daemon.addWebServer(RestWebServer.builder().withName("tests")
                .withPlugin(CouchbaseWebServerPlugin.builder())
                .withApplicationContextConfig("applicationContext.xml"));

        Thread stopping_thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    TestDoc newDoc = new TestDoc();
                    newDoc.name = "testInit";

                    ServiceClientFactory daoReadClientFactory = daemon.getAdminWebServer().getServiceDiscoveryManager().getClientFactory(DaoHelperConfigProperties.DAO_READ_SERVICES_DOMAIN.get());
                    ServiceClientFactory daoWriteClientFactory = daemon.getAdminWebServer().getServiceDiscoveryManager().getClientFactory(DaoHelperConfigProperties.DAO_WRITE_SERVICES_DOMAIN.get());
                    Thread.sleep(100);
                    Response response = daoWriteClientFactory.getClient("dao#test#testdoc$write", "1.0.0")
                            .getInstance()
                            .request()
                            .post(Entity.json(newDoc));
                    TestDoc createdTestDoc = response.readEntity(new GenericType<>(TestDoc.class));
                    //DaoHelperServiceUtils.finalizeFromResponse(response, createdTestDoc);
                    assertEquals(newDoc.name, createdTestDoc.name);

                    String[] keyParts = createdTestDoc.getMeta().getKey().split("/");
                    Response readDocResponse = daoReadClientFactory.getClient("dao#test#testdoc$read", "1.0.0")
                            .getInstance()
                            .path(keyParts[keyParts.length - 1])
                            .request()
                            .get();

                    TestDoc readDoc = readDocResponse.readEntity(new GenericType<>(TestDoc.class));
                    //DaoHelperServiceUtils.finalizeFromResponse(readDocResponse, readDoc);
                    assertEquals(createdTestDoc.name, readDoc.name);
                    assertEquals(createdTestDoc.getMeta().getKey(), readDoc.getMeta().getKey());

                } catch (Throwable e) {
                    nbErrors.incrementAndGet();
                    LOG.error("!!!!! ERROR !!!!!Error during dao service access tests", e);
                }
                try {
                    DaoDiscovery discovery = new DaoDiscovery(daemon.getCuratorClient());
                    discovery.start();
                    Thread.sleep(100);
                    List<DaoInstanceInfo> daos = discovery.getList();
                    assertEquals(1, daos.size());
                    DaoInstanceInfo instanceInfo = daos.get(0);
                    assertEquals(TestDoc.class.getName(), instanceInfo.getMainEntity().getClassName());
                    assertEquals(1, daos.get(0).getChildEntities().size());
                    assertEquals(TestDocEnhanced.class.getName(), daos.get(0).getChildEntities().get(0).getClassName());
                } catch (Throwable e) {
                    nbErrors.incrementAndGet();
                    LOG.error("!!!!! ERROR !!!!!Error during status read", e);
                }
                try {
                    daemon.getDaemonLifeCycle().stop();
                }
                catch(Exception e){

                }
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