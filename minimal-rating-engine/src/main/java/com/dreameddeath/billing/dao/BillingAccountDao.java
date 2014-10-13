package com.dreameddeath.billing.dao;

import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.core.dao.common.BaseCouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.common.BucketDocument;
import com.dreameddeath.core.storage.CouchbaseBucketWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import com.dreameddeath.core.storage.GenericTranscoder;

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