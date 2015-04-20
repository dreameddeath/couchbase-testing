/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.dao;

import com.dreameddeath.core.annotation.dao.DaoForClass;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.dao.view.CouchbaseViewDao;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.view.IViewKeyTranscoder;
import com.dreameddeath.core.model.view.IViewTranscoder;
import com.dreameddeath.core.model.view.impl.ViewStringKeyTranscoder;
import com.dreameddeath.core.model.view.impl.ViewStringTranscoder;
import com.dreameddeath.core.session.ICouchbaseSession;
import com.dreameddeath.core.storage.BucketDocument;

import java.util.Arrays;
import java.util.List;

/**
 * Created by CEAJ8230 on 14/04/2015.
 */
@DaoForClass(TestDocChild.class)
public class TestChildDao extends CouchbaseDocumentWithKeyPatternDao<TestDocChild> {
    public static final String TEST_CHILD_CNT_KEY = "%s/child/cnt";
    public static final String TEST_CHILD_CNT_KEY_PATTERN = "test/\\d{10}/child/cnt";
    public static final String TEST_CHILD_KEY_FMT = "%s/child/%05d";
    public static final String TEST_CHILD_KEY_PATTERN = "test/\\d{10}/child/\\d{5}";

    @Override
    public String getKeyPattern() {
        return TEST_CHILD_KEY_PATTERN;
    }

    public static class LocalBucketDocument extends BucketDocument<TestDocChild> {
        public LocalBucketDocument(TestDocChild obj) {
            super(obj);
        }
    }

    @Override
    public Class<? extends BucketDocument<TestDocChild>> getBucketDocumentClass() {
        return LocalBucketDocument.class;
    }


    @Override
    public List<CouchbaseCounterDao.Builder> getCountersBuilder() {
        return Arrays.asList(
                new CouchbaseCounterDao.Builder().withKeyPattern(TEST_CHILD_CNT_KEY_PATTERN).withDefaultValue(1L).withBaseDao(this)
        );
    }

    @Override
    public TestDocChild buildKey(ICouchbaseSession session, TestDocChild newObject) throws DaoException, StorageException {
        long result = session.incrCounter(String.format(TEST_CHILD_CNT_KEY,newObject.parent.getKey()),1);
        newObject.getBaseMeta().setKey(String.format(TEST_CHILD_KEY_FMT,newObject.parent.getKey(), result));

        return newObject;
    }



    @Override
    public List<CouchbaseViewDao> generateViewDaos() {
        return Arrays.asList(
                new AllElementsViewDao(this)
        );
    }

    public static class AllElementsViewDao extends CouchbaseViewDao<String,String,TestDocChild>{
        private static final IViewKeyTranscoder<String> KEY_TRANSCODER = new ViewStringKeyTranscoder();
        private static final IViewTranscoder<String> VALUE_TRANSCODER = new ViewStringTranscoder();

        public AllElementsViewDao(TestChildDao parentDao){
            super("test","all_testChild",parentDao);
        }

        @Override
        public String getContent() {
            return "emit(doc.parent.key,null);";
        }

        @Override public IViewKeyTranscoder<String> getKeyTranscoder(){
            return KEY_TRANSCODER;
        }
        @Override public IViewTranscoder<String> getValueTranscoder(){
            return VALUE_TRANSCODER;
        }
    }

}
