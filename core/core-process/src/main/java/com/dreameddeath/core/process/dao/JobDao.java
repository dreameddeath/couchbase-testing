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

package com.dreameddeath.core.process.dao;


import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.couchbase.annotation.BucketDocumentForClass;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.process.model.AbstractJob;
import rx.Observable;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
@DaoForClass(AbstractJob.class)
public class JobDao extends CouchbaseDocumentDaoWithUID<AbstractJob> {
    public static final String JOB_FMT_KEY="job/%s";
    public static final String JOB_KEY_PATTERN="job/{uid}";

    @BucketDocumentForClass(AbstractJob.class)
    public static class LocalBucketDocument extends BucketDocument<AbstractJob> {
        public LocalBucketDocument(AbstractJob obj){super(obj);}
    }

    @Override
    protected AbstractJob updateTransientFromKeyPattern(AbstractJob obj, String... keyParams) {
        obj.setUid(UUID.fromString(keyParams[0]));
        return obj;
    }

    @Override
    public String getKeyFromParams(Object... params) {
        return String.format(JOB_FMT_KEY, params[0].toString());
    }

    @Override
    public Class<? extends BucketDocument<AbstractJob>> getBucketDocumentClass() { return LocalBucketDocument.class; }


    @Override
    public Observable<AbstractJob> asyncBuildKey(ICouchbaseSession session, AbstractJob newObject) throws DaoException {
        newObject.getBaseMeta().setKey(getKeyFromParams(newObject.getUid()));
        return Observable.just(newObject);
    }

    @Override
    public String getKeyRawPattern(){
        return JOB_KEY_PATTERN;
    }

}
