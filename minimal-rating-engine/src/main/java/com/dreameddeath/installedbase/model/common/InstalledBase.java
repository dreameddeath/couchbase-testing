package com.dreameddeath.installedbase.model.common;

import com.dreameddeath.billing.model.account.BillingAccountLink;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.ArrayListProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.StandardProperty;
import com.dreameddeath.installedbase.model.contract.InstalledContract;
import com.dreameddeath.installedbase.model.offer.InstalledOffer;
import com.dreameddeath.party.model.PartyLink;

import java.util.Collection;
import java.util.List;

/**
 * Created by ceaj8230 on 10/08/2014.
 */
public class InstalledBase extends CouchbaseDocument {
    /**
     *  contract : The installed Contract for this installed base (if any)
     */
    @DocumentProperty("contract")
    private Property<InstalledContract> _contract = new StandardProperty<InstalledContract>(InstalledBase.this);
    /**
     *  offers : List of offers being installed
     */
    @DocumentProperty("offers")
    private ListProperty<InstalledOffer> _offers = new ArrayListProperty<InstalledOffer>(InstalledBase.this);
    /**
     *  partys : List of linked Partys
     */
    @DocumentProperty("partys")
    private ListProperty<PartyLink> _partys = new ArrayListProperty<PartyLink>(InstalledBase.this);
    /**
     *  billingAccount : Default Billing Account linked to this installed Base
     */
    @DocumentProperty("billingAccount")
    private Property<BillingAccountLink> _billingAccount = new StandardProperty<BillingAccountLink>(InstalledBase.this);

    // contract accessors
    public InstalledContract getContract() { return _contract.get(); }
    public void setContract(InstalledContract val) { _contract.set(val); }

    // Offers Accessors
    public List<InstalledOffer> getOffers() { return _offers.get(); }
    public void setOffers(Collection<InstalledOffer> vals) { _offers.set(vals); }
    public boolean addOffers(InstalledOffer val){ return _offers.add(val); }
    public boolean removeOffers(InstalledOffer val){ return _offers.remove(val); }

    // Partys Accessors
    public List<PartyLink> getPartys() { return _partys.get(); }
    public void setPartys(Collection<PartyLink> vals) { _partys.set(vals); }
    public boolean addPartys(PartyLink val){ return _partys.add(val); }
    public boolean removePartys(PartyLink val){ return _partys.remove(val); }

    // billingAccount accessors
    public BillingAccountLink getBillingAccount() { return _billingAccount.get(); }
    public void setBillingAccount(BillingAccountLink val) { _billingAccount.set(val); }

}
