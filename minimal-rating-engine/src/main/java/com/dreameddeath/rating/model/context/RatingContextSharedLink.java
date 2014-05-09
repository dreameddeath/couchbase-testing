package com.dreameddeath.rating.model.context;

import org.joda.time.DateTime;

import com.dreameddeath.common.annotation.DocumentProperty;

public class RatingContextSharedLink extends RatingContextLink {
    @DocumentProperty("startDate")
    private DateTime _startDate;
    @DocumentProperty("endDate")
    private DateTime _endDate;
    
    public DateTime getStartDate(){ return _startDate;}
    public void setStartDate(DateTime startDate){ this._startDate = startDate; }
    

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