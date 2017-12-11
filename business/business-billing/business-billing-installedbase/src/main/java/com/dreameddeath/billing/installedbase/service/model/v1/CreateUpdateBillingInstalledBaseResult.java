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
 * Created by Christophe Jeunesse on 16/11/2016.
 */
public class CreateUpdateBillingInstalledBaseResult extends VersionedDocumentElement {
    /**
     * action : the action on the element
     */
    @DocumentProperty("action")
    private Property<CreateUpdateBillingInstalledBaseAction> action = new ImmutableProperty<>(CreateUpdateBillingInstalledBaseResult.this);
    /**
     * items : list of items being updated/modified
     */
    @DocumentProperty("items")
    private ListProperty<CreateUpdateBillingInstalledBaseItemResult> items = new ArrayListProperty<>(CreateUpdateBillingInstalledBaseResult.this);

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
    /**
     * Getter of the attribute {@link #items}
     * return the current list contained in {@link #items}
     */
    public List<CreateUpdateBillingInstalledBaseItemResult> getItems(){
        return this.items;
    }

    /**
     * Replace the content of the attribute {@link #items}
     * @param newContent the new content of {@link #items}
     */
    public void setItems(Collection<CreateUpdateBillingInstalledBaseItemResult> newContent){
        this.items.set(newContent);
    }

    /**
     * Adds an item to the attribute {@link #items}
     * @param newItem the new item to be added to {@link #items}
     */
    public void addItem(CreateUpdateBillingInstalledBaseItemResult newItem){
        this.items.add(newItem);
    }

}
