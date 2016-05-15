/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.billing.model.account;

import com.dreameddeath.billing.model.cycle.BillingCycleLink;
import com.dreameddeath.common.model.ExternalId;
import com.dreameddeath.core.business.dao.BusinessCouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.business.model.BusinessDocument;
import com.dreameddeath.core.helper.annotation.dao.Counter;
import com.dreameddeath.core.helper.annotation.dao.DaoEntity;
import com.dreameddeath.core.helper.annotation.dao.UidDef;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.validation.annotation.NotNull;
import com.dreameddeath.party.model.v1.PartyLink;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@DocumentEntity(domain = "billing",name="ba",version="1.0.0")
@DaoEntity(baseDao= BusinessCouchbaseDocumentDaoWithUID.class,dbPath = "ba/",idPattern = "\\d{10}",idFormat = "%010d")
@Counter(name = "cnt",dbName = "cnt",isKeyGen = true)
@UidDef(fieldName = "uid")
public class BillingAccount extends BusinessDocument {
    @DocumentProperty("uid") @NotNull
    private ImmutableProperty<String> uid=new ImmutableProperty<>(BillingAccount.this);
	@DocumentProperty("ledgerSegment")
    private Property<String> ledgerSegment = new StandardProperty<>(BillingAccount.this);
    @DocumentProperty("taxProfile")
    private Property<String> taxProfile = new StandardProperty<>(BillingAccount.this);
	@DocumentProperty("type")
    private Property<Type> type= new StandardProperty<>(BillingAccount.this);
    @DocumentProperty("creationDate")
    private Property<DateTime> creationDate= new ImmutableProperty<>(BillingAccount.this,DateTime.now());
	@DocumentProperty("billDay") @NotNull
    private Property<Integer> billDay = new StandardProperty<>(BillingAccount.this);
    @DocumentProperty("billCycleLength") @NotNull
    private Property<Integer> billCycleLength = new StandardProperty<>(BillingAccount.this);
    @DocumentProperty("currency")
    private Property<String> currency = new StandardProperty<>(BillingAccount.this);
    @DocumentProperty("paymentMethod")
    private Property<String> paymentMethod = new StandardProperty<>(BillingAccount.this);
    @DocumentProperty(value="billCycles",setter="setBillingCycleLinks",getter="getBillingCycleLinks")
    private ListProperty<BillingCycleLink> billingCycleLinks = new ArrayListProperty<>(BillingAccount.this);
    @DocumentProperty(value="partys",setter="setPartyLinks",getter="getPartyLinks")
    private ListProperty<PartyLink> partyLinks = new ArrayListProperty<>(BillingAccount.this);
    /**
     *  externalIds : List of external ids of the billing account
     */
    @DocumentProperty("externalIds")
    private ListProperty<ExternalId> externalIds = new ArrayListProperty<>(BillingAccount.this);
    /**
     *  contributors : List of contributors links to the billing Account
     */
    @DocumentProperty("contributors")
    private ListProperty<BillingAccountContributorLink> contributors = new ArrayListProperty<>(BillingAccount.this);

    // uid Accessors
    public String getUid() { return uid.get(); }
    public void setUid(String uid) { this.uid.set(uid); }
    //
    public String getLedgerSegment() { return ledgerSegment.get(); }
    public void setLedgerSegment(String ledgerSegment) { this.ledgerSegment.set(ledgerSegment); }
    
    public String getTaxProfile() { return taxProfile.get(); }
    public void setTaxProfile(String taxProfile) { this.taxProfile.set(taxProfile); }
    
    public Type getType() { return type.get(); }
    public void setType(Type type) { this.type.set(type); }
    
    public DateTime getCreationDate() { return creationDate.get(); }
    public void setCreationDate(DateTime creationDate) { this.creationDate.set(creationDate); }
    
    public Integer getBillDay() { return billDay.get(); }
    public void setBillDay(Integer billDay) { this.billDay.set(billDay); }
    
    public Integer getBillCycleLength() { return billCycleLength.get(); }
    public void setBillCycleLength(Integer billingCycleLength) { billCycleLength.set(billingCycleLength); }
    
    public String getCurrency() { return currency.get(); }
    public void setCurrency(String currency) { this.currency.set(currency); }
    
    public String getPaymentMethod() { return paymentMethod.get(); }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod.set(paymentMethod); }
    
    public List<BillingCycleLink> getBillingCycleLinks() { return billingCycleLinks.get(); }
    public BillingCycleLink getBillingCycleLink(DateTime refDate){
        for(BillingCycleLink billCycleLink:billingCycleLinks){
            if(billCycleLink.isValidForDate(refDate)){
                return billCycleLink;
            }
        }
        return null;
    }
    
    public void setBillingCycleLinks(Collection<BillingCycleLink> billingCycleLinks){this.billingCycleLinks.set(billingCycleLinks);}

    public void addBillingCycleLink(BillingCycleLink billingCycleLink){
        if(getBillingCycleLink(billingCycleLink.getStartDate())!=null){
            ///TODO generate a duplicate error
        }
        billingCycleLinks.add(billingCycleLink);
    }


    // ExternalIds Accessors
    public List<ExternalId> getExternalIds() { return externalIds.get(); }
    public void setExternalIds(Collection<ExternalId> vals) { externalIds.set(vals); }
    public boolean addExternalIds(ExternalId val){ return externalIds.add(val); }
    public boolean removeExternalIds(ExternalId val){ return externalIds.remove(val); }

    public BillingAccountLink newLink(){
        return new BillingAccountLink(this);
    }


    public List<PartyLink> getPartyLinks() { return Collections.unmodifiableList(partyLinks); }
    public void setPartyLinks(List<PartyLink> links) { partyLinks.clear();partyLinks.addAll(links);}
    public void addPartyLink(PartyLink link){partyLinks.add(link);}

    // Contributors Accessors
    public List<BillingAccountContributorLink> getContributors() { return contributors.get(); }
    public void setContributors(Collection<BillingAccountContributorLink> vals) { contributors.set(vals); }
    public boolean addContributors(BillingAccountContributorLink val){ return contributors.add(val); }
    public boolean removeContributors(BillingAccountContributorLink val){ return contributors.remove(val); }

    /**
     * the types of billing account
     */
    public enum Type {
        prepaid("prepaid"),
        postpaid("postpaid");
        private String value;
        
        Type(String value){ this.value = value; }
        @Override
        public String toString(){ return value; }
    }
    
    @Override
    public String toString(){
        String result=super.toString()+",\n";
        result+="uid:"+uid+",\n";
        result+="ledgerSegment:"+ledgerSegment+",\n";
        result+="billingCycleLinks: "+billingCycleLinks+"\n";
        return result;
    }
}