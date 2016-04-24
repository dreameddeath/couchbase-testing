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

package com.dreameddeath.installedbase.model.contract;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.installedbase.model.common.IHasInstalledItemLink;
import com.dreameddeath.installedbase.model.common.InstalledItem;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 31/08/2014.
 */
public class InstalledContract extends InstalledItem<InstalledContractRevision> implements IHasInstalledItemLink<InstalledContractLink> {
    /**
     *  links : links between contracts
     */
    @DocumentProperty("links")
    private ListProperty<InstalledContractLink> links = new ArrayListProperty<>(InstalledContract.this);

    /**
     * Getter of links
     * @return the whole (immutable) list of links
     */
    public List<InstalledContractLink> getLinks() { return links.get(); }
    /**
     * Setter of links
     * @param vals the new collection of values
     */
    public void setLinks(Collection<InstalledContractLink> vals) { links.set(vals); }
    /**
     * Add a new entry to the property links
     * @param val the new entry to be added
     * @return true if the entry has been added
     */
    public boolean addLink(InstalledContractLink val){ return links.add(val); }
    /**
     * Add a new entry to the property links at the specified position
     * @param index the new entry to be added
     * @param val the new entry to be added
     * @return true if the entry has been added
     */
    public boolean addLink(int index,InstalledContractLink val){ return links.add(val); }
    /**
     * Remove an entry to the property links
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeLink(InstalledContractLink val){ return links.remove(val); }
    /**
     * Remove an entry to the property links at the specified position
     * @param index the position of element to be removed
     * @return the entry removed if any
     */
    public InstalledContractLink removeLink(int index){ return links.remove(index); }
}
