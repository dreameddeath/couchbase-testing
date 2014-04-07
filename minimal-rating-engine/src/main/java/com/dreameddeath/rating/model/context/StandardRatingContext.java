package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonInclude(Include.NON_EMPTY)
public final class StandardRatingContext extends AbstractRatingContext{
    private List<RatingContextGuidingKey> _guidingKeys=new ArrayList<RatingContextGuidingKey>();
    private List<RatingContextRatePlan> _ratePlans=new ArrayList<RatingContextRatePlan>();
    private List<RatingContextSharedLink> _sharedRatingCtxtLinks=new ArrayList<RatingContextSharedLink>();

    @JsonProperty("guidingKeys")
    public List<RatingContextGuidingKey> getGuidingKeys(){ return _guidingKeys; }
    public void setGuidingKeys(List<RatingContextGuidingKey> guidingKeys){ _guidingKeys = guidingKeys; }
    
    @JsonProperty("ratePlans")
    public List<RatingContextRatePlan> getRatePlans(){ return _ratePlans; }
    public void setRatePlans(List<RatingContextRatePlan> ratePlans){_ratePlans.clear();_ratePlans.addAll(ratePlans);}
    
    @JsonProperty("sharedContexts")
    public List<RatingContextSharedLink> getSharedContexts(){return _sharedRatingCtxtLinks;}
    public void setSharedContexts(List<RatingContextSharedLink> sharedContexts){_sharedRatingCtxtLinks.clear();_sharedRatingCtxtLinks.addAll(sharedContexts);}
    
}