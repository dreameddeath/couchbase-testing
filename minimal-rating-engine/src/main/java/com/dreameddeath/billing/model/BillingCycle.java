package com.dreameddeath.billing.model;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.dreameddeath.common.model.CouchbaseDocument;
import com.dreameddeath.common.model.CouchbaseDocumentArrayList;
import com.dreameddeath.rating.model.context.RatingContextLink;
import com.dreameddeath.rating.model.context.AbstractRatingContext;
import com.dreameddeath.common.model.ImmutableProperty;

public class BillingCycle extends CouchbaseDocument{
    @JsonProperty("ba")
    private ImmutableProperty<BillingAccountLink>  _baLink=new ImmutableProperty<BillingAccountLink>(BillingCycle.this);
    @JsonProperty("startDate")
    private DateTime _startDate;
	@JsonProperty("endDate")
    private DateTime _endDate;
	@JsonProperty("ratingContexts")
    private List<RatingContextLink> _ratingContexts=new CouchbaseDocumentArrayList<RatingContextLink>(BillingCycle.this);
    
    public BillingAccountLink getBillingAccountLink() { return _baLink.get(); }
    public void setBillingAccountLink(BillingAccountLink baLink) { _baLink.set(baLink); }
    public void setBillingAccount(BillingAccount ba){ ba.addBillingCycle(this); }
    
    public DateTime getStartDate() { return _startDate; }
    public void setStartDate(DateTime startDate) { _startDate=startDate; }
    
    public DateTime getEndDate() { return _endDate; }
    public void setEndDate(DateTime endDate) { _endDate=endDate; }
    
    public Collection<RatingContextLink> getRatingContextsLinks() { return Collections.unmodifiableList(_ratingContexts); }
    public void setRatingContextsLinks(Collection<RatingContextLink> ratingCtxtLinks) { _ratingContexts.clear();_ratingContexts.addAll(ratingCtxtLinks); }
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
}