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

import com.dreameddeath.core.config.ConfigManagerFactory;
import com.dreameddeath.core.dao.config.CouchbaseDaoConfigProperties;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.json.JsonProviderFactory;
import com.dreameddeath.core.process.model.discovery.JobExecutorClientInfo;
import com.dreameddeath.core.process.model.discovery.TaskExecutorClientInfo;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.core.user.StandardMockUserFactory;
import com.dreameddeath.infrastructure.common.CommonConfigProperties;
import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.lifecycle.IDaemonLifeCycle;
import com.dreameddeath.infrastructure.daemon.utils.ServerConnectorUtils;
import com.dreameddeath.infrastructure.daemon.webserver.RestWebServer;
import com.dreameddeath.infrastructure.plugin.config.InfrastructureProcessPluginConfigProperties;
import com.dreameddeath.infrastructure.plugin.couchbase.CouchbaseDaemonPlugin;
import com.dreameddeath.infrastructure.plugin.couchbase.CouchbaseWebServerPlugin;
import com.dreameddeath.testing.couchbase.CouchbaseBucketFactorySimulator;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * Created by Christophe Jeunesse on 03/01/2016.
 */
public class ProcessesWebServerPluginTest {
    private final static Logger LOG = LoggerFactory.getLogger(ProcessesWebServerPluginTest.class);
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
        //ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("test", "testdoc").getName(), "testBucketName");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("test", "testdocprocess").getName(), "testBucketName");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("core", "abstractjob").getName(), "testBucketName");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("core", "abstracttask").getName(), "testBucketName");
        ConfigManagerFactory.addPersistentConfigurationEntry(InfrastructureProcessPluginConfigProperties.REMOTE_SERVICE_FOR_DOMAIN.getProperty("test").getName(),"test");
        final AbstractDaemon daemon = AbstractDaemon.builder()
                .withName("testing Daemon")
                .withUserFactory(new StandardMockUserFactory())
                .withPlugin(CouchbaseDaemonPlugin.builder().withBucketFactory(couchbaseBucketFactory))
                .build();

        daemon.addWebServer(RestWebServer.builder().withName("tests")
                //.withServiceDiscoveryManager(true)
                .withPlugin(CouchbaseWebServerPlugin.builder())
                .withPlugin(ProcessesWebServerPlugin.builder())
                .withApplicationContextConfig("applicationContext.xml"));

        Thread stopping_thread = new Thread(() -> {

            try {
                ProcessesWebServerPlugin plugin=daemon.getAdditionalWebServers().get(0).getPlugin(ProcessesWebServerPlugin.class);
                CouchbaseWebServerPlugin cbPlugin=daemon.getAdditionalWebServers().get(0).getPlugin(CouchbaseWebServerPlugin.class);
                {
                    IJobExecutorClient<TestDocCreateJob> executorClient = plugin.getExecutorClientFactory().buildJobClient(TestDocCreateJob.class);
                    TestDocCreateJob createJob = new TestDocCreateJob();
                    createJob.name = "test";
                    JobContext<TestDocCreateJob> createJobJobContext = executorClient.executeJob(createJob, AnonymousUser.INSTANCE);
                    ICouchbaseSession session = cbPlugin.getSessionFactory().newReadOnlySession(AnonymousUser.INSTANCE);
                    TestDocProcess processDoc = session.toBlocking().blockingGet(createJobJobContext.getTasks(TestDocCreateJob.TestDocCreateTask.class).get(0).getDocKey(), TestDocProcess.class);
                    assertEquals(processDoc.name, createJob.name);
                }

                {
                    IJobExecutorClient<RemoteTestDocCreateJob> executorClient = plugin.getExecutorClientFactory().buildJobClient(RemoteTestDocCreateJob.class);
                    RemoteTestDocCreateJob createJob = new RemoteTestDocCreateJob();
                    createJob.remoteName = "test2";
                    JobContext<RemoteTestDocCreateJob> createJobJobContext = executorClient.executeJob(createJob, AnonymousUser.INSTANCE);
                    ICouchbaseSession session = cbPlugin.getSessionFactory().newReadOnlySession(AnonymousUser.INSTANCE);
                    TestDocProcess processDoc = session.toBlocking().blockingGet(createJobJobContext.getTasks(RemoteTestDocCreateJob.RemoteTestDocCreateTask.class).get(0).key, TestDocProcess.class);
                    assertEquals(processDoc.name, createJob.remoteName);
                }
                {
                    List<JobExecutorClientInfo> jobClients = ClientBuilder.newClient()
                            .register(JsonProviderFactory.getProvider("service"))
                            .target("http://"+ ServerConnectorUtils.getUrl(daemon.getAdditionalWebServers().get(0).getServerConnector()))
                            .path("/apis/tests/processors/jobs")
                            .request()
                            .accept(MediaType.APPLICATION_JSON)
                            .get(new GenericType<List<JobExecutorClientInfo>>(){});
                    assertEquals(2L, jobClients.size());
                }

                {
                    List<TaskExecutorClientInfo> taskClients = ClientBuilder.newClient()
                            .register(JsonProviderFactory.getProvider("service"))
                            .target("http://" + ServerConnectorUtils.getUrl(daemon.getAdditionalWebServers().get(0).getServerConnector()))
                            .path("/apis/tests/processors/tasks")
                            .request()
                            .accept(MediaType.APPLICATION_JSON)
                            .get(new GenericType<List<TaskExecutorClientInfo>>() {
                            });
                    assertEquals(2L, taskClients.size());
                }

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