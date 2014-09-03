package com.dreameddeath.billing.dao;

import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.billing.model.cycle.BillingCycle;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.storage.CouchbaseClientWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import net.spy.memcached.transcoders.Transcoder;

import java.util.ArrayList;
import java.util.List;

public class BillingAccountDao extends CouchbaseDocumentDaoWithUID<BillingAccount> {
    public static final String BA_CNT_KEY="ba/cnt";
    public static final String BA_CNT_KEY_PATTERN="ba/cnt";
    public static final String BA_FMT_KEY="ba/%010d";
    public static final String BA_FMT_UID="%010d";
    public static final String BA_KEY_PATTERN="ba/\\d{10}";
    
    private static GenericJacksonTranscoder<BillingAccount> _tc = new GenericJacksonTranscoder<BillingAccount>(BillingAccount.class);

    @Override
    public  Transcoder<BillingAccount> getTranscoder(){
        return _tc;
    }
    
    public BillingAccountDao(CouchbaseClientWrapper client,CouchbaseDocumentDaoFactory factory){
        super(client,factory);
        registerCounterDao(new CouchbaseCounterDao.Builder().withKeyPattern(BA_CNT_KEY_PATTERN).withDefaultValue(1L));
    }

    @Override
    public void buildKey(BillingAccount obj) throws DaoException,StorageException{
        long result = obj.getSession().incrCounter(BA_CNT_KEY,1);
        obj.setKey(String.format(BA_FMT_KEY,result));
        if(obj.getUid()==null){
            obj.setUid(String.format(BA_FMT_UID,result));
        }
        getDaoFactory().getDaoForClass(BillingCycle.class).buildKeysForLinks(obj.getBillingCycleLinks());
    }

    @Override
    public String getKeyPattern(){
        return BA_KEY_PATTERN;
    }

    @Override
    public String getKeyFromUID(String uid){return String.format(BA_FMT_KEY,Long.parseLong(uid));}
}