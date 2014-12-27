package com.dreameddeath.core.storage;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.annotation.dao.DaoForClass;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.dao.view.CouchbaseViewDao;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.view.IViewQuery;
import com.dreameddeath.core.model.view.IViewQueryResult;
import com.dreameddeath.core.model.view.IViewValueTranscoder;
import com.dreameddeath.core.model.view.impl.ViewQuery;
import com.dreameddeath.core.session.ICouchbaseSession;
import com.dreameddeath.core.test.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ceaj8230 on 18/12/2014.
 */
public class ViewTests {

    public static class TestDoc extends CouchbaseDocument{
        @DocumentProperty("name")
        public String name;

        @DocumentProperty("intVal")
        public Integer intVal;

        @DocumentProperty("longVal")
        public Long longVal;

        @DocumentProperty("doubleVal")
        public Double doubleVal;

        @DocumentProperty("boolVal")
        public Boolean boolVal;

        @DocumentProperty("arrayVal")
        public List<SubElem> arrayVal;

        public static class SubElem{
            @DocumentProperty("longVal")
            public Long longVal;
        }
    }

    @DaoForClass(TestDoc.class)
    public static class TestDao extends CouchbaseDocumentWithKeyPatternDao<TestDoc> {
        public static final String TEST_CNT_KEY="test/cnt";
        public static final String TEST_CNT_KEY_PATTERN="test/cnt";
        public static final String TEST_KEY_FMT="test/%010d";
        public static final String TEST_KEY_PATTERN="test/\\d{10}";

        @Override
        public String getKeyPattern() {
            return TEST_KEY_PATTERN;
        }

        public static class TestViewDao extends CouchbaseViewDao<String,String,TestDoc>{
            public TestViewDao(TestDao parentDao){
                super("test/","testView",parentDao);
            }

            public static class TestViewQuery extends ViewQuery<String,String,TestDoc>{
                public TestViewQuery(CouchbaseViewDao dao) {super(dao);}

                @Override
                public KeyType getKeyType() {return KeyType.STRING;}
            }
            @Override
            public String getContent() {
                return
                        "emit(meta.id,doc);"+
                        "emit(doc.name,doc.name);"+
                        "emit(doc.doubleVal,doc.doubleVal);"+
                        "emit(doc.intVal,doc.intVal);"+
                        "emit(doc.boolVal,doc.boolVal);"+
                        "emit(doc.longVal,doc.longVal);"+
                        "emit(doc.arrayVal,doc.arrayVal);";
            }

            @Override
            public IViewQuery<String,String,TestDoc> buildViewQuery() {
                return new TestViewQuery(this);
            }

            @Override
            public IViewValueTranscoder<String> getValueTranscoder() {
                return null;
            }
        }

        public static class LocalBucketDocument extends BucketDocument<TestDoc> {
            public LocalBucketDocument(TestDoc obj){super(obj);}
        }

        @Override
        public Class<? extends BucketDocument<TestDoc>> getBucketDocumentClass() {
            return LocalBucketDocument.class;
        }

        @Override
        public List<CouchbaseViewDao> getViews(){
            return Arrays.asList(
                new TestViewDao(this)
            );
        }

        @Override
        public List<CouchbaseCounterDao.Builder> getCountersBuilder() {
            return Arrays.asList(
                    new CouchbaseCounterDao.Builder().withKeyPattern(TEST_CNT_KEY_PATTERN).withDefaultValue(1L).withBaseDao(this)
            );
        }
        @Override
        public TestDoc buildKey(ICouchbaseSession session, TestDoc newObject) throws DaoException, StorageException {
            long result = session.incrCounter(TEST_CNT_KEY, 1);
            newObject.getBaseMeta().setKey(String.format(TEST_KEY_FMT, result));

            return newObject;
        }
    }

    Utils.TestEnvironment _env;
    @Before
    public void initTest() throws  Exception{
        _env = new Utils.TestEnvironment("ViewTests");
        _env.addDocumentDao(new TestDao(),TestDoc.class);
        _env.start();
    }

    @Test
    public void testView() throws Exception{
        ICouchbaseSession session = _env.getSessionFactory().newReadWriteSession(null);
        for(int i=0;i<10;++i){
            TestDoc doc = session.newEntity(TestDoc.class);
            doc.name="test "+i;
            doc.doubleVal=i*1.1;
            doc.longVal=i+1L;
            doc.intVal=i;
            doc.boolVal=(i%2==0)?true:false;
            doc.arrayVal = new ArrayList<>(i);
            for(int j=0;j<i;++j){
                TestDoc.SubElem elem=new TestDoc.SubElem();
                elem.longVal=j+1L;
                doc.arrayVal.add(elem);
            }
            session.save(doc);
        }

        IViewQuery<String,String,TestDoc> query = session.initViewQuery(TestDoc.class, "testView");
        query.withStartKey("test 3").withEndKey("test 5",true).withLimit(20).syncWithDoc();
        IViewQueryResult<String,String,TestDoc> result = session.executeQuery(query);

    }

    @After
    public void endTest(){
        _env.shutdown(true);
    }
}
