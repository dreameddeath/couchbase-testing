/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.installedbase.process.service;

import com.dreameddeath.core.config.ConfigManagerFactory;
import com.dreameddeath.core.curator.CuratorFrameworkFactory;
import com.dreameddeath.core.dao.config.CouchbaseDaoConfigProperties;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.date.DateTimeServiceFactory;
import com.dreameddeath.core.date.MockDateTimeServiceImpl;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.core.user.StandardMockUserFactory;
import com.dreameddeath.couchbase.testing.daemon.DaemonWrapperForTesting;
import com.dreameddeath.infrastructure.common.CommonConfigProperties;
import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.webserver.RestWebServer;
import com.dreameddeath.infrastructure.plugin.couchbase.CouchbaseDaemonPlugin;
import com.dreameddeath.infrastructure.plugin.couchbase.CouchbaseWebServerPlugin;
import com.dreameddeath.infrastructure.plugin.notification.NotificationWebServerPlugin;
import com.dreameddeath.infrastructure.plugin.process.ProcessesWebServerPlugin;
import com.dreameddeath.installedbase.model.EntityConstants;
import com.dreameddeath.installedbase.model.v1.InstalledBase;
import com.dreameddeath.installedbase.process.model.v1.CreateUpdateInstalledBaseJob;
import com.dreameddeath.testing.couchbase.CouchbaseBucketFactorySimulator;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import com.dreameddeath.testing.dataset.DatasetManager;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.dreameddeath.installedbase.process.model.v1.CreateUpdateInstalledBaseRequest.OrderStatus.COMPLETED;
import static com.dreameddeath.installedbase.process.model.v1.CreateUpdateInstalledBaseRequest.OrderStatus.IN_ORDER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Christophe Jeunesse on 03/05/2016.
 */
public class CreateUpdateInstalledBaseJobProcessingServiceTest {
    private final static Logger LOG = LoggerFactory.getLogger(CreateUpdateInstalledBaseJobProcessingServiceTest.class);
    private DatasetManager manager;
    private static final String DATASET_NAME="job_create_update_inputs";
    private static final String DATASET_ELT_NOMINAL_CASE="job_create_update_simple_installed_base";

    private CuratorTestUtils testUtils;
    private CouchbaseBucketFactorySimulator couchbaseBucketFactory;
    private DaemonWrapperForTesting daemonWrapper;
    private static final DateTime REFERENCE_DATE = DateTime.parse("2016-01-01T00:00:00");
    private final AtomicReference<DateTime> dateTimeRef=new AtomicReference<>(REFERENCE_DATE);

