package com.dreameddeath.installedbase.model.common;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by Christophe Jeunesse on 06/04/2016.
 */
public interface IHasStatus {
    InstalledStatus getStatus(DateTime refDate);
    List<InstalledStatus> getStatuses();
    List<InstalledStatus> getStatuses(DateTime startDate, DateTime endDate);
    List<InstalledStatus> getOverlappingStatuses(InstalledStatus refStatus);
    boolean addStatus(InstalledStatus val);
    boolean removeStatus(InstalledStatus val);
}
