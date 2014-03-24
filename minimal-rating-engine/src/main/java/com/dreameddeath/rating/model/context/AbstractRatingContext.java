package com.dreammdeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

@JsonTypeInfo(use=Id.CLASS, include=As.PROPERTY, property="class")
public abstract class AbstractRatingContext{
    private String _uid;
    private DateTime   _billingPeriodStartDate;
    private DateTime   _billingPeriodEndDate;
    private String _billingAccountUid;
    private String _holderUid;
    private String _rootInstalledBaseUid;
    private String _parentInstalledBaseUid;
    
    private List<RatingContextBucket> _buckets;
    private List<RatingContextAttribute> _attributes;
    
    @JsonProperty("uid")
    public String getUid(){ return _uid;}
    public void setUid(String uid){ this._uid = uid; }
    
    @JsonProperty("billingPeriodStartDate")
    public DateTime getBillingPeriodStartDate(){ return _billingPeriodStartDate; }
    public void setBillingPeriodStartDate(DateTime billingPeriodStartDate){ _billingPeriodStartDate=billingPeriodStartDate;}
    
    @JsonProperty("billingPeriodEndDate")
    public DateTime getBillingPeriodEndDate(){ return _billingPeriodEndDate; }
    public void setBillingPeriodEndDate(DateTime billingPeriodEndDate){ _billingPeriodEndDate=billingPeriodEndDate;}
    
    @JsonProperty("billingAccountUid")
    public String getBillingAccountUid(){ return _billingAccountUid;}
    public void setBillingAccountUid(String uid){ _billingAccountUid = uid;}
    
    @JsonProperty("holderUid")
    public String getHolderUid(){return _holderUid;}
    public void setHolderUid(String uid){_holderUid = uid;}
    
    @JsonProperty("rootInstalledBaseUid")
    public String getRootInstalledBaseUid(){return _rootInstalledBaseUid;}
    public void setRootInstalledBaseUid(String uid){_rootInstalledBaseUid = uid;}
    
    @JsonProperty("parentInstalledBaseUid")
    public String getParentInstalledBaseUid(){return _parentInstalledBaseUid;}
    public void setParentInstalledBaseUid(String uid){_parentInstalledBaseUid = uid;}
    
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
    
    
}