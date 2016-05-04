package com.dreameddeath.installedbase.model.v1.common;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 05/04/2016.
 */
public interface IHasInstalledItemLink<T extends InstalledItemLink> {
    List<T> getLinks();
    void setLinks(Collection<T> vals);
    boolean addLink(T val);
    boolean removeLink(T val);
}
