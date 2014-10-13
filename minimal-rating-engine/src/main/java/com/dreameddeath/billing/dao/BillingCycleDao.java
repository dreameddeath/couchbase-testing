package com.dreameddeath.billing.dao;

import com.dreameddeath.billing.model.cycle.BillingCycle;
import com.dreameddeath.core.dao.common.BaseCouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.common.BucketDocument;
import com.dreameddeath.core.storage.CouchbaseBucketWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import com.dreameddeath.core.storage.GenericTranscoder;


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