package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import com.dreameddeath.common.model.CouchbaseDocument;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;


import com.dreameddeath.common.model.ImmutableProperty;
import com.dreameddeath.common.model.CouchbaseDocumentArrayList;

import com.dreameddeath.billing.model.BillingAccountLink;
import com.dreameddeath.billing.model.BillingCycleLink;
import com.dreameddeath.billing.model.BillingCycle;


@JsonTypeInfo(use=Id.MINIMAL_CLASS, include=As.PROPERTY, property="@c")
public abstract class AbstractRatingContext extends CouchbaseDocument{
    private ImmutableProperty<BillingCycleLink> _billingCycle=new ImmutableProperty<BillingCycleLink>(AbstractRatingContext.this);
    private ImmutableProperty<BillingAccountLink> _billingAccount=new ImmutableProperty<BillingAccountLink>(AbstractRatingContext.this);
    @JsonProperty("attributes")
    private List<RatingContextAttribute> _attributes=new CouchbaseDocumentArrayList<RatingContextAttribute>(AbstractRatingContext.this);
    @JsonProperty("buckets")
    private List<RatingContextBucket> _buckets=new CouchbaseDocumentArrayList<RatingContextBucket>(AbstractRatingContext.this);
    
    
    @JsonProperty("billingCycle")
    public BillingCycleLink getBillingCycleLink(){ return _billingCycle.get(); }
    public void setBillingCycleLink(BillingCycleLink billingCycleLink){
        _billingAccount.set(new BillingAccountLink(billingCycleLink.getLinkedObject().getBillingAccountLink()));
        _billingCycle.set(billingCycleLink);
    }
    public void setBillingCycle(BillingCycle billingCycle){ billingCycle.addRatingContext(this); }
    
    @JsonProperty("billingAccount")
    public BillingAccountLink getBillingAccountLink(){ return _billingAccount.get();}
    public void setBillingAccountLink(BillingAccountLink baLink){ _billingAccount.set(baLink);}
    
    public List<RatingContextBucket> getBuckets(){ return Collections.unmodifiableList(_buckets); }
    public void setBuckets(List<RatingContextBucket> buckets){_buckets.clear(); _buckets.addAll(buckets);}
    public void addBuckets(List<RatingContextBucket> buckets){ _buckets.addAll(buckets);}
    public void addBucket(RatingContextBucket bucket){ _buckets.add(bucket);}
    
    public List<RatingContextAttribute> getAttributes(){return Collections.unmodifiableList(_attributes);}
    public void setAttributes(List<RatingContextAttribute> attributes){_attributes.clear(); _attributes.addAll(attributes);}
    public void addAttributes(List<RatingContextAttribute> attributes){_attributes.addAll(attributes);}
    public void addAttribute(RatingContextAttribute attribute){_attributes.add(attribute);}
    
    public RatingContextLink newRatingContextLink(){ return new RatingContextLink(this);}
    
    @Override
    public String toString(){
        String result = super.toString()+"\n";
        result+="ba : "+getBillingAccountLink().toString()+",\n";
        result+="billCycle : "+getBillingCycleLink().toString()+",\n";
        return result;
    }
}