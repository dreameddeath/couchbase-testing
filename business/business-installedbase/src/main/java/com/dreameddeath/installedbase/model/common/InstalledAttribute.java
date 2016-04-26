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

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.validation.annotation.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 10/08/2014.
 */
public abstract class InstalledAttribute extends CouchbaseDocumentElement {
    /**
     *  code : The catalogue code of the attribute
     */
    @DocumentProperty("code") @NotNull
    private Property<String> code = new StandardProperty<>(InstalledAttribute.this);
    /**
     *  values : List of values (and their history)
     */
    @DocumentProperty("values")
    private ListProperty<InstalledValue> values = new ArrayListProperty<>(InstalledAttribute.this);

    // code accessors
    public String getCode() { return code.get(); }
    public void setCode(String val) { code.set(val); }

    // Values Accessors
    public List<InstalledValue> getValues() { return values.get(); }
    public void setValues(Collection<InstalledValue> vals) { values.set(vals); }
    public boolean addValues(InstalledValue val){ return values.add(val); }
    public void sortValues(){
        values.sort((a,b)->{
            int res=(a.getStartDate().compareTo(b.getStartDate()));
            if(res==0) res=a.getEndDate().compareTo(b.getEndDate());
            if(res==0) res=a.getValue().compareTo(b.getValue());
            return res;
        });
    }
    public boolean removeValues(InstalledValue val){ return values.remove(val); }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstalledAttribute that = (InstalledAttribute) o;

        return code.equals(that.code);

    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
