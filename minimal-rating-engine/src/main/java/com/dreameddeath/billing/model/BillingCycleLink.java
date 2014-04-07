package com.dreameddeath.billing.model;

import java.util.Collection;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.dreameddeath.common.model.CouchbaseDocumentLink;

import com.dreameddeath.rating.model.context.AbstractRatingContext;

@JsonInclude(Include.NON_EMPTY)
public class BillingCycleLink extends CouchbaseDocumentLink<BillingCycle>{
    private DateTime _startDate;
	private DateTime _endDate;
	
    @JsonProperty("startDate")
    public DateTime getStartDate() { return _startDate; }
    public void setStartDate(DateTime startDate) { _startDate=startDate; }
    
    @JsonProperty("endDate")
    public DateTime getEndDate() { return _endDate; }
    public void setEndDate(DateTime endDate) { _endDate=endDate; }
    
    public BillingCycleLink(){}
    
    public BillingCycleLink(BillingCycle billCycle){
        super(billCycle);
        setKey(billCycle.getKey());
        setStartDate(billCycle.getStartDate());
        setEndDate(billCycle.getEndDate());
        setLinkedObject(billCycle);
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