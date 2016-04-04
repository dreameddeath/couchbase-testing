package com.dreameddeath.installedbase.service.utils;

import com.dreameddeath.installedbase.model.offer.InstalledCompositeOffer;
import com.dreameddeath.installedbase.model.process.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.process.model.InstalledItemUpdateResult;

/**
 * Created by Christophe Jeunesse on 25/03/2016.
 */
public class CompositeOfferUpdateWorkingInfo extends OfferUpdateWorkingInfo<InstalledCompositeOffer>{
    public CompositeOfferUpdateWorkingInfo(InstalledItemUpdateResult result, CreateUpdateInstalledBaseRequest.Offer updateRequest, InstalledCompositeOffer targetItem) {
        super(result, updateRequest, targetItem);
    }
}
