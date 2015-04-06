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

package com.dreameddeath.installedbase.dao;

import com.dreameddeath.core.dao.business.BusinessCouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.session.ICouchbaseSession;
import com.dreameddeath.core.storage.BucketDocument;
import com.dreameddeath.installedbase.model.common.InstalledBase;

/**
 * Created by ceaj8230 on 31/08/2014.
 */
public class InstalledBaseDao extends BusinessCouchbaseDocumentDaoWithUID<InstalledBase> {
    public static final String INSTALLED_BASE_FMT_UID="%010d";
    public static final String INSTALLED_BASE_CNT_KEY="instBase/cnt";
    public static final String INSTALLED_BASE_FMT_KEY="instBase/%010d";
    public static final String INSTALLED_BASE_KEY_PATTERN="instBase/\\d{10}";
    public static final String INSTALLED_BASE_CNT_PATTERN="instBase/cnt";

    public static class LocalBucketDocument extends BucketDocument<InstalledBase> {
        public LocalBucketDocument(InstalledBase obj){super(obj);}
    }

    @Override
    public Class<? extends BucketDocument<InstalledBase>> getBucketDocumentClass() {
        return LocalBucketDocument.class;
    }

    @Override
    public InstalledBase buildKey(ICouchbaseSession session, InstalledBase newObject) throws DaoException, StorageException {
        long result = session.incrCounter(INSTALLED_BASE_CNT_KEY, 1);
        newObject.setUid(String.format(INSTALLED_BASE_FMT_UID,result));
        newObject.getMeta().setKey(String.format(INSTALLED_BASE_FMT_KEY, result));
        return newObject;
    }


    @Override
    public String getKeyPattern(){
        return INSTALLED_BASE_KEY_PATTERN;
    }
    @Override
    public String getKeyFromUID(String uid){ return String.format(INSTALLED_BASE_FMT_KEY, Long.parseLong(uid));}
}
