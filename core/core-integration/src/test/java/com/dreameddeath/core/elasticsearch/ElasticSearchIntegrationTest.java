/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.elasticsearch;

import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.couchbase.annotation.BucketDocumentForClass;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.model.view.IViewKeyTranscoder;
import com.dreameddeath.core.dao.model.view.IViewTranscoder;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.view.CouchbaseViewDao;
import com.dreameddeath.core.elasticsearch.dao.ElasticSearchResult;
import com.dreameddeath.core.java.utils.NumberUtils;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.testing.Utils;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Christophe Jeunesse on 22/07/2015.
 */
public class ElasticSearchIntegrationTest {

    @DocumentEntity(domain="testEs",version = "1.0")
    public static class TestDoc extends CouchbaseDocument {
        @DocumentProperty("strVal")
        public String strVal;

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
        public String getKeyRawPattern() {
            return TEST_KEY_PATTERN;
        }

        @Override
        public String getKeyFromParams(Object... params) {
            return String.format(TEST_KEY_FMT, NumberUtils.asInt(params[0]));
        }

        public static class TestViewDao extends CouchbaseViewDao<String,String,TestDoc> {
            public TestViewDao(TestDao parentDao){
                super("test/","testView",parentDao);
            }

            @Override
            public String getContent() {
                return
                        "emit(meta.id,doc);\n"+
                                "emit(doc.strVal,doc.strVal);\n"+
                                "emit(doc.doubleVal,doc.doubleVal);\n"+
                                "emit(doc.intVal,doc.intVal);\n"+
                                "emit(doc.boolVal,doc.boolVal);\n"+
                                "emit(doc.longVal,doc.longVal);\n"+
                                "emit(doc.arrayVal,doc.arrayVal);\n";
            }

            @Override public IViewTranscoder<String> getValueTranscoder() {return IViewTranscoder.Utils.stringTranscoder();}
            @Override public IViewKeyTranscoder<String> getKeyTranscoder() {return IViewTranscoder.Utils.stringKeyTranscoder();}
        }

        @BucketDocumentForClass(TestDoc.class)
        public static class LocalBucketDocument extends BucketDocument<TestDoc> {
            public LocalBucketDocument(TestDoc obj){super(obj);}
        }

        @Override
        public Class<? extends BucketDocument<TestDoc>> getBucketDocumentClass() {
            return LocalBucketDocument.class;
        }

        @Override
        public List<CouchbaseViewDao> generateViewDaos(){
            return Arrays.asList(
                    new TestViewDao(this)
            );
        }


        @Override
        public List<CouchbaseCounterDao.Builder> getCountersBuilder() {
            return Arrays.asList(
                    new CouchbaseCounterDao.Builder().withKeyPattern(TEST_CNT_KEY_PATTERN).withBaseDao(this)
            );
        }
        @Override
        public Observable<TestDoc> asyncBuildKey(ICouchbaseSession session, TestDoc newObject) throws DaoException {
            return session.asyncIncrCounter(TEST_CNT_KEY, 1)
                    .map(cntVal->{newObject.getBaseMeta().setKey(String.format(TEST_KEY_FMT, cntVal));return newObject;});
        }
    }

    Utils.TestEnvironment env;
    @Before
    public void initTest() throws  Exception{
        env = new Utils.TestEnvironment("ViewTests", Utils.TestEnvironment.TestEnvType.COUCHBASE_ELASTICSEARCH);
        env.addDocumentDao(new TestDao(), TestDoc.class);
        env.start();
    }



    @Test
    public void test() throws Exception{
        ICouchbaseSession session = env.getSessionFactory().newReadWriteSession(null);
        for(int i=0;i<10;++i){
            TestDoc doc = session.newEntity(TestDoc.class);
            doc.strVal="test "+i;
            doc.doubleVal=i*1.1;
            doc.longVal=i+1L;
            doc.intVal=i;
            doc.boolVal= ((i % 2) == 0);
            doc.arrayVal = new ArrayList<>(i);
            for(int j=0;j<i;++j){
                TestDoc.SubElem elem=new TestDoc.SubElem();
                elem.longVal=j+1L;
                doc.arrayVal.add(elem);
            }
            session.toBlocking().blockingSave(doc);
        }

        //_env.getEsServer()
        //Thread.sleep(10000);
        IElasticSearchSession esSession = env.getEsSessionFactory().newSession(null);
        env.fullElasticSearchReSync();

        ElasticSearchResult<TestDoc> result = esSession.newElasticSearchQuery(TestDoc.class).setQuery(QueryBuilders.matchAllQuery()).setSize(20).search();
        //result.getTotalHitCount();
        assertEquals(10,result.getTotalHitCount());
    }

    @After
    public void after(){
        env.shutdown(true);
    }

}
