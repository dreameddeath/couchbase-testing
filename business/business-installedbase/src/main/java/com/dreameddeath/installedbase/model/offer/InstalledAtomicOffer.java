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

package com.dreameddeath.installedbase.model.offer;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.installedbase.model.productservice.InstalledProductService;

/**
 * Created by Christophe Jeunesse on 21/10/2014.
 */
public class InstalledAtomicOffer extends InstalledOffer {
    /**
     *  product : Installed Product
     */
    @DocumentProperty("product")
    private Property<InstalledProductService> product = new StandardProperty<>(InstalledAtomicOffer.this);

    // product accessors
    public InstalledProductService getProduct() { return product.get(); }
    public void setProduct(InstalledProductService val) { product.set(val); }
}
