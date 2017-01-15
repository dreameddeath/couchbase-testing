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

package com.dreameddeath.core.dao.archive;

import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import io.reactivex.Single;

/**
 * Created by Christophe Jeunesse on 17/09/2014.
 */
public abstract class CouchbaseArchiveDao<T extends CouchbaseDocument> extends CouchbaseDocumentDao<T> {
    public static String BASE_PATTERN_FMT ="arch/%s";
    public static String BASE_PATTERN ="arch/";
    private CouchbaseDocumentDao<T> refDao;

    public void setRefDao(CouchbaseDocumentDao<T> refDao){this.refDao = refDao;}
    public CouchbaseDocumentDao<T> getRefDao(){return refDao;}


    @Override
    public Class<? extends BucketDocument<T>> getBucketDocumentClass() {
        return refDao.getBucketDocumentClass();
    }

    @Override
    public Single<T> asyncBuildKey(ICouchbaseSession session, T newObject) throws DaoException {
        String key = String.format(BASE_PATTERN_FMT,newObject.getBaseMeta().getKey());
        newObject.getBaseMeta().setKey(key);
        return Single.just(newObject);
    }
}
