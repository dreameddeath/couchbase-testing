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

package com.dreameddeath.core.dao.document;

import com.dreameddeath.core.config.ConfigManagerFactory;
import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.couchbase.ICouchbaseBucketFactory;
import com.dreameddeath.core.couchbase.annotation.BucketDocumentForClass;
import com.dreameddeath.core.curator.discovery.standard.ICuratorDiscovery;
import com.dreameddeath.core.curator.discovery.standard.ICuratorDiscoveryLifeCycleListener;
import com.dreameddeath.core.curator.discovery.standard.ICuratorDiscoveryListener;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.annotation.ParentDao;
import com.dreameddeath.core.dao.config.CouchbaseDaoConfigProperties;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.discovery.DaoDiscovery;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.factory.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.model.discovery.DaoInstanceInfo;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.testing.couchbase.CouchbaseBucketFactorySimulator;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import io.reactivex.Single;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Created by Christophe Jeunesse on 27/12/2015.
 */
public class CouchbaseDocumentWithKeyPatternDaoCrossDomainTest {


    @DocumentEntity(domain = "testDomain",version = "1.0.0")
    public static class StandardTestDoc extends CouchbaseDocument{
        public String toto;
        public String tutu;
        public String titi;
    }

    @DocumentEntity(domain = "abstractTestDomain",version = "1.0.0")
    public abstract static class TestDoc extends CouchbaseDocument{
        public String toto;
        public String tutu;
        public String titi;
    }


    @DocumentEntity(domain = "testDomain",version = "1.0.0")
    public static class TestDocChild extends TestDoc{
    }

    @DocumentEntity(domain = "testDomain2",version = "1.0.0")
    public static class TestDocChildSibling extends TestDoc{
    }


    @DocumentEntity(domain="inherited",version = "1.0")
    public static class InheritedClass extends CouchbaseDocument{}

    @DaoForClass(TestDoc.class)
    public static class TestWithKeyPatternSharedAccrossDomainDao extends CouchbaseDocumentWithKeyPatternDao<TestDoc>{
        @Override
        protected String getKeyRawPattern() {
            return "test/{ tutu : \\w+ }/sub/{ toto : [^/]{1,} }/sub2/{titi}/subStd/\\d{2}";
        }

        @Override
        public boolean isKeySharedAcrossDomains() {
            return true;
        }

        @Override
        public Class<? extends BucketDocument<TestDoc>> getBucketDocumentClass() {
            return LocalBucketDocument.class ;
        }

        @BucketDocumentForClass(TestDoc.class)
        public static class LocalBucketDocument extends BucketDocument<TestDoc>{
            public LocalBucketDocument(TestDoc doc) {
                super(doc);
            }
        }

        @Override
        public Single<TestDoc> asyncBuildKey(ICouchbaseSession session, TestDoc newObject) {
            return null;
        }

        @Override
        public String getKeyFromParams(Object... params) {
            return null;
        }

        @Override
        protected TestDoc updateTransientFromKeyPattern(TestDoc obj, String... params) {
            obj.tutu=params[0];
            obj.toto=params[1];
            obj.titi=params[2];
            return obj;
        }
    }

    @DaoForClass(StandardTestDoc.class)
    public static class StandardTestDocDao extends CouchbaseDocumentDao<StandardTestDoc>{

        public static final String STANDARD_CNT = "standard/cnt";
        public static final String STANDARD_FMT = "standard/%10d";


        @Override
        public List<CouchbaseCounterDao.Builder> getCountersBuilder() {
            return Arrays.asList(
                    new CouchbaseCounterDao.Builder().withKeyPattern(STANDARD_CNT).withBaseDao(this)
            );
        }

        @Override
        public Class<? extends BucketDocument<StandardTestDoc>> getBucketDocumentClass() {
            return LocalBucketDocument.class ;
        }

        @Override
        public Single<StandardTestDoc> asyncBuildKey(ICouchbaseSession session, StandardTestDoc newObject) throws DaoException {
            return session.asyncIncrCounter(STANDARD_CNT,1)
                    .map(newId->{
                        newObject.getBaseMeta().setKey(String.format(STANDARD_FMT,newId));
                        return newObject;
                    });
        }

