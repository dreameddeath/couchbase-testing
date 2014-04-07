package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.dreameddeath.rating.model.cdr.CdrsBucketLink;

@JsonInclude(Include.NON_EMPTY)
public final class StandardRatingContext extends AbstractRatingContext{
    private List<CdrsBucketLink> _cdrsBuckets=new ArrayList<CdrsBucketLink>();
    
    private List<RatingContextGuidingKey> _guidingKeys=new ArrayList<RatingContextGuidingKey>();
    private List<RatingContextRatePlan> _ratePlans=new ArrayList<RatingContextRatePlan>();
    private List<RatingContextSharedLink> _sharedRatingCtxtLinks=new ArrayList<RatingContextSharedLink>();

    @JsonProperty("cdrsBucketLinks")
    public List<CdrsBucketLink> getCdrsBuckets(){ return _cdrsBuckets; }
    public void setCdrsBuckets(List<CdrsBucketLink> cdrsBucketsLnk){ _cdrsBuckets.clear(); _cdrsBuckets.addAll(cdrsBucketsLnk); }
    
    @JsonProperty("guidingKeys")
    public List<RatingContextGuidingKey> getGuidingKeys(){ return _guidingKeys; }
    public void setGuidingKeys(List<RatingContextGuidingKey> guidingKeys){ _guidingKeys.clear(); _guidingKeys.addAll(guidingKeys); }
    
    @JsonProperty("ratePlans")
    public List<RatingContextRatePlan> getRatePlans(){ return _ratePlans; }
    public void setRatePlans(List<RatingContextRatePlan> ratePlans){_ratePlans.clear();_ratePlans.addAll(ratePlans);}
    
    @JsonProperty("sharedContexts")
    public List<RatingContextSharedLink> getSharedContexts(){return _sharedRatingCtxtLinks;}
    public void setSharedContexts(List<RatingContextSharedLink> sharedContexts){_sharedRatingCtxtLinks.clear();_sharedRatingCtxtLinks.addAll(sharedContexts);}
    
}