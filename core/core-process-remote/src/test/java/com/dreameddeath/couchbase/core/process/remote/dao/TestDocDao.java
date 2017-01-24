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

package com.dreameddeath.couchbase.core.process.remote.dao;

import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.couchbase.annotation.BucketDocumentForClass;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.couchbase.core.process.remote.model.TestDoc;
import io.reactivex.Single;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Christophe Jeunesse on 23/02/2016.
 */
@DaoForClass(TestDoc.class)
public class TestDocDao extends CouchbaseDocumentWithKeyPatternDao<TestDoc> {
    public static AtomicInteger cnt = new AtomicInteger();

    @Override
    public boolean isKeySharedAcrossDomains() {
        return false;
    }

    @BucketDocumentForClass(TestDoc.class)
    public static class LocalBucketDocument extends BucketDocument<TestDoc> {
        public LocalBucketDocument(TestDoc obj){super(obj);}
    }

    @Override
    public Single<TestDoc> asyncBuildKey(ICouchbaseSession session, final TestDoc newObject) throws DaoException {
        newObject.getBaseMeta().setKey(getKeyFromParams(cnt.incrementAndGet()));
        return Single.just(newObject);
    }

    @Override
    public Class<? extends BucketDocument<TestDoc>> getBucketDocumentClass() {
        return LocalBucketDocument.class;
    }


    @Override
    protected String getKeyRawPattern() {
        return "testdoc/{tid}";
    }

    @Override
    public String getKeyFromParams(Object... params) {
        return String.format("testdoc/%d",(Integer)params[0]);
    }
}