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

package com.dreameddeath.installedbase.model.common;

import com.dreameddeath.core.business.model.BusinessDocumentLink;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;

/**
 * Created by Christophe Jeunesse on 10/08/2014.
 */
public class InstalledBaseLink extends BusinessDocumentLink<InstalledBase> {
    /**
     *  offerId : The offer id linked (if only a subpart is pointed by the link)
     */
    @DocumentProperty("offerId")
    private Property<String> offerId = new StandardProperty<String>(InstalledBaseLink.this);
    /**
     *  productId : The installedProductId (if only a sub part of the installed base is pointed by the link)
     */
    @DocumentProperty("productId")
    private Property<String> productId = new StandardProperty<String>(InstalledBaseLink.this);


    // offerId accessors
    public String getOfferId() { return offerId.get(); }
    public void setOfferId(String val) { offerId.set(val); }

    // productId accessors
    public String getProductId() { return productId.get(); }
    public void setProductId(String val) { productId.set(val); }

    public InstalledBaseLink(){}
    public InstalledBaseLink (InstalledBase installedBase){
        super(installedBase);
    }
    public InstalledBaseLink(InstalledBaseLink srcLink){
        super(srcLink);
    }

    @Override
    public String toString(){
        String result = "{\n"+super.toString()+",\n";
        result+="}\n";
        return result;
    }
}