        @BucketDocumentForClass(StandardTestDoc.class)
        public static class LocalBucketDocument extends BucketDocument<StandardTestDoc>{
            public LocalBucketDocument(StandardTestDoc doc) {
                super(doc);
            }
        }

    }

    @DaoForClass(InheritedClass.class) @ParentDao(TestWithKeyPatternSharedAccrossDomainDao.class)
    public static class InheritedDomainClassDao extends CouchbaseDocumentDao<InheritedClass>{
        @Override
        public Class<? extends BucketDocument<InheritedClass>> getBucketDocumentClass() {
            return LocalBucketDocument.class;
        }

        @BucketDocumentForClass(InheritedClass.class)
        public static class LocalBucketDocument extends BucketDocument<InheritedClass>{
            public LocalBucketDocument(InheritedClass doc) {
                super(doc);
            }
        }

        @Override
        public Single<InheritedClass> asyncBuildKey(ICouchbaseSession session, InheritedClass newObject) throws DaoException {
            return null;
        }
    }



    private CuratorTestUtils testUtils;

    @Test
    public void testDaoWithCrossDomains() throws Exception{
        testUtils= new CuratorTestUtils().prepare(1);
        CuratorFramework framework= testUtils.getClient("test");
        ICouchbaseBucketFactory factory = new CouchbaseBucketFactorySimulator();

        final List<DaoInstanceInfo> daoInfoList=new ArrayList<>();
        CountDownLatch secondDaoAddedCountDown = new CountDownLatch(1);
        CountDownLatch thirdCountDown = new CountDownLatch(1);
        final List<DaoInstanceInfo> daoRemoveList=new ArrayList<>();
        final List<DaoInstanceInfo> daoUpdateList=new ArrayList<>();
        DaoDiscovery discovery = new DaoDiscovery(framework);
        discovery.addListener(new ICuratorDiscoveryListener<DaoInstanceInfo>() {
            @Override
            public void onRegister(String uid, DaoInstanceInfo obj) {
                synchronized (daoInfoList){
                    daoInfoList.add(obj);
                    if(daoInfoList.size()==3){
                        secondDaoAddedCountDown.countDown();
                    }
                    else if(daoInfoList.size()==5){
                        thirdCountDown.countDown();
                    }
                }
            }

            @Override
            public void onUnregister(String uid, DaoInstanceInfo oldObj) {
                daoRemoveList.add(oldObj);
            }

            @Override
            public void onUpdate(String uid, DaoInstanceInfo oldObj, DaoInstanceInfo newObj) {
                daoUpdateList.add(oldObj);
            }
        });
        CountDownLatch startFinished = new CountDownLatch(1);
        discovery.addLifeCycleListener(new ICuratorDiscoveryLifeCycleListener() {
            @Override
            public void onStart(ICuratorDiscovery discoverer, boolean isBefore) {
                startFinished.countDown();
            }

            @Override
            public void onStop(ICuratorDiscovery discoverer, boolean isBefore) {

            }
        });


        CouchbaseDocumentDaoFactory daoFactory =new CouchbaseDocumentDaoFactory.Builder()
                .withBucketFactory(factory)
                .withCuratorFramework(framework)
                .withDaemonUid("testDaemon1")
                .withWebServerUid("testServer1")
                .build();
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_DOMAIN_BUCKET_NAME.getProperty("testDomain").getName(), "testBucketName");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_DOMAIN_BUCKET_NAME.getProperty("testDomain2").getName(), "testBucketName2");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_READ_ONLY.getProperty("testDomain","standardtestdoc").getName(), true);
        List<CouchbaseDocumentDao> createdDaosWithCrossDomains = daoFactory.addDaosForEffectiveDomainsEntity(TestDoc.class);
        assertThat(createdDaosWithCrossDomains)
                .hasSize(2)
                .extracting("domain","client.bucketName","rootEntity.modelId")
                .containsExactlyInAnyOrder(
                        tuple("testDomain","testBucketName", EntityModelId.build("abstractTestDomain/testdoc/1.0.0")),
                        tuple("testDomain2","testBucketName2",EntityModelId.build("abstractTestDomain/testdoc/1.0.0"))
                );

        assertThat(createdDaosWithCrossDomains)
                .flatExtracting("childEntities")
                .hasSize(2)
                .extracting("modelId")
                .containsExactlyInAnyOrder(
                        EntityModelId.build("testDomain/testdocchild/1.0.0"),
                        EntityModelId.build("testDomain2/testdocchildsibling/1.0.0")
                );

        discovery.start();
        startFinished.await(10, TimeUnit.SECONDS);
        assertThat(daoInfoList).hasSize(2);
        assertThat(daoInfoList)
                .hasSize(2)
                .extracting("domain","bucketName","mainEntity.modelId","readOnly","daemonUid","webServerUid")
                .containsExactlyInAnyOrder(
                        tuple("testDomain","testBucketName", EntityModelId.build("abstractTestDomain/testdoc/1.0.0"),false,"testDaemon1","testServer1"),
                        tuple("testDomain2","testBucketName2",EntityModelId.build("abstractTestDomain/testdoc/1.0.0"),false,"testDaemon1","testServer1")
                );
        assertThat(daoInfoList)
                .flatExtracting("childEntities")
                .hasSize(2)
                .extracting("modelId")
                .containsExactlyInAnyOrder(
                        EntityModelId.build("testDomain/testdocchild/1.0.0"),
                        EntityModelId.build("testDomain2/testdocchildsibling/1.0.0")
                );


        List<CouchbaseDocumentDao> createdDaos = daoFactory.addDaosForEffectiveDomainsEntity(StandardTestDoc.class);
        assertThat(createdDaos)
                .hasSize(1)
                .extracting("domain","client.bucketName")
                .containsExactly(tuple("testDomain","testBucketName"));

        secondDaoAddedCountDown.await(10,TimeUnit.SECONDS);
        assertThat(daoInfoList).hasSize(3);
        DaoInstanceInfo lastAdded = daoInfoList.get(daoInfoList.size()-1);
        assertThat(lastAdded)
                .extracting("domain","bucketName","mainEntity.modelId","readOnly","daemonUid","webServerUid","className")
                .containsExactly("testDomain","testBucketName", EntityModelId.build("testDomain/standardtestdoc/1.0.0"),true,"testDaemon1","testServer1",StandardTestDocDao.class.getName())
        ;

        List<CouchbaseDocumentDao> inheritedCreatedDaos = daoFactory.addDaosForEffectiveDomainsEntity(InheritedClass.class);
        assertThat(inheritedCreatedDaos)
                .hasSize(2)
                .extracting("domain","client.bucketName","rootEntity.modelId")
                .containsExactlyInAnyOrder(
                        tuple("testDomain","testBucketName", EntityModelId.build("inherited/inheritedclass/1.0.0")),
                        tuple("testDomain2","testBucketName2",EntityModelId.build("inherited/inheritedclass/1.0.0"))
                );

        assertThat(inheritedCreatedDaos)
                .flatExtracting("childEntities")
                .hasSize(0);
        thirdCountDown.await(10,TimeUnit.SECONDS);
        assertThat(daoInfoList).hasSize(5);
        List<DaoInstanceInfo> inheritedInfoList = daoInfoList.subList(3,5);
        assertThat(inheritedInfoList)
                .hasSize(2)
                .extracting("domain","bucketName","mainEntity.modelId","readOnly","daemonUid","webServerUid","className")
                .containsExactlyInAnyOrder(
                        tuple("testDomain","testBucketName", EntityModelId.build("inherited/inheritedclass/1.0.0"),false,"testDaemon1","testServer1",InheritedDomainClassDao.class.getName()),
                        tuple("testDomain2","testBucketName2",EntityModelId.build("inherited/inheritedclass/1.0.0"),false,"testDaemon1","testServer1",InheritedDomainClassDao.class.getName())
                );




        testUtils.getCluster().stop();
    }

}