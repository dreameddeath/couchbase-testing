/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.billing.model.v1.order;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import org.joda.time.DateTime;

/**
 * Created by Christophe Jeunesse on 01/09/2014.
 */
public class BillingOrderItemStatus extends CouchbaseDocumentElement {
    /**
     *  status : Status of the billing item
     */
    @DocumentProperty("status")
    private Property<Status> status = new StandardProperty<>(BillingOrderItemStatus.this);
    /**
     *  startDate : The start Date of the status
     */
    @DocumentProperty("startDate")
    private Property<DateTime> startDate = new StandardProperty<>(BillingOrderItemStatus.this);
    /**
     *  endDate : The end of validity date of the status
     */
    @DocumentProperty("endDate")
    private Property<DateTime> endDate = new StandardProperty<>(BillingOrderItemStatus.this);

    // status accessors
    public Status getStatus() { return status.get(); }
    public void setStatus(Status val) { status.set(val); }
    // startDate accessors
    public DateTime getStartDate() { return startDate.get(); }
    public void setStartDate(DateTime val) { startDate.set(val); }
    // endDate accessors
    public DateTime getEndDate() { return endDate.get(); }
    public void setEndDate(DateTime val) { endDate.set(val); }

    public enum Status{
        ACTIVE,
        SUSPENDED,
        CLOSED
    }
}
