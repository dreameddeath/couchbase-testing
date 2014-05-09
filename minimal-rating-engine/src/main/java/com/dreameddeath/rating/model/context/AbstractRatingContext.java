package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.Collections;
import com.dreameddeath.common.model.CouchbaseDocument;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;


import com.dreameddeath.common.model.ImmutableProperty;
import com.dreameddeath.common.model.CouchbaseDocumentArrayList;
import com.dreameddeath.common.annotation.DocumentProperty;

import com.dreameddeath.billing.model.BillingAccountLink;
import com.dreameddeath.billing.model.BillingCycleLink;
import com.dreameddeath.billing.model.BillingCycle;


@JsonTypeInfo(use=Id.MINIMAL_CLASS, include=As.PROPERTY, property="@c")
public abstract class AbstractRatingContext extends CouchbaseDocument{
    @DocumentProperty(value="billingCycle",setter="setBillingCycleLink",getter="getBillingCycleLink")
    private ImmutableProperty<BillingCycleLink> _billingCycleLink=new ImmutableProperty<BillingCycleLink>(AbstractRatingContext.this);
    @DocumentProperty("billingAccount")
    private ImmutableProperty<BillingAccountLink> _billingAccountLink=new ImmutableProperty<BillingAccountLink>(AbstractRatingContext.this);
    @DocumentProperty("attributes")
    private List<RatingContextAttribute> _attributes=new CouchbaseDocumentArrayList<RatingContextAttribute>(AbstractRatingContext.this);
    @DocumentProperty("buckets")
    private List<RatingContextBucket> _buckets=new CouchbaseDocumentArrayList<RatingContextBucket>(AbstractRatingContext.this);
    
    
    public BillingCycleLink getBillingCycleLink(){ return _billingCycleLink.get(); }
    public void setBillingCycleLink(BillingCycleLink billingCycleLink){
        _billingAccountLink.set(new BillingAccountLink(billingCycleLink.getLinkedObject().getBillingAccountLink()));
        _billingCycleLink.set(billingCycleLink);
    }
    public void setBillingCycle(BillingCycle billingCycle){ billingCycle.addRatingContext(this); }
    

    public BillingAccountLink getBillingAccountLink(){ return _billingAccountLink.get();}
    public void setBillingAccountLink(BillingAccountLink baLink){ _billingAccountLink.set(baLink);}
    
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