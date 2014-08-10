package com.dreameddeath.installedbase.model;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ArrayListProperty;
import com.dreameddeath.core.model.property.ListProperty;

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
