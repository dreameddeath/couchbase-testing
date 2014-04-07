package com.dreameddeath.billing.model;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.dreameddeath.common.model.CouchbaseDocument;


@JsonInclude(Include.NON_EMPTY)
public class BillingAccount extends CouchbaseDocument{
    private String _uid;
	private String _ledgerSegment;
	private String _taxProfile;
	private Type _type;
	private DateTime _creationDate;
	private int _billDay;
	private int _billingCycleLength;
	private String _currency;
	private String _paymentMethod;
    private List<BillingCycleLink> _billingCycleLinks = new ArrayList<BillingCycleLink>();
    
    @JsonProperty("uid")
    public String getUid() { return _uid; }
    public void setUid(String uid) { _uid=uid; }
    
    @JsonProperty("ledgerSegment")
    public String getLedgerSegment() { return _ledgerSegment; }
    public void setLedgerSegment(String ledgerSegment) { _ledgerSegment=ledgerSegment; }
    
    @JsonProperty("taxProfile")
    public String getTaxProfile() { return _taxProfile; }
    public void setTaxProfile(String taxProfile) { _taxProfile=taxProfile; }
    
    @JsonProperty("type")
    public Type getType() { return _type; }
    public void setType(Type type) { _type=type; }
    
    @JsonProperty("creationDate")
    public DateTime getCreationDate() { return _creationDate; }
    public void setCreationDate(DateTime creationDate) { _creationDate=creationDate; }
    
    @JsonProperty("billDay")
    public int getBillDay() { return _billDay; }
    public void setBillDay(int billDay) { _billDay=billDay; }
    
    @JsonProperty("billCycleLength")
    public int getBillingCycleLength() { return _billingCycleLength; }
    public void setBillingCycleLength(int billingCycleLength) { _billingCycleLength=billingCycleLength; }
    
    @JsonProperty("currency")
    public String getCurrency() { return _currency; }
    public void setCurrency(String currency) { _currency=currency; }
    
    @JsonProperty("paymentMethod")
    public String getPaymentMethod() { return _paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { _paymentMethod=paymentMethod; }
    
    @JsonProperty("billingPeriods")
    public Collection<BillingCycleLink> getBillingCycles() { return _billingCycleLinks; }
    public void setBillingCycles(Collection<BillingCycleLink> billingCycleLinks) { _billingCycleLinks.clear();_billingCycleLinks.addAll(billingCycleLinks); }
    public void addBillingCycle(BillingCycleLink billingCycleLink) { _billingCycleLinks.add(billingCycleLink); }
    
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