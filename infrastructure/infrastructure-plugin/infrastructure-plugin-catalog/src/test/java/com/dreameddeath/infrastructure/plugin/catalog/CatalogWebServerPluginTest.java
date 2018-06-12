package com.dreameddeath.infrastructure.plugin.catalog;

import com.dreameddeath.core.config.ConfigManagerFactory;
import com.dreameddeath.core.dao.config.CouchbaseDaoConfigProperties;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.core.user.StandardMockUserFactory;
import com.dreameddeath.couchbase.core.catalog.model.v1.Catalog;
import com.dreameddeath.couchbase.core.catalog.model.v1.changeset.CatalogChangeSet;
import com.dreameddeath.couchbase.core.catalog.model.v1.changeset.ChangeSetItem;
import com.dreameddeath.couchbase.core.catalog.service.ICatalogRef;
import com.dreameddeath.couchbase.core.catalog.service.impl.CatalogService;
import com.dreameddeath.infrastructure.common.CommonConfigProperties;
import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.lifecycle.IDaemonLifeCycle;
import com.dreameddeath.infrastructure.daemon.webserver.RestWebServer;
import com.dreameddeath.infrastructure.plugin.couchbase.CouchbaseDaemonPlugin;
import com.dreameddeath.infrastructure.plugin.couchbase.CouchbaseWebServerPlugin;
import com.dreameddeath.testing.couchbase.CouchbaseBucketFactorySimulator;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class CatalogWebServerPluginTest {
    private final static Logger LOG = LoggerFactory.getLogger(CatalogWebServerPluginTest.class);
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
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("test", "catalogelemtest").getName(), "testBucketName");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("test", "catalog").getName(), "testBucketName");

        final AbstractDaemon daemon = AbstractDaemon.builder()
                .withName("testing Daemon")
                .withUserFactory(new StandardMockUserFactory())
                .withPlugin(CouchbaseDaemonPlugin.builder().withBucketFactory(couchbaseBucketFactory))
                .build();

        daemon.addWebServer(RestWebServer.builder().withName("tests")
                .withPlugin(CouchbaseWebServerPlugin.builder().withAutoInitView(true))
                .withPlugin(CatalogWebServerPlugin.builder())
                .withApplicationContextConfig("applicationContextLocal.xml"));


        Thread stopping_thread = new Thread(() -> {
            try {
                CatalogWebServerPlugin plugin=daemon.getAdditionalWebServers().get(0).getPlugin(CatalogWebServerPlugin.class);
                CouchbaseWebServerPlugin cbPlugin=daemon.getAdditionalWebServers().get(0).getPlugin(CouchbaseWebServerPlugin.class);
                ICouchbaseSession session = cbPlugin.getSessionFactory().newReadWriteSession("test", AnonymousUser.INSTANCE);
                CatalogElemTest catalogElemTest = session.newEntity(CatalogElemTest.class);
                catalogElemTest.value1 = "test1";
                catalogElemTest = session.toBlocking().blockingCreate(catalogElemTest);
                Catalog catalog = new Catalog();
                catalog.setDomain("test");
                catalog.setState(Catalog.State.PROD);
                CatalogChangeSet changeSet = new CatalogChangeSet();
                changeSet.addChange(ChangeSetItem.build(catalogElemTest));
                catalog.addChangeSet(changeSet);
                catalog.getBaseMeta().setKey("catalog/0000000001");
                cbPlugin.getDocumentDaoFactory().getBucketFactory().getBucket("testBucketName").toBlocking().add(catalog);

                //session.toBlocking().blockingCreate(catalog);
                CatalogService test = plugin.getCatalogServiceFactory().getCatalogService("test");
                List<ICatalogRef.ICatalogItemRef> iCatalogItemRefs = test.getCatalog().getItems().toList().blockingGet();
                assertEquals(1,iCatalogItemRefs.size());
                CatalogElemTest readItem = test.getCatalog().getCatalogElement(catalogElemTest.getId(), CatalogElemTest.class).blockingGet();
                assertEquals(catalogElemTest.value1,readItem.value1);

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