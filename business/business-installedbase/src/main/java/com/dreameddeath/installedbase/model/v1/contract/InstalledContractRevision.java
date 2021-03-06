/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.installedbase.model.v1.contract;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.installedbase.model.EntityConstants;
import com.dreameddeath.installedbase.model.v1.common.IHasLinkRevision;
import com.dreameddeath.installedbase.model.v1.common.InstalledItemLinkRevision;
import com.dreameddeath.installedbase.model.v1.common.InstalledItemRevision;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 31/08/2014.
 */
@DocumentEntity(domain = EntityConstants.INSTALLED_BASE_DOMAIN)
public class InstalledContractRevision extends InstalledItemRevision implements IHasLinkRevision {
    /**
     *  links : list of links attached to the revision
     */
    @DocumentProperty("links")
    private ListProperty<InstalledItemLinkRevision> links = new ArrayListProperty<>(InstalledContractRevision.this);
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
    public boolean addLink(InstalledItemLinkRevision val){ return links.add(val); }
    /**
     * Remove an entry to the property links
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    @Override
    public boolean removeLink(InstalledItemLinkRevision val){ return links.remove(val); }


    /**
     * comparator of revisions
     * @param revision the target revision to compare with
     */
    @Override
    public boolean isSame(InstalledItemRevision revision){
        return super.isSame(revision)
                && (revision instanceof InstalledContractRevision)
                //Add other fields
                ;
    }
}
