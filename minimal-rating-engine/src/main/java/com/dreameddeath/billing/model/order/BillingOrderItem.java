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

package com.dreameddeath.billing.model.v1.order;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 01/09/2014.
 */
public class BillingOrderItem extends BaseCouchbaseDocumentElement {
    /**
     *  id : The internal id of the order being billed
     */
    @DocumentProperty("id")
    private Property<Long> _id = new StandardProperty<Long>(BillingOrderItem.this);
    /**
     *  statuses : Statuses of the order item
     */
    @DocumentProperty("statuses")
    private ListProperty<BillingOrderItemStatus> _statuses = new ArrayListProperty<BillingOrderItemStatus>(BillingOrderItem.this);


    // id accessors
    public Long getId() { return _id.get(); }
    public void setId(Long val) { _id.set(val); }
    // Statuses Accessors
    public List<BillingOrderItemStatus> getStatuses() { return _statuses.get(); }
    public void setStatuses(Collection<BillingOrderItemStatus> vals) { _statuses.set(vals); }
    public boolean addStatuses(BillingOrderItemStatus val){ return _statuses.add(val); }
    public boolean removeStatuses(BillingOrderItemStatus val){ return _statuses.remove(val); }
}
