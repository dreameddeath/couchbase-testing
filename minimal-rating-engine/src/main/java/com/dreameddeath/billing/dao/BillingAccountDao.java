package com.dreameddeath.billing.dao;

import com.dreameddeath.billing.model.BillingAccount;
import com.dreameddeath.billing.model.BillingCycle;
import com.dreameddeath.common.dao.CouchbaseDocumentDao;
import com.dreameddeath.common.dao.CouchbaseDocumentDaoFactory;

import net.spy.memcached.transcoders.Transcoder;
import com.dreameddeath.common.storage.GenericJacksonTranscoder;
import com.dreameddeath.common.storage.CouchbaseClientWrapper;

public class BillingAccountDao extends CouchbaseDocumentDao<BillingAccount> {
    public static final String BA_CNT_KEY="ba/cnt";
    public static final String BA_FMT_KEY="ba/%010d";
    public static final String BA_FMT_UID="%010d";
    
    private static GenericJacksonTranscoder<BillingAccount> _tc = new GenericJacksonTranscoder<BillingAccount>(BillingAccount.class);
    
    public  Transcoder<BillingAccount> getTranscoder(){
        return _tc;
    }
    
    public BillingAccountDao(CouchbaseClientWrapper client,CouchbaseDocumentDaoFactory factory){
        super(client,factory);
    }
    
    public void buildKey(BillingAccount obj){
        long result = getClientWrapper().getClient().incr(BA_CNT_KEY,1,1,0);
        obj.setKey(String.format(BA_FMT_KEY,result));
        if(obj.getUid()==null){
            obj.setUid(String.format(BA_FMT_UID,result));
        }
        
        getDaoFactory().getDaoFor(BillingCycle.class).buildKeysForLinks(obj.getBillingCycles());
    }
}