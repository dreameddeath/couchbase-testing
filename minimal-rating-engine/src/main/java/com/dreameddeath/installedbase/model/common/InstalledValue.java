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
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import org.joda.time.DateTime;

/**
 * Created by ceaj8230 on 10/08/2014.
 */
public class InstalledValue extends BaseCouchbaseDocumentElement {
    /**
     *  value : the value itself
     */
    @DocumentProperty("value")
    private Property<String> _value = new StandardProperty<String>(InstalledValue.this);
    /**
     *  startDate : The value startDate
     */
    @DocumentProperty("startDate")
    private Property<DateTime> _startDate = new StandardProperty<DateTime>(InstalledValue.this);
    /**
     *  endDate : The value endDate
     */
    @DocumentProperty("endDate")
    private Property<DateTime> _endDate = new StandardProperty<DateTime>(InstalledValue.this);
    /**
     *  keyType : Type of public key if applicable
     */
    @DocumentProperty("keyType")
    private Property<String> _keyType = new StandardProperty<String>(InstalledValue.this);

    // value accessors
    public String getValue() { return _value.get(); }
    public void setValue(String val) { _value.set(val); }

    // startDate accessors
    public DateTime getStartDate() { return _startDate.get(); }
    public void setStartDate(DateTime val) { _startDate.set(val); }

    // endDate accessors
    public DateTime getEndDate() { return _endDate.get(); }
    public void setEndDate(DateTime val) { _endDate.set(val); }

    // keyType accessors
    public String getKeyType() { return _keyType.get(); }
    public void setKeyType(String val) { _keyType.set(val); }

}
