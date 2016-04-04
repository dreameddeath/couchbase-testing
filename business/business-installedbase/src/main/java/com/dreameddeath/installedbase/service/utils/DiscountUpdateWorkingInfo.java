package com.dreameddeath.installedbase.service.utils;

import com.dreameddeath.installedbase.model.process.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.model.tariff.InstalledDiscount;
import com.dreameddeath.installedbase.model.tariff.InstalledDiscountRevision;
import com.dreameddeath.installedbase.process.model.DiscountUpdateResult;

/**
 * Created by Christophe Jeunesse on 04/04/2016.
 */
public class DiscountUpdateWorkingInfo extends CreateUpdateItemWorkingInfo<InstalledDiscountRevision,DiscountUpdateResult,CreateUpdateInstalledBaseRequest.Discount,InstalledDiscount> {
    public DiscountUpdateWorkingInfo(DiscountUpdateResult result, CreateUpdateInstalledBaseRequest.Discount updateRequest, InstalledDiscount targetItem) {
        super(result, updateRequest, targetItem);
    }

    @Override
    protected InstalledDiscountRevision newRevision() {
        return new InstalledDiscountRevision();
    }
}
