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

package com.dreameddeath.billing.installedbase.model.v1;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;

/**
 * Created by Christophe Jeunesse on 12/08/2014.
 */
public class BillingInstalledBaseItemDiscount extends BillingInstalledBaseItem {
    /**
     *  discountId : The installed base discount id being billed
     */
    @DocumentProperty("discountId")
    private Property<String> discountId = new StandardProperty<String>(BillingInstalledBaseItemDiscount.this);

    // discountId accessors
    public String getDiscountId() { return discountId.get(); }
    public void setDiscountId(String val) { discountId.set(val); }

}
