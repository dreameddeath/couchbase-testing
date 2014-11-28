package com.dreameddeath.billing.model.account;

import com.dreameddeath.billing.model.cycle.BillingCycleLink;
import com.dreameddeath.common.model.ExternalId;
import com.dreameddeath.core.annotation.DocumentDef;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.annotation.NotNull;
import com.dreameddeath.core.model.business.BusinessCouchbaseDocument;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.party.model.base.PartyLink;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@DocumentDef(domain = "billing",version="1.0.0",name="ba")
public class BillingAccount extends BusinessCouchbaseDocument {
    @DocumentProperty("uid") @NotNull
    private ImmutableProperty<String> _uid=new ImmutableProperty<String>(BillingAccount.this);
	@DocumentProperty("ledgerSegment")
    private Property<String> _ledgerSegment = new StandardProperty<String>(BillingAccount.this);
    @DocumentProperty("taxProfile")
    private Property<String> _taxProfile = new StandardProperty<String>(BillingAccount.this);
	@DocumentProperty("type")
    private Property<Type> _type= new StandardProperty<Type>(BillingAccount.this);
    @DocumentProperty("creationDate")
    private Property<DateTime> _creationDate= new ImmutableProperty<DateTime>(BillingAccount.this,DateTime.now());
	@DocumentProperty("billDay") @NotNull
    private Property<Integer> _billDay = new StandardProperty<Integer>(BillingAccount.this);
    @DocumentProperty("billCycleLength") @NotNull
    private Property<Integer> _billCycleLength = new StandardProperty<Integer>(BillingAccount.this);
    @DocumentProperty("currency")
    private Property<String> _currency = new StandardProperty<String>(BillingAccount.this);
    @DocumentProperty("paymentMethod")
    private Property<String> _paymentMethod = new StandardProperty<String>(BillingAccount.this);
    @DocumentProperty(value="billCycles",setter="setBillingCycleLinks",getter="getBillingCycleLinks")
    private ListProperty<BillingCycleLink> _billingCycleLinks = new ArrayListProperty<BillingCycleLink>(BillingAccount.this);
    @DocumentProperty(value="partys",setter="setPartyLinks",getter="getPartyLinks")
    private ListProperty<PartyLink> _partyLinks = new ArrayListProperty<PartyLink>(BillingAccount.this);
    /**
     *  externalIds : List of external ids of the billing account
     */
    @DocumentProperty("externalIds")
    private ListProperty<ExternalId> _externalIds = new ArrayListProperty<ExternalId>(BillingAccount.this);
    /**
     *  contributors : List of contributors links to the billing Account
     */
    @DocumentProperty("contributors")
    private ListProperty<BillingAccountContributorLink> _contributors = new ArrayListProperty<BillingAccountContributorLink>(BillingAccount.this);

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
    
    public Integer getBillCycleLength() { return _billCycleLength.get(); }
    public void setBillCycleLength(Integer billingCycleLength) { _billCycleLength.set(billingCycleLength); }
    
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
        if(getBillingCycleLink(billingCycleLink.getStartDate())!=null){
            ///TODO generate a duplicate error
        }
        _billingCycleLinks.add(billingCycleLink);
    }


    // ExternalIds Accessors
    public List<ExternalId> getExternalIds() { return _externalIds.get(); }
    public void setExternalIds(Collection<ExternalId> vals) { _externalIds.set(vals); }
    public boolean addExternalIds(ExternalId val){ return _externalIds.add(val); }
    public boolean removeExternalIds(ExternalId val){ return _externalIds.remove(val); }

    public BillingAccountLink newLink(){
        return new BillingAccountLink(this);
    }


    public List<PartyLink> getPartyLinks() { return Collections.unmodifiableList(_partyLinks); }
    public void setPartyLinks(List<PartyLink> links) { _partyLinks.clear();_partyLinks.addAll(links);}
    public void addPartyLink(PartyLink link){_partyLinks.add(link);}

    // Contributors Accessors
    public List<BillingAccountContributorLink> getContributors() { return _contributors.get(); }
    public void setContributors(Collection<BillingAccountContributorLink> vals) { _contributors.set(vals); }
    public boolean addContributors(BillingAccountContributorLink val){ return _contributors.add(val); }
    public boolean removeContributors(BillingAccountContributorLink val){ return _contributors.remove(val); }

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