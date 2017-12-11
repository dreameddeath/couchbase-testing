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

package com.dreameddeath.billing.installedbase.service.model.v1;

import com.dreameddeath.core.business.model.VersionedDocumentElement;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 25/09/2017.
 */

public abstract class CreateUpdateBillingInstalledBaseItemResult extends VersionedDocumentElement {
    /**
     * id : the id of the created/updated item
     */
    @DocumentProperty("id")
    private Property<Long> id = new ImmutableProperty<>(CreateUpdateBillingInstalledBaseItemResult.this);
    /**
     * action : the action made on the element
     */
    @DocumentProperty("action")
    private Property<CreateUpdateBillingInstalledBaseAction> action = new ImmutableProperty<>(CreateUpdateBillingInstalledBaseItemResult.this);
    /**
     * statuses : The statuses update result
     */
    @DocumentProperty("statuses")
    private ListProperty<CreateUpdateBillingInstalledBaseItemStatusUpdateResult> statuses = new ArrayListProperty<>(CreateUpdateBillingInstalledBaseItemResult.this);

    /**
     * Getter of the attribute {@link #id}
     * return the currentValue of {@link #id}
     */
    public Long getId(){
        return this.id.get();
    }

    /**
     * Setter of the attribute {@link #id}
     * @param newValue the newValue of {@link #id}
     */
    public void setId(Long newValue){
        this.id.set(newValue);
    }
    /**
     * Getter of the attribute {@link #statuses}
     * return the current list contained in {@link #statuses}
     */
    public List<CreateUpdateBillingInstalledBaseItemStatusUpdateResult> getStatuses(){
        return this.statuses;
    }

    /**
     * Replace the content of the attribute {@link #statuses}
     * @param newContent the new content of {@link #statuses}
     */
    public void setStatuses(Collection<CreateUpdateBillingInstalledBaseItemStatusUpdateResult> newContent){
        this.statuses.set(newContent);
    }

    /**
     * Adds an item to the attribute {@link #statuses}
     * @param newItem the new item to be added to {@link #statuses}
     */
    public void addStatus(CreateUpdateBillingInstalledBaseItemStatusUpdateResult newItem){
        this.statuses.add(newItem);
    }
    /**
     * Getter of the attribute {@link #action}
     * return the currentValue of {@link #action}
     */
    public CreateUpdateBillingInstalledBaseAction getAction(){
        return this.action.get();
    }
    /**
     * Setter of the attribute {@link #action}
     * @param newValue the newValue of {@link #action}
     */
    public void setAction(CreateUpdateBillingInstalledBaseAction newValue){
        this.action.set(newValue);
    }



}
