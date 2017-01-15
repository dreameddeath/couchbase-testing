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

package com.dreameddeath.core.model;


import com.dreameddeath.core.business.model.BusinessDocument;
import com.dreameddeath.core.business.model.VersionedDocumentElement;
import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.couchbase.annotation.BucketDocumentForClass;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.annotation.DocumentVersionUpgrader;
import com.dreameddeath.core.model.entity.EntityVersionUpgradeManager;
import com.dreameddeath.core.session.impl.CouchbaseSessionFactory;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentObjectMapperConfigurator;
import com.dreameddeath.testing.couchbase.CouchbaseBucketSimulator;
import io.reactivex.Single;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Christophe Jeunesse on 17/12/2014.
 */
public class UpgraderTest {
    public static abstract class TestElement extends VersionedDocumentElement {
        @DocumentProperty("baseValue")
        public String baseValue;
    }

    @DocumentEntity(domain="test",name="elementType1",version = "1.0.0")
    public static class TestElementType1 extends TestElement{
        @DocumentProperty("value")
        public String valueType1;
    }

    @DocumentEntity(domain="test",name="elementType2",version = "1.0.0")
    public static class TestElementType2 extends TestElement{
        @DocumentProperty("value")
        public String valueType2;
    }

    @DocumentEntity(domain="test",name="elementUnified",version = "1.0.0")
    public static class TestElementV2 extends VersionedDocumentElement {
        @DocumentProperty("baseValue2")
        public String baseValue2;
        @DocumentProperty("value2")
        public String value2;
    }

    @DocumentEntity(domain="test",name="test",version = "1.0.0")
    public static class TestModel extends BusinessDocument {
        @DocumentProperty("value")
        public String value;

        @DocumentProperty("element")
        public List<TestElement> element=new ArrayList<>();
    }

    @DocumentEntity(domain="test",name="test",version = "2.1.0")
    public static class TestModelV2 extends BusinessDocument {
        @DocumentProperty("value")
        public String value2;

        @DocumentProperty("element")
        public List<TestElementV2> element2=new ArrayList<>();

    }

    //@DaoForClass(TestModel.class)
    public static class TestDaoV1 extends CouchbaseDocumentDao<TestModel>{
        public static final String TEST_CNT_KEY="test/cnt";
        public static final String TEST_CNT_KEY_PATTERN="test/cnt";
        public static final String TEST_KEY_FMT="test/%010d";

        @BucketDocumentForClass(TestModel.class)
        public static class LocalBucketDocument extends BucketDocument<TestModel> {
            public LocalBucketDocument(TestModel obj){super(obj);}
        }

        @Override
        public Class<? extends BucketDocument<TestModel>> getBucketDocumentClass() {
            return LocalBucketDocument.class;
        }


        @Override
        public List<CouchbaseCounterDao.Builder> getCountersBuilder() {
            return Arrays.asList(
                    new CouchbaseCounterDao.Builder().withKeyPattern(TEST_CNT_KEY_PATTERN).withBaseDao(this)
            );
        }


        @Override
        public Single<TestModel> asyncBuildKey(ICouchbaseSession session, TestModel newObject) throws DaoException {
            return session.asyncIncrCounter(TEST_CNT_KEY, 1)
                    .map(val->{newObject.getBaseMeta().setKey(String.format(TEST_KEY_FMT, val));return newObject;});
        }
    }

    //@DaoForClass(TestModelV2.class)
    public static class TestDaoV2 extends CouchbaseDocumentDao<TestModelV2>{
        public static final String TEST_CNT_KEY="test/cnt";
        public static final String TEST_CNT_KEY_PATTERN="test/cnt";

        @BucketDocumentForClass(TestModelV2.class)
        public static class LocalBucketDocument extends BucketDocument<TestModelV2> {
            public LocalBucketDocument(TestModelV2 obj){super(obj);}
        }

        @Override
        public Class<? extends BucketDocument<TestModelV2>> getBucketDocumentClass() {
            return LocalBucketDocument.class;
        }

        @Override
        public Single<TestModelV2> asyncBuildKey(ICouchbaseSession session, TestModelV2 newObject) throws DaoException {
            return session.asyncIncrCounter(TEST_CNT_KEY, 1).
                    map(cntVal->{newObject.getBaseMeta().setKey(String.format(TEST_CNT_KEY, cntVal));return newObject;});
        }

    }


