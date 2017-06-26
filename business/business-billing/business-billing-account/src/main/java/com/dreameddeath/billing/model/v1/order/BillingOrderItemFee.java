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

package com.dreameddeath.billing.model.v1.order;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 01/09/2014.
 */
public class BillingOrderItemFee extends BillingOrderItem {
    /**
     *  tariffId : The tariff id being billed
     */
    @DocumentProperty("tariffId")
    private Property<String> tariffId = new StandardProperty<>(BillingOrderItemFee.this);
    /**
     *  discountIds : The list of applicable billing item ids
     */
    @DocumentProperty("discountIds")
    private ListProperty<Long> discountIds = new ArrayListProperty<>(BillingOrderItemFee.this);

    // tariffId accessors
    public String getTariffId() { return tariffId.get(); }
    public void setTariffId(String val) { tariffId.set(val); }
    // DiscountIds Accessors
    public List<Long> getDiscountIds() { return discountIds.get(); }
    public void setDiscountIds(Collection<Long> vals) { discountIds.set(vals); }
    public boolean addDiscountIds(Long val){ return discountIds.add(val); }
    public boolean removeDiscountIds(Long val){ return discountIds.remove(val); }


}
