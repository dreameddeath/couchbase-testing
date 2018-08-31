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

package com.dreameddeath.billing.installedbase.service.model.v1;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;

import java.util.List;

/**
 * Created by Christophe Jeunesse on 26/09/2017.
 */
public class CreateUpdateBillingInstalledBaseTariffItemResult extends CreateUpdateBillingInstalledBaseItemResult {
    /**
     * tariffId : The id of the tariff being impact
     */
    @DocumentProperty("tariffId")
    private Property<String> tariffId = new ImmutableProperty<>(CreateUpdateBillingInstalledBaseTariffItemResult.this);

    /**
     * addedDiscounts : List of discounts being added
     */
    @DocumentProperty("addedDiscounts")
    private ListProperty<String> addedDiscounts= new ArrayListProperty<>(CreateUpdateBillingInstalledBaseTariffItemResult.this);

    /**
     * Getter of the attribute {@link #tariffId}
     * return the currentValue of {@link #tariffId}
     */
    public String getTariffId(){
        return this.tariffId.get();
    }

    /**
     * Setter of the attribute {@link #tariffId}
     * @param newValue the newValue of {@link #tariffId}
     */
    public void setTariffId(String newValue){
        this.tariffId.set(newValue);
    }

    public void setAddedDiscounts(List<String> discounts){
        this.addedDiscounts.set(discounts);
    }

    public List<String> getAddedDiscount(){
        return this.addedDiscounts.get();
    }

    public void addDiscount(String discountId){
        this.addedDiscounts.add(discountId);
    }

}
