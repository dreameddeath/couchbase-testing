package com.dreameddeath.rating.model.context;


import org.joda.time.DateTime;

import com.dreameddeath.common.model.CouchbaseDocumentElement;
import com.dreameddeath.common.annotation.DocumentProperty;

public class RatingContextAttributeValue extends CouchbaseDocumentElement{
    @DocumentProperty("value")
    private String   _value;
    @DocumentProperty("startDate")
    private DateTime _startDate;
    @DocumentProperty("endDate")
    private DateTime _endDate;
    
    public String getValue(){ return _value;}
    public void setValue(String value){ this._value = value; }
 
    public DateTime getStartDate(){ return _startDate;}
    public void setStartDate(DateTime startDate){ this._startDate = startDate; }
 
    public DateTime getEndDate(){ return _endDate;}
    public void setEndDate(DateTime endDate){ this._endDate = endDate; }
 
}