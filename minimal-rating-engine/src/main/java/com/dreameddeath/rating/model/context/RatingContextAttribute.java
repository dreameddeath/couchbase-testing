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

package com.dreameddeath.rating.model.context;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;

import java.util.Collections;
import java.util.List;

public class RatingContextAttribute extends BaseCouchbaseDocumentElement {
    @DocumentProperty("code")
    private Property<String> _code=new StandardProperty<String>(RatingContextAttribute.this);
    @DocumentProperty("values")
    private List<RatingContextAttributeValue> _values=new ArrayListProperty<RatingContextAttributeValue>(RatingContextAttribute.this);
    
    public String getCode(){ return _code.get();}
    public void setCode(String code){ _code.set(code); }

    public List<RatingContextAttributeValue> getValues(){return Collections.unmodifiableList(_values);}
    public void setValues(List<RatingContextAttributeValue> values){ _values.clear();_values.addAll(values); }
    public void addValues(List<RatingContextAttributeValue> values){ _values.addAll(values); }
    public void addValue(RatingContextAttributeValue value){ _values.add(value); }

 
}