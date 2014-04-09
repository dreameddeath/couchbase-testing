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

import com.dreameddeath.common.model.CouchbaseDocumentArrayList;
import com.dreameddeath.billing.model.BillingAccountLink;
import com.dreameddeath.billing.model.BillingCycleLink;

@JsonTypeInfo(use=Id.MINIMAL_CLASS, include=As.PROPERTY, property="@c")
public abstract class AbstractRatingContext extends CouchbaseDocument{
    @JsonProperty("uid")
    private Long _uid;
    @JsonProperty("billingCycle")
    private BillingCycleLink _billingCycle;
    @JsonProperty("billingAccount")
    private BillingAccountLink _billingAccount;
    @JsonProperty("attributes")
    private List<RatingContextAttribute> _attributes=new CouchbaseDocumentArrayList<RatingContextAttribute>(AbstractRatingContext.this);
    @JsonProperty("buckets")
    private List<RatingContextBucket> _buckets=new CouchbaseDocumentArrayList<RatingContextBucket>(AbstractRatingContext.this);
   
    public Long getUid(){ return _uid;}
    public void setUid(Long uid){ _uid = uid; }
    
    public BillingCycleLink getBillingCycleLink(){ return _billingCycle; }
    public void setBillingCycleLink(BillingCycleLink billingCycle){ _billingCycle=billingCycle;}
    
    public BillingAccountLink getBillingAccountLink(){ return _billingAccount;}
    public void setBillingAccountLink(BillingAccountLink billingAccount){ _billingAccount = billingAccount;}
    
    public List<RatingContextBucket> getBuckets(){ return Collections.unmodifiableList(_buckets); }
    public void setBuckets(List<RatingContextBucket> buckets){_buckets.clear(); _buckets.addAll(buckets);}
    public void addBuckets(List<RatingContextBucket> buckets){ _buckets.addAll(buckets);}
    public void addBucket(RatingContextBucket bucket){ _buckets.add(bucket);}
    
    public List<RatingContextAttribute> getAttributes(){return Collections.unmodifiableList(_attributes);}
    public void setAttributes(List<RatingContextAttribute> attributes){_attributes.clear(); _attributes.addAll(attributes);}
    public void addAttributes(List<RatingContextAttribute> attributes){_attributes.addAll(attributes);}
    public void addAttribute(RatingContextAttribute attribute){_attributes.add(attribute);}
    
    @Override
    public String toString(){
        String result = super.toString()+"\n";
        result+="uid : "+getUid()+",\n";
        result+="ba : "+getBillingAccountLink().toString()+",\n";
        result+="billCycle : "+getBillingCycleLink().toString()+",\n";
        return result;
    }
}