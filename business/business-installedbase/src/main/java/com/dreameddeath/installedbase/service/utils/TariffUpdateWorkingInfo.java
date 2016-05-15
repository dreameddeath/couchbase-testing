package com.dreameddeath.installedbase.service.utils;

import com.dreameddeath.installedbase.model.v1.tariff.InstalledTariff;
import com.dreameddeath.installedbase.model.v1.tariff.InstalledTariffRevision;
import com.dreameddeath.installedbase.process.model.v1.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.process.model.v1.TariffUpdateResult;

/**
 * Created by Christophe Jeunesse on 04/04/2016.
 */
public class TariffUpdateWorkingInfo extends CreateUpdateItemWorkingInfo<InstalledTariffRevision,TariffUpdateResult,CreateUpdateInstalledBaseRequest.Tariff,InstalledTariff> {
    public TariffUpdateWorkingInfo(TariffUpdateResult result, CreateUpdateInstalledBaseRequest.Tariff updateRequest, InstalledTariff targetItem,CreateUpdateItemWorkingInfo parent) {
        super(result, updateRequest, targetItem,parent);
    }

    @Override
    protected InstalledTariffRevision newRevision() {
        return new InstalledTariffRevision();
    }
}
