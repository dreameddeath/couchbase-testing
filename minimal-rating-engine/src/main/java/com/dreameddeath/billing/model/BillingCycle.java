package com.dreameddeath.billing.model;

import com.dreameddeath.common.annotation.DocumentProperty;
import com.dreameddeath.common.model.*;
import com.dreameddeath.rating.model.context.AbstractRatingContext;
import com.dreameddeath.rating.model.context.RatingContextLink;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BillingCycle extends CouchbaseDocument{
    @DocumentProperty("ba")
    private ImmutableProperty<BillingAccountLink>  _baLink=new ImmutableProperty<BillingAccountLink>(BillingCycle.this);
    @DocumentProperty("startDate")
    private Property<DateTime> _startDate = new StandardProperty<DateTime>(BillingCycle.this);
    @DocumentProperty("endDate")
    private Property<DateTime> _endDate= new StandardProperty<DateTime>(BillingCycle.this);
    @DocumentProperty("ratingContexts")
    private List<RatingContextLink> _ratingContexts=new CouchbaseDocumentArrayList<RatingContextLink>(BillingCycle.this);
    

    public BillingAccountLink getBillingAccountLink() { return _baLink.get(); }
    public void setBillingAccountLink(BillingAccountLink baLink) { _baLink.set(baLink); }
    public void setBillingAccount(BillingAccount ba){ ba.addBillingCycle(this); }
    
    public DateTime getStartDate() { return _startDate.get(); }
    public void setStartDate(DateTime startDate) { _startDate.set(startDate); }
    

    public DateTime getEndDate() { return _endDate.get(); }
    public void setEndDate(DateTime endDate) { _endDate.set(endDate); }
    
    public List<RatingContextLink> getRatingContextLinks() { return Collections.unmodifiableList(_ratingContexts); }
    public void setRatingContextLinks(Collection<RatingContextLink> ratingCtxtLinks) { _ratingContexts.clear();_ratingContexts.addAll(ratingCtxtLinks); }
    public void addRatingContext(AbstractRatingContext ratingCtxt){  
        if(_ratingContexts.add(ratingCtxt.newRatingContextLink())){
            ratingCtxt.setBillingCycleLink(newBillingCycleLink());
        }
    }
    
    public BillingCycleLink newBillingCycleLink(){
        return new BillingCycleLink(this);
    }
    
    public boolean isValidForDate(DateTime refDate){
        return BillingCycle.isValidForDate(refDate,_startDate.get(),_endDate.get());
    }
    
    public static boolean isValidForDate(DateTime refDate, DateTime startTime,DateTime endTime){
        if((refDate.compareTo(startTime)>=0) && (refDate.compareTo(endTime)<0)){
            return true;
        }
        return false;
    }
    
    @Override
    public String toString(){
        String result= super.toString()+",\n";
        result+="startDate:"+_startDate+",\n";
        result+="startend:"+_endDate+",\n";
        result+="ratingContexts:"+_ratingContexts+"\n";
        return result;
    }
}