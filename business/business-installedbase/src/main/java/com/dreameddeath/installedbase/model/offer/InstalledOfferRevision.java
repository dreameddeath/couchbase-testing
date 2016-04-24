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

package com.dreameddeath.installedbase.model.offer;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.installedbase.model.common.IHasLinkRevision;
import com.dreameddeath.installedbase.model.common.InstalledAttributeRevision;
import com.dreameddeath.installedbase.model.common.InstalledItemLinkRevision;
import com.dreameddeath.installedbase.model.common.InstalledItemRevision;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 11/08/2014.
 */
@DocumentEntity
public class InstalledOfferRevision extends InstalledItemRevision implements IHasLinkRevision {
    /**
     *  parent : the  parent of the offer
     */
    @DocumentProperty("parent")
    private Property<String> parent = new StandardProperty<>(InstalledOfferRevision.this);
    /**
     *  links : list of links attached to the revision
     */
    @DocumentProperty("links")
    private ListProperty<InstalledItemLinkRevision> links = new ArrayListProperty<>(InstalledOfferRevision.this);
    /**
     *  commercialParameters : commercial parameters of the revision
     */
    @DocumentProperty("commercialParameters")
    private ListProperty<InstalledAttributeRevision> commercialParameters = new ArrayListProperty<>(InstalledOfferRevision.this);


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
     * Getter of parent
     * @return the content
     */
    public String getParent() { return parent.get(); }
    /**
     * Setter of parent
     * @param val the new content
     */
    public void setParent(String val) { parent.set(val); }

    /**
     * Getter of commercialParameters
     * @return the content
     */
    public List<InstalledAttributeRevision> getCommercialParameters() { return commercialParameters.get(); }
    /**
     * Setter of commercialParameters
     * @param vals the new collection of values
     */
    public void setCommercialParameters(Collection<InstalledAttributeRevision> vals) { commercialParameters.set(vals); }
    /**
     * Add a new entry to the property commercialParameters
     * @param val the new entry to be added
     */
    public boolean addCommercialParameters(InstalledAttributeRevision val){ return commercialParameters.add(val); }
    /**
     * Remove an entry to the property commercialParameters
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeCommercialParameters(InstalledAttributeRevision val){ return commercialParameters.remove(val); }


    /**
     * comparator of revisions
     * @param revision the target revision to compare with
     */
    @Override
    public boolean isSame(InstalledItemRevision revision){
        return super.isSame(revision)
                && (revision instanceof InstalledOfferRevision)
                && parent.equals(((InstalledOfferRevision) revision).parent)
                && InstalledItemLinkRevision.isSameLinkList(links,((InstalledOfferRevision) revision).links)
                && InstalledAttributeRevision.isSameAttributeList(commercialParameters,((InstalledOfferRevision) revision).commercialParameters)
                //Add other fields
                ;
    }

}
