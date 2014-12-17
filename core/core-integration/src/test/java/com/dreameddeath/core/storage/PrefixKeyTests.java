package com.dreameddeath.core.storage;

import com.dreameddeath.core.annotation.DocumentDef;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.annotation.dao.DaoForClass;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.business.BusinessCouchbaseDocument;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;
import com.dreameddeath.core.session.ICouchbaseSession;
import com.dreameddeath.core.session.impl.CouchbaseSessionFactory;
import com.dreameddeath.core.storage.impl.CouchbaseBucketSimulator;
import com.dreameddeath.core.transcoder.json.GenericJacksonTranscoder;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by ceaj8230 on 17/12/2014.
 */
public class PrefixKeyTests {

    @DocumentDef(domain="test",name="prefixKey",version = "1.0.0")
    public static class TestPrefixKey extends BusinessCouchbaseDocument {
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
        _client = new CouchbaseBucketSimulator("test","user1/");
        _sessionFactory = (new CouchbaseSessionFactory.Builder()).build();
        _sessionFactory.getUniqueKeyDaoFactory().setDefaultTranscoder(new GenericJacksonTranscoder<>(CouchbaseUniqueKey.class));
        _sessionFactory.getDocumentDaoFactory().addDao(new TestPrefixKeyDao().setClient(_client), new GenericJacksonTranscoder<>(TestPrefixKey.class));

        _client.start();
    }

    @Test
    public void testPrefixKey()throws Exception{
        ICouchbaseSession session = _sessionFactory.newReadWriteSession(null);
        TestPrefixKey testClass = session.newEntity(TestPrefixKey.class);
        testClass.value = "simple Test";

        session.save(testClass);

        assertNotNull(_client.getFromCache("user1/" + testClass.getBaseMeta().getKey(), TestPrefixKeyDao.LocalBucketDocument.class));
        session.reset();
        TestPrefixKey readClass = session.get(testClass.getMeta().getKey(),TestPrefixKey.class);
        assertEquals(readClass.value,testClass.value);
        assertEquals(readClass.getMeta().getKey(),testClass.getMeta().getKey());
    }
}
