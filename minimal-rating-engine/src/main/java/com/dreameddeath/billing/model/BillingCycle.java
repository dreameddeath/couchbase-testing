package com.dreameddeath.billing.model;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIgnore;

import net.spy.memcached.transcoders.Transcoder;


import com.dreameddeath.common.storage.GenericJacksonTranscoder;
import com.dreameddeath.common.storage.CouchbaseDocument;
import com.dreameddeath.rating.model.context.RatingContextLink;

@JsonInclude(Include.NON_EMPTY)
public class BillingCycle extends CouchbaseDocument{
    private static GenericJacksonTranscoder<BillingCycle> _tc = new GenericJacksonTranscoder<BillingCycle>(BillingCycle.class);
    @JsonIgnore
    public  Transcoder<BillingCycle> getTranscoder(){
        return _tc;
    }
    
    private BillingAccountLink  _baLink;
    private DateTime _startDate;
	private DateTime _endDate;
	private List<RatingContextLink> _ratingContexts=new ArrayList<RatingContextLink>();
    
    @JsonProperty("ba")
    public BillingAccountLink getBillingAccountLink() { return _baLink; }
    public void setBillingAccountLink(BillingAccountLink baLink) { _baLink = baLink; }
    
    @JsonProperty("startDate")
    public DateTime getStartDate() { return _startDate; }
    public void setStartDate(DateTime startDate) { _startDate=startDate; }
    
    @JsonProperty("endDate")
    public DateTime getEndDate() { return _endDate; }
    public void setEndDate(DateTime endDate) { _endDate=endDate; }
    
    @JsonProperty("ratingContexts")
    public Collection<RatingContextLink> getRatingContextsLinks() { return _ratingContexts; }
    public void setRatingContextsLinks(Collection<RatingContextLink> ratingCtxtLinks) { _ratingContexts.clear();_ratingContexts.addAll(ratingCtxtLinks); }
    public void addRatingContextLink(RatingContextLink ratingCtxtLink) { _ratingContexts.add(ratingCtxtLink); }
    
    
    @JsonIgnore
    public BillingCycleLink buildLink(){
        return BillingCycleLink.buildLink(this);
    }
    
    
}