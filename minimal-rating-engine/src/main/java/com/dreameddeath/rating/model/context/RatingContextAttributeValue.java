package com.dreameddeath.rating.model.context;


import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.dreameddeath.common.model.CouchbaseDocumentElement;

public class RatingContextAttributeValue extends CouchbaseDocumentElement{
    @JsonProperty("value")
    private String   _value;
    @JsonProperty("startDate")
    private DateTime _startDate;
    @JsonProperty("endDate")
    private DateTime _endDate;
    
    public String getValue(){ return _value;}
    public void setValue(String value){ this._value = value; }
 
    public DateTime getStartDate(){ return _startDate;}
    public void setStartDate(DateTime startDate){ this._startDate = startDate; }
 
    public DateTime getEndDate(){ return _endDate;}
    public void setEndDate(DateTime endDate){ this._endDate = endDate; }
 
}