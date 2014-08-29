package com.dreameddeath.billing.dao;

import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.billing.model.cycle.BillingCycle;
import com.dreameddeath.core.dao.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.CouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.storage.CouchbaseClientWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import net.spy.memcached.transcoders.Transcoder;

public class BillingAccountDao extends CouchbaseDocumentDaoWithUID<BillingAccount> {
    public static final String BA_CNT_KEY="ba/cnt";
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
    }

    @Override
    public void buildKey(BillingAccount obj){
        long result = getClientWrapper().getClient().incr(BA_CNT_KEY,1,1,0);
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