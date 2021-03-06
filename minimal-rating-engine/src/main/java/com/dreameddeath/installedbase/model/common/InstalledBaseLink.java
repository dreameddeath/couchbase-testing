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

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.business.CouchbaseDocumentLink;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;

/**
 * Created by Christophe Jeunesse on 10/08/2014.
 */
public class InstalledBaseLink extends CouchbaseDocumentLink<InstalledBase> {
    /**
     *  offerId : The offer id linked (if only a subpart is pointed by the link)
     */
    @DocumentProperty("offerId")
    private Property<String> _offerId = new StandardProperty<String>(InstalledBaseLink.this);
    /**
     *  productId : The installedProductId (if only a sub part of the installed base is pointed by the link)
     */
    @DocumentProperty("productId")
    private Property<String> _productId = new StandardProperty<String>(InstalledBaseLink.this);


    // offerId accessors
    public String getOfferId() { return _offerId.get(); }
    public void setOfferId(String val) { _offerId.set(val); }

    // productId accessors
    public String getProductId() { return _productId.get(); }
    public void setProductId(String val) { _productId.set(val); }

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
