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
import com.dreameddeath.core.model.property.impl.StandardProperty;
import org.joda.time.DateTime;

public class RatingContextAttributeValue extends BaseCouchbaseDocumentElement {
    @DocumentProperty("value")
    private Property<String>   _value=new StandardProperty<String>(RatingContextAttributeValue.this);
    @DocumentProperty("startDate")
    private Property<DateTime> _startDate=new StandardProperty<DateTime>(RatingContextAttributeValue.this);
    @DocumentProperty("endDate")
    private Property<DateTime> _endDate=new StandardProperty<DateTime>(RatingContextAttributeValue.this);
    
    public String getValue(){ return _value.get();}
    public void setValue(String value){ _value.set(value); }
 
    public DateTime getStartDate(){ return _startDate.get();}
    public void setStartDate(DateTime startDate){ _startDate.set(startDate); }
 
    public DateTime getEndDate(){ return _endDate.get();}
    public void setEndDate(DateTime endDate){ _endDate.set(endDate); }
 
}