/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.billing.model.v1.account;

import com.dreameddeath.billing.model.v1.account.BillingAccountContributor.ContributorType;
import com.dreameddeath.core.business.model.BusinessDocumentLink;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;

/**
 * Created by Christophe Jeunesse on 25/11/2014.
 */
public class BillingAccountContributorLink extends BusinessDocumentLink<BillingAccountContributor> {
    /**
     *  type : Type of contributor
     */
    @DocumentProperty("type")
    private Property<ContributorType> type = new ImmutableProperty<>(BillingAccountContributorLink.this);

    // type accessors
    public BillingAccountContributor.ContributorType getType() { return type.get(); }
    public void setType(BillingAccountContributor.ContributorType val) { type.set(val); }


    public BillingAccountContributorLink(){}
    public BillingAccountContributorLink (BillingAccountContributor contributor){
        super(contributor);
        this.setType(contributor.getContributorType());
    }
    public BillingAccountContributorLink(BillingAccountContributorLink srcLink){
        super(srcLink);
        this.setType(srcLink.getType());
    }

}
