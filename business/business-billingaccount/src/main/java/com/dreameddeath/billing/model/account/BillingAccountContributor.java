package com.dreameddeath.billing.model.account;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.annotation.NotNull;
import com.dreameddeath.core.model.business.BusinessCouchbaseDocument;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;

/**
 * Created by CEAJ8230 on 25/11/2014.
 */
public abstract class BillingAccountContributor extends BusinessCouchbaseDocument{
    /**
     *  contributorType : Type of contributor to the billing Account
     */
    @DocumentProperty("contributorType") @NotNull
    private Property<ContributorType> _contributorType = new StandardProperty<ContributorType>(BillingAccountContributor.this);

    // contributorType accessors
    public ContributorType getContributorType() { return _contributorType.get(); }
    public void setContributorType(ContributorType val) { _contributorType.set(val); }

    public enum ContributorType{
        RECURRING,
        ONESHOT
    }

    public BillingAccountContributorLink newLink(){return new BillingAccountContributorLink(this);}
}
