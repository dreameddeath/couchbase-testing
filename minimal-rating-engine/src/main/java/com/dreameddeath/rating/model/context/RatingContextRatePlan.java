package com.dreameddeath.rating.model.context;


import com.dreameddeath.common.annotation.DocumentProperty;
import com.dreameddeath.common.model.document.CouchbaseDocumentElement;
import com.dreameddeath.common.model.property.StandardProperty;
import com.dreameddeath.common.model.property.Property;
import org.joda.time.DateTime;

public class RatingContextRatePlan extends CouchbaseDocumentElement {
    @DocumentProperty("code")
    private Property<String>   _code=new StandardProperty<String>(RatingContextRatePlan.this);
    @DocumentProperty("startDate")
    private Property<DateTime> _startDate=new StandardProperty<DateTime>(RatingContextRatePlan.this);
    @DocumentProperty("endDate")
    private Property<DateTime> _endDate=new StandardProperty<DateTime>(RatingContextRatePlan.this);
    
    public String getCode(){ return _code.get();}
    public void setCode(String code){ _code.set(code); }
    
    public DateTime getStartDate(){ return _startDate.get();}
    public void setStartDate(DateTime startDate){ _startDate.set(startDate); }
    

    public DateTime getEndDate(){ return _endDate.get();}
    public void setEndDate(DateTime endDate){ _endDate.set(endDate); }
 
}