    public static class UpgraderClass {
        @DocumentVersionUpgrader(domain = "test", name = "test", from = "1.0", to = "1.1.0")
        public TestModel upgradeToV1_1(TestModel src) {
            src.value += " v1.1";
            return src;
        }

        @DocumentVersionUpgrader(domain = "test", name = "test", from = "1.1", to = "2.0.0")
        public TestModelV2 upgradeToV2(TestModel src) {
            TestModelV2 v2 = new TestModelV2();

            v2.value2 = src.value + " v2";
            for(TestElement element:src.element) {
                TestElementV2 mergedElement= new TestElementV2();
                mergedElement.baseValue2 = element.baseValue + " to v2";
                if (element instanceof TestElementType1) {
                    mergedElement.value2 = ((TestElementType1) element).valueType1 + " to v2";
                } else if (element instanceof TestElementType2) {
                    mergedElement.value2 = ((TestElementType2) element).valueType2 + " to v2";
                }
                v2.element2.add(mergedElement);
            }
            return v2;
        }

        public void updateTestElement(TestElement element){
            element.baseValue+=" to V1_1";
        }

        @DocumentVersionUpgrader(domain = "test", name = "elementType1", from = "1.0", to = "1.1.0")
        public TestElementType1 upgrateElementType(TestElementType1 element){
            updateTestElement(element);
            element.valueType1+=" to V1_1";
            return element;
        }

        //Voluntary use of same method name to check access by type
        @DocumentVersionUpgrader(domain = "test", name = "elementType2", from = "1.0", to = "1.1.0")
        public TestElementType2 upgrateElementType(TestElementType2 element){
            updateTestElement(element);
            element.valueType2+=" to V1_1";
            return element;
        }
    }


    private final static CouchbaseSessionFactory sessionFactory ;
    static {
        CouchbaseBucketSimulator client = new CouchbaseBucketSimulator("test");

        CouchbaseSessionFactory.Builder sessionBuilder = new CouchbaseSessionFactory.Builder();
        sessionFactory = sessionBuilder.build();
        try {
            sessionFactory.getDocumentDaoFactory().addDaoFor(TestModel.class,new TestDaoV1().setClient(client));
            sessionFactory.getDocumentDaoFactory().addDaoFor(TestModelV2.class,new TestDaoV2().setClient(client));
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }

        client.start();
    }

    @Test
    public void upgradeUnitTests() throws Exception{
        EntityVersionUpgradeManager upgradeManager = (EntityVersionUpgradeManager) ObjectMapperFactory.BASE_INSTANCE.getMapper(CouchbaseDocumentObjectMapperConfigurator.BASE_COUCHBASE_STORAGE).getDeserializationConfig().getAttributes().getAttribute(EntityVersionUpgradeManager.class);
        ICouchbaseSession session = sessionFactory.newReadWriteSession(null);
        TestModel v1 =session.newEntity(TestModel.class);
        String refValue = "A first Value";
        v1.value = refValue;
        TestElementType1 element = new TestElementType1();
        String refBaseElementValue = "The base value";
        String refElementValue = "The inherited value";
        element.baseValue=refBaseElementValue;
        element.valueType1=refElementValue;
        v1.element.add(element);

        TestElementType2 element2 = new TestElementType2();
        element2.baseValue=refBaseElementValue;
        element2.valueType2=refElementValue;
        v1.element.add(element2);

        try {
            upgradeManager.addVersionToDiscard("test", "test", "2.0.0");
            session.toBlocking().blockingSave(v1);
            upgradeManager.removeVersionToDiscard("test", "test", "2.0.0");

            session.reset();

            Object result = session.toBlocking().blockingGet(v1.getMeta().getKey(),TestModelV2.class);
            assertEquals(TestModelV2.class,result.getClass());
            TestModelV2 resultV2 = (TestModelV2)result;
            assertEquals(refValue + " v1.1 v2", resultV2.value2);
            assertEquals("test/test/2.0.0", resultV2.getDocumentFullVersionId());
            assertEquals(2, resultV2.element2.size());
            for(TestElementV2 elementMigrated:resultV2.element2) {
                assertEquals(refBaseElementValue + " to V1_1 to v2", elementMigrated.baseValue2);
                assertEquals(refElementValue + " to V1_1 to v2", elementMigrated.value2);
            }
        }
        catch(Exception e){
            //fail("Unexpected exception " + e.getMessage());
            throw e;
        }
    }



}
