package com.dreameddeath.billing.model.account;

import com.dreameddeath.billing.model.cycle.BillingCycleLink;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.*;
import com.dreameddeath.installedbase.model.InstalledBaseLink;
import com.dreameddeath.party.model.PartyLink;
import com.sun.istack.internal.NotNull;
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
    private Property<DateTime> _creationDate= new ImmutableProperty<DateTime>(BillingAccount.this,DateTime.now());
	@DocumentProperty("billDay")
    private Property<Integer> _billDay = new StandardProperty<Integer>(BillingAccount.this);;
    @DocumentProperty("billCycleLength")
    private Property<Integer> _billingCycleLength = new StandardProperty<Integer>(BillingAccount.this);;
	@DocumentProperty("currency")
    private Property<String> _currency = new StandardProperty<String>(BillingAccount.this);;
	@DocumentProperty("paymentMethod")
    private Property<String> _paymentMethod = new StandardProperty<String>(BillingAccount.this);;
    @DocumentProperty(value="billCycles",setter="setBillingCycleLinks",getter="getBillingCycleLinks")
    private ListProperty<BillingCycleLink> _billingCycleLinks = new ArrayListProperty<BillingCycleLink>(BillingAccount.this);
    @DocumentProperty(value="partys",setter="setPartyLinks",getter="getPartyLinks")
    private ListProperty<PartyLink> _partyLinks = new ArrayListProperty<PartyLink>(BillingAccount.this);
    /**
     *  installedBases : List of installed base (or parts of installed base) to bill
     */
    @DocumentProperty("installedBases")
    private ListProperty<InstalledBaseLink> _installedBases = new ArrayListProperty<InstalledBaseLink>(BillingAccount.this);
    // uid Accessors
    public String getUid() { return _uid.get(); }
    public void setUid(String uid) { _uid.set(uid); }
    //
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
    
    public List<BillingCycleLink> getBillingCycleLinks() { return _billingCycleLinks.get(); }
    public BillingCycleLink getBillingCycleLink(DateTime refDate){
        for(BillingCycleLink billCycleLink:_billingCycleLinks){
            if(billCycleLink.isValidForDate(refDate)){
                return billCycleLink;
            }
        }
        return null;
    }
    
    public void setBillingCycleLinks(Collection<BillingCycleLink> billingCycleLinks){_billingCycleLinks.set(billingCycleLinks);}

    public void addBillingCycleLink(BillingCycleLink billingCycleLink){
        if(getBillingCycleLink(billingCycleLink.getLinkedObject().getStartDate())!=null){
            ///TODO generate a duplicate error
        }
        _billingCycleLinks.add(billingCycleLink);
    }

    // InstalledBases Accessors
    public List<InstalledBaseLink> getInstalledBases() { return _installedBases.get(); }
    public void setInstalledBases(Collection<InstalledBaseLink> vals) { _installedBases.set(vals); }
    public boolean addInstalledBases(InstalledBaseLink val){ return _installedBases.add(val); }
    public boolean removeInstalledBases(InstalledBaseLink val){ return _installedBases.remove(val); }


    public BillingAccountLink newLink(){
        return new BillingAccountLink(this);
    }


    public List<PartyLink> getPartyLinks() { return Collections.unmodifiableList(_partyLinks); }
    public void getPartyLinks(List<PartyLink> links) { _partyLinks.clear();_partyLinks.addAll(links);}
    public void addPartyLink(PartyLink link){_partyLinks.add(link);}

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