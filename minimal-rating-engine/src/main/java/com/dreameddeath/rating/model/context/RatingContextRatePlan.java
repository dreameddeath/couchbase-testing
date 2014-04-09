package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.dreameddeath.common.model.CouchbaseDocumentElement;

public class RatingContextRatePlan extends CouchbaseDocumentElement {
    @JsonProperty("code")
    private String   _code;
    @JsonProperty("startDate")
    private DateTime _startDate;
    @JsonProperty("endDate")
    private DateTime _endDate;
    
    public String getCode(){ return _code;}
    public void setCode(String code){ this._code = code; }
    
    public DateTime getStartDate(){ return _startDate;}
    public void setStartDate(DateTime startDate){ this._startDate = startDate; }
    
    public DateTime getEndDate(){ return _endDate;}
    public void setEndDate(DateTime endDate){ this._endDate = endDate; }
 
}