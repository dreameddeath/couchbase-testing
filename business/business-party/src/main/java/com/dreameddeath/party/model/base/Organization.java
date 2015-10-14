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

package com.dreameddeath.party.model.base;

import com.dreameddeath.core.model.annotation.DocumentDef;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
@DocumentDef(domain = "party",version="1.0.0",name="organization")
public class Organization extends Party {
    @DocumentProperty("tradingName")
    private Property<String> tradingName=new StandardProperty<String>(Organization.this);
    @DocumentProperty("brand")
    private Property<String> brand=new StandardProperty<String>(Organization.this);

    public String getTradingName(){return tradingName.get();}
    public void setTradingName(String name){tradingName.set(name);}

    public String getBrand(){return brand.get();}
    public void setBrand(String name){brand.set(name);}

}
