package com.dreameddeath.billing.model.cycle;

import com.dreameddeath.billing.model.account.BillingAccountLink;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.business.CouchbaseDocument;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.rating.model.context.RatingContext;
import com.dreameddeath.rating.model.context.RatingContextLink;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;

public class BillingCycle extends CouchbaseDocument {
    @DocumentProperty(value="ba",getter = "getBillingAccountLink",setter="setBillingAccountLink")
    private ImmutableProperty<BillingAccountLink> _baLink=new ImmutableProperty<BillingAccountLink>(BillingCycle.this);
    @DocumentProperty("startDate")
    private Property<DateTime> _startDate = new StandardProperty<DateTime>(BillingCycle.this);
    @DocumentProperty("endDate")
    private Property<DateTime> _endDate= new StandardProperty<DateTime>(BillingCycle.this);
    @DocumentProperty(value = "ratingContexts",getter="getRatingContextLinks",setter = "setRatingContextLinks")
    private ListProperty<RatingContextLink> _ratingContexts=new ArrayListProperty<RatingContextLink>(BillingCycle.this);

    public BillingAccountLink getBillingAccountLink() { return _baLink.get(); }
    public void setBillingAccountLink(BillingAccountLink baLink) { _baLink.set(baLink); }

    public DateTime getStartDate() { return _startDate.get(); }
    public void setStartDate(DateTime startDate) { _startDate.set(startDate); }

    public DateTime getEndDate() { return _endDate.get(); }
    public void setEndDate(DateTime endDate) { _endDate.set(endDate); }
    
    public List<RatingContextLink> getRatingContextLinks() { return _ratingContexts.get(); }
    public void setRatingContextLinks(Collection<RatingContextLink> ratingCtxtLinks) { _ratingContexts.set(ratingCtxtLinks); }
    public void addRatingContext(RatingContext ratingCtxt){
        if(_ratingContexts.add(ratingCtxt.newRatingContextLink())){
            ratingCtxt.setBillingCycleLink(newLink());
        }
    }
    
    public BillingCycleLink newLink(){
        return new BillingCycleLink(this);
    }
    
    public boolean isValidForDate(DateTime refDate){
        return BillingCycle.isValidForDate(refDate,_startDate.get(),_endDate.get());
    }
    
    public static boolean isValidForDate(DateTime refDate, DateTime startTime,DateTime endTime){
        return (refDate.compareTo(startTime) >= 0) && (refDate.compareTo(endTime) < 0);
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