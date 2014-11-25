package com.dreameddeath.billing.dao;

import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.core.annotation.dao.DaoForClass;
import com.dreameddeath.core.dao.business.BusinessCouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.session.ICouchbaseSession;
import com.dreameddeath.core.storage.BucketDocument;

import java.util.Arrays;
import java.util.List;


@DaoForClass(BillingAccount.class)
public class BillingAccountDao extends BusinessCouchbaseDocumentDaoWithUID<BillingAccount> {
    public static final String BA_CNT_KEY="ba/cnt";
    public static final String BA_CNT_KEY_PATTERN="ba/cnt";
    public static final String BA_FMT_KEY="ba/%010d";
    public static final String BA_FMT_UID="%010d";
    public static final String BA_KEY_PATTERN="ba/\\d{10}";

    public static class LocalBucketDocument extends BucketDocument<BillingAccount> {
        public LocalBucketDocument(BillingAccount obj){super(obj);}
    }



    @Override
    public Class<? extends BucketDocument<BillingAccount>> getBucketDocumentClass() {
        return LocalBucketDocument.class;
    }

    @Override
    public List<CouchbaseCounterDao.Builder> getCountersBuilder() {
        return Arrays.asList(
                new CouchbaseCounterDao.Builder().withKeyPattern(BA_CNT_KEY_PATTERN).withDefaultValue(1L).withBaseDao(this)
        );
    }

    @Override
    public BillingAccount buildKey(ICouchbaseSession session,BillingAccount obj) throws DaoException,StorageException{
        long result = session.incrCounter(BA_CNT_KEY, 1);
        obj.getBaseMeta().setKey(String.format(BA_FMT_KEY, result));
        if(obj.getUid()==null){
            obj.setUid(String.format(BA_FMT_UID,result));
        }
        return obj;
    }

    @Override
    public String getKeyPattern(){
        return BA_KEY_PATTERN;
    }

    @Override
    public String getKeyFromUID(String uid){return String.format(BA_FMT_KEY,Long.parseLong(uid));}
}