package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.dreameddeath.common.model.CouchbaseDocumentElement;

public class RatingContextGuidingKey extends CouchbaseDocumentElement{
    private String   _key;
    private String   _type;
    private DateTime _startDate;
    private DateTime _endDate;

    @JsonProperty("key")
    public String getKey(){ return _key;}
    public void setKey(String key){ this._key = key; }
    
    @JsonProperty("type")
    public String getType(){ return _type;}
    public void setType(String type){ this._type = type; }
    
    @JsonProperty("startDate")
    public DateTime getStartDate(){ return _startDate;}
    public void setStartDate(DateTime startDate){ this._startDate = startDate; }
    
    @JsonProperty("endDate")
    public DateTime getEndDate(){ return _endDate;}
    public void setEndDate(DateTime endDate){ this._endDate = endDate; }
 
}