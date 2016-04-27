package com.dreameddeath.installedbase.service.utils;

import com.dreameddeath.installedbase.model.process.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.model.tariff.InstalledTariff;
import com.dreameddeath.installedbase.model.tariff.InstalledTariffRevision;
import com.dreameddeath.installedbase.process.model.TariffUpdateResult;

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
