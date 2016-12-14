/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.billing.installedbase.model.v1;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
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
public class BillingInstalledBaseItem extends CouchbaseDocumentElement {
    /**
     *  id : internal id of the item for crossrefs
     */
    @DocumentProperty("id")
    private Property<Long> id = new StandardProperty<>(BillingInstalledBaseItem.this);
    /**
     *  statuses : List of statuses (history by dates)
     */
    @DocumentProperty("statuses")
    private ListProperty<BillingInstalledBaseItemStatus> statuses = new ArrayListProperty<>(BillingInstalledBaseItem.this);

    // id accessors
    public Long getId() { return id.get(); }
    public void setId(Long val) { id.set(val); }
    // Statuses Accessors
    public List<BillingInstalledBaseItemStatus> getStatuses() { return statuses.get(); }
    public void setStatuses(Collection<BillingInstalledBaseItemStatus> vals) { statuses.set(vals); }
    public boolean addStatuses(BillingInstalledBaseItemStatus val){ return statuses.add(val); }
    public boolean removeStatuses(BillingInstalledBaseItemStatus val){ return statuses.remove(val); }

    public BillingInstalledBaseItem getItemById(Long id){ return Helper.getFirstParentOfClass(this,BillingInstalledBase.class).getItemById(id);}
    public <T extends BillingInstalledBaseItem> T getItemById(Long id,Class<T> clazz){ return (T)getItemById(id);}
}
