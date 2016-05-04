package com.dreameddeath.installedbase.service.utils;

import com.dreameddeath.installedbase.model.v1.offer.InstalledAtomicOffer;
import com.dreameddeath.installedbase.model.v1.process.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.process.model.v1.InstalledItemUpdateResult;

/**
 * Created by Christophe Jeunesse on 25/03/2016.
 */
public class AtomicOfferUpdateWorkingInfo extends OfferUpdateWorkingInfo<InstalledAtomicOffer>{
    public AtomicOfferUpdateWorkingInfo(InstalledItemUpdateResult result, CreateUpdateInstalledBaseRequest.Offer updateRequest, InstalledAtomicOffer targetItem) {
        super(result, updateRequest, targetItem);
    }

}
