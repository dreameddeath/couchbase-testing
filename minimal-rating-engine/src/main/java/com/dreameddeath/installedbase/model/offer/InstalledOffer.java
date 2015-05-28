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

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.installedbase.model.common.InstalledItem;
import com.dreameddeath.installedbase.model.tariff.InstalledTariff;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 10/08/2014.
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.MINIMAL_CLASS, include= JsonTypeInfo.As.PROPERTY, property="@c")
public abstract class InstalledOffer extends InstalledItem<InstalledOfferRevision> {
    @DocumentProperty("links")
    private ListProperty<InstalledOfferLink> _links = new ArrayListProperty<InstalledOfferLink>(InstalledOffer.this);
    @DocumentProperty("tariffs")
    private ListProperty<InstalledTariff> _tariffs = new ArrayListProperty<InstalledTariff>(InstalledOffer.this);
    /**
     *  commercialParameters : explain the commercial parameters defined for the given offer
     */
    @DocumentProperty("commercialParameters")
    private ListProperty<InstalledCommercialParameter> _commercialParameters = new ArrayListProperty<InstalledCommercialParameter>(InstalledOffer.this);

    // links accessors
    public List<InstalledOfferLink> getLinks() { return _links.get(); }
    public void setLinks(Collection<InstalledOfferLink> vals) { _links.set(vals); }
    public boolean addLink(InstalledOfferLink val){ return _links.add(val); }
    public boolean removeLink(InstalledOfferLink val){ return _links.remove(val); }

    // tariffs accessors
    public List<InstalledTariff> getTariffs() { return _tariffs.get(); }
    public void setTariffs(Collection<InstalledTariff> vals) { _tariffs.set(vals); }
    public boolean addTariff(InstalledTariff val){ return _tariffs.add(val); }
    public boolean removeTariff(InstalledTariff val){ return _tariffs.remove(val); }

    // CommercialParameters Accessors
    public List<InstalledCommercialParameter> getCommercialParameters() { return _commercialParameters.get(); }
    public void setCommercialParameters(Collection<InstalledCommercialParameter> vals) { _commercialParameters.set(vals); }
    public boolean addCommercialParameter(InstalledCommercialParameter val){ return _commercialParameters.add(val); }
    public boolean removeCommercialParameter(InstalledCommercialParameter val){ return _commercialParameters.remove(val); }

}
