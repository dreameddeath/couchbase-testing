package com.dreameddeath.billing.dao;

import com.dreameddeath.billing.model.BillingCycle;
import com.dreameddeath.common.dao.CouchbaseDocumentDao;

import net.spy.memcached.transcoders.Transcoder;
import com.dreameddeath.common.storage.GenericJacksonTranscoder;
import com.dreameddeath.common.storage.CouchbaseClientWrapper;

public class BillingCycleDao extends CouchbaseDocumentDao<BillingCycle> {
    public static final String BA_CYCLE_CNT_KEY="%s/cycle/cnt";
    public static final String BA_CYCLE_FMT_KEY="%s/cycle/%d";
    
    private static GenericJacksonTranscoder<BillingCycle> _tc = new GenericJacksonTranscoder<BillingCycle>(BillingCycle.class);
    
    public  Transcoder<BillingCycle> getTranscoder(){
        return _tc;
    }
    
    public BillingCycleDao(CouchbaseClientWrapper client){
        super(client);
    }
    
    public void buildKey(BillingCycle obj){
        long result = getClientWrapper().getClient().incr(String.format(BA_CYCLE_CNT_KEY,obj.getBillingAccountLink().getKey()),1,1,0);
        obj.setKey(String.format(BA_CYCLE_FMT_KEY,obj.getBillingAccountLink().getKey(),result));
    }
}