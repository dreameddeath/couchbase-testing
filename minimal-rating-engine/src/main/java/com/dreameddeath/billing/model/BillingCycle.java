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

public class BillingCycle extends CouchbaseDocument{
    @JsonProperty("ba")
    private BillingAccountLink  _baLink;
    @JsonProperty("startDate")
    private DateTime _startDate;
	@JsonProperty("endDate")
    private DateTime _endDate;
	@JsonProperty("ratingContexts")
    private List<RatingContextLink> _ratingContexts=new CouchbaseDocumentArrayList<RatingContextLink>(BillingCycle.this);
    
    public BillingAccountLink getBillingAccountLink() { return _baLink; }
    public void setBillingAccountLink(BillingAccountLink baLink) { _baLink = baLink; }
    
    public DateTime getStartDate() { return _startDate; }
    public void setStartDate(DateTime startDate) { _startDate=startDate; }
    
    public DateTime getEndDate() { return _endDate; }
    public void setEndDate(DateTime endDate) { _endDate=endDate; }
    
    public Collection<RatingContextLink> getRatingContextsLinks() { return Collections.unmodifiableList(_ratingContexts); }
    public void setRatingContextsLinks(Collection<RatingContextLink> ratingCtxtLinks) { _ratingContexts.clear();_ratingContexts.addAll(ratingCtxtLinks); }
    public void addRatingContextLink(RatingContextLink ratingCtxtLink) { _ratingContexts.add(ratingCtxtLink); }
    
}