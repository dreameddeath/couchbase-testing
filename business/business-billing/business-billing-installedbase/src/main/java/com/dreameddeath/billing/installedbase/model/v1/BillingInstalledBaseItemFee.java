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
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 12/08/2014.
 */
public class BillingInstalledBaseItemFee extends BillingInstalledBaseItem {
    /**
     * tariffId : The id of the corresponding tariff item in the installed base
     */
    @DocumentProperty("tariffId")
    private Property<String> tariffId = new ImmutableProperty<>(BillingInstalledBaseItemFee.this);
    /**
     * code : the tariff catalogue code if existing
     */
    @DocumentProperty("code")
    private Property<String> code = new ImmutableProperty<>(BillingInstalledBaseItemFee.this);
    /**
     *  discountsIds : List of discount items applicable
     */
    @DocumentProperty("discountsIds")
    private ListProperty<Long> discountsIds = new ArrayListProperty<Long>(BillingInstalledBaseItemFee.this);

    /**
     * Getter of the attribute {@link #code}
     * return the currentValue of {@link #code}
     */
    public String getCode(){
        return this.code.get();
    }

    /**
     * Setter of the attribute {@link #code}
     * @param newValue the newValue of {@link #code}
     */
    public void setCode(String newValue){
        this.code.set(newValue);
    }
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


    // DiscountsIds Accessors
    public List<Long> getDiscountsIds() { return discountsIds.get(); }
    public void setDiscountsIds(Collection<Long> vals) { discountsIds.set(vals); }
    public boolean addDiscountsIds(Long val){ return discountsIds.add(val); }
    public boolean removeDiscountsIds(Long val){ return discountsIds.remove(val); }
}
