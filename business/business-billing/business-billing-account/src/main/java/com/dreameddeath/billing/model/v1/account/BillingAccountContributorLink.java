/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.billing.model.v1.account;

import com.dreameddeath.billing.model.v1.account.BillingAccountContributor.ContributorType;
import com.dreameddeath.core.business.model.BusinessDocumentLink;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.dto.annotation.processor.model.SuperClassGenMode;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.query.annotation.QueryExpose;

/**
 * Created by Christophe Jeunesse on 25/11/2014.
 */
@QueryExpose(rootPath = "", notDirecltyExposed = true,superClassGenMode = SuperClassGenMode.UNWRAP)
public class BillingAccountContributorLink extends BusinessDocumentLink<BillingAccountContributor> {
    /**
     *  type : Type of contributor
     */
    @DocumentProperty("type")
    private Property<ContributorType> type = new ImmutableProperty<>(BillingAccountContributorLink.this);
    /**
     *  sourceKey : The source key being used as a ref for contributor
     */
    @DocumentProperty("sourceKey")
    private Property<String> sourceKey = new ImmutableProperty<>(BillingAccountContributorLink.this);

    public BillingAccountContributorLink(){}
    public BillingAccountContributorLink (BillingAccountContributor contributor){
        super(contributor);
        this.setType(contributor.getContributorType());
    }
    public BillingAccountContributorLink(BillingAccountContributorLink srcLink){
        super(srcLink);
        this.setType(srcLink.getType());
    }

    // type accessors
    public BillingAccountContributor.ContributorType getType() { return type.get(); }
    public void setType(BillingAccountContributor.ContributorType val) { type.set(val); }

    /**
     * Getter of sourceKey
     * @return the value of sourceKey
     */
    public String getSourceKey() { return sourceKey.get(); }
    /**
     * Setter of sourceKey
     * @param val the new value for sourceKey
     */
    public void setSourceKey(String val) { sourceKey.set(val); }
}
