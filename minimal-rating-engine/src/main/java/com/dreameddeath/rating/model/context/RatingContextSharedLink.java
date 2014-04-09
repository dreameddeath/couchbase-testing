package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RatingContextSharedLink extends RatingContextLink {
    @JsonProperty("startDate")
    private DateTime _startDate;
    @JsonProperty("endDate")
    private DateTime _endDate;
    
    public DateTime getStartDate(){ return _startDate;}
    public void setStartDate(DateTime startDate){ this._startDate = startDate; }
    
    public DateTime getEndDate(){ return _endDate;}
    public void setEndDate(DateTime endDate){ this._endDate = endDate; }
    
    public RatingContextSharedLink(){}
    public RatingContextSharedLink(SharedRatingContext ctxt){
        super(ctxt);
    }
 
}