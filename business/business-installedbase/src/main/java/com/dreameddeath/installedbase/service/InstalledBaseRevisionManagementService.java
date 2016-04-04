package com.dreameddeath.installedbase.service;

import com.dreameddeath.core.date.IDateTimeService;
import com.dreameddeath.installedbase.model.InstalledBase;
import com.dreameddeath.installedbase.model.common.InstalledItem;
import com.dreameddeath.installedbase.model.common.InstalledItemRevision;
import com.dreameddeath.installedbase.model.offer.InstalledOffer;
import com.dreameddeath.installedbase.model.productservice.InstalledProductService;
import com.dreameddeath.installedbase.model.tariff.InstalledDiscount;
import com.dreameddeath.installedbase.model.tariff.InstalledTariff;
import com.dreameddeath.installedbase.process.model.InstalledBaseUpdateResult;
import com.dreameddeath.installedbase.process.model.RevisionUpdateResult;
import com.dreameddeath.installedbase.service.utils.InstalledItemRevisionsToApply;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 30/03/2016.
 */
public class InstalledBaseRevisionManagementService implements IInstalledBaseRevisionManagementService{
    public IDateTimeService dateTimeService;

    @Autowired
    public void setDateTimeService(IDateTimeService dateTimeService){
        this.dateTimeService = dateTimeService;
    }

    @Override
    public <TREV extends InstalledItemRevision,TITEM extends InstalledItem<TREV>> RevisionUpdateResult addOrReplaceRevision(InstalledBase ref, TITEM item, TREV targetRevision){
        RevisionUpdateResult result = new RevisionUpdateResult();
        result.setRevision(targetRevision);
        //Find existing revision to replace it if needed
        if(targetRevision.getOrderId()!=null){
            Preconditions.checkNotNull(targetRevision.getOrderItemId(),"The order item isn't given for item %s",item.getId());
            TREV correspondingExistingRevision=null;
            for(TREV existingRevision:item.getRevisions()){
                if(targetRevision.getOrderId().equals(existingRevision.getOrderId()) &&
                        targetRevision.getOrderItemId().equals(existingRevision.getOrderItemId())){
                    correspondingExistingRevision=existingRevision;
                    break;
                }
            }

            if(correspondingExistingRevision!=null){
                if(targetRevision.isSame(correspondingExistingRevision)){
                    result.setRevision(correspondingExistingRevision);
                    result.setAction(RevisionUpdateResult.UpdateAction.UNCHANGED);
                    return result;
                }
                else{
                    result.setOldRevision(correspondingExistingRevision);
                    result.setAction(RevisionUpdateResult.UpdateAction.REPLACED);
                    //TODO check state of item revision
                    item.getRevisions().remove(correspondingExistingRevision);
                }
            }
            else{
                result.setAction(RevisionUpdateResult.UpdateAction.CREATED);
            }
        }

        //TODO manage ordering to ease future analysis
        item.addRevisions(targetRevision);
        return result;
    }


    public void applyApplicableRevisions(InstalledBaseUpdateResult result,InstalledBase ref,List<InstalledItemRevisionsToApply> revisions){

    }

    public List<InstalledItemRevisionsToApply> findApplicableRevisions(InstalledBase ref){
        List<InstalledItemRevisionsToApply> revisions=new ArrayList<>();
        revisions.add(findApplicableRevisions(ref.getContract()));
        for(InstalledOffer item:ref.getOffers()){
            revisions.add(findApplicableRevisions(item));
            for(InstalledTariff tariff:item.getTariffs()){
                revisions.add(findApplicableRevisions(tariff));
                for(InstalledDiscount discount:tariff.getDiscounts()){
                    revisions.add(findApplicableRevisions(discount));
                }
            }
        }
        for(InstalledProductService ps:ref.getPsList()){
            revisions.add(findApplicableRevisions(ps));
        }
        return revisions;
    }

    public <T extends InstalledItemRevision,TITEM extends InstalledItem<T>> InstalledItemRevisionsToApply<T,TITEM> findApplicableRevisions(TITEM baseItem){
        InstalledItemRevisionsToApply<T,TITEM> result=new InstalledItemRevisionsToApply<>(baseItem);
        for(T rev:baseItem.getRevisions()){
            if(rev.getRevState().equals(InstalledItemRevision.RevStatus.PLANNED)){
                if((rev.getEffectiveDate()==null) || (rev.getEffectiveDate().compareTo(dateTimeService.getCurrentDate())<=0)){
                    result.addRevisionToApply(rev);
                }
            }
        }
        return result;
    }

}
