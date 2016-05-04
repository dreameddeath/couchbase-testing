package com.dreameddeath.installedbase.model.v1.common;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 05/04/2016.
 */
public interface IHasLinkRevision {
    List<InstalledItemLinkRevision> getLinks();
    void setLinks(Collection<InstalledItemLinkRevision> vals);
    boolean addLink(InstalledItemLinkRevision val);
    boolean removeLink(InstalledItemLinkRevision val);
}
