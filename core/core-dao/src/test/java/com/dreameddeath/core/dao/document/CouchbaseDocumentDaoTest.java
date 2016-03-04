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

package com.dreameddeath.core.dao.document;

import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.annotation.BucketDocumentForClass;
import com.dreameddeath.core.couchbase.impl.GenericCouchbaseTranscoder;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.annotation.DocumentDef;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.transcoder.DocumentDecodingException;
import com.dreameddeath.core.model.exception.transcoder.DocumentEncodingException;
import com.dreameddeath.core.model.transcoder.ITranscoder;
import com.dreameddeath.testing.couchbase.CouchbaseBucketSimulator;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Christophe Jeunesse on 28/12/2015.
 */
public class CouchbaseDocumentDaoTest{
    private static Logger LOG = LoggerFactory.getLogger(CouchbaseDocumentDaoTest.class);

    @DocumentDef(domain = "test")
    public static class TestRootDaoDoc extends CouchbaseDocument{
        @DocumentProperty("test")
        public String test;
    }

    @DaoForClass(TestRootDaoDoc.class)
    public static class DaoRootTest extends CouchbaseDocumentDao<TestRootDaoDoc>{
        private final CouchbaseCounterDao cntDao;

        public DaoRootTest(CouchbaseCounterDao cntDao){
            this.cntDao = cntDao;
            cntDao.setBaseDao(this);
        }

        @BucketDocumentForClass(TestRootDaoDoc.class)
        public static class LocalBucketDocument extends BucketDocument<TestRootDaoDoc>{
            public LocalBucketDocument(TestRootDaoDoc doc) {
                super(doc);
            }
        }

        @Override
        public Class<? extends BucketDocument<TestRootDaoDoc>> getBucketDocumentClass() {
            return LocalBucketDocument.class;
        }

        @Override
        public Observable<TestRootDaoDoc> asyncBuildKey(ICouchbaseSession session, final TestRootDaoDoc newObject) throws DaoException {
            return cntDao.asyncIncrCounter(session,"cnt",1,false)
                    .map(cntVal->{newObject.getBaseMeta().setKey("test/"+cntVal);return newObject;});
        }

    }

    private DaoRootTest daoTest;

    @Before
    public void init(){
        ICouchbaseBucket wrapper = new CouchbaseBucketSimulator("test");
        GenericCouchbaseTranscoder<TestRootDaoDoc> transcoder = new GenericCouchbaseTranscoder<TestRootDaoDoc>(TestRootDaoDoc.class, DaoRootTest.LocalBucketDocument.class);
        transcoder.setTranscoder(new ITranscoder<TestRootDaoDoc>() {
            @Override
            public Class<TestRootDaoDoc> getBaseClass() {
                return TestRootDaoDoc.class;
            }

            @Override
            public TestRootDaoDoc decode(byte[] buf) throws DocumentDecodingException {
                TestRootDaoDoc result = new TestRootDaoDoc();
                result.getBaseMeta().setDbSize(buf.length);
                result.test = new String(buf);
                return result;
            }

            @Override
            public byte[] encode(TestRootDaoDoc doc) throws DocumentEncodingException {
                return doc.test.getBytes();
            }
        });
        wrapper.addTranscoder(transcoder);
        wrapper.start();
        daoTest = new DaoRootTest(new CouchbaseCounterDao(new CouchbaseCounterDao.Builder().withKeyPattern("test").withDefaultValue(1)));
        daoTest.setClient(wrapper);

    }

    @Test
    public void testRootDao() throws Throwable{
        {
            TestRootDaoDoc testRootDaoDoc = new TestRootDaoDoc();
            testRootDaoDoc.test = "toto";
            long start = System.nanoTime();
            TestRootDaoDoc resultingCreate = daoTest.create(new DummySession(), testRootDaoDoc, false);
            LOG.info("create duration {}", (System.nanoTime() - start) / (1000 * 1000));
            assertEquals("test/1", testRootDaoDoc.getBaseMeta().getKey());
            assertTrue(resultingCreate == testRootDaoDoc);

            testRootDaoDoc.test = "toto 2";
            start = System.nanoTime();
            TestRootDaoDoc resultingUpdate = daoTest.update(new DummySession(), testRootDaoDoc, false);
            LOG.info("update duration {}", (System.nanoTime() - start) / (1000 * 1000));
            assertTrue(resultingUpdate == testRootDaoDoc);
            assertEquals(testRootDaoDoc.test, resultingUpdate.test);

            start = System.nanoTime();
            TestRootDaoDoc resultingGetDoc = daoTest.get(new DummySession(), testRootDaoDoc.getBaseMeta().getKey());
            LOG.info("get duration {}", (System.nanoTime() - start) / (1000 * 1000));
            assertEquals(resultingGetDoc.test, testRootDaoDoc.test);
            assertEquals(resultingGetDoc.getBaseMeta().getKey(), testRootDaoDoc.getBaseMeta().getKey());
            assertEquals(resultingGetDoc.getBaseMeta().getCas(), testRootDaoDoc.getBaseMeta().getCas());
        }

        {
            TestRootDaoDoc testRootDaoDoc2 = new TestRootDaoDoc();
            testRootDaoDoc2.test = "toto";
            long start = System.nanoTime();
            TestRootDaoDoc resultingCreate2 = daoTest.create(new DummySession(), testRootDaoDoc2, false);
            LOG.info("create duration {}", (System.nanoTime() - start) / (1000 * 1000));
            assertEquals("test/2", resultingCreate2.getBaseMeta().getKey());
            assertTrue(resultingCreate2 == resultingCreate2);
        }

    }

}