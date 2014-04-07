package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import com.dreameddeath.common.model.CouchbaseDocument;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.dreameddeath.billing.model.BillingAccountLink;
import com.dreameddeath.billing.model.BillingCycleLink;

@JsonInclude(Include.NON_EMPTY)
@JsonTypeInfo(use=Id.MINIMAL_CLASS, include=As.PROPERTY, property="@c")
public abstract class AbstractRatingContext extends CouchbaseDocument{
    private Long _uid;
    private BillingCycleLink _billingCycle;
    private BillingAccountLink _billingAccount;
    
    private List<RatingContextBucket> _buckets=new ArrayList<RatingContextBucket>();
    private List<RatingContextAttribute> _attributes=new ArrayList<RatingContextAttribute>();
    
    @JsonProperty("uid")
    public Long getUid(){ return _uid;}
    public void setUid(Long uid){ _uid = uid; }
    
    @JsonProperty("billingCycle")
    public BillingCycleLink getBillingCycleLink(){ return _billingCycle; }
    public void setBillingCycleLink(BillingCycleLink billingCycle){ _billingCycle=billingCycle;}
    
    @JsonProperty("billingAccount")
    public BillingAccountLink getBillingAccountLink(){ return _billingAccount;}
    public void setBillingAccountLink(BillingAccountLink billingAccount){ _billingAccount = billingAccount;}
    
    @JsonProperty("buckets")
    public List<RatingContextBucket> getBuckets(){ return _buckets; }
    public void setBuckets(List<RatingContextBucket> buckets){_buckets.clear(); _buckets.addAll(buckets);}
    
    @JsonProperty("attributes")
    public List<RatingContextAttribute> getAttributes(){return _attributes;}
    public void setAttributes(List<RatingContextAttribute> attributes){_attributes.clear(); _attributes.addAll(attributes);}
    
    public RatingContextLink buildLink(){
        return RatingContextLink.buildLink(this);
    }
    
    @Override
    public String toString(){
        String result = super.toString()+"\n";
        result+="uid : "+getUid()+",\n";
        result+="ba : "+getBillingAccountLink().toString()+",\n";
        result+="billCycle : "+getBillingCycleLink().toString()+",\n";
        return result;
    }
}