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

package com.dreameddeath.core.notification.dao;

import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.couchbase.annotation.BucketDocumentForClass;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.notification.model.v1.Event;
import io.reactivex.Single;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 01/07/2016.
 */
@DaoForClass(Event.class)
public class EventDao extends CouchbaseDocumentDaoWithUID<Event> {
    public static final String EVENT_FMT_KEY ="event/%s";
    public static final String EVENT_KEY_PATTERN ="event/{uid}";

    @BucketDocumentForClass(Event.class)
    public static class LocalBucketDocument extends BucketDocument<Event> {
        public LocalBucketDocument(Event obj){super(obj);}
    }

    @Override
    protected Event updateTransientFromKeyPattern(Event obj, String... keyParams) {
        obj.setId(UUID.fromString(keyParams[0]));
        return obj;
    }

    @Override
    public String getKeyFromParams(Object... params) {
        return String.format(EVENT_FMT_KEY, params[0].toString());
    }

    @Override
    public Class<? extends BucketDocument<Event>> getBucketDocumentClass() { return LocalBucketDocument.class; }


    @Override
    public Single<Event> asyncBuildKey(ICouchbaseSession session, Event newObject) throws DaoException {
        newObject.getBaseMeta().setKey(getKeyFromParams(newObject.getId()));
        return Single.just(newObject);
    }

    @Override
    public String getKeyRawPattern(){
        return EVENT_KEY_PATTERN;
    }

    @Override
    public final boolean isKeySharedAcrossDomains(){
        return true;
    }

}
