package com.dreameddeath.rating.model.context;


import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.dreameddeath.common.model.CouchbaseDocumentElement;

public class RatingContextAttributeValue extends CouchbaseDocumentElement{
    private String   _value;
    private DateTime _startDate;
    private DateTime _endDate;
    
    @JsonProperty("value")
    public String getValue(){ return _value;}
    public void setValue(String value){ this._value = value; }
 
    @JsonProperty("startDate")
    public DateTime getStartDate(){ return _startDate;}
    public void setStartDate(DateTime startDate){ this._startDate = startDate; }
 
    @JsonProperty("endDate")
    public DateTime getEndDate(){ return _endDate;}
    public void setEndDate(DateTime endDate){ this._endDate = endDate; }
 
}