package com.dreameddeath.rating.model.context;


import com.dreameddeath.common.annotation.DocumentProperty;
import com.dreameddeath.common.model.document.CouchbaseDocumentElement;
import com.dreameddeath.common.model.property.StandardProperty;
import com.dreameddeath.common.model.property.Property;
import org.joda.time.DateTime;

public class RatingContextAttributeValue extends CouchbaseDocumentElement{
    @DocumentProperty("value")
    private Property<String>   _value=new StandardProperty<String>(RatingContextAttributeValue.this);
    @DocumentProperty("startDate")
    private Property<DateTime> _startDate=new StandardProperty<DateTime>(RatingContextAttributeValue.this);
    @DocumentProperty("endDate")
    private Property<DateTime> _endDate=new StandardProperty<DateTime>(RatingContextAttributeValue.this);
    
    public String getValue(){ return _value.get();}
    public void setValue(String value){ _value.set(value); }
 
    public DateTime getStartDate(){ return _startDate.get();}
    public void setStartDate(DateTime startDate){ _startDate.set(startDate); }
 
    public DateTime getEndDate(){ return _endDate.get();}
    public void setEndDate(DateTime endDate){ _endDate.set(endDate); }
 
}