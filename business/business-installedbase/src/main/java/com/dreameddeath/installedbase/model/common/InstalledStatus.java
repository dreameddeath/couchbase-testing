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
import com.dreameddeath.core.validation.annotation.NotNull;
import org.joda.time.DateTime;

/**
 * Created by Christophe Jeunesse on 10/08/2014.
 */
public class InstalledStatus extends CouchbaseDocumentElement {
    /**
     *  code : Status Code
     */
    @DocumentProperty("code") @NotNull
    private Property<Code> code = new StandardProperty<>(InstalledStatus.this);
    /**
     *  startDate : Start Validity date of the status
     */
    @DocumentProperty("startDate") @NotNull
    private Property<DateTime> startDate = new StandardProperty<>(InstalledStatus.this);
    /**
     *  endDate : End validity date of the status
     */
    @DocumentProperty("endDate") @NotNull
    private Property<DateTime> endDate = new StandardProperty<>(InstalledStatus.this);

    public InstalledStatus(){}

    public InstalledStatus(InstalledStatus status){
        this.setCode(status.getCode());
        this.setStartDate(status.getStartDate());
        this.setEndDate(status.getEndDate());
    }

    // code accessors
    public Code getCode() { return code.get(); }
    public void setCode(Code val) { code.set(val); }
    // startDate accessors
    public DateTime getStartDate() { return startDate.get(); }
    public void setStartDate(DateTime val) { startDate.set(val); }
    // endDate accessors
    public DateTime getEndDate() { return endDate.get(); }
    public void setEndDate(DateTime val) { endDate.set(val); }

    public enum Code{
        INEXISTING,
        ACTIVE,
        SUSPENDED,
        CLOSED,
        REMOVED,
        ABORTED
    }
}
