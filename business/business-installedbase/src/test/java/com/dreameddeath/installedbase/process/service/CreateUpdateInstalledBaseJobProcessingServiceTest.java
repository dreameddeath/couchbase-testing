package com.dreameddeath.installedbase.process.service;

import com.dreameddeath.core.config.ConfigManagerFactory;
import com.dreameddeath.core.dao.config.CouchbaseDaoConfigProperties;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.date.DateTimeServiceFactory;
import com.dreameddeath.core.date.MockDateTimeServiceImpl;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.core.user.StandardMockUserFactory;
import com.dreameddeath.infrastructure.common.CommonConfigProperties;
import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.lifecycle.IDaemonLifeCycle;
import com.dreameddeath.infrastructure.daemon.webserver.RestWebServer;
import com.dreameddeath.infrastructure.plugin.couchbase.CouchbaseDaemonPlugin;
import com.dreameddeath.infrastructure.plugin.couchbase.CouchbaseWebServerPlugin;
import com.dreameddeath.infrastructure.plugin.process.ProcessesWebServerPlugin;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.dreameddeath.installedbase.model.v1.process.CreateUpdateInstalledBaseRequest.OrderStatus.COMPLETED;
import static com.dreameddeath.installedbase.model.v1.process.CreateUpdateInstalledBaseRequest.OrderStatus.IN_ORDER;
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
    private static final DateTime REFERENCE_DATE = DateTime.parse("2016-01-01T00:00:00");
    private final AtomicReference<DateTime> dateTimeRef=new AtomicReference<>(REFERENCE_DATE);
    private AbstractDaemon daemon;

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
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("core", "abstractjob").getName(), "testCoreBucketName");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("core", "abstracttask").getName(), "testCoreBucketName");
        daemon = AbstractDaemon.builder()
                .withName("testing Daemon")
                .withUserFactory(new StandardMockUserFactory())
                .withPlugin(CouchbaseDaemonPlugin.builder().withBucketFactory(couchbaseBucketFactory))
                .build();

        daemon.addWebServer(RestWebServer.builder().withName("tests")
                //.withServiceDiscoveryManager(true)
                .withPlugin(CouchbaseWebServerPlugin.builder())
                .withPlugin(ProcessesWebServerPlugin.builder())
                .withDateTimeServiceFactory(new DateTimeServiceFactory(new MockDateTimeServiceImpl(MockDateTimeServiceImpl.Calculator.fixedCalculator(dateTimeRef))))
                .withApplicationContextConfig("META-INF/spring/webserver.installedbase.processes.applicationContext.xml"));
    }

    @Test
    public void testDaemon() throws Exception {
        final AtomicInteger nbErrors = new AtomicInteger(0);

        Thread stopping_thread = new Thread(() -> {
            final Map<String,String> tempIdsMap=new HashMap<>();
            CreateUpdateInstalledBaseJob request;
            try {
                ProcessesWebServerPlugin plugin=daemon.getAdditionalWebServers().get(0).getPlugin(ProcessesWebServerPlugin.class);
                CouchbaseWebServerPlugin cbPlugin=daemon.getAdditionalWebServers().get(0).getPlugin(CouchbaseWebServerPlugin.class);
                IJobExecutorClient<CreateUpdateInstalledBaseJob> executorClient = plugin.getExecutorClientFactory().buildJobClient(CreateUpdateInstalledBaseJob.class);

                {
                    Map<String, Object> params = new HashMap<>();
                    params.put("origDate", dateTimeRef.get());
                    params.put("orderStatus",IN_ORDER.toString());
                    params.put("tempIdsMap",tempIdsMap);
                    request = manager.build(CreateUpdateInstalledBaseJob.class, DATASET_NAME, DATASET_ELT_NOMINAL_CASE,params );
                    JobContext<CreateUpdateInstalledBaseJob> createJobJobContext = executorClient.executeJob(request, AnonymousUser.INSTANCE);

                    ICouchbaseSession session = cbPlugin.getSessionFactory().newReadOnlySession(AnonymousUser.INSTANCE);
                    InstalledBase installedBase = createJobJobContext.getTasks(CreateUpdateInstalledBaseJob.UpdateInstalledBaseTask.class).get(0).getDocument(session);
                    //assertEquals(processDoc.name, createJob.name);
                    assertNotNull(installedBase.getContract());
                    assertEquals(4,installedBase.getOffers().size());
                    assertEquals(2,installedBase.getPsList().size());
                    assertEquals(0,installedBase.getContract().getStatuses().size());
                    assertEquals(0,installedBase.getOffers().stream().flatMap(elt->elt.getStatuses().stream()).count());
                    assertEquals(0,installedBase.getPsList().stream().flatMap(elt->elt.getStatuses().stream()).count());
                }
                {
                    Map<String, Object> params = new HashMap<>();
                    params.put("origDate", dateTimeRef.get());
                    params.put("orderStatus",COMPLETED.toString());
                    params.put("tempIdsMap",tempIdsMap);
                    request = manager.build(CreateUpdateInstalledBaseJob.class, DATASET_NAME, DATASET_ELT_NOMINAL_CASE,params );

                    JobContext<CreateUpdateInstalledBaseJob> createJobJobContext = executorClient.executeJob(request, AnonymousUser.INSTANCE);
                    ICouchbaseSession session = cbPlugin.getSessionFactory().newReadOnlySession(AnonymousUser.INSTANCE);
                    InstalledBase installedBase = createJobJobContext.getTasks(CreateUpdateInstalledBaseJob.UpdateInstalledBaseTask.class).get(0).getDocument(session);
                    //assertEquals(processDoc.name, createJob.name);
                    assertNotNull(installedBase.getContract());
                    assertEquals(4,installedBase.getOffers().size());
                    assertEquals(2,installedBase.getPsList().size());
                    assertEquals(1,installedBase.getContract().getStatuses().size());
                    assertEquals(4,installedBase.getOffers().stream().flatMap(elt->elt.getStatuses().stream()).count());
                    assertEquals(2,installedBase.getPsList().stream().flatMap(elt->elt.getStatuses().stream()).count());
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