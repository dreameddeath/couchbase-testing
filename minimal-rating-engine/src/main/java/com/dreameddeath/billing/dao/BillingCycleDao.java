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

import com.dreameddeath.billing.model.cycle.BillingCycle;
import com.dreameddeath.core.couchbase.CouchbaseBucketWrapper;
import com.dreameddeath.core.couchbase.GenericJacksonTranscoder;
import com.dreameddeath.core.couchbase.GenericTranscoder;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.business.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.BaseCouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.exception.dao.DaoException;
import com.dreameddeath.core.model.document.BucketDocument;


public class BillingCycleDao extends CouchbaseDocumentDao<BillingCycle> {
    public static final String BA_CYCLE_CNT_KEY="%s/cycle/cnt";
    public static final String BA_CYCLE_FMT_KEY="%s/cycle/%d";
    public static final String BA_CYCLE_KEY_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/cycle/\\d+";
    public static final String BA_CYCLE_CNT_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/cycle/cnt";

    public static class LocalBucketDocument extends BucketDocument<BillingCycle> {
        public LocalBucketDocument(BillingCycle obj){super(obj);}
    }
    private static GenericJacksonTranscoder<BillingCycle> _tc = new GenericJacksonTranscoder<BillingCycle>(BillingCycle.class,LocalBucketDocument.class);
    
    public GenericTranscoder<BillingCycle> getTranscoder(){
        return _tc;
    }
    
    public BillingCycleDao(CouchbaseBucketWrapper client,BaseCouchbaseDocumentDaoFactory factory){
        super(client,factory);
        registerCounterDao(new CouchbaseCounterDao.Builder().withKeyPattern(BA_CYCLE_CNT_PATTERN).withDefaultValue(1L));
    }
    
    public void buildKey(BillingCycle obj) throws DaoException,StorageException{
        long result = obj.getBaseMeta().getSession().incrCounter(String.format(BA_CYCLE_CNT_KEY, obj.getBillingAccountLink().getKey()), 1);
        obj.getBaseMeta().setKey(String.format(BA_CYCLE_FMT_KEY, obj.getBillingAccountLink().getKey(), result));
    }
    
    public String getKeyPattern(){
        return BA_CYCLE_KEY_PATTERN;
    }
}