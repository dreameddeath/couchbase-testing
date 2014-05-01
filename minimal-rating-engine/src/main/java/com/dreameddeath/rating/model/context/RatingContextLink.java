package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.dreameddeath.common.model.CouchbaseDocumentLink;

public class RatingContextLink extends CouchbaseDocumentLink<AbstractRatingContext>{
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
    
    public RatingContextLink(){}
    public RatingContextLink(AbstractRatingContext ratingCtxt){
        super(ratingCtxt);
        setType(ratingCtxt.getClass().getSimpleName());
    }
    
    public RatingContextLink(RatingContextLink srcLink){
        super(srcLink);
        setType(srcLink.getType());
    }
    
}