package com.dreameddeath.installedbase.model.common;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 05/04/2016.
 */
public interface IHasLinkRevision {
    List<InstalledItemLinkRevision> getLinks();
    void setLinks(Collection<InstalledItemLinkRevision> vals);
    boolean addLinks(InstalledItemLinkRevision val);
    boolean removeLinks(InstalledItemLinkRevision val);
}
