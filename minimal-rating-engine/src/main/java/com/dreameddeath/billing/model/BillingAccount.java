package com.dreameddeath.billing.model;

import java.util.Collection;
import java.util.Collections;
//import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.dreameddeath.common.model.CouchbaseDocument;
import com.dreameddeath.common.model.CouchbaseDocumentArrayList;
import com.dreameddeath.common.model.ImmutableProperty;

public class BillingAccount extends CouchbaseDocument{
    private ImmutableProperty<String> _uid=new ImmutableProperty<String>(BillingAccount.this);
	@JsonProperty("ledgerSegment")
    private String _ledgerSegment;
    @JsonProperty("taxProfile")
	private String _taxProfile;
	@JsonProperty("type")
    private Type _type;
	@JsonProperty("creationDate")
    private DateTime _creationDate;
	@JsonProperty("billDay")
    private Integer _billDay;
	@JsonProperty("billCycleLength")
    private Integer _billingCycleLength;
	@JsonProperty("currency")
    private String _currency;
	@JsonProperty("paymentMethod")
    private String _paymentMethod;
    @JsonProperty("billingPeriods")
    private List<BillingCycleLink> _billingCycleLinks = new CouchbaseDocumentArrayList<BillingCycleLink>(BillingAccount.this);
    
    @JsonProperty("uid")
    public String getUid() { return _uid.get(); }
    public void setUid(String uid) { _uid.set(uid); }
    
    public String getLedgerSegment() { return _ledgerSegment; }
    public void setLedgerSegment(String ledgerSegment) { _ledgerSegment=ledgerSegment; }
    
    public String getTaxProfile() { return _taxProfile; }
    public void setTaxProfile(String taxProfile) { _taxProfile=taxProfile; }
    
    public Type getType() { return _type; }
    public void setType(Type type) { _type=type; }
    
    public DateTime getCreationDate() { return _creationDate; }
    public void setCreationDate(DateTime creationDate) { _creationDate=creationDate; }
    
    public Integer getBillDay() { return _billDay; }
    public void setBillDay(Integer billDay) { _billDay=billDay; }
    
    public Integer getBillingCycleLength() { return _billingCycleLength; }
    public void setBillingCycleLength(Integer billingCycleLength) { _billingCycleLength=billingCycleLength; }
    
    public String getCurrency() { return _currency; }
    public void setCurrency(String currency) { _currency=currency; }
    
    public String getPaymentMethod() { return _paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { _paymentMethod=paymentMethod; }
    
    public Collection<BillingCycleLink> getBillingCycleLinks() { return Collections.unmodifiableCollection(_billingCycleLinks); }
    public BillingCycleLink getBillingCycleLink(DateTime refDate){
        for(BillingCycleLink billCycleLink:_billingCycleLinks){
            if(billCycleLink.isValidForDate(refDate)){
                return billCycleLink;
            }
        }
        return null;
    }
    public void setBillingCycleLinks(Collection<BillingCycleLink> billingCycleLinks) { _billingCycleLinks.clear();_billingCycleLinks.addAll(billingCycleLinks); }
    public void addBillingCycle(BillingCycle billingCycle){
        if(getBillingCycleLink(billingCycle.getStartDate())!=null){
            ///TODO generate an error
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
        public String toString(){ return _value; }
    }
    

}