package com.dreameddeath.billing.model;

import com.dreameddeath.common.annotation.DocumentProperty;
import com.dreameddeath.common.model.CouchbaseDocument;
import com.dreameddeath.common.model.CouchbaseDocumentArrayList;
import com.dreameddeath.common.model.StandardProperty;
import com.dreameddeath.common.model.Property;
import com.dreameddeath.common.model.ImmutableProperty;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class BillingAccount extends CouchbaseDocument{
    @DocumentProperty("uid")
    private ImmutableProperty<String> _uid=new ImmutableProperty<String>(BillingAccount.this);
	@DocumentProperty("ledgerSegment")
    private Property<String> _ledgerSegment = new StandardProperty<String>(BillingAccount.this);
    @DocumentProperty("taxProfile")
    private Property<String> _taxProfile = new StandardProperty<String>(BillingAccount.this);
	@DocumentProperty("type")
    private Property<Type> _type= new StandardProperty<Type>(BillingAccount.this);;
	@DocumentProperty("creationDate")
    private Property<DateTime> _creationDate= new StandardProperty<DateTime>(BillingAccount.this);;
	@DocumentProperty("billDay")
    private Property<Integer> _billDay = new StandardProperty<Integer>(BillingAccount.this);;
    @DocumentProperty("billCycleLength")
    private Property<Integer> _billingCycleLength = new StandardProperty<Integer>(BillingAccount.this);;
	@DocumentProperty("currency")
    private Property<String> _currency = new StandardProperty<String>(BillingAccount.this);;
	@DocumentProperty("paymentMethod")
    private Property<String> _paymentMethod = new StandardProperty<String>(BillingAccount.this);;
    @DocumentProperty(value="billCycle",setter="setBillingCycleLinks",getter="getBillingCycleLinks")
    private List<BillingCycleLink> _billingCycleLinks = new CouchbaseDocumentArrayList<BillingCycleLink>(BillingAccount.this);
    
    public String getUid() { return _uid.get(); }
    public void setUid(String uid) { _uid.set(uid); }
    
    public String getLedgerSegment() { return _ledgerSegment.get(); }
    public void setLedgerSegment(String ledgerSegment) { _ledgerSegment.set(ledgerSegment); }
    
    public String getTaxProfile() { return _taxProfile.get(); }
    public void setTaxProfile(String taxProfile) { _taxProfile.set(taxProfile); }
    
    public Type getType() { return _type.get(); }
    public void setType(Type type) { _type.set(type); }
    
    public DateTime getCreationDate() { return _creationDate.get(); }
    public void setCreationDate(DateTime creationDate) { _creationDate.set(creationDate); }
    
    public Integer getBillDay() { return _billDay.get(); }
    public void setBillDay(Integer billDay) { _billDay.set(billDay); }
    
    public Integer getBillingCycleLength() { return _billingCycleLength.get(); }
    public void setBillingCycleLength(Integer billingCycleLength) { _billingCycleLength.set(billingCycleLength); }
    
    public String getCurrency() { return _currency.get(); }
    public void setCurrency(String currency) { _currency.set(currency); }
    
    public String getPaymentMethod() { return _paymentMethod.get(); }
    public void setPaymentMethod(String paymentMethod) { _paymentMethod.set(paymentMethod); }
    
    public List<BillingCycleLink> getBillingCycleLinks() { return Collections.unmodifiableList(_billingCycleLinks); }
    public BillingCycleLink getBillingCycleLink(DateTime refDate){
        for(BillingCycleLink billCycleLink:_billingCycleLinks){
            if(billCycleLink.isValidForDate(refDate)){
                return billCycleLink;
            }
        }
        return null;
    }
    
    public void setBillingCycleLinks(Collection<BillingCycleLink> billingCycleLinks) { _billingCycleLinks.clear();System.out.println("Adding links to ba"+billingCycleLinks); _billingCycleLinks.addAll(billingCycleLinks); }
    public void addBillingCycle(BillingCycle billingCycle){
        if(getBillingCycleLink(billingCycle.getStartDate())!=null){
            ///TODO generate a duplicate error
        }
        _billingCycleLinks.add(billingCycle.newBillingCycleLink());
        billingCycle.setBillingAccountLink(newBillingAccountLink());
    }
    
    public BillingAccountLink newBillingAccountLink(){
        return new BillingAccountLink(this);
    }
    
    /**
     * the types of billing account
     */
    public static enum Type {
        prepaid("prepaid"),
        postpaid("postpaid");
        private String _value;
        
        Type(String value){ _value = value; }
        @Override
        public String toString(){ return _value; }
    }
    
    @Override
    public String toString(){
        String result=super.toString()+",\n";
        result+="uid:"+_uid+",\n";
        result+="ledgerSegment:"+_ledgerSegment+",\n";
        result+="billingCycleLinks: "+_billingCycleLinks+"\n";
        return result;
    }
}