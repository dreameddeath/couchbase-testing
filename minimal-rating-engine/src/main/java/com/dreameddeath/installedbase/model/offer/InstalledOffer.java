package com.dreameddeath.installedbase.model.offer;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.installedbase.model.tariff.InstalledTariff;
import com.dreameddeath.installedbase.model.common.InstalledItem;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Collection;
import java.util.List;

/**
 * Created by ceaj8230 on 10/08/2014.
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
