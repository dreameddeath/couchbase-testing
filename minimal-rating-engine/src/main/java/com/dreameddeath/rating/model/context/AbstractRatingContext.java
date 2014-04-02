package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import com.dreameddeath.common.storage.CouchbaseDocument;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.spy.memcached.transcoders.Transcoder;

import com.dreameddeath.common.storage.GenericJacksonTranscoder;
import com.dreameddeath.billing.model.BillingAccountLink;
import com.dreameddeath.billing.model.BillingCycleLink;

@JsonInclude(Include.NON_EMPTY)
@JsonTypeInfo(use=Id.MINIMAL_CLASS, include=As.PROPERTY, property="@c")
public abstract class AbstractRatingContext extends CouchbaseDocument{
    private static GenericJacksonTranscoder<AbstractRatingContext> _tc = new GenericJacksonTranscoder<AbstractRatingContext>(AbstractRatingContext.class);
    @JsonIgnore
    public  Transcoder<AbstractRatingContext> getTranscoder(){
        return _tc;
    }
    
    private BillingCycleLink _billingCycle;
    private BillingAccountLink _billingAccount;
    
    private List<RatingContextBucket> _buckets;
    private List<RatingContextAttribute> _attributes;
    
    @JsonProperty("uid")
    public String getUid(){ return getKey();}
    public void setUid(String uid){ setKey(uid); }
    
    @JsonProperty("billingCycle")
    public BillingCycleLink getBillingCycleLink(){ return _billingCycle; }
    public void setBillingCycleLink(BillingCycleLink billingCycle){ _billingCycle=billingCycle;}
    
    @JsonProperty("billingAccount")
    public BillingAccountLink getBillingAccountLink(){ return _billingAccount;}
    public void setBillingAccountLink(BillingAccountLink billingAccount){ _billingAccount = billingAccount;}
    
    @JsonProperty("buckets")
    public List<RatingContextBucket> getBuckets(){
        if(_buckets==null){ _buckets = new ArrayList<RatingContextBucket>();}
        return _buckets;
    }
    public void setBuckets(List<RatingContextBucket> buckets){_buckets = buckets;}
    
    @JsonProperty("attributes")
    public List<RatingContextAttribute> getAttributes(){
        if(_attributes==null){ _attributes = new ArrayList<RatingContextAttribute>(); }
        return _attributes;
    }
    public void setAttributes(List<RatingContextAttribute> attributes){_attributes = attributes;}
    
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