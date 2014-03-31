package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.dreameddeath.common.storage.GenericJacksonTranscoder;
import net.spy.memcached.transcoders.Transcoder;
import com.dreameddeath.common.storage.CouchbaseDocument;

@JsonInclude(Include.NON_EMPTY)
public final class StandardRatingContext extends AbstractRatingContext{
    private static GenericJacksonTranscoder<StandardRatingContext> _tc = new GenericJacksonTranscoder<StandardRatingContext>(StandardRatingContext.class);
    @JsonIgnore
    public  Transcoder<StandardRatingContext> getTranscoder(){
        return _tc;
    }


    private List<RatingContextGuidingKey> _guidingKeys;
    private List<RatingContextRatePlan> _ratePlans;
    private List<RatingContextSharedLink> _sharedRatingCtxtLinks;

    @JsonProperty("guidingKeys")
    public List<RatingContextGuidingKey> getGuidingKeys(){
        if(_guidingKeys==null){ _guidingKeys = new ArrayList<RatingContextGuidingKey>();}
        return _guidingKeys;
    }
    public void setGuidingKeys(List<RatingContextGuidingKey> guidingKeys){_guidingKeys = guidingKeys;}
    
    @JsonProperty("ratePlans")
    public List<RatingContextRatePlan> getRatePlans(){
        if(_ratePlans==null){ _ratePlans = new ArrayList<RatingContextRatePlan>();}
        return _ratePlans;
    }
    public void setRatePlans(List<RatingContextRatePlan> ratePlans){_ratePlans = ratePlans;}
    
    @JsonProperty("sharedContexts")
    public List<RatingContextSharedLink> getSharedContexts(){
        if(_sharedRatingCtxtLinks==null){ _sharedRatingCtxtLinks = new ArrayList<RatingContextSharedLink>();}
        return _sharedRatingCtxtLinks;
    }
    public void setSharedContexts(List<RatingContextSharedLink> sharedContexts){_sharedRatingCtxtLinks = sharedContexts;}
    
}