package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import com.dreameddeath.common.storage.CouchbaseDocument;
import com.dreameddeath.common.storage.CouchbaseDocumentLink;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import net.spy.memcached.transcoders.Transcoder;

import com.dreameddeath.common.storage.GenericJacksonTranscoder;


@JsonInclude(Include.NON_EMPTY)
public class RatingContextLink extends CouchbaseDocumentLink<AbstractRatingContext>{
    private static GenericJacksonTranscoder<AbstractRatingContext> _tc = new GenericJacksonTranscoder<AbstractRatingContext>(AbstractRatingContext.class);
    @JsonIgnore
    public  Transcoder<AbstractRatingContext> getTranscoder(){
        return _tc;
    }
    
    private String _class;
    
    @JsonProperty("@c")
    public String getType() { return _class;}
    public void setType(String clazz) { _class=clazz;}
    
    @Override
    public String toString(){
        String result ="{\n"+super.toString()+",\n";
        result+="type : "+getType();
        result+="}\n";
        return result;
    }
    
    
    public static RatingContextLink buildLink(AbstractRatingContext ratingCtxt){
        RatingContextLink newLink = new RatingContextLink();
        newLink.setKey(ratingCtxt.getKey());
        newLink.setType(ratingCtxt.getClass().getSimpleName());
        newLink.setLinkedObject(ratingCtxt);
        return newLink;
    }
}