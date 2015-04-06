/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.installedbase.model.tariff;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.installedbase.model.common.InstalledItem;

import java.util.Collection;
import java.util.List;

/**
 * Created by ceaj8230 on 10/08/2014.
 */
public class InstalledTariff extends InstalledItem<InstalledTariffRevision> {
    /**
     *  discounts : list of discounts attached to the tariff
     */
    @DocumentProperty("discounts")
    private ListProperty<InstalledDiscount> _discounts = new ArrayListProperty<InstalledDiscount>(InstalledTariff.this);

    // Discounts Accessors
    public List<InstalledDiscount> getDiscounts() { return _discounts.get(); }
    public void setDiscounts(Collection<InstalledDiscount> vals) { _discounts.set(vals); }
    public boolean addDiscounts(InstalledDiscount val){ return _discounts.add(val); }
    public boolean removeDiscounts(InstalledDiscount val){ return _discounts.remove(val); }

}
