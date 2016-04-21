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

package com.dreameddeath.installedbase.model.productservice;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.installedbase.annotation.EntityConstants;
import com.dreameddeath.installedbase.model.common.IHasLinkRevision;
import com.dreameddeath.installedbase.model.common.InstalledAttributeRevision;
import com.dreameddeath.installedbase.model.common.InstalledItemLinkRevision;
import com.dreameddeath.installedbase.model.common.InstalledItemRevision;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 11/08/2014.
 */
@DocumentEntity(domain = EntityConstants.DOMAIN)
public class InstalledProductServiceRevision extends InstalledItemRevision implements IHasLinkRevision {
    /**
     *  links : list of link revisions
     */
    @DocumentProperty("links")
    private ListProperty<InstalledItemLinkRevision> links = new ArrayListProperty<>(InstalledProductServiceRevision.this);
    /**
     *  functions : Functions revisions
     */
    @DocumentProperty("functions")
    private ListProperty<InstalledAttributeRevision> functions = new ArrayListProperty<>(InstalledProductServiceRevision.this);

    /**
     * Getter of functions
     * @return the content
     */
    public List<InstalledAttributeRevision> getFunctions() { return functions.get(); }
    /**
     * Setter of functions
     * @param vals the new collection of values
     */
    public void setFunctions(Collection<InstalledAttributeRevision> vals) { functions.set(vals); }
    /**
     * Add a new entry to the property functions
     * @param val the new entry to be added
     */
    public boolean addFunctions(InstalledAttributeRevision val){ return functions.add(val); }
    /**
     * Remove an entry to the property functions
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeFunctions(InstalledAttributeRevision val){ return functions.remove(val); }
    /**
     * Getter of links
     * @return the content
     */
    @Override
    public List<InstalledItemLinkRevision> getLinks() { return links.get(); }
    /**
     * Setter of links
     * @param vals the new collection of values
     */
    @Override
    public void setLinks(Collection<InstalledItemLinkRevision> vals) { links.set(vals); }
    /**
     * Add a new entry to the property links
     * @param val the new entry to be added
     */
    @Override
    public boolean addLinks(InstalledItemLinkRevision val){ return links.add(val); }
    /**
     * Remove an entry to the property links
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    @Override
    public boolean removeLinks(InstalledItemLinkRevision val){ return links.remove(val); }


    /**
     * comparator of revisions
     * @param revision the target revision to compare with
     */
    @Override
    public boolean isSame(InstalledItemRevision revision){
        return super.isSame(revision)
                && (revision instanceof InstalledProductServiceRevision)
                && InstalledItemLinkRevision.isSameLinkList(links,((InstalledProductServiceRevision) revision).links)
                && InstalledAttributeRevision.isSameAttributeList(functions,((InstalledProductServiceRevision) revision).functions)
                //Add other fields
                ;
    }

}
