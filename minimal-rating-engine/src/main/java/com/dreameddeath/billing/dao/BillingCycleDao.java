package com.dreameddeath.billing.dao;

import com.dreameddeath.billing.model.cycle.BillingCycle;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.storage.CouchbaseClientWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import com.dreameddeath.rating.model.context.RatingContext;
import net.spy.memcached.transcoders.Transcoder;

import java.util.ArrayList;
import java.util.List;

public class BillingCycleDao extends CouchbaseDocumentDao<BillingCycle> {
    public static final String BA_CYCLE_CNT_KEY="%s/cycle/cnt";
    public static final String BA_CYCLE_FMT_KEY="%s/cycle/%d";
    public static final String BA_CYCLE_KEY_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/cycle/\\d+";
    public static final String BA_CYCLE_CNT_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/cycle/cnt";

    private static GenericJacksonTranscoder<BillingCycle> _tc = new GenericJacksonTranscoder<BillingCycle>(BillingCycle.class);
    
    public  Transcoder<BillingCycle> getTranscoder(){
        return _tc;
    }
    
    public BillingCycleDao(CouchbaseClientWrapper client,CouchbaseDocumentDaoFactory factory){
        super(client,factory);
        registerCounterDao(new CouchbaseCounterDao.Builder().withKeyPattern(BA_CYCLE_CNT_PATTERN).withDefaultValue(1L));
    }
    
    public void buildKey(BillingCycle obj) throws DaoException,StorageException{
        long result = obj.getSession().incrCounter(String.format(BA_CYCLE_CNT_KEY,obj.getBillingAccountLink().getKey()),1);
        obj.setKey(String.format(BA_CYCLE_FMT_KEY,obj.getBillingAccountLink().getKey(),result));
        getDaoFactory().getDaoForClass(RatingContext.class).buildKeysForLinks(obj.getRatingContextLinks());
    }
    
    public String getKeyPattern(){
        return BA_CYCLE_KEY_PATTERN;
    }
}