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

package com.dreameddeath.core.helper;

import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.couchbase.annotation.BucketDocumentForClass;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.model.view.IViewKeyTranscoder;
import com.dreameddeath.core.dao.model.view.IViewTranscoder;
import com.dreameddeath.core.dao.model.view.impl.ViewStringKeyTranscoder;
import com.dreameddeath.core.dao.model.view.impl.ViewStringTranscoder;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.view.CouchbaseViewDao;
import rx.Observable;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 14/04/2015.
 */
@DaoForClass(TestDoc.class)
public class TestDao extends CouchbaseDocumentWithKeyPatternDao<TestDoc> {
    public static final String TEST_CNT_KEY = "test/cnt";
    public static final String TEST_CNT_KEY_PATTERN = "test/cnt";
    public static final String TEST_KEY_FMT = "test/%010d";
    public static final String TEST_KEY_PATTERN = "test/{uid:\\d{10}}";

    @Override
    public String getKeyRawPattern() {
        return TEST_KEY_PATTERN;
    }

    @BucketDocumentForClass(TestDoc.class)
    public static class LocalBucketDocument extends BucketDocument<TestDoc> {
        public LocalBucketDocument(TestDoc obj) {
            super(obj);
        }
    }

    @Override
    public Class<? extends BucketDocument<TestDoc>> getBucketDocumentClass() {
        return LocalBucketDocument.class;
    }

    @Override
    public List<CouchbaseViewDao> generateViewDaos() {
        return Arrays.asList(
                new AllElementsViewDao(this),
                new TestViewDao(this)
        );
    }

    @Override
    public Class<TestDoc> getBaseClass() {
        return TestDoc.class;
    }

    @Override
    public List<CouchbaseCounterDao.Builder> getCountersBuilder() {
        return Arrays.asList(
                new CouchbaseCounterDao.Builder().withKeyPattern(TEST_CNT_KEY_PATTERN).withDefaultValue(1L).withBaseDao(this)
        );
    }

    @Override
    public String getKeyFromParams(Object... params) {
        return String.format(TEST_KEY_FMT, (Long)params[0]);
    }

    @Override
    public Observable<TestDoc> asyncBuildKey(ICouchbaseSession session, TestDoc newObject) throws DaoException {
        return session.asyncIncrCounter(TEST_CNT_KEY, 1)
                .map(new BuildKeyFromCounterFunc(newObject));
    }

    public static class AllElementsViewDao extends CouchbaseViewDao<String,String,TestDoc>{
        private static final IViewKeyTranscoder<String> KEY_TRANSCODER = new ViewStringKeyTranscoder();
        private static final IViewTranscoder<String> VALUE_TRANSCODER = new ViewStringTranscoder();

        public AllElementsViewDao(TestDao parentDao){
            super("test","all_test",parentDao);
        }

        @Override
        public String getContent() {
            return "emit(null,null);";
        }

        @Override public IViewKeyTranscoder<String> getKeyTranscoder(){
            return KEY_TRANSCODER;
        }
        @Override public IViewTranscoder<String> getValueTranscoder(){
            return VALUE_TRANSCODER;
        }
    }

}
