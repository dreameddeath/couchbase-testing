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
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;

/**
 * Created by Christophe Jeunesse on 26/09/2017.
 */
public class CreateUpdateBillingInstalledBaseDiscountItemResult extends CreateUpdateBillingInstalledBaseItemResult{
    /**
     * discountId : the id of the discount being modified
     */
    @DocumentProperty("discountId")
    private Property<String> discountId = new ImmutableProperty<>(CreateUpdateBillingInstalledBaseDiscountItemResult.this);

    /**
     * Getter of the attribute {@link #discountId}
     * return the currentValue of {@link #discountId}
     */
    public String getDiscountId(){
        return this.discountId.get();
    }

    /**
     * Setter of the attribute {@link #discountId}
     * @param newValue the newValue of {@link #discountId}
     */
    public void setDiscountId(String newValue){
        this.discountId.set(newValue);
    }


}
