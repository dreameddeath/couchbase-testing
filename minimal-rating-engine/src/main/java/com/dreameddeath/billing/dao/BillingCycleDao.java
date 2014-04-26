package com.dreameddeath.billing.dao;

import com.dreameddeath.billing.model.BillingCycle;
import com.dreameddeath.rating.model.context.AbstractRatingContext;

import com.dreameddeath.common.dao.CouchbaseDocumentDao;
import com.dreameddeath.common.dao.CouchbaseDocumentDaoFactory;

import net.spy.memcached.transcoders.Transcoder;
import com.dreameddeath.common.storage.GenericJacksonTranscoder;
import com.dreameddeath.common.storage.CouchbaseClientWrapper;

public class BillingCycleDao extends CouchbaseDocumentDao<BillingCycle> {
    public static final String BA_CYCLE_CNT_KEY="%s/cycle/cnt";
    public static final String BA_CYCLE_FMT_KEY="%s/cycle/%d";
    public static final String BA_CYCLE_KEY_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/cycle/\\d+";
    
    
    private static GenericJacksonTranscoder<BillingCycle> _tc = new GenericJacksonTranscoder<BillingCycle>(BillingCycle.class);
    
    public  Transcoder<BillingCycle> getTranscoder(){
        return _tc;
    }
    
    public BillingCycleDao(CouchbaseClientWrapper client,CouchbaseDocumentDaoFactory factory){
        super(client,factory);
    }
    
    public void buildKey(BillingCycle obj){
        long result = getClientWrapper().getClient().incr(String.format(BA_CYCLE_CNT_KEY,obj.getBillingAccountLink().getKey()),1,1,0);
        obj.setKey(String.format(BA_CYCLE_FMT_KEY,obj.getBillingAccountLink().getKey(),result));
        
        getDaoFactory().getDaoForClass(AbstractRatingContext.class).buildKeysForLinks(obj.getRatingContextsLinks());
    }
    
    public String getKeyPattern(){
        return BA_CYCLE_KEY_PATTERN;
    }
}