    @Before
    public void setup() throws Exception {
        testUtils = new CuratorTestUtils();
        testUtils.prepare(1);
        couchbaseBucketFactory = new CouchbaseBucketFactorySimulator();

        manager = new DatasetManager();
        manager.addDatasetsFromResourceFilename("datasets/createUpdateInstalledBaseInput.json_dataset");
        manager.addDatasetsFromResourceFilename("datasets/job.createUpdateInstalledBaseInput.json_dataset");
        manager.prepareDatasets();
        String connectionString = testUtils.getCluster().getConnectString();

        ConfigManagerFactory.addPersistentConfigurationEntry(CommonConfigProperties.ZOOKEEPER_CLUSTER_ADDREES.getName(), connectionString);
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_DOMAIN_BUCKET_NAME.getProperty("installedbase").getName(), "testBucketName");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_DOMAIN_BUCKET_NAME.getProperty("party").getName(), "testBucketName");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_DOMAIN_BUCKET_NAME.getProperty("billing").getName(), "testBucketName");


        AbstractDaemon daemon = AbstractDaemon.builder()
                .withName("testing Daemon")
                .withUserFactory(new StandardMockUserFactory())
                .withPlugin(CouchbaseDaemonPlugin.builder().withBucketFactory(couchbaseBucketFactory))
                .build();

        daemon.addWebServer(RestWebServer.builder().withName("tests")
                //.withServiceDiscoveryManager(true)
                .withPlugin(CouchbaseWebServerPlugin.builder())
                .withPlugin(NotificationWebServerPlugin.builder())
                .withPlugin(ProcessesWebServerPlugin.builder())
                .withDateTimeServiceFactory(new DateTimeServiceFactory(new MockDateTimeServiceImpl(MockDateTimeServiceImpl.Calculator.fixedCalculator(dateTimeRef))))
                .withApplicationContextConfig("applicationContext.xml"));

        /*daemon.getAdditionalWebServers("tests").get(0).getPlugin(NotificationWebServerPlugin.class)
                .getEventBus().addListener(new CreateUpdateInstalledBaseEventListener());*/
        daemonWrapper = new DaemonWrapperForTesting(daemon);
        daemonWrapper.start();
    }

    @Test
    public void testDaemon() throws Exception {
        final Map<String,String> tempIdsMap=new HashMap<>();
        CreateUpdateInstalledBaseJob request;
        ProcessesWebServerPlugin plugin=daemonWrapper.getDaemon().getAdditionalWebServers().get(0).getPlugin(ProcessesWebServerPlugin.class);
        CouchbaseWebServerPlugin cbPlugin=daemonWrapper.getDaemon().getAdditionalWebServers().get(0).getPlugin(CouchbaseWebServerPlugin.class);
        IJobExecutorClient<CreateUpdateInstalledBaseJob> executorClient = plugin.getExecutorClientFactory().buildJobClient(CreateUpdateInstalledBaseJob.class);

        {
            Map<String, Object> params = new HashMap<>();
            params.put("origDate", dateTimeRef.get());
            params.put("orderStatus",IN_ORDER.toString());
            params.put("tempIdsMap",tempIdsMap);
            request = manager.build(CreateUpdateInstalledBaseJob.class, DATASET_NAME, DATASET_ELT_NOMINAL_CASE,params );
            JobContext<CreateUpdateInstalledBaseJob> createJobJobContext = executorClient.executeJob(request, AnonymousUser.INSTANCE).blockingGet();

            ICouchbaseSession session = cbPlugin.getSessionFactory().newReadOnlySession(EntityConstants.INSTALLED_BASE_DOMAIN,AnonymousUser.INSTANCE);
            CreateUpdateInstalledBaseJob.UpdateInstalledBaseTask updateInstalledBaseTask =session.asyncGet(createJobJobContext.getTasks(CreateUpdateInstalledBaseJob.UpdateInstalledBaseTask.class).get(0).getInternalTask().getBaseMeta().getKey(),CreateUpdateInstalledBaseJob.UpdateInstalledBaseTask.class).blockingGet();
            InstalledBase installedBase = updateInstalledBaseTask.blockingGetDocument(session);
            //assertEquals(processDoc.name, createJob.name);
            assertNotNull(installedBase.getContract());
            assertEquals(4,installedBase.getOffers().size());
            assertEquals(2,installedBase.getPsList().size());
            assertEquals(0,installedBase.getContract().getStatuses().size());
            assertEquals(0,installedBase.getOffers().stream().flatMap(elt->elt.getStatuses().stream()).count());
            assertEquals(0,installedBase.getPsList().stream().flatMap(elt->elt.getStatuses().stream()).count());
            Thread.sleep(50);
            assertEquals(1,CreateUpdateInstalledBaseEventListener.counter.get());
        }
        {
            Map<String, Object> params = new HashMap<>();
            params.put("origDate", dateTimeRef.get());
            params.put("orderStatus",COMPLETED.toString());
            params.put("tempIdsMap",tempIdsMap);
            request = manager.build(CreateUpdateInstalledBaseJob.class, DATASET_NAME, DATASET_ELT_NOMINAL_CASE,params );

            JobContext<CreateUpdateInstalledBaseJob> createJobJobContext = executorClient.executeJob(request, AnonymousUser.INSTANCE).blockingGet();
            ICouchbaseSession session = cbPlugin.getSessionFactory().newReadOnlySession(EntityConstants.INSTALLED_BASE_DOMAIN,AnonymousUser.INSTANCE);
            CreateUpdateInstalledBaseJob.UpdateInstalledBaseTask updateInstalledBaseTask =session.asyncGet(createJobJobContext.getTasks(CreateUpdateInstalledBaseJob.UpdateInstalledBaseTask.class).get(0).getInternalTask().getBaseMeta().getKey(),CreateUpdateInstalledBaseJob.UpdateInstalledBaseTask.class).blockingGet();
            InstalledBase installedBase = updateInstalledBaseTask.blockingGetDocument(session);
            //assertEquals(processDoc.name, createJob.name);
            assertNotNull(installedBase.getContract());
            assertEquals(4,installedBase.getOffers().size());
            assertEquals(2,installedBase.getPsList().size());
            assertEquals(1,installedBase.getContract().getStatuses().size());
            assertEquals(4,installedBase.getOffers().stream().flatMap(elt->elt.getStatuses().stream()).count());
            assertEquals(2,installedBase.getPsList().stream().flatMap(elt->elt.getStatuses().stream()).count());

            Thread.sleep(50);
            assertEquals(2,CreateUpdateInstalledBaseEventListener.counter.get());
        }

    }

    @After
    public void close() throws Exception {
        if(daemonWrapper!=null) daemonWrapper.stop();
        if (testUtils != null) testUtils.stop();
        CuratorFrameworkFactory.cleanup();
    }


}