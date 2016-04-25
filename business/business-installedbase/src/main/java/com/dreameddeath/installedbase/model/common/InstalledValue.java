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
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import org.joda.time.DateTime;

/**
 * Created by Christophe Jeunesse on 10/08/2014.
 */
public class InstalledValue extends CouchbaseDocumentElement {
    /**
     *  value : the value itself
     */
    @DocumentProperty("value")
    private Property<String> value = new StandardProperty<>(InstalledValue.this);
    /**
     *  startDate : The value startDate
     */
    @DocumentProperty("startDate")
    private Property<DateTime> startDate = new StandardProperty<>(InstalledValue.this);
    /**
     *  endDate : The value endDate
     */
    @DocumentProperty("endDate")
    private Property<DateTime> endDate = new StandardProperty<>(InstalledValue.this);
    /**
     *  keyType : Type of public key if applicable
     */
    @DocumentProperty("keyType")
    private Property<String> keyType = new StandardProperty<>(InstalledValue.this);

    // value accessors
    public String getValue() { return value.get(); }
    public void setValue(String val) { value.set(val); }

    // startDate accessors
    public DateTime getStartDate() { return startDate.get(); }
    public void setStartDate(DateTime val) { startDate.set(val); }

    // endDate accessors
    public DateTime getEndDate() { return endDate.get(); }
    public void setEndDate(DateTime val) { endDate.set(val); }

    // keyType accessors
    public String getKeyType() { return keyType.get(); }
    public void setKeyType(String val) { keyType.set(val); }

    @Override
    public String toString() {
        return "InstalledValue{" +
                "value=" + value +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", keyType=" + keyType +
                '}';
    }
}
