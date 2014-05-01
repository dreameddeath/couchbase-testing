package com.dreameddeath.billing.model;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import com.dreameddeath.common.model.CouchbaseDocument;
import com.dreameddeath.common.model.CouchbaseDocumentArrayList;
import com.dreameddeath.rating.model.context.RatingContextLink;
import com.dreameddeath.rating.model.context.AbstractRatingContext;
import com.dreameddeath.common.model.ImmutableProperty;

public class BillingCycle extends CouchbaseDocument{
    private ImmutableProperty<BillingAccountLink>  _baLink=new ImmutableProperty<BillingAccountLink>(BillingCycle.this);
    private DateTime _startDate;
	private DateTime _endDate;
	private List<RatingContextLink> _ratingContexts=new CouchbaseDocumentArrayList<RatingContextLink>(BillingCycle.this);
    
    @JsonProperty("ba")
    public BillingAccountLink getBillingAccountLink() { return _baLink.get(); }
    public void setBillingAccountLink(BillingAccountLink baLink) { _baLink.set(baLink); }
    public void setBillingAccount(BillingAccount ba){ ba.addBillingCycle(this); }
    
    @JsonProperty("startDate")
    public DateTime getStartDate() { return _startDate; }
    public void setStartDate(DateTime startDate) { _startDate=startDate; }
    
    @JsonProperty("endDate")
    public DateTime getEndDate() { return _endDate; }
    public void setEndDate(DateTime endDate) { _endDate=endDate; }
    
    @JsonProperty("ratingContexts")
    public List<RatingContextLink> getRatingContextLinks() { return Collections.unmodifiableList(_ratingContexts); }
    public void setRatingContextLinks(Collection<RatingContextLink> ratingCtxtLinks) { _ratingContexts.clear();_ratingContexts.addAll(ratingCtxtLinks); }
    public void addRatingContext(AbstractRatingContext ratingCtxt){  
        if(_ratingContexts.add(ratingCtxt.newRatingContextLink())){
            ratingCtxt.setBillingCycleLink(newBillingCycleLink());
        }
    }
    
    public BillingCycleLink newBillingCycleLink(){
        return new BillingCycleLink(this);
    }
    
    public boolean isValidForDate(DateTime refDate){
        return BillingCycle.isValidForDate(refDate,_startDate,_endDate);
    }
    
    public static boolean isValidForDate(DateTime refDate, DateTime startTime,DateTime endTime){
        if((refDate.compareTo(startTime)>=0) && (refDate.compareTo(endTime)<0)){
            return true;
        }
        return false;
    }
    
    @Override
    public String toString(){
        String result= super.toString()+",\n";
        result+="startDate:"+_startDate+",\n";
        result+="startend:"+_endDate+",\n";
        result+="ratingContexts:"+_ratingContexts+"\n";
        return result;
    }
}