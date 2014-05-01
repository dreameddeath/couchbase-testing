package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RatingContextSharedLink extends RatingContextLink {
    private DateTime _startDate;
    private DateTime _endDate;
    
    @JsonProperty("startDate")
    public DateTime getStartDate(){ return _startDate;}
    public void setStartDate(DateTime startDate){ this._startDate = startDate; }
    
    @JsonProperty("endDate")
    public DateTime getEndDate(){ return _endDate;}
    public void setEndDate(DateTime endDate){ this._endDate = endDate; }
    
    public RatingContextSharedLink(){}
    public RatingContextSharedLink(SharedRatingContext ctxt){
        super(ctxt);
    }
    
    public RatingContextSharedLink(RatingContextSharedLink srcLink){
        super(srcLink);
    }
    
 
}