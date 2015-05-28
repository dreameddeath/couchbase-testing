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

import com.dreameddeath.core.business.model.BusinessDocument;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.validation.annotation.NotNull;

/**
 * Created by Christophe Jeunesse on 25/11/2014.
 */
public abstract class BillingAccountContributor extends BusinessDocument {
    /**
     *  contributorType : Type of contributor to the billing Account
     */
    @DocumentProperty("contributorType") @NotNull
    private Property<ContributorType> _contributorType = new StandardProperty<>(BillingAccountContributor.this);

    // contributorType accessors
    public ContributorType getContributorType() { return _contributorType.get(); }
    public void setContributorType(ContributorType val) { _contributorType.set(val); }

    public enum ContributorType{
        RECURRING,
        ONESHOT
    }

    public BillingAccountContributorLink newLink(){return new BillingAccountContributorLink(this);}
}
