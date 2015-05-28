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

package com.dreameddeath.billing.model.installedbase;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 12/08/2014.
 */
public class BillingInstalledBaseRatingContext extends BillingInstalledBaseItem {
    /**
     *  productIds : List of product ids linked to this context
     */
    @DocumentProperty("productIds")
    private ListProperty<String> _productIds = new ArrayListProperty<String>(BillingInstalledBaseRatingContext.this);

    // ProductIds Accessors
    public List<String> getProductIds() { return _productIds.get(); }
    public void setProductIds(Collection<String> vals) { _productIds.set(vals); }
    public boolean addProductIds(String val){ return _productIds.add(val); }
    public boolean removeProductIds(String val){ return _productIds.remove(val); }

}
