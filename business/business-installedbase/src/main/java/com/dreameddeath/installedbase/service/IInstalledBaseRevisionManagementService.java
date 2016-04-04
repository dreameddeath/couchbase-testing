package com.dreameddeath.installedbase.service;

import com.dreameddeath.installedbase.model.InstalledBase;
import com.dreameddeath.installedbase.model.common.InstalledItem;
import com.dreameddeath.installedbase.model.common.InstalledItemRevision;
import com.dreameddeath.installedbase.process.model.RevisionUpdateResult;

/**
 * Created by Christophe Jeunesse on 30/03/2016.
 */
public interface IInstalledBaseRevisionManagementService {
    <TREV extends InstalledItemRevision,TITEM extends InstalledItem<TREV>> RevisionUpdateResult addOrReplaceRevision(InstalledBase ref, TITEM item, TREV targetRevision);
}
