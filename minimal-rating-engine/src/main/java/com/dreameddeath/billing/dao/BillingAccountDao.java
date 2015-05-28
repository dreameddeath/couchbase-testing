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

import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.core.couchbase.CouchbaseBucketWrapper;
import com.dreameddeath.core.couchbase.GenericJacksonTranscoder;
import com.dreameddeath.core.couchbase.GenericTranscoder;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.business.CouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.BaseCouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.exception.dao.DaoException;
import com.dreameddeath.core.model.document.BucketDocument;

public class BillingAccountDao extends CouchbaseDocumentDaoWithUID<BillingAccount> {
    public static final String BA_CNT_KEY="ba/cnt";
    public static final String BA_CNT_KEY_PATTERN="ba/cnt";
    public static final String BA_FMT_KEY="ba/%010d";
    public static final String BA_FMT_UID="%010d";
    public static final String BA_KEY_PATTERN="ba/\\d{10}";


    public static class LocalBucketDocument extends BucketDocument<BillingAccount> {
        public LocalBucketDocument(BillingAccount obj){super(obj);}
    }

    private static GenericJacksonTranscoder<BillingAccount> _tc = new GenericJacksonTranscoder<BillingAccount>(BillingAccount.class,LocalBucketDocument.class);

    @Override
    public GenericTranscoder<BillingAccount> getTranscoder(){
        return _tc;
    }
    
    public BillingAccountDao(CouchbaseBucketWrapper client,BaseCouchbaseDocumentDaoFactory factory){
        super(client,factory);
        registerCounterDao(new CouchbaseCounterDao.Builder().withKeyPattern(BA_CNT_KEY_PATTERN).withDefaultValue(1L));
    }

    @Override
    public void buildKey(BillingAccount obj) throws DaoException,StorageException{
        long result = obj.getBaseMeta().getSession().incrCounter(BA_CNT_KEY, 1);
        obj.getBaseMeta().setKey(String.format(BA_FMT_KEY, result));
        if(obj.getUid()==null){
            obj.setUid(String.format(BA_FMT_UID,result));
        }
    }

    @Override
    public String getKeyPattern(){
        return BA_KEY_PATTERN;
    }

    @Override
    public String getKeyFromUID(String uid){return String.format(BA_FMT_KEY,Long.parseLong(uid));}
}