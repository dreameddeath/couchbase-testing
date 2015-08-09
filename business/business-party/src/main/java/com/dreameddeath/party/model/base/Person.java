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
import com.dreameddeath.core.validation.annotation.NotNull;
import org.joda.time.DateTime;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
@DocumentDef(domain = "party",version="1.0.0")
public class Person extends Party {
    @DocumentProperty("firstName") @NotNull
    private Property<String> _firstName=new StandardProperty<String>(Person.this);
    @DocumentProperty("lastName") @NotNull
    private Property<String> _lastName=new StandardProperty<String>(Person.this);
    @DocumentProperty("birthDate")
    private Property<DateTime> _birthDate = new StandardProperty<DateTime>(Person.this);

    public String getFirstName(){return _firstName.get();}
    public void setFirstName(String firstName){_firstName.set(firstName);}

    public String getLastName(){return _lastName.get();}
    public void setLastName(String lastName){_lastName.set(lastName);}

    public DateTime getBirthDate() { return _birthDate.get(); }
    public void setBirthDate(DateTime val) { _birthDate.set(val); }

}
