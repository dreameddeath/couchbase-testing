package com.dreameddeath.rating.model.context;


import org.joda.time.DateTime;

import com.dreameddeath.common.model.CouchbaseDocumentElement;
import com.dreameddeath.common.annotation.DocumentProperty;

public class RatingContextRatePlan extends CouchbaseDocumentElement {
    @DocumentProperty("code")
    private String   _code;
    @DocumentProperty("startDate")
    private DateTime _startDate;
    @DocumentProperty("endDate")
    private DateTime _endDate;
    
    public String getCode(){ return _code;}
    public void setCode(String code){ this._code = code; }
    
    public DateTime getStartDate(){ return _startDate;}
    public void setStartDate(DateTime startDate){ this._startDate = startDate; }
    

    public DateTime getEndDate(){ return _endDate;}
    public void setEndDate(DateTime endDate){ this._endDate = endDate; }
 
}