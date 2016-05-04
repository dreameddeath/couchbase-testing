package com.dreameddeath.installedbase.service.utils;

import com.dreameddeath.installedbase.model.v1.process.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.model.v1.productservice.InstalledProductService;
import com.dreameddeath.installedbase.model.v1.productservice.InstalledProductServiceRevision;
import com.dreameddeath.installedbase.process.model.v1.InstalledItemUpdateResult;

/**
 * Created by Christophe Jeunesse on 25/03/2016.
 */
public class ProductServiceUpdateWorkingInfo extends CreateUpdateItemWorkingInfo<InstalledProductServiceRevision,InstalledItemUpdateResult,CreateUpdateInstalledBaseRequest.ProductService,InstalledProductService> {
    public ProductServiceUpdateWorkingInfo(InstalledItemUpdateResult result, CreateUpdateInstalledBaseRequest.ProductService updateRequest, InstalledProductService targetItem,CreateUpdateItemWorkingInfo parent) {
        super(result, updateRequest, targetItem,parent);
    }

    @Override
    protected InstalledProductServiceRevision newRevision() {
        return new InstalledProductServiceRevision();
    }
}
