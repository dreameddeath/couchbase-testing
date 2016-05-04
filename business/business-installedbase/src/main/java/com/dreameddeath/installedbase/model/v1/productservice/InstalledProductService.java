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

package com.dreameddeath.installedbase.model.v1.productservice;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.installedbase.model.v1.common.IHasInstalledItemLink;
import com.dreameddeath.installedbase.model.v1.common.InstalledItem;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 10/08/2014.
 */
public class InstalledProductService extends InstalledItem<InstalledProductServiceRevision> implements IHasInstalledItemLink<InstalledProductServiceLink> {
    /**
     *  functions : give the list of functions attached to the Product
     */
    @DocumentProperty("functions")
    private ListProperty<InstalledFunction> functions = new ArrayListProperty<>(InstalledProductService.this);
    /**
     *  links : give the list of the links around product items
     */
    @DocumentProperty("links")
    private ListProperty<InstalledProductServiceLink> links = new ArrayListProperty<>(InstalledProductService.this);

    // Functions Accessors
    public List<InstalledFunction> getFunctions() { return functions.get(); }
    public void setFunctions(Collection<InstalledFunction> vals) { functions.set(vals); }
    public boolean addFunctions(InstalledFunction val){ return functions.add(val); }
    public boolean removeFunctions(InstalledFunction val){ return functions.remove(val); }


    /**
     * Getter of links
     * @return the content
     */
    public List<InstalledProductServiceLink> getLinks() { return links.get(); }
    /**
     * Setter of links
     * @param vals the new collection of values
     */
    @Override
    public void setLinks(Collection<InstalledProductServiceLink> vals) { links.set(vals); }
    /**
     * Add a new entry to the property links
     * @param val the new entry to be added
     */
    @Override
    public boolean addLink(InstalledProductServiceLink val){ return links.add(val); }
    /**
     * Remove an entry to the property links
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    @Override
    public boolean removeLink(InstalledProductServiceLink val){ return links.remove(val); }
}
