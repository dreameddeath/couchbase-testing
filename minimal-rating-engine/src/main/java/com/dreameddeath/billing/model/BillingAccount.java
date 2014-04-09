package com.dreameddeath.billing.model;

import java.util.Collection;
import java.util.Collections;
//import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.dreameddeath.common.model.CouchbaseDocument;
import com.dreameddeath.common.model.CouchbaseDocumentArrayList;

@JsonInclude(Include.NON_EMPTY)
public class BillingAccount extends CouchbaseDocument{
    @JsonProperty("uid")
    private String _uid;
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
    
    
    public String getUid() { return _uid; }
    public void setUid(String uid) { _uid=uid; }
    
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
    
    public Collection<BillingCycleLink> getBillingCycles() { return Collections.unmodifiableCollection(_billingCycleLinks); }
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