package com.dreameddeath.installedbase.service.utils;

import com.dreameddeath.installedbase.model.contract.InstalledContract;
import com.dreameddeath.installedbase.model.contract.InstalledContractRevision;
import com.dreameddeath.installedbase.model.process.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.process.model.InstalledItemUpdateResult;

/**
 * Created by Christophe Jeunesse on 24/03/2016.
 */
public class ContractUpdateWorkingInfo extends CreateUpdateItemWorkingInfo<InstalledContractRevision, InstalledItemUpdateResult, CreateUpdateInstalledBaseRequest.Contract, InstalledContract> {
    public ContractUpdateWorkingInfo(InstalledItemUpdateResult result, CreateUpdateInstalledBaseRequest.Contract updateRequest, InstalledContract targetItem) {
        super(result, updateRequest, targetItem);
    }

    @Override
    protected InstalledContractRevision newRevision() {
        return new InstalledContractRevision();
    }
}
