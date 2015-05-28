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
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 12/08/2014.
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.MINIMAL_CLASS, include= JsonTypeInfo.As.PROPERTY, property="@c")
public class BillingInstalledBaseItem extends BaseCouchbaseDocumentElement {
    /**
     *  id : internal id of the item for crossrefs
     */
    @DocumentProperty("id")
    private Property<Long> _id = new StandardProperty<Long>(BillingInstalledBaseItem.this);
    /**
     *  statuses : List of statuses (history by dates)
     */
    @DocumentProperty("statuses")
    private ListProperty<BillingInstalledBaseItemStatus> _statuses = new ArrayListProperty<BillingInstalledBaseItemStatus>(BillingInstalledBaseItem.this);

    // id accessors
    public Long getId() { return _id.get(); }
    public void setId(Long val) { _id.set(val); }
    // Statuses Accessors
    public List<BillingInstalledBaseItemStatus> getStatuses() { return _statuses.get(); }
    public void setStatuses(Collection<BillingInstalledBaseItemStatus> vals) { _statuses.set(vals); }
    public boolean addStatuses(BillingInstalledBaseItemStatus val){ return _statuses.add(val); }
    public boolean removeStatuses(BillingInstalledBaseItemStatus val){ return _statuses.remove(val); }

    public BillingInstalledBaseItem getItemById(Long id){ return getFirstParentOfClass(BillingInstalledBase.class).getItemById(id);}
    public <T extends BillingInstalledBaseItem> T getItemById(Long id,Class<T> clazz){ return (T)getItemById(id);}
}
