package com.dreameddeath.installedbase.service;

import com.dreameddeath.core.date.IDateTimeService;
import com.dreameddeath.installedbase.model.InstalledBase;
import com.dreameddeath.installedbase.model.common.*;
import com.dreameddeath.installedbase.model.offer.InstalledCommercialParameter;
import com.dreameddeath.installedbase.model.offer.InstalledOffer;
import com.dreameddeath.installedbase.model.offer.InstalledOfferLink;
import com.dreameddeath.installedbase.model.offer.InstalledOfferRevision;
import com.dreameddeath.installedbase.model.productservice.InstalledFunction;
import com.dreameddeath.installedbase.model.productservice.InstalledProductService;
import com.dreameddeath.installedbase.model.productservice.InstalledProductServiceLink;
import com.dreameddeath.installedbase.model.productservice.InstalledProductServiceRevision;
import com.dreameddeath.installedbase.model.tariff.InstalledDiscount;
import com.dreameddeath.installedbase.model.tariff.InstalledTariff;
import com.dreameddeath.installedbase.process.model.*;
import com.dreameddeath.installedbase.service.utils.InstalledItemRevisionsToApply;
import com.dreameddeath.installedbase.utils.InstalledBaseTools;
import com.google.common.base.Preconditions;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
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


    @Override
    public List<InstalledItemRevisionsToApply> findApplicableRevisions(InstalledBaseUpdateResult result,InstalledBase ref){
        List<InstalledItemRevisionsToApply> revisions=new ArrayList<>();
        revisions.add(findApplicableRevisions(result,ref.getContract()));
        for(InstalledOffer item:ref.getOffers()){
            revisions.add(findApplicableRevisions(result,item));
            for(InstalledTariff tariff:item.getTariffs()){
                revisions.add(findApplicableRevisions(result,tariff));
                for(InstalledDiscount discount:tariff.getDiscounts()){
                    revisions.add(findApplicableRevisions(result,discount));
                }
            }
        }
        for(InstalledProductService ps:ref.getPsList()){
            revisions.add(findApplicableRevisions(result,ps));
        }
        return revisions;
    }

    @Override
    public void applyApplicableRevisions(InstalledBaseUpdateResult result,InstalledBase ref){
        List<InstalledItemRevisionsToApply> revisions=findApplicableRevisions(result,ref);
        applyApplicableRevisions(result,ref,revisions);
    }

    @Override
    public void applyApplicableRevisions(InstalledBaseUpdateResult result,InstalledBase ref,List<InstalledItemRevisionsToApply> revisions) {
        for(InstalledItemRevisionsToApply<? extends InstalledItemRevision,? extends InstalledItem> itemWithRevs:revisions){
            itemWithRevs.sortRevisions();
            boolean hasChanges = false;
            hasChanges|=applyStatusesFromRevision(itemWithRevs);
            if(itemWithRevs.getItemType().isHasLink()){
                hasChanges|=applyLinksFromRevision(ref,(InstalledItemRevisionsToApply)itemWithRevs);
            }
            if(itemWithRevs.getItemType().equals(InstalledItemRevisionsToApply.Type.OFFER)){
                hasChanges|=applyCommercialParametersFromRevision((InstalledItemRevisionsToApply<? extends InstalledOfferRevision, ? extends InstalledOffer>) itemWithRevs);
            }
            else if(itemWithRevs.getParent() instanceof InstalledProductService){
                hasChanges|=applyFunctionsFromRevision((InstalledItemRevisionsToApply<? extends InstalledProductServiceRevision, ? extends InstalledProductService>)itemWithRevs);
            }
            if(hasChanges && itemWithRevs.isNewUpdateResult()){
                switch (itemWithRevs.getItemType()){
                    case CONTRACT:result.setContract(itemWithRevs.getUpdateResult(InstalledItemUpdateResult.class));break;
                    case OFFER:result.addOfferUpdates(itemWithRevs.getUpdateResult(InstalledItemUpdateResult.class));break;
                    case PS:result.addProducts(itemWithRevs.getUpdateResult(InstalledItemUpdateResult.class));break;
                    case TARIFF:result.addTariffs(itemWithRevs.getUpdateResult(TariffUpdateResult.class));break;
                    case DISCOUNT:result.addDiscounts(itemWithRevs.getUpdateResult(DiscountUpdateResult.class));break;
                }
            }
        }
    }


    private boolean applyFunctionsFromRevision(InstalledItemRevisionsToApply<? extends InstalledProductServiceRevision, ? extends InstalledProductService> itemWithRevs) {
        boolean hasChanges=false;
        for(InstalledProductServiceRevision psRevision:itemWithRevs.getRevisionsToApply()){
            for(InstalledAttributeRevision attrRev:psRevision.getFunctions()){
                boolean isNew=false;
                InstalledFunction existingAttr=findExistingAttribute(attrRev,itemWithRevs.getParent().getFunctions());
                if(existingAttr==null){
                    existingAttr=initNewAttribute(attrRev,new InstalledFunction());
                    itemWithRevs.getParent().addFunctions(existingAttr);
                    isNew=true;
                }
                AttributeUpdateResult result=applyAttributeUpdateFromRevision(psRevision,attrRev,existingAttr,isNew);
                if(result!=null){
                    hasChanges=true;
                    itemWithRevs.getUpdateResult(InstalledItemUpdateResult.class).addAttributes(result);
                }
            }
        }
        return hasChanges;
    }

    private boolean applyCommercialParametersFromRevision(InstalledItemRevisionsToApply<? extends InstalledOfferRevision, ? extends InstalledOffer> itemWithRevs) {
        boolean hasChanges=false;
        for(InstalledOfferRevision offerRevision:itemWithRevs.getRevisionsToApply()){
            for(InstalledAttributeRevision attrRev:offerRevision.getCommercialParameters()){
                boolean isNew=false;
                InstalledCommercialParameter existingAttr=findExistingAttribute(attrRev,itemWithRevs.getParent().getCommercialParameters());
                if(existingAttr==null){
                    existingAttr=initNewAttribute(attrRev,new InstalledCommercialParameter());
                    itemWithRevs.getParent().addCommercialParameter(existingAttr);
                    isNew=true;
                }
                AttributeUpdateResult result=applyAttributeUpdateFromRevision(offerRevision,attrRev,existingAttr,isNew);
                if(result!=null){
                    hasChanges=true;
                    itemWithRevs.getUpdateResult(InstalledItemUpdateResult.class).addAttributes(result);
                }
            }
        }
        return hasChanges;
    }

    private <T extends InstalledAttribute> T initNewAttribute(InstalledAttributeRevision rev,T newAttr){
        newAttr.setCode(rev.getCode());
        return newAttr;
    }

    private <T extends InstalledAttribute> T findExistingAttribute(InstalledAttributeRevision rev,List<T> existingAttributes){
        for(T currAttr:existingAttributes){
            if(currAttr.getCode().equals(rev.getCode())){
                return currAttr;
            }
        }
        return null;
    }

    private AttributeUpdateResult applyAttributeUpdateFromRevision(InstalledItemRevision itemRev,InstalledAttributeRevision rev,InstalledAttribute attribute,boolean isNew){
        boolean hasChange=isNew;
        AttributeUpdateResult result=new AttributeUpdateResult();
        result.setCode(rev.getCode());
        if(isNew){
            Preconditions.checkArgument(rev.getAction()!=null && rev.getAction().equals(InstalledAttributeRevision.Action.ADD),"The action is not corresponding");
            result.setAction(AttributeUpdateResult.Action.ADD);
        }
        else{
            Preconditions.checkArgument(rev.getAction()!=null && !rev.getAction().equals(InstalledAttributeRevision.Action.ADD),"The action is not corresponding");
            result.setAction(AttributeUpdateResult.Action.MODIFY);
        }

        //TODO check consistency between values revisions request
        for(InstalledValueRevision valueRev:rev.getValues()){
            //Use of action for multiple values
            if(valueRev.getAction()!=null){
                switch(valueRev.getAction()){
                    case ADD:break;
                    case REMOVE: break;
                    case MODIFY: break;
                }
            }
            //No action : mono value, manage only dates
            else {
                DateTime newValueStartDate=valueRev.getStartDate();
                DateTime newValueEndDate=valueRev.getEndDate();
                if(newValueStartDate==null){newValueStartDate=itemRev.getEffectiveDate();}
                if(newValueStartDate==null){newValueStartDate=dateTimeService.getCurrentDate();}
                if(newValueEndDate==null)  {newValueEndDate = dateTimeService.max();}
                // empty value duration
                if(newValueStartDate.equals(newValueEndDate)){
                    continue;
                }

                List<InstalledValue> matchingInstalledValues = InstalledBaseTools.Values.findMatchingInstalledValues(attribute.getValues(),newValueStartDate,newValueEndDate);
                boolean addValue=true;
                for(InstalledValue value:matchingInstalledValues){
                    ValueUpdateResult valueUpdateResult=null;
                    //The installedValue is entirely within the new value
                    if( newValueStartDate.equals(value.getStartDate()) && newValueEndDate.equals(value.getEndDate()) && value.getValue().equals(valueRev.getValue()))
                    {
                        addValue=false; //the exact matching value is existing
                    }
                    else if((newValueStartDate.compareTo(value.getStartDate())<=0) && (newValueEndDate.compareTo(value.getEndDate())>=0) ){
                        hasChange=true;
                        valueUpdateResult=new ValueUpdateResult(value, ValueUpdateResult.Action.REMOVE);
                        attribute.removeValues(value);
                    }
                    else{
                        //It is an extension of currValue
                        if(value.getValue().equals(valueRev.getValue())){
                            addValue=false;
                            valueUpdateResult=new ValueUpdateResult(value, ValueUpdateResult.Action.MODIFY_DATES);
                            if(!newValueStartDate.equals(value.getStartDate())){
                                value.setStartDate(newValueStartDate);
                                valueUpdateResult.setStart(newValueStartDate);
                            }
                            if(!newValueEndDate.equals(value.getEndDate())){
                                value.setEndDate(newValueEndDate);
                                valueUpdateResult.setEnd(newValueEndDate);
                            }
                        }
                        //Split
                        else{
                            //it can be :
                            //   * change of start date
                            //   * change of end date
                            //   * split in two values
                            //Split in two values
                            if(value.getStartDate().compareTo(newValueEndDate)<0 && value.getEndDate().compareTo(newValueEndDate)>0){
                                //Add the new splitted value
                                InstalledValue newSplittedValue = new InstalledValue();
                                newSplittedValue.setStartDate(newValueEndDate);
                                newSplittedValue.setEndDate(value.getEndDate());
                                newSplittedValue.setValue(value.getValue());
                                newSplittedValue.setKeyType(value.getKeyType());
                                ValueUpdateResult splittedValueResult=new ValueUpdateResult(newSplittedValue, ValueUpdateResult.Action.ADD);
                                result.addValues(splittedValueResult);

                                valueUpdateResult=new ValueUpdateResult(value, ValueUpdateResult.Action.MODIFY_DATES);
                                splittedValueResult.setEnd(newSplittedValue.getEndDate());
                                value.setEndDate(newValueStartDate);
                                valueUpdateResult.setEnd(newValueStartDate);

                            }
                            //change of start date (insert of the new value in the past)
                            else if(newValueStartDate.compareTo(value.getStartDate())<0){
                                valueUpdateResult=new ValueUpdateResult(value, ValueUpdateResult.Action.MODIFY_DATES);
                                valueUpdateResult.setStart(newValueEndDate);
                                value.setStartDate(newValueEndDate);
                            }
                            //change of end date  of existing value(insert of the new value in the past)
                            else{
                                valueUpdateResult=new ValueUpdateResult(value, ValueUpdateResult.Action.MODIFY_DATES);
                                valueUpdateResult.setEnd(newValueStartDate);
                                value.setEndDate(newValueStartDate);
                            }

                        }
                    }

                    if(valueUpdateResult!=null){
                        hasChange=true;
                        result.addValues(valueUpdateResult);
                    }
                }

                if(addValue && valueRev.getValue()!=null){
                    hasChange=true;
                    InstalledValue newValue = new InstalledValue();
                    newValue.setStartDate(newValueStartDate);
                    newValue.setEndDate(newValueEndDate);
                    newValue.setValue(valueRev.getValue());
                    newValue.setKeyType(valueRev.getKeyType());
                    ValueUpdateResult newValueResult=new ValueUpdateResult(newValue, ValueUpdateResult.Action.ADD);
                    result.addValues(newValueResult);

                }
            }
        }

        if(hasChange){
            return result;
        }
        else{
            return null;
        }
    }

    public <TREV extends InstalledItemRevision & IHasLinkRevision,TITEM extends InstalledItem<TREV> & IHasInstalledItemLink<InstalledItemLink>> boolean applyLinksFromRevision(InstalledBase ref, InstalledItemRevisionsToApply<TREV,TITEM> itemWithRevs){
        boolean hasChanges=false;
        for(IHasLinkRevision itemRev:itemWithRevs.getRevisionsToApply()){
            for(InstalledItemLinkRevision rev:itemRev.getLinks()){
                LinkUpdateResult linkUpdateResult = applyLinkFromRevision(ref,itemWithRevs,rev,((InstalledItemRevision)itemRev).getEffectiveDate());
                hasChanges|= (linkUpdateResult!=null);
                if(linkUpdateResult!=null){
                    hasChanges=true;
                    itemWithRevs.getUpdateResult(InstalledItemUpdateResult.class).addLinkUpdates(linkUpdateResult);
                }
            }
        }
        return hasChanges;
    }

    public <TREV extends InstalledItemRevision & IHasLinkRevision,TITEM extends InstalledItem<TREV> & IHasInstalledItemLink<InstalledItemLink>> LinkUpdateResult applyLinkFromRevision(InstalledBase ref,InstalledItemRevisionsToApply<TREV,TITEM> itemWithRevs,InstalledItemLinkRevision linkRev,@Nullable DateTime effectiveDate){
        boolean hasChanges=false;
        InstalledItemLink existingLink=null;
        for(InstalledItemLink currLink:itemWithRevs.getParent().getLinks()){
            if(     currLink.getTargetId().equals(linkRev.getTargetId())
                    && currLink.getType().equals(linkRev.getType())
                    && currLink.getDirection().equals(linkRev.getDirection())
               )
            {
                existingLink=currLink;
                break;
            }
        }
        if(existingLink==null){
            //check the fact that it is added
            Preconditions.checkArgument(linkRev.getAction()!=null && linkRev.getAction().equals(InstalledItemLinkRevision.Action.ADD),"The new link %s on item %s has bad action %s",linkRev,itemWithRevs.getItemUid(),linkRev.getAction());
            switch(itemWithRevs.getItemType()){
                case OFFER: existingLink = new InstalledOfferLink(); break;
                case PS: existingLink = new InstalledProductServiceLink();break;
                case CONTRACT: //TODO;
                default: throw new RuntimeException("Cannot manage links on type "+itemWithRevs.getItemType()+" for item "+itemWithRevs.getItemUid());
            }
            itemWithRevs.getParent().addLink(existingLink);
            existingLink.setTargetId(linkRev.getTargetId());
            existingLink.setType(linkRev.getType());
            existingLink.setDirection(linkRev.getDirection());
            InstalledStatus status =new InstalledStatus();
            status.setCode(InstalledStatus.Code.INITIALIZED);
            status.setStartDate(dateTimeService.getCurrentDate());
            existingLink.setStatus(status);
            hasChanges=true;
        }
        else{
            //check the fact that it is added
            Preconditions.checkArgument(InstalledItemLinkRevision.Action.ADD.equals(linkRev.getAction()),"The existing link %s on item %s has bad action %s",linkRev,itemWithRevs.getItemUid(),linkRev.getAction());
        }

        //Changing status
        StatusUpdateResult resultUpdate = manageStatusUpdate(existingLink,linkRev.getStatus(),effectiveDate);
        if(resultUpdate!=null){
            hasChanges=true;
        }

        if(hasChanges){
            LinkUpdateResult result = new LinkUpdateResult();
            result.setTargetId(existingLink.getTargetId());
            result.setDirection(existingLink.getDirection());
            result.setType(existingLink.getType());
            result.setStatus(resultUpdate);
            return result;
        }
        else{
            return null;
        }

    }

    public <TREV extends InstalledItemRevision,TITEM extends InstalledItem<TREV>> boolean applyStatusesFromRevision(InstalledItemRevisionsToApply<TREV,TITEM> itemWithRevs){
        boolean hasChanges=false;
        for(InstalledItemRevision rev:itemWithRevs.getRevisionsToApply()){
            InstalledStatus revStatus=rev.getStatus();
            if(revStatus==null){
                continue;
            }
            StatusUpdateResult resultUpdate=manageStatusUpdate(itemWithRevs.getParent(),revStatus,rev.getEffectiveDate());
            if(resultUpdate!=null){
                hasChanges=true;
                itemWithRevs.getUpdateResult().addStatusUpdate(resultUpdate);
            }
        }

        return hasChanges;
    }

    public StatusUpdateResult manageStatusUpdate(IHasStatus refElement,InstalledStatus targetStatus,@Nullable DateTime targetEffectiveDate){
        InstalledStatus currStatus = refElement.getStatus();
        if(currStatus.getCode().equals(targetStatus.getCode())) {
            return null;
        }
        else{
            ///TODO check change status applicability
            DateTime effectiveDate;
            InstalledStatus newStatus = new InstalledStatus();
            InstalledStatus oldStatus = new InstalledStatus();

            oldStatus.setCode(currStatus.getCode());
            oldStatus.setStartDate(currStatus.getStartDate());

            newStatus.setCode(targetStatus.getCode());
            if(targetStatus.getStartDate()!=null){
                effectiveDate=targetStatus.getStartDate();
            }
            else if(targetEffectiveDate!=null) {
                effectiveDate = targetEffectiveDate;
            }
            else{
                effectiveDate = dateTimeService.getCurrentDate();
            }

            newStatus.setStartDate(effectiveDate);
            oldStatus.setEndDate(effectiveDate);
            refElement.addStatusHistory(oldStatus);
            refElement.setStatus(newStatus);

            StatusUpdateResult resultUpdate = new StatusUpdateResult();
            resultUpdate.setOldStatus(oldStatus.getCode());
            resultUpdate.setNewStatus(newStatus.getCode());
            resultUpdate.setEffectiveDate(effectiveDate);

            return resultUpdate;
        }
    }



    public <T extends InstalledItemRevision,TITEM extends InstalledItem<T>> InstalledItemRevisionsToApply<T,TITEM> findApplicableRevisions(InstalledBaseUpdateResult baseResult,TITEM baseItem){
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
