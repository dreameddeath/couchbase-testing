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

package com.dreameddeath.installedbase.model.common;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by ceaj8230 on 10/08/2014.
 */
public abstract class InstalledAttribute extends BaseCouchbaseDocumentElement {
    /**
     *  code : The catalogue code of the attribute
     */
    @DocumentProperty("code")
    private Property<String> _code = new StandardProperty<String>(InstalledAttribute.this);
    /**
     *  values : List of values (and their history)
     */
    @DocumentProperty("values")
    private ListProperty<InstalledValue> _values = new ArrayListProperty<InstalledValue>(InstalledAttribute.this);

    // code accessors
    public String getCode() { return _code.get(); }
    public void setCode(String val) { _code.set(val); }

    // Values Accessors
    public List<InstalledValue> getValues() { return _values.get(); }
    public void setValues(Collection<InstalledValue> vals) { _values.set(vals); }
    public boolean addValues(InstalledValue val){ return _values.add(val); }
    public boolean removeValues(InstalledValue val){ return _values.remove(val); }

}
