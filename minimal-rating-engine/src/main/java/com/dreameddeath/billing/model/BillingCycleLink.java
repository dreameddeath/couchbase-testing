package com.dreameddeath.billing.model;

import java.util.Collection;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIgnore;

import net.spy.memcached.transcoders.Transcoder;


import com.dreameddeath.common.storage.GenericJacksonTranscoder;
import com.dreameddeath.common.storage.CouchbaseDocumentLink;

import com.dreameddeath.rating.model.context.AbstractRatingContext;

@JsonInclude(Include.NON_EMPTY)
public class BillingCycleLink extends CouchbaseDocumentLink<BillingCycle>{
    private static GenericJacksonTranscoder<BillingCycle> _tc = new GenericJacksonTranscoder<BillingCycle>(BillingCycle.class);
    @JsonIgnore
    public  Transcoder<BillingCycle> getTranscoder(){
        return _tc;
    }
    
    private DateTime _startDate;
	private DateTime _endDate;
	
    @JsonProperty("startDate")
    public DateTime getStartDate() { return _startDate; }
    public void setStartDate(DateTime startDate) { _startDate=startDate; }
    
    @JsonProperty("endDate")
    public DateTime getEndDate() { return _endDate; }
    public void setEndDate(DateTime endDate) { _endDate=endDate; }
    
    @JsonIgnore
    public static BillingCycleLink buildLink(BillingCycle billCycle){
        BillingCycleLink newLink = new BillingCycleLink();
        newLink.setKey(billCycle.getKey());
        newLink.setStartDate(billCycle.getStartDate());
        newLink.setEndDate(billCycle.getEndDate());
        newLink.setLinkedObject(billCycle);
        return newLink;
    }
    
    @Override
    public String toString(){
        String result = "{\n"+super.toString()+",\n";
        result+="startDate : "+getStartDate()+",\n";
        result+="endDate : "+getEndDate()+",\n";
        result+="}\n";
        return result;
    }
    
}