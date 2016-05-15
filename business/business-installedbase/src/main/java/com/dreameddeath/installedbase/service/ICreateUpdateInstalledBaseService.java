package com.dreameddeath.installedbase.service;

import com.dreameddeath.installedbase.model.v1.InstalledBase;
import com.dreameddeath.installedbase.process.model.v1.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.process.model.v1.InstalledBaseUpdateResult;

/**
 * Created by Christophe Jeunesse on 02/05/2016.
 */
public interface ICreateUpdateInstalledBaseService {
    InstalledBaseUpdateResult manageCreateUpdate(CreateUpdateInstalledBaseRequest request, InstalledBase ref, CreateUpdateInstalledBaseRequest.Contract reqContract);
}
