package com.dreameddeath.rating.model.context;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import org.joda.time.DateTime;

public class RatingContextGuidingKey extends BaseCouchbaseDocumentElement {
    @DocumentProperty("key")
    private Property<String>   _key=new StandardProperty<String>(RatingContextGuidingKey.this);
    @DocumentProperty("type")
    private Property<String>   _type= new StandardProperty<String>(RatingContextGuidingKey.this);
    @DocumentProperty("startDate")
    private Property<DateTime> _startDate= new StandardProperty<DateTime>(RatingContextGuidingKey.this);
    @DocumentProperty("endDate")
    private Property<DateTime> _endDate=new StandardProperty<DateTime>(RatingContextGuidingKey.this);

    public String getKey(){ return _key.get();}
    public void setKey(String key){ _key.set(key); }
    

    public String getType(){ return _type.get();}
    public void setType(String type){ _type.set(type); }
    
    public DateTime getStartDate(){ return _startDate.get();}
    public void setStartDate(DateTime startDate){ _startDate.set(startDate); }
    
    public DateTime getEndDate(){ return _endDate.get();}
    public void setEndDate(DateTime endDate){ _endDate.set(endDate); }
 
}