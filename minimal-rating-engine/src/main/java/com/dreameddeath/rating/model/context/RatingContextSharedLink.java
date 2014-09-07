package com.dreameddeath.rating.model.context;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.model.property.Property;
import org.joda.time.DateTime;

public class RatingContextSharedLink extends RatingContextLink {
    @DocumentProperty("startDate")
    private Property<DateTime> _startDate= new StandardProperty<DateTime>(RatingContextSharedLink.this);
    @DocumentProperty("endDate")
    private Property<DateTime> _endDate=new StandardProperty<DateTime>(RatingContextSharedLink.this);
    
    public DateTime getStartDate(){ return _startDate.get();}
    public void setStartDate(DateTime startDate){ _startDate.set(startDate); }
    

    public DateTime getEndDate(){ return _endDate.get();}
    public void setEndDate(DateTime endDate){ _endDate.set(endDate); }
    
    public RatingContextSharedLink(){}
    public RatingContextSharedLink(SharedRatingContext ctxt){
        super(ctxt);
    }
    
    public RatingContextSharedLink(RatingContextSharedLink srcLink){
        super(srcLink);
    }
    
 
}