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

package com.dreameddeath.core.couchbase;

import com.dreameddeath.core.business.model.BusinessDocument;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.annotation.DocumentDef;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;
import com.dreameddeath.core.session.impl.CouchbaseSessionFactory;
import com.dreameddeath.core.transcoder.json.GenericJacksonTranscoder;
import com.dreameddeath.testing.couchbase.CouchbaseBucketSimulator;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Christophe Jeunesse on 17/12/2014.
 */
public class PrefixKeyTest {

    @DocumentDef(domain="test",name="prefixKey",version = "1.0.0")
    public static class TestPrefixKey extends BusinessDocument {
        @DocumentProperty("value")
        public String value;
    }


    @DaoForClass(TestPrefixKey.class)
    public static class TestPrefixKeyDao extends CouchbaseDocumentDao<TestPrefixKey> {
        public static final String TEST_CNT_KEY="test/cnt";
        public static final String TEST_CNT_KEY_PATTERN="test/cnt";
        public static final String TEST_KEY_FMT="test/%010d";

        public static class LocalBucketDocument extends BucketDocument<TestPrefixKey> {
            public LocalBucketDocument(TestPrefixKey obj){super(obj);}
        }

        @Override
        public Class<? extends BucketDocument<TestPrefixKey>> getBucketDocumentClass() {
            return LocalBucketDocument.class;
        }


        @Override
        public List<CouchbaseCounterDao.Builder> getCountersBuilder() {
            return Arrays.asList(
                    new CouchbaseCounterDao.Builder().withKeyPattern(TEST_CNT_KEY_PATTERN).withDefaultValue(1L).withBaseDao(this)
            );
        }
        @Override
        public TestPrefixKey buildKey(ICouchbaseSession session, TestPrefixKey newObject) throws DaoException, StorageException {
            long result = session.incrCounter(TEST_CNT_KEY, 1);
            newObject.getBaseMeta().setKey(String.format(TEST_KEY_FMT, result));

            return newObject;
        }
    }

    private final static CouchbaseSessionFactory _sessionFactory ;
    private final static CouchbaseBucketSimulator _client ;
    static {
        _client = new CouchbaseBucketSimulator("test","user1");
        CouchbaseSessionFactory.Builder sessionBuilder = new CouchbaseSessionFactory.Builder();
        sessionBuilder.getDocumentDaoFactoryBuilder().getUniqueKeyDaoFactoryBuilder().withDefaultTranscoder(new GenericJacksonTranscoder<>(CouchbaseUniqueKey.class));
        _sessionFactory = sessionBuilder.build();
        try {
            _sessionFactory.getDocumentDaoFactory().addDao(new TestPrefixKeyDao().setClient(_client), new GenericJacksonTranscoder<>(TestPrefixKey.class));
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }

        _client.start();
    }

    @Test
    public void testPrefixKey()throws Exception{
        ICouchbaseSession session = _sessionFactory.newReadWriteSession(null);
        TestPrefixKey testClass = session.newEntity(TestPrefixKey.class);
        testClass.value = "simple Test";

        session.save(testClass);

        assertNotNull(_client.getFromCache("user1"+ICouchbaseBucket.Utils.KEY_SEP + testClass.getBaseMeta().getKey(), TestPrefixKeyDao.LocalBucketDocument.class));
        session.reset();
        TestPrefixKey readClass = session.get(testClass.getMeta().getKey(),TestPrefixKey.class);
        assertEquals(readClass.value,testClass.value);
        assertEquals(readClass.getMeta().getKey(),testClass.getMeta().getKey());
    }
}
