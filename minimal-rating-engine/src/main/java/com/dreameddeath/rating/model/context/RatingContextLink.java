package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import com.dreameddeath.common.model.CouchbaseDocumentLink;



@JsonInclude(Include.NON_EMPTY)
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
}