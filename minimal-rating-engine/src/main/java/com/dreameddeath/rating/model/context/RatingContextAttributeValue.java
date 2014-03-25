package com.dreameddeath.rating.model.context;


import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class RatingContextAttributeValue{
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