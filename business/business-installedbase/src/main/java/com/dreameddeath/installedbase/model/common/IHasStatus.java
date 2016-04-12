package com.dreameddeath.installedbase.model.common;

import java.util.List;

/**
 * Created by Christophe Jeunesse on 06/04/2016.
 */
public interface IHasStatus {
    InstalledStatus getStatus();
    void setStatus(InstalledStatus status);
    List<InstalledStatus> getStatusHistory();
    boolean addStatusHistory(InstalledStatus val);
    boolean removeStatusHistory(InstalledStatus val);
}
