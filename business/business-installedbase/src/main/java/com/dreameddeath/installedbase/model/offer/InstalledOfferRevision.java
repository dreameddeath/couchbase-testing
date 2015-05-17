/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.installedbase.model.offer;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.installedbase.model.common.InstalledItemRevision;

import java.util.Collection;
import java.util.List;

/**
 * Created by ceaj8230 on 11/08/2014.
 */
public class InstalledOfferRevision extends InstalledItemRevision {
    /**
     *  links : links for this revision
     */
    @DocumentProperty("links")
    private ListProperty<InstalledOfferLink> _links = new ArrayListProperty<InstalledOfferLink>(InstalledOfferRevision.this);
    /**
     *  commercialParameters : list of commercial parameters of the revision
     */
    @DocumentProperty("commercialParameters")
    private ListProperty<InstalledCommercialParameter> _commercialParameters = new ArrayListProperty<InstalledCommercialParameter>(InstalledOfferRevision.this);

    // Links Accessors
    public List<InstalledOfferLink> getLinks() { return _links.get(); }
    public void setLinks(Collection<InstalledOfferLink> vals) { _links.set(vals); }
    public boolean addLinks(InstalledOfferLink val){ return _links.add(val); }
    public boolean removeLinks(InstalledOfferLink val){ return _links.remove(val); }

    // CommercialParameters Accessors
    public List<InstalledCommercialParameter> getCommercialParameters() { return _commercialParameters.get(); }
    public void setCommercialParameters(Collection<InstalledCommercialParameter> vals) { _commercialParameters.set(vals); }
    public boolean addCommercialParameters(InstalledCommercialParameter val){ return _commercialParameters.add(val); }
    public boolean removeCommercialParameters(InstalledCommercialParameter val){ return _commercialParameters.remove(val); }

}
