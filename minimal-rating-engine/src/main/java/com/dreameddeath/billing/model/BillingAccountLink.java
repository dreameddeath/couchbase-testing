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
    private String _uid;
	
    @JsonProperty("uid")
    public String getUid() { return _uid; }
    public void setUid(String uid) { _uid=uid; }
    
    public BillingAccountLink(){}
    
    @JsonIgnore
    public BillingAccountLink (BillingAccount ba){
        super(ba);
        setUid(ba.getUid());
    }
    
    @Override
    public String toString(){
        String result = "{\n"+super.toString()+",\n";
        result+="uid : "+getUid()+",\n";
        result+="}\n";
        return result;
    }
}