package com.dreameddeath.rating.model.context;

import org.joda.time.DateTime;

import com.dreameddeath.common.model.CouchbaseDocumentElement;
import com.dreameddeath.common.annotation.DocumentProperty;

public class RatingContextGuidingKey extends CouchbaseDocumentElement{

    @DocumentProperty("key")
    private String   _key;
    @DocumentProperty("type")
    private String   _type;
    @DocumentProperty("startDate")
    private DateTime _startDate;
    @DocumentProperty("endDate")
    private DateTime _endDate;

    public String getKey(){ return _key;}
    public void setKey(String key){ this._key = key; }
    

    public String getType(){ return _type;}
    public void setType(String type){ this._type = type; }
    
    public DateTime getStartDate(){ return _startDate;}
    public void setStartDate(DateTime startDate){ this._startDate = startDate; }
    
    public DateTime getEndDate(){ return _endDate;}
    public void setEndDate(DateTime endDate){ this._endDate = endDate; }
 
}