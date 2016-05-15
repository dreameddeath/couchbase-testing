package com.dreameddeath.installedbase.service.utils;

import com.dreameddeath.installedbase.model.v1.tariff.InstalledDiscount;
import com.dreameddeath.installedbase.model.v1.tariff.InstalledDiscountRevision;
import com.dreameddeath.installedbase.process.model.v1.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.process.model.v1.DiscountUpdateResult;

/**
 * Created by Christophe Jeunesse on 04/04/2016.
 */
public class DiscountUpdateWorkingInfo extends CreateUpdateItemWorkingInfo<InstalledDiscountRevision,DiscountUpdateResult,CreateUpdateInstalledBaseRequest.Discount,InstalledDiscount> {
    public DiscountUpdateWorkingInfo(DiscountUpdateResult result, CreateUpdateInstalledBaseRequest.Discount updateRequest, InstalledDiscount targetItem,CreateUpdateItemWorkingInfo parent) {
        super(result, updateRequest, targetItem,parent);
    }

    @Override
    protected InstalledDiscountRevision newRevision() {
        return new InstalledDiscountRevision();
    }
}
