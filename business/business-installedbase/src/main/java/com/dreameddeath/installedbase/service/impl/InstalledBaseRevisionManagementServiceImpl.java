/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.installedbase.service.impl;

import com.dreameddeath.core.date.IDateTimeService;
import com.dreameddeath.installedbase.model.v1.InstalledBase;
import com.dreameddeath.installedbase.model.v1.common.*;
import com.dreameddeath.installedbase.model.v1.contract.InstalledContractLink;
import com.dreameddeath.installedbase.model.v1.offer.InstalledCommercialParameter;
import com.dreameddeath.installedbase.model.v1.offer.InstalledOffer;
import com.dreameddeath.installedbase.model.v1.offer.InstalledOfferLink;
import com.dreameddeath.installedbase.model.v1.offer.InstalledOfferRevision;
import com.dreameddeath.installedbase.model.v1.productservice.InstalledFunction;
import com.dreameddeath.installedbase.model.v1.productservice.InstalledProductService;
import com.dreameddeath.installedbase.model.v1.productservice.InstalledProductServiceLink;
import com.dreameddeath.installedbase.model.v1.productservice.InstalledProductServiceRevision;
import com.dreameddeath.installedbase.model.v1.tariff.InstalledDiscount;
import com.dreameddeath.installedbase.model.v1.tariff.InstalledTariff;
import com.dreameddeath.installedbase.process.model.v1.*;
import com.dreameddeath.installedbase.service.IInstalledBaseRevisionManagementService;
import com.dreameddeath.installedbase.service.utils.InstalledItemRevisionsToApply;
import com.dreameddeath.installedbase.utils.InstalledBaseTools;
import com.google.common.base.Preconditions;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Created by Christophe Jeunesse on 30/03/2016.
 */
