package com.dreameddeath.installedbase.service.utils;

import com.dreameddeath.installedbase.model.offer.InstalledAtomicOffer;
import com.dreameddeath.installedbase.model.process.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.process.model.InstalledItemUpdateResult;

/**
 * Created by Christophe Jeunesse on 25/03/2016.
 */
public class AtomicOfferUpdateWorkingInfo extends OfferUpdateWorkingInfo<InstalledAtomicOffer>{
    public AtomicOfferUpdateWorkingInfo(InstalledItemUpdateResult result, CreateUpdateInstalledBaseRequest.Offer updateRequest, InstalledAtomicOffer targetItem) {
        super(result, updateRequest, targetItem);
    }

}
