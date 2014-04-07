package com.dreameddeath.billing.model;

import java.util.Collection;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIgnore;


import com.dreameddeath.common.model.CouchbaseDocumentLink;




@JsonInclude(Include.NON_EMPTY)
public class BillingAccountLink extends CouchbaseDocumentLink<BillingAccount>{
    /*import net.spy.memcached.transcoders.Transcoder;
    import com.dreameddeath.common.storage.GenericJacksonTranscoder;
    private static GenericJacksonTranscoder<BillingAccount> _tc = new GenericJacksonTranscoder<BillingAccount>(BillingAccount.class);
    @JsonIgnore
    public  Transcoder<BillingAccount> getTranscoder(){
        return _tc;
    }
    */
    private String _uid;
	
    @JsonProperty("uid")
    public String getUid() { return _uid; }
    public void setUid(String uid) { _uid=uid; }
    
    @JsonIgnore
    public static BillingAccountLink buildLink(BillingAccount ba){
        BillingAccountLink newLink = new BillingAccountLink();
        newLink.setKey(ba.getKey());
        newLink.setUid(ba.getUid());
        newLink.setLinkedObject(ba);
        return newLink;
    }
    
    @Override
    public String toString(){
        String result = "{\n"+super.toString()+",\n";
        result+="uid : "+getUid()+",\n";
        result+="}\n";
        return result;
    }
}