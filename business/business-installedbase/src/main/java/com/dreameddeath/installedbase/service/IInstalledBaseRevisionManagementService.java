package com.dreameddeath.installedbase.service;

import com.dreameddeath.installedbase.model.v1.InstalledBase;
import com.dreameddeath.installedbase.model.v1.common.InstalledItem;
import com.dreameddeath.installedbase.model.v1.common.InstalledItemRevision;
import com.dreameddeath.installedbase.process.model.v1.InstalledBaseUpdateResult;
import com.dreameddeath.installedbase.process.model.v1.RevisionUpdateResult;
import com.dreameddeath.installedbase.service.utils.InstalledItemRevisionsToApply;

import java.util.List;

/**
 * Created by Christophe Jeunesse on 30/03/2016.
 */
public interface IInstalledBaseRevisionManagementService {
    <TREV extends InstalledItemRevision,TITEM extends InstalledItem<TREV>> RevisionUpdateResult addOrReplaceRevision(InstalledBase ref, TITEM item, TREV targetRevision);
    void applyApplicableRevisions(InstalledBaseUpdateResult result, InstalledBase ref);
    void applyApplicableRevisions(InstalledBaseUpdateResult result, InstalledBase ref, List<InstalledItemRevisionsToApply> revisions);
    List<InstalledItemRevisionsToApply> findApplicableRevisions(InstalledBaseUpdateResult result,InstalledBase ref);
}
