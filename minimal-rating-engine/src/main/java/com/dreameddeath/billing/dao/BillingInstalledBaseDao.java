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

package com.dreameddeath.billing.dao;


import com.dreameddeath.billing.model.installedbase.BillingInstalledBase;
import com.dreameddeath.core.couchbase.CouchbaseBucketWrapper;
import com.dreameddeath.core.couchbase.GenericJacksonTranscoder;
import com.dreameddeath.core.couchbase.GenericTranscoder;
import com.dreameddeath.core.dao.business.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.BaseCouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.exception.dao.DaoException;
import com.dreameddeath.core.model.document.BucketDocument;


/**
 * Created by Christophe Jeunesse on 12/08/2014.
 */
public class BillingInstalledBaseDao extends CouchbaseDocumentDao<BillingInstalledBase> {
    public static final String BA_BASE_CNT_KEY="%s/base/cnt";
    public static final String BA_BASE_FMT_KEY="%s/base/%d";
    public static final String BA_BASE_KEY_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/base/\\d+";
    public static final String BA_BASE_CNT_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/base/cnt";

    public static class LocalBucketDocument extends BucketDocument<BillingInstalledBase> {
        public LocalBucketDocument(BillingInstalledBase baInstBase){super(baInstBase);}
    }

    private static GenericJacksonTranscoder<BillingInstalledBase> _tc = new GenericJacksonTranscoder<BillingInstalledBase>(BillingInstalledBase.class,LocalBucketDocument.class);

    public GenericTranscoder<BillingInstalledBase> getTranscoder(){ return _tc; }

    public BillingInstalledBaseDao(CouchbaseBucketWrapper client,BaseCouchbaseDocumentDaoFactory factory){
        super(client,factory);
        registerCounterDao(new CouchbaseCounterDao.Builder().withKeyPattern(BA_BASE_CNT_PATTERN).withDefaultValue(1L));
    }

    public void buildKey(BillingInstalledBase obj) throws DaoException{
        long result = obj.getBaseMeta().getSession().incrCounter(String.format(BA_BASE_CNT_KEY, obj.getBaLink().getKey()), 1);
        obj.getBaseMeta().setKey(String.format(BA_BASE_FMT_KEY, obj.getBaLink().getKey(), result));
    }

    public String getKeyPattern(){
        return BA_BASE_KEY_PATTERN;
    }
}
