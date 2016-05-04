package com.dreameddeath.installedbase.service.utils;

import com.dreameddeath.installedbase.model.v1.contract.InstalledContract;
import com.dreameddeath.installedbase.model.v1.contract.InstalledContractRevision;
import com.dreameddeath.installedbase.model.v1.process.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.process.model.v1.InstalledItemUpdateResult;

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
