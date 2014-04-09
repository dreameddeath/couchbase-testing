package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.dreameddeath.common.model.CouchbaseDocumentElement;

public class RatingContextGuidingKey extends CouchbaseDocumentElement{
    @JsonProperty("key")
    private String   _key;
    @JsonProperty("type")
    private String   _type;
    @JsonProperty("startDate")
    private DateTime _startDate;
    @JsonProperty("endDate")
    private DateTime _endDate;

    public String getKey(){ return _key;}
    public void setKey(String key){ this._key = key; }
    
    public String getType(){ return _type;}
    public void setType(String type){ this._type = type; }
    
    public DateTime getStartDate(){ return _startDate;}
    public void setStartDate(DateTime startDate){ this._startDate = startDate; }
    
    public DateTime getEndDate(){ return _endDate;}
    public void setEndDate(DateTime endDate){ this._endDate = endDate; }
 
}