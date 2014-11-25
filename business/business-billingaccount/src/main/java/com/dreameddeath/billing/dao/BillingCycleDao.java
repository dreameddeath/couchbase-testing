package com.dreameddeath.billing.dao;

import com.dreameddeath.billing.model.cycle.BillingCycle;
import com.dreameddeath.core.dao.business.BusinessCouchbaseDocumentDao;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.session.ICouchbaseSession;
import com.dreameddeath.core.storage.BucketDocument;


public class BillingCycleDao extends BusinessCouchbaseDocumentDao<BillingCycle> {
    public static final String BA_CYCLE_CNT_KEY="%s/cycle/cnt";
    public static final String BA_CYCLE_FMT_KEY="%s/cycle/%d";
    public static final String BA_CYCLE_KEY_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/cycle/\\d+";
    public static final String BA_CYCLE_CNT_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/cycle/cnt";

    public static class LocalBucketDocument extends BucketDocument<BillingCycle> {
        public LocalBucketDocument(BillingCycle obj){super(obj);}
    }

    @Override
    public Class<? extends BucketDocument<BillingCycle>> getBucketDocumentClass() {
        return LocalBucketDocument.class;
    }

    @Override
    public BillingCycle buildKey(ICouchbaseSession session,BillingCycle obj) throws DaoException,StorageException{
        long result = session.incrCounter(String.format(BA_CYCLE_CNT_KEY, obj.getBillingAccountLink().getKey()), 1);
        obj.getBaseMeta().setKey(String.format(BA_CYCLE_FMT_KEY, obj.getBillingAccountLink().getKey(), result));
        return obj;
    }

    @Override
    public String getKeyPattern(){
        return BA_CYCLE_KEY_PATTERN;
    }
}