public class InstalledBaseRevisionManagementServiceImpl implements IInstalledBaseRevisionManagementService {
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
                    item.removeRevision(correspondingExistingRevision);
                }
            }
            else{
                result.setAction(RevisionUpdateResult.UpdateAction.CREATED);
            }
        }
        else{
            result.setAction(RevisionUpdateResult.UpdateAction.CREATED);
        }

        //TODO manage ordering to ease future analysis
        item.addRevision(targetRevision);
        return result;
    }


    @Override
    public List<InstalledItemRevisionsToApply> findApplicableRevisions(InstalledBaseUpdateResult result, InstalledBase ref){
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
            if(itemWithRevs.getRevisionsToApply().size()==0){
                continue;
            }
            itemWithRevs.sortRevisions();
            boolean hasChanges = false;
            hasChanges|=applyStatusesFromRevision(itemWithRevs);
            if(itemWithRevs.getItemType().isHasLink()){
                hasChanges|=applyLinksFromRevision(ref,(InstalledItemRevisionsToApply)itemWithRevs);
            }
            if(itemWithRevs.getItemType().equals(InstalledItemRevisionsToApply.Type.OFFER)){
                hasChanges|=applyCommercialParametersFromRevision((InstalledItemRevisionsToApply<InstalledOfferRevision,InstalledOffer>)itemWithRevs);
            }
            else if(itemWithRevs.getParent() instanceof InstalledProductService){
                hasChanges|=applyFunctionsFromRevision((InstalledItemRevisionsToApply<? extends InstalledProductServiceRevision, ? extends InstalledProductService>)itemWithRevs);
            }
            if(hasChanges && itemWithRevs.isNewUpdateResult()){
                switch (itemWithRevs.getItemType()){
                    case CONTRACT:result.setContract(itemWithRevs.getUpdateResult(InstalledItemUpdateResult.class));break;
                    case OFFER:result.addOfferUpdate(itemWithRevs.getUpdateResult(InstalledItemUpdateResult.class));break;
                    case PS:result.addProducts(itemWithRevs.getUpdateResult(InstalledItemUpdateResult.class));break;
                    case TARIFF:result.addTariffs(itemWithRevs.getUpdateResult(TariffUpdateResult.class));break;
                    case DISCOUNT:result.addDiscounts(itemWithRevs.getUpdateResult(DiscountUpdateResult.class));break;
                }
            }
            if(hasChanges){
                itemWithRevs.getParent().setLastModificationDate(dateTimeService.getCurrentDate());
            }
            updateRevisions(itemWithRevs.getParent(),itemWithRevs.getRevisionsToApply());
        }
    }

    public void updateRevisions(InstalledItem<? extends InstalledItemRevision> installedItem,List<? extends InstalledItemRevision> revisions){
        Integer lastRank=installedItem.getRevisions().stream()
                .filter(rev->rev.getRank()!=null)
                .map(InstalledItemRevision::getRank)
                .max(Comparator.naturalOrder())
                .orElse(-1);

        for(InstalledItemRevision revision:revisions){
            revision.setRank(++lastRank);
            revision.setRevState(InstalledItemRevision.RevState.DONE);
            revision.setRunDate(dateTimeService.getCurrentDate());
        }
    }

    public boolean applyFunctionsFromRevision(InstalledItemRevisionsToApply<? extends InstalledProductServiceRevision, ? extends InstalledProductService> itemWithRevs) {
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
                hasChanges|=applyAttributeUpdateFromRevision(itemWithRevs,psRevision,attrRev,existingAttr,isNew);
            }
        }
        return hasChanges;
    }

    public boolean applyCommercialParametersFromRevision(InstalledItemRevisionsToApply<InstalledOfferRevision,InstalledOffer> itemWithRevs) {
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
                hasChanges|=applyAttributeUpdateFromRevision(itemWithRevs,offerRevision,attrRev,existingAttr,isNew);
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

    public boolean applyAttributeUpdateFromRevision(InstalledItemRevisionsToApply<? extends InstalledItemRevision,? extends InstalledItem<? extends InstalledItemRevision>> itemWithRevs, InstalledItemRevision itemRev,InstalledAttributeRevision rev,InstalledAttribute attribute,boolean isNew){
        boolean hasChange=isNew;
        boolean isNewUpdateResult=false;
        AttributeUpdateResult result=null;

        for(AttributeUpdateResult existingUpdate:itemWithRevs.getUpdateResult(InstalledItemUpdateResult.class).getAttributes()){
            if(existingUpdate.getCode().equals(rev.getCode())){
                result=existingUpdate;
                break;
            }
        }

        if(result==null) {
            isNewUpdateResult=true;
            result=new AttributeUpdateResult();
            result.setAction(isNew? AttributeUpdateResult.Action.ADD: AttributeUpdateResult.Action.MODIFY);
            result.setCode(rev.getCode());
        }

        if(isNew){
            Preconditions.checkArgument(rev.getAction()==null || rev.getAction().equals(InstalledAttributeRevision.Action.ADD),"The action %s is not corresponding for NOT existing attribute %s",rev.getAction(),rev.getCode());
        }
        else{
            Preconditions.checkArgument(rev.getAction()==null || !rev.getAction().equals(InstalledAttributeRevision.Action.ADD),"The action %s is not corresponding for ALREADY existing attribute %s",rev.getAction(),rev.getCode());
        }

        int valueRevPos=0;
        for(InstalledValueRevision valueRev:rev.getValues()){
            ++valueRevPos;
            List<ValueUpdateResult> valueUpdateResults;
            //Use of action for multiple values
            if(valueRev.getAction()!=null){
                valueUpdateResults = applyAttributeValueUpdatesFromRevisionByAction(itemWithRevs,itemRev,rev,attribute,valueRev,valueRevPos);
            }
            //No action : mono value, manage only using dates
            else {
                valueUpdateResults = applyAttributeValueUpdatesFromRevisionWithoutAction(itemWithRevs,itemRev,rev,attribute,valueRev,valueRevPos);
            }

            if(valueUpdateResults.size()>0){
                hasChange|=true;
                mergeAttributeValuesUpdate(result.getValues(),valueUpdateResults).forEach(result::addValues);
            }
        }

        if(hasChange){
            attribute.sortValues();
            if(isNewUpdateResult) {
                itemWithRevs.getUpdateResult(InstalledItemUpdateResult.class).addAttributes(result);
            }
        }

        return hasChange;
    }


    public List<ValueUpdateResult> applyAttributeValueUpdatesFromRevisionByAction(InstalledItemRevisionsToApply<?,?> itemWithRevs, InstalledItemRevision itemRev,InstalledAttributeRevision rev,InstalledAttribute attribute,InstalledValueRevision valueRev,int valueRevPos){
        Preconditions.checkNotNull(valueRev.getValue(),"The value of value rev %s/#%s must have an value defined for item %s",attribute.getCode(),valueRevPos,itemWithRevs.getItemUid());
        DateTime startDate=valueRev.getStartDate();
        if(startDate==null){startDate=itemRev.getEffectiveDate();}
        DateTime endDate=valueRev.getEndDate();
        if(endDate==null){endDate= dateTimeService.max();}

        List<InstalledValue> matchingInstalledValues = InstalledBaseTools.Values.findMatchingInstalledValues(attribute.getValues(),startDate,valueRev.getValue());
        ValueUpdateResult valueUpdateResult=null;
        if(valueRev.getAction()== InstalledValueRevision.Action.ADD){
            Preconditions.checkArgument(matchingInstalledValues.size()==0,"The existing attribute value creation request %s/#%s on item %s has already matching attribute list <%s>",attribute.getCode(),valueRevPos,itemWithRevs.getItemUid(),matchingInstalledValues.toString());
            InstalledValue newInstalledValue= new InstalledValue();
            newInstalledValue.setValue(valueRev.getValue());
            newInstalledValue.setStartDate(startDate);
            newInstalledValue.setEndDate(endDate);
            attribute.addValues(newInstalledValue);
            valueUpdateResult = new ValueUpdateResult(newInstalledValue, ValueUpdateResult.Action.ADD);
        }
        else{
            Preconditions.checkArgument(valueRev.getStartDate()!=null,"The existing attribute value update request %s/#%s on item %s must have a startDocument date as the action is %s",attribute.getCode(),valueRevPos,itemWithRevs.getItemUid(),valueRev.getAction());
            Preconditions.checkArgument(matchingInstalledValues.size()==1,"The existing attribute value update request %s/#%s on item %s has wrong matching attribute list <%s> for action %s",attribute.getCode(),valueRevPos,itemWithRevs.getItemUid(),matchingInstalledValues.toString(),valueRev.getAction());
            InstalledValue valueToUpdate=matchingInstalledValues.get(0);
            ValueUpdateResult.Action effectiveAction;
            if(valueRev.getAction()== InstalledValueRevision.Action.MODIFY){
                effectiveAction= ValueUpdateResult.Action.MODIFY_DATES;
                if(endDate.equals(valueToUpdate.getEndDate())) {
                    return Collections.emptyList();
                }
                else if(endDate.equals(valueToUpdate.getStartDate())){
                    effectiveAction= ValueUpdateResult.Action.REMOVE;
                }
            }
            else{
                effectiveAction= ValueUpdateResult.Action.REMOVE;
            }

            valueUpdateResult = new ValueUpdateResult(valueToUpdate, effectiveAction);
            if(effectiveAction== ValueUpdateResult.Action.REMOVE){
                attribute.removeValues(valueToUpdate);
            }
            else{
                valueToUpdate.setEndDate(endDate);
                valueUpdateResult.setEndDate(endDate);
            }
        }

        List<ValueUpdateResult> result=new ArrayList<>(1);
        result.add(valueUpdateResult);
        return result;
    }


    public List<ValueUpdateResult> applyAttributeValueUpdatesFromRevisionWithoutAction(InstalledItemRevisionsToApply<?,?> itemWithRevs, InstalledItemRevision itemRev,InstalledAttributeRevision rev,InstalledAttribute attribute,InstalledValueRevision valueRev,int valueRevPos){
        DateTime newValueStartDate=valueRev.getStartDate();
        DateTime newValueEndDate=valueRev.getEndDate();
        if(newValueStartDate==null){newValueStartDate=itemRev.getEffectiveDate();}
        if(newValueStartDate==null){newValueStartDate=dateTimeService.getCurrentDate();}
        if(newValueEndDate==null)  {newValueEndDate = dateTimeService.max();}

        // empty value duration
        if(newValueStartDate.equals(newValueEndDate)){
            return Collections.emptyList();
        }

        List<ValueUpdateResult> result=new ArrayList<>();
        List<InstalledValue> matchingInstalledValues = InstalledBaseTools.Values.findMatchingInstalledValues(attribute.getValues(),newValueStartDate,newValueEndDate);

        boolean addValue=true;
        for(InstalledValue value:matchingInstalledValues){
            ValueUpdateResult valueUpdateResult=null;
            //The installedValue is entirely within the new value
            if( newValueStartDate.equals(value.getStartDate()) && newValueEndDate.equals(value.getEndDate()) && value.getValue().equals(valueRev.getValue())) {
                addValue=false; //the exact matching value is existing
            }
            else if((newValueStartDate.compareTo(value.getStartDate())<=0) && (newValueEndDate.compareTo(value.getEndDate())>=0) ){
                valueUpdateResult=new ValueUpdateResult(value, ValueUpdateResult.Action.REMOVE);
                attribute.removeValues(value);
            }
            else{
                //It is an extension of currValue
                if(value.getValue().equals(valueRev.getValue())){
                    addValue=false;
                    //No change required has the new value is "included in the current one
                    if(newValueStartDate.compareTo(value.getStartDate())>=0 &&
                            newValueEndDate.compareTo(value.getEndDate())<=0){
                        continue;
                    }
                    valueUpdateResult=new ValueUpdateResult(value, ValueUpdateResult.Action.MODIFY_DATES);
                    if(!newValueEndDate.equals(value.getEndDate())){
                        value.setEndDate(newValueEndDate);
                        valueUpdateResult.setEndDate(newValueEndDate);
                    }
                }
                //Split
                else{
                    //it can be :
                    //   * change of startDocument date
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
                        attribute.addValues(newSplittedValue);
                        ValueUpdateResult splittedValueResult=new ValueUpdateResult(newSplittedValue, ValueUpdateResult.Action.ADD);
                        result.add(splittedValueResult);

                        valueUpdateResult=new ValueUpdateResult(value, ValueUpdateResult.Action.MODIFY_DATES);
                        splittedValueResult.setEndDate(newSplittedValue.getEndDate());
                        value.setEndDate(newValueStartDate);
                        valueUpdateResult.setEndDate(newValueStartDate);

                    }
                    //change of startDocument date (insert of the new value in the past)
                    else if(newValueStartDate.compareTo(value.getStartDate())<0){
                        valueUpdateResult=new ValueUpdateResult(value, ValueUpdateResult.Action.MODIFY_DATES);
                        valueUpdateResult.setStartDate(newValueEndDate);
                        value.setStartDate(newValueEndDate);
                    }
                    //change of end date  of existing value(insert of the new value in the past)
                    else{
                        valueUpdateResult=new ValueUpdateResult(value, ValueUpdateResult.Action.MODIFY_DATES);
                        valueUpdateResult.setEndDate(newValueStartDate);
                        value.setEndDate(newValueStartDate);
                    }
                }
            }

            if(valueUpdateResult!=null){
                result.add(valueUpdateResult);
            }
        }

        if(addValue && valueRev.getValue()!=null){
            InstalledValue newValue = new InstalledValue();
            newValue.setStartDate(newValueStartDate);
            newValue.setEndDate(newValueEndDate);
            newValue.setValue(valueRev.getValue());
            newValue.setKeyType(valueRev.getKeyType());
            attribute.addValues(newValue);
            ValueUpdateResult newValueResult=new ValueUpdateResult(newValue, ValueUpdateResult.Action.ADD);
            result.add(newValueResult);
        }

        result.sort((a,b)->{
            int res=a.getStartDate().compareTo(b.getStartDate());
            res=(res==0)?a.getValue().compareTo(b.getValue()):res;
            res=(res==0)?a.getEndDate().compareTo(b.getEndDate()):res;
            return res;
        });

        return result;
    }

    public List<ValueUpdateResult> mergeAttributeValuesUpdate(List<ValueUpdateResult> origStatusUpdates,List<ValueUpdateResult> newResults){
        Iterator<ValueUpdateResult> newResultIterator=newResults.iterator();
        while(newResultIterator.hasNext()) {
            ValueUpdateResult newResult=newResultIterator.next();
            for(ValueUpdateResult oldValueUpdate:origStatusUpdates){
                if(newResult.getValue().equals(oldValueUpdate.getValue()) && newResult.getStartDate().equals(oldValueUpdate.getStartDate())){
                    newResultIterator.remove();
                    oldValueUpdate.setEndDate(newResult.getEndDate());
                }
            }
        }
        return newResults;
    }


    public <TLINK extends InstalledItemLink,TREV extends InstalledItemRevision & IHasLinkRevision,TITEM extends InstalledItem<TREV> & IHasInstalledItemLink<TLINK>> boolean applyLinksFromRevision(InstalledBase ref, InstalledItemRevisionsToApply<TREV,TITEM> itemWithRevs){
        boolean hasChanges=false;
        for(IHasLinkRevision itemRev:itemWithRevs.getRevisionsToApply()){
            for(InstalledItemLinkRevision rev:itemRev.getLinks()){
                LinkUpdateResult linkUpdateResult = applyLinkFromRevision(ref,itemWithRevs,rev,((InstalledItemRevision)itemRev).getEffectiveDate());
                hasChanges|= (linkUpdateResult!=null);
                if(linkUpdateResult!=null){
                    hasChanges=true;
                    itemWithRevs.getUpdateResult(InstalledItemUpdateResult.class).addLink(linkUpdateResult);
                }
            }
        }
        return hasChanges;
    }


    public <TLINK extends InstalledItemLink,TREV extends InstalledItemRevision & IHasLinkRevision,TITEM extends InstalledItem<TREV> & IHasInstalledItemLink<TLINK>> LinkUpdateResult applyLinkFromRevision(InstalledBase ref,InstalledItemRevisionsToApply<TREV,TITEM> itemWithRevs,InstalledItemLinkRevision linkRev,@Nullable DateTime parentRevEffectiveDate){
        boolean hasChanges=false;
        TLINK existingLink=null;
        for(TLINK currLink:itemWithRevs.getParent().getLinks()){
            if(     currLink.getTargetId().equals(linkRev.getTargetId())
                    && currLink.getType().equals(linkRev.getType())
                    && (
                            (currLink.isReverse()==null && (linkRev.isReverse()==null))
                            || (currLink.isReverse()==linkRev.isReverse())
                    )
               )
            {
                existingLink=currLink;
                break;
            }
        }
        if(existingLink==null){
            //check the fact that it is added
            Preconditions.checkArgument(linkRev.getAction()==null || linkRev.getAction().equals(InstalledItemLinkRevision.Action.ADD),"The new link %s on item %s has bad action %s",linkRev,itemWithRevs.getItemUid(),linkRev.getAction());
            switch(itemWithRevs.getItemType()){
                case OFFER: existingLink = (TLINK)new InstalledOfferLink(); break;
                case PS: existingLink = (TLINK)new InstalledProductServiceLink();break;
                case CONTRACT: existingLink=(TLINK)new InstalledContractLink();break;
                default: throw new RuntimeException("Cannot manage links on type "+itemWithRevs.getItemType()+" for item "+itemWithRevs.getItemUid());
            }
            itemWithRevs.getParent().addLink(existingLink);
            existingLink.setTargetId(linkRev.getTargetId());
            existingLink.setType(linkRev.getType());
            existingLink.isReverse(linkRev.isReverse());
            hasChanges=true;
        }
        else{
            //check the fact that it is not added
            Preconditions.checkArgument(!InstalledItemLinkRevision.Action.ADD.equals(linkRev.getAction()),"The existing link %s on item %s has bad action %s",linkRev,itemWithRevs.getItemUid(),linkRev.getAction());
        }
        InstalledStatus.Code statusCode=linkRev.getStatus();
        if(statusCode==null){
            Preconditions.checkNotNull(linkRev.getAction(),"The existing link %s on item %s must have an action as status not given",linkRev,itemWithRevs.getItemUid());
            Preconditions.checkNotNull(linkRev.getAction()== InstalledItemLinkRevision.Action.ADD||linkRev.getAction()== InstalledItemLinkRevision.Action.REMOVE,"The existing link %s on item %s without status must have a valid action and not %s",linkRev,itemWithRevs.getItemUid(),linkRev.getAction());
            switch (linkRev.getAction()){
                case ADD: statusCode=InstalledStatus.Code.ACTIVE;break;
                case REMOVE:statusCode= InstalledStatus.Code.CLOSED;break;
            }
        }
        DateTime effectiveDate=linkRev.getStatusDate();
        if(effectiveDate==null){
            effectiveDate=parentRevEffectiveDate;
        }
        if(effectiveDate==null){
            effectiveDate=dateTimeService.getCurrentDate();
        }
        //Changing status
        List<StatusUpdateResult> statusUpdateResults = manageStatusesUpdate(existingLink,statusCode,effectiveDate);
        hasChanges|=(statusUpdateResults.size()>0);

        if(hasChanges){
            boolean isNew=false;
            //First find if already link update Result
            LinkUpdateResult result=null;
            for(LinkUpdateResult existingResult:itemWithRevs.getUpdateResult(InstalledItemUpdateResult.class).getLinks()){
                if( existingResult.getType().equals(existingLink.getType())
                    && existingResult.getTargetId().equals(existingLink.getTargetId())
                    && (
                        (existingResult.isReverse()==null && (existingLink.isReverse()==null))
                        || (existingResult.isReverse()!=null && existingResult.isReverse().equals(existingLink.isReverse()))
                    )
                )
                {
                    result =existingResult;
                    break;
                }
            }

            if(result==null) {
                result=new LinkUpdateResult();
                result.setTargetId(existingLink.getTargetId());
                result.isReverse(existingLink.isReverse());
                result.setType(existingLink.getType());
                isNew=true;
            }
            mergeStatusesUpdate(result.getStatuses(),statusUpdateResults).forEach(result::addStatus);
            return (isNew)?result:null;
        }
        else{
            return null;
        }
    }

    public <TREV extends InstalledItemRevision,TITEM extends InstalledItem<TREV>> boolean applyStatusesFromRevision(InstalledItemRevisionsToApply<TREV,TITEM> itemWithRevs){
        boolean hasChanges=false;
        for(InstalledItemRevision rev:itemWithRevs.getRevisionsToApply()){
            InstalledStatus.Code revStatusCode=rev.getStatus();
            TITEM item=itemWithRevs.getParent();
            IdentifiedItemUpdateResult updateResult=itemWithRevs.getUpdateResult();
            if(revStatusCode==null){
                continue;
            }
            DateTime effectiveStatusDate=rev.getEffectiveDate();
            if(effectiveStatusDate==null){
                effectiveStatusDate=dateTimeService.getCurrentDate();
            }
            List<StatusUpdateResult> statusUpdateResults = manageStatusesUpdate(item,revStatusCode,effectiveStatusDate);
            hasChanges|=(statusUpdateResults.size()>0);
            mergeStatusesUpdate(updateResult.getStatuses(),statusUpdateResults).forEach(updateResult::addStatus);
        }

        return hasChanges;
    }

    public List<StatusUpdateResult> mergeStatusesUpdate(List<StatusUpdateResult> origStatusUpdates,List<StatusUpdateResult> newResults){
        Iterator<StatusUpdateResult> newResultIterator=newResults.iterator();
        while(newResultIterator.hasNext()) {
            StatusUpdateResult newResult=newResultIterator.next();
            for(StatusUpdateResult oldStatusUpdate:origStatusUpdates){
                if(newResult.getCode().equals(oldStatusUpdate.getCode()) && newResult.getStartDate().equals(oldStatusUpdate.getStartDate())){
                    newResultIterator.remove();
                    oldStatusUpdate.setEndDate(newResult.getEndDate());
                }
            }
        }
        return newResults;
    }

    public List<StatusUpdateResult> manageStatusesUpdate(IHasStatus item,InstalledStatus.Code targetStatusCode,DateTime effectiveStatusDate){
        List<InstalledStatus> currStatuses = item.getStatuses(effectiveStatusDate,dateTimeService.max());
        List<StatusUpdateResult> results=new ArrayList<>(currStatuses.size()+1);
        for(InstalledStatus currMathingStatus:currStatuses) {
            StatusUpdateResult resultUpdate = manageStatusUpdate(item,currMathingStatus, targetStatusCode, effectiveStatusDate);
            //check if  added
            if (resultUpdate != null) {
                results.add(resultUpdate);
            }
        }
        if((currStatuses.size()==0) || !(currStatuses.get(0).getCode().equals(targetStatusCode))){
            InstalledStatus status=new InstalledStatus();
            status.setCode(targetStatusCode);
            status.setStartDate(effectiveStatusDate);
            status.setEndDate(dateTimeService.max());
            item.addStatus(status);
            StatusUpdateResult updateResult = new StatusUpdateResult();
            updateResult.setAction(StatusUpdateResult.Action.NEW);
            updateResult.setCode(targetStatusCode);
            updateResult.setStartDate(status.getStartDate());
            updateResult.setEndDate(status.getEndDate());
            results.add(updateResult);
        }
        return results;
    }



    public StatusUpdateResult manageStatusUpdate(IHasStatus item,InstalledStatus currStatus,InstalledStatus.Code targetStatusCode,DateTime targetEffectiveDate) {
        StatusUpdateResult resultUpdate = new StatusUpdateResult();
        resultUpdate.setCode(currStatus.getCode());
        resultUpdate.setStartDate(currStatus.getStartDate());
        resultUpdate.setOldEndDate(currStatus.getEndDate());

        //Full Delete
        if (targetEffectiveDate.compareTo(currStatus.getStartDate()) < 0) {
            resultUpdate.setAction(StatusUpdateResult.Action.DELETED);
            item.removeStatus(currStatus);
            currStatus.setEndDate(currStatus.getStartDate());
            return resultUpdate;
        }
        //if same code, if same end date, ignore the modification it may be a change of end date
        else if (currStatus.getCode().equals(targetStatusCode)) {
            if (dateTimeService.isMax(currStatus.getEndDate())) {
                return null;
            } else {
                resultUpdate.setAction(StatusUpdateResult.Action.MODIFIED);
                currStatus.setEndDate(dateTimeService.max());
            }
        } else {
            resultUpdate.setAction(StatusUpdateResult.Action.MODIFIED);
            currStatus.setEndDate(targetEffectiveDate);
        }

        resultUpdate.setEndDate(currStatus.getEndDate());

        return resultUpdate;

    }



    public <T extends InstalledItemRevision,TITEM extends InstalledItem<T>> InstalledItemRevisionsToApply<T,TITEM> findApplicableRevisions(InstalledBaseUpdateResult baseResult,TITEM baseItem){
        InstalledItemRevisionsToApply<T,TITEM> result=new InstalledItemRevisionsToApply<>(baseItem,baseResult);

        for(T rev:baseItem.getRevisions()){
            if(InstalledItemRevision.RevState.PLANNED.equals(rev.getRevState())){
                if((rev.getEffectiveDate()==null) || (rev.getEffectiveDate().compareTo(dateTimeService.getCurrentDate())<=0)){
                    result.addRevisionToApply(rev);
                }
            }
        }

        return result;
    }

}
