package com.dreameddeath.billing.model.account;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.business.BusinessCouchbaseDocumentLink;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.billing.model.account.BillingAccountContributor.ContributorType;
import com.dreameddeath.core.model.property.impl.SynchronizedLinkProperty;

/**
 * Created by CEAJ8230 on 25/11/2014.
 */
public class BillingAccountContributorLink extends BusinessCouchbaseDocumentLink<BillingAccountContributor> {
    /**
     *  type : Type of contributor
     */
    @DocumentProperty("type")
    private Property<ContributorType> _type = new SynchronizedLinkProperty<ContributorType,BillingAccountContributor>(BillingAccountContributorLink.this){
        @Override
        protected  ContributorType getRealValue(BillingAccountContributor doc){
            return doc.getContributorType();
        }
    };

    // type accessors
    public BillingAccountContributor.ContributorType getType() { return _type.get(); }
    public void setType(BillingAccountContributor.ContributorType val) { _type.set(val); }


    public BillingAccountContributorLink(){}
    public BillingAccountContributorLink (BillingAccountContributor contributor){
        super(contributor);
    }
    public BillingAccountContributorLink(BillingAccountContributorLink srcLink){
        super(srcLink);
    }

}
