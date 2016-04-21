package com.dreameddeath.installedbase.service.utils;

import com.dreameddeath.installedbase.model.offer.InstalledOffer;
import com.dreameddeath.installedbase.model.offer.InstalledOfferRevision;
import com.dreameddeath.installedbase.model.process.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.process.model.InstalledItemUpdateResult;

/**
 * Created by Christophe Jeunesse on 25/03/2016.
 */
public abstract class OfferUpdateWorkingInfo<TTYPE extends InstalledOffer> extends CreateUpdateItemWorkingInfo<InstalledOfferRevision,InstalledItemUpdateResult,CreateUpdateInstalledBaseRequest.Offer,TTYPE>{
    public OfferUpdateWorkingInfo(InstalledItemUpdateResult result, CreateUpdateInstalledBaseRequest.Offer updateRequest, TTYPE targetItem) {
        super(result, updateRequest, targetItem);
    }

    @Override
    protected InstalledOfferRevision newRevision() {
        return new InstalledOfferRevision();
    }
}