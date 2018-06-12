package com.dreameddeath.couchbase.core.catalog.service.impl;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.dreameddeath.core.config.ConfigManagerFactory;
import com.dreameddeath.core.couchbase.config.CouchbaseConfigProperties;
import com.dreameddeath.core.dao.config.CouchbaseDaoConfigProperties;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.json.model.Version;
import com.dreameddeath.core.session.impl.CouchbaseSessionFactory;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.couchbase.core.catalog.dao.v1.CatalogDao;
import com.dreameddeath.couchbase.core.catalog.model.v1.Catalog;
import com.dreameddeath.couchbase.core.catalog.model.v1.changeset.CatalogChangeSet;
import com.dreameddeath.couchbase.core.catalog.model.v1.changeset.ChangeSetItem;
import com.dreameddeath.couchbase.core.catalog.service.CatalogServiceFactory;
import com.dreameddeath.couchbase.core.catalog.service.ICatalogRef;
import com.dreameddeath.couchbase.core.catalog.service.impl.dao.domain1.TestCatItemDomain1Dao;
import com.dreameddeath.couchbase.core.catalog.service.impl.dao.domain2.TestCatItemDomain2Dao;
import com.dreameddeath.couchbase.core.catalog.service.impl.model.domain1.TestCatItemDomain1;
import com.dreameddeath.testing.couchbase.CouchbaseBucketSimulator;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class CatalogServiceTest {
    private CuratorTestUtils curatorUtils;
    private CouchbaseBucketSimulator cbSimulator;
    private CouchbaseBucketSimulator cbSimulator2;

    private CouchbaseSessionFactory sessionFactory;
    private MetricRegistry metricRegistry;
    private CuratorFramework client;

    @Before
    public void init() throws Exception {
        curatorUtils = new CuratorTestUtils().prepare(1);

        cbSimulator = new CouchbaseBucketSimulator("testBucketName");
        cbSimulator.start();
        cbSimulator2 = new CouchbaseBucketSimulator("testBucketName2");
        cbSimulator2.start();
        sessionFactory = new CouchbaseSessionFactory.Builder().build();
        metricRegistry = new MetricRegistry();

        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseConfigProperties.COUCHBASE_BUCKET_CLUSTER_NAME.getProperty("domain1").getName(), "cluster");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseConfigProperties.COUCHBASE_BUCKET_CLUSTER_NAME.getProperty("domain2").getName(), "cluster");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_DOMAIN_BUCKET_NAME.getProperty("domain1").getName(), "testBucketName");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_DOMAIN_BUCKET_NAME.getProperty("domainSharedWith1").getName(), "testBucketName");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_DOMAIN_BUCKET_NAME.getProperty("domain2").getName(), "testBucketName2");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("domain1", "catalog").getName(), "domain1");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("domainSharedWith1", "catalog").getName(), "domain1");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("domain2", "catalog").getName(), "domain2");

        sessionFactory.getDocumentDaoFactory().addDao(new CatalogDao().setDomain("domain1").setClient(cbSimulator));
        sessionFactory.getDocumentDaoFactory().addDao(new CatalogDao().setDomain("domainSharedWith1").setClient(cbSimulator));
        sessionFactory.getDocumentDaoFactory().addDao(new CatalogDao().setDomain("domain2").setClient(cbSimulator2));
        sessionFactory.getDocumentDaoFactory().addDao(new TestCatItemDomain1Dao().setClient(cbSimulator));
        sessionFactory.getDocumentDaoFactory().addDao(new TestCatItemDomain2Dao().setClient(cbSimulator2));
        sessionFactory.getDocumentDaoFactory().getViewDaoFactory().initAllViews();
    }

    @Test
    public void testCatalogLoadingCrossDomain() throws Exception{
        final CatalogServiceFactory factory = new CatalogServiceFactory(false);
        factory.setDateTimeService(sessionFactory.getDateTimeServiceFactory().getService());
        factory.setMetricRegistry(metricRegistry);
        factory.setFactory(sessionFactory.getDocumentDaoFactory());
        factory.init();

        ICouchbaseSession domain1Session = sessionFactory.newReadWriteSession("domain1", AnonymousUser.INSTANCE);
        ICouchbaseSession domainSharedWith1Session = sessionFactory.newReadWriteSession("domainSharedWith1", AnonymousUser.INSTANCE);
        ICouchbaseSession domain2Session = sessionFactory.newReadWriteSession("domain2", AnonymousUser.INSTANCE);
        TestCatItemDomain1 testCatItemDomain1;
        TestCatItemDomain1 testCatItemDomain1overloaded;
        TestCatItemDomain1 testCatItemDomain1overload;
        {
            testCatItemDomain1 = new TestCatItemDomain1();
            testCatItemDomain1.value1 = "domain1_item1";
            testCatItemDomain1.setId("item1");
            testCatItemDomain1.setVersion(Version.version("1.0.0"));
            domain1Session.toBlocking().blockingCreate(testCatItemDomain1);
        }
        {
            testCatItemDomain1overloaded = new TestCatItemDomain1();
            testCatItemDomain1overloaded.value1 = "domain1_item2_v1";
            testCatItemDomain1overloaded.setId("item2");
            testCatItemDomain1overloaded.setVersion(Version.version("1.0.0"));
            domain1Session.toBlocking().blockingCreate(testCatItemDomain1overloaded);
        }
        {
            testCatItemDomain1overload = new TestCatItemDomain1();
            testCatItemDomain1overload.value1 = "domain1_item2_v2";
            testCatItemDomain1overload.setId("item2");
            testCatItemDomain1overload.setVersion(Version.version("2.0.0"));
            domain1Session.toBlocking().blockingCreate(testCatItemDomain1overload);
        }



        {
            Catalog catalogDomain1_1 = new Catalog();
            catalogDomain1_1.setDomain("domain1");
            catalogDomain1_1.setName("cat_domain1_1");
            catalogDomain1_1.setVersion(Version.version("1.0.0"));
            CatalogChangeSet catalogChangeSet = new CatalogChangeSet();
            catalogChangeSet.addChange(ChangeSetItem.build(testCatItemDomain1));
            catalogChangeSet.addChange(ChangeSetItem.build(testCatItemDomain1overloaded));
            catalogDomain1_1.addChangeSet(catalogChangeSet);
            domain1Session.toBlocking().blockingCreate(catalogDomain1_1);
        }
        {
            Catalog catalogDomain1_2 = new Catalog();
            catalogDomain1_2.setDomain("domain1");
            catalogDomain1_2.setName("cat_domain1_2");
            catalogDomain1_2.setVersion(Version.version("2.0.0"));
            catalogDomain1_2.setPatchedVersion(Version.version("1.0.0"));
            CatalogChangeSet catalogChangeSet = new CatalogChangeSet();
            catalogChangeSet.addChange(ChangeSetItem.build(testCatItemDomain1overload));
            catalogDomain1_2.addChangeSet(catalogChangeSet);

            domain1Session.toBlocking().blockingCreate(catalogDomain1_2);
        }
        {
            Catalog catalogDomainShared = new Catalog();
            catalogDomainShared.setDomain("domainSharedWith1");
            catalogDomainShared.setName("cat_domainshared1");
            catalogDomainShared.setVersion(Version.version("4.0.0"));
            domainSharedWith1Session.toBlocking().blockingCreate(catalogDomainShared);
        }


        {
            Catalog catalogDomain2_1 = new Catalog();
            catalogDomain2_1.setDomain("domain2");
            catalogDomain2_1.setName("cat_domain2_1");
            catalogDomain2_1.setVersion(Version.version("1.0.0"));
            domain2Session.toBlocking().blockingCreate(catalogDomain2_1);
        }
        {
            Catalog catalogDomain2_2 = new Catalog();
            catalogDomain2_2.setDomain("domain2");
            catalogDomain2_2.setName("cat_domain2_2");
            catalogDomain2_2.setVersion(Version.version("2.0.0"));
            catalogDomain2_2.setPatchedVersion(Version.version("1.0.0"));
            domain2Session.toBlocking().blockingCreate(catalogDomain2_2);
        }

        CatalogService catalogService = factory.getCatalogService("domain1");

        CatalogService catalogService2 = factory.getCatalogService("domain2");

        CatalogService catalogServiceSharedWith1 = factory.getCatalogService("domainSharedWith1");

        assertEquals("cat_domain1_2",catalogService.getCatalog().getCatalogName().blockingGet());
        List<ICatalogRef.ICatalogItemRef> iCatalogItemRefs = catalogService.getCatalog().getItems().toList().blockingGet();
        assertEquals(2,iCatalogItemRefs.size());
        for(ICatalogRef.ICatalogItemRef ref:iCatalogItemRefs){
            if(ref.id().equals("item1")){
                assertEquals(TestCatItemDomain1.class,ref.clazz());
                assertEquals(Version.version("1.0.0"),ref.version());
            }
            else{
                assertEquals(TestCatItemDomain1.class,ref.clazz());
                assertEquals(Version.version("2.0.0"),ref.version());
            }
        }
        assertEquals(Arrays.asList("item1","item2"),iCatalogItemRefs.stream().map(ICatalogRef.ICatalogItemRef::id).sorted().collect(Collectors.toList()));
        //assertEquals(Arrays.asList("item1","item2"),iCatalogItemRefs.stream().map(ICatalogRef.ICatalogItemRef::id).sorted().collect(Collectors.toList()));
        assertEquals("domain1_item1",catalogService.getCatalog().getCatalogElement("item1",TestCatItemDomain1.class).blockingGet().value1);
        assertEquals("domain1_item2_v2",catalogService.getCatalog().getCatalogElement("item2",TestCatItemDomain1.class).blockingGet().value1);
        assertEquals("domain1_item2_v1",catalogService.getCatalog(Version.version("1.0.0")).getCatalogElement("item2",TestCatItemDomain1.class).blockingGet().value1);
        assertEquals("domain1_item1",catalogService.getCatalog(Version.version("1.0.0")).getCatalogElement("item1",TestCatItemDomain1.class).blockingGet().value1);

        assertEquals(3,metricRegistry.getCounters(MetricFilter.ALL).get("catalog.cache.domain1.loads-success").getCount());
    }


}
