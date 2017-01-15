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
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDao;
import com.dreameddeath.core.notification.model.v1.Notification;
import io.reactivex.Single;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 01/07/2016.
 */
@DaoForClass(Notification.class)
public class NotificationDao extends CouchbaseDocumentWithKeyPatternDao<Notification> {
    public static final String NOTIFICATION_UID_NAMESPACE="core/notification/id";
    public static final String FMT_KEY=EventDao.EVENT_FMT_KEY+"/notif/%s";
    public static final String PATTERN_KEY=EventDao.EVENT_KEY_PATTERN+"/notif/{nid:[^/]+}";
    public static final String NOTIFICATION_CNT_FMT=EventDao.EVENT_FMT_KEY+"/notifcnt";
    public static final String NOTIFICATION_CNT_PATTERN =EventDao.EVENT_KEY_PATTERN+"/notifcnt";


    @BucketDocumentForClass(Notification.class)
    public static class LocalBucketDocument extends BucketDocument<Notification> {
        public LocalBucketDocument(Notification obj){super(obj);}
    }

    @Override
    public List<CouchbaseCounterDao.Builder> getCountersBuilder() {
        List<CouchbaseCounterDao.Builder> result = new ArrayList<>();
        result.add(
                new CouchbaseCounterDao.Builder().withKeyPattern(NOTIFICATION_CNT_PATTERN).withBaseDao(this)
        );
        return result;
    }


    @Override
    public List<CouchbaseUniqueKeyDao.Builder> getUniqueKeysBuilder() {
        return Arrays.asList(
                CouchbaseUniqueKeyDao.builder().withBaseDao(this).withNameSpace(NOTIFICATION_UID_NAMESPACE)
        );
    }

    @Override
    protected String getKeyRawPattern() {
        return PATTERN_KEY;
    }

    @Override
    protected Notification updateTransientFromKeyPattern(Notification obj, String... params) {
        obj.setEventId(UUID.fromString(params[0]));
        obj.setId(Long.valueOf(params[1]));
        return obj;
    }

    @Override
    public String getKeyFromParams(Object... params) {
        return String.format(FMT_KEY,
                params[0],
                params[1]
        );
    }

    @Override
    public Single<Notification> asyncBuildKey(ICouchbaseSession session, final Notification newObject) throws DaoException {
        return session.asyncIncrCounter(String.format(NOTIFICATION_CNT_FMT,newObject.getEventId()),1)
                .map(newId->{
                    newObject.getBaseMeta().setKey(getKeyFromParams(newObject.getEventId(),newId));
                    return newObject;
                });
        /*newObject.getBaseMeta().setKey(getKeyFromParams(newObject.getEventId(),newObject.getId()));
        return Observable.just(newObject)*/
    }

    @Override
    public Class<? extends BucketDocument<Notification>> getBucketDocumentClass() {
        return LocalBucketDocument.class;
    }
}
