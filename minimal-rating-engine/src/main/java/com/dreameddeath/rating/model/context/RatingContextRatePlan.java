package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class RatingContextRatePlan {
    private String   _code;
    private DateTime _startDate;
    private DateTime _endDate;
    
    @JsonProperty("code")
    public String getCode(){ return _code;}
    public void setCode(String code){ this._code = code; }
    
    @JsonProperty("startDate")
    public DateTime getStartDate(){ return _startDate;}
    public void setStartDate(DateTime startDate){ this._startDate = startDate; }
    
    @JsonProperty("endDate")
    public DateTime getEndDate(){ return _endDate;}
    public void setEndDate(DateTime endDate){ this._endDate = endDate; }
 
}