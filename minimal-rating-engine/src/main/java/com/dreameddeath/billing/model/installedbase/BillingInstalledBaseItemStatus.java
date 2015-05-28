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

package com.dreameddeath.billing.model.installedbase;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import org.joda.time.DateTime;

/**
 * Created by Christophe Jeunesse on 12/08/2014.
 */
public class BillingInstalledBaseItemStatus extends BaseCouchbaseDocumentElement {
    /**
     *  status : Status of the billing item
     */
    @DocumentProperty("status")
    private Property<Status> _status = new StandardProperty<Status>(BillingInstalledBaseItemStatus.this);
    /**
     *  startDate : The start Date of the status
     */
    @DocumentProperty("startDate")
    private Property<DateTime> _startDate = new StandardProperty<DateTime>(BillingInstalledBaseItemStatus.this);
    /**
     *  endDate : The end of validity date of the status
     */
    @DocumentProperty("endDate")
    private Property<DateTime> _endDate = new StandardProperty<DateTime>(BillingInstalledBaseItemStatus.this);

    // status accessors
    public Status getStatus() { return _status.get(); }
    public void setStatus(Status val) { _status.set(val); }
    // startDate accessors
    public DateTime getStartDate() { return _startDate.get(); }
    public void setStartDate(DateTime val) { _startDate.set(val); }
    // endDate accessors
    public DateTime getEndDate() { return _endDate.get(); }
    public void setEndDate(DateTime val) { _endDate.set(val); }

    public enum Status{
        ACTIVE,
        SUSPENDED,
        CLOSED
    }
}
