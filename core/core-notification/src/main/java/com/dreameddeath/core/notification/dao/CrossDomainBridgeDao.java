/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.notification.dao;

import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.couchbase.annotation.BucketDocumentForClass;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.annotation.ParentDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.notification.model.v1.CrossDomainBridge;
import io.reactivex.Single;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 01/07/2016.
 */
@DaoForClass(CrossDomainBridge.class) @ParentDao(EventDao.class)
public class CrossDomainBridgeDao extends CouchbaseDocumentWithKeyPatternDao<CrossDomainBridge> {
    public static final String NOTIFICATION_UID_NAMESPACE="core/crossdomainbridge/id";
    public static final String FMT_KEY=EventDao.EVENT_FMT_KEY+"/xdomain/%s";
    public static final String PATTERN_KEY=EventDao.EVENT_KEY_PATTERN+"/xdomain/{dom:[^/]+}";


    @BucketDocumentForClass(CrossDomainBridge.class)
    public static class LocalBucketDocument extends BucketDocument<CrossDomainBridge> {
        public LocalBucketDocument(CrossDomainBridge obj){super(obj);}
    }

    @Override
    public final boolean isKeySharedAcrossDomains() {
        return true;
    }

    @Override
    protected String getKeyRawPattern() {
        return PATTERN_KEY;
    }

    @Override
    protected CrossDomainBridge updateTransientFromKeyPattern(CrossDomainBridge obj, String... params) {
        obj.setEventId(UUID.fromString(params[0]));
        obj.setTargetDomain(String.valueOf(params[1]));
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
    public Single<CrossDomainBridge> asyncBuildKey(ICouchbaseSession session, final CrossDomainBridge newObject) throws DaoException {
        newObject.getBaseMeta().setKey(getKeyFromParams(newObject.getEventId(),newObject.getTargetDomain()));
        return Single.just(newObject);

    }

    @Override
    public Class<? extends BucketDocument<CrossDomainBridge>> getBucketDocumentClass() {
        return LocalBucketDocument.class;
    }
}
