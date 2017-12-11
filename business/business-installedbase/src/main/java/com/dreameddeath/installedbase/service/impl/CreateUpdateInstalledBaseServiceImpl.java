/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.installedbase.service.impl;

import com.dreameddeath.core.date.IDateTimeService;
import com.dreameddeath.installedbase.model.v1.InstalledBase;
import com.dreameddeath.installedbase.model.v1.common.*;
import com.dreameddeath.installedbase.model.v1.contract.InstalledContract;
import com.dreameddeath.installedbase.model.v1.offer.InstalledAtomicOffer;
import com.dreameddeath.installedbase.model.v1.offer.InstalledCompositeOffer;
import com.dreameddeath.installedbase.model.v1.offer.InstalledOffer;
import com.dreameddeath.installedbase.model.v1.productservice.InstalledProductService;
import com.dreameddeath.installedbase.model.v1.tariff.InstalledDiscount;
import com.dreameddeath.installedbase.model.v1.tariff.InstalledTariff;
import com.dreameddeath.installedbase.process.model.v1.*;
import com.dreameddeath.installedbase.service.ICreateUpdateInstalledBaseService;
import com.dreameddeath.installedbase.service.IInstalledBaseRevisionManagementService;
import com.dreameddeath.installedbase.service.utils.*;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Christophe Jeunesse on 16/10/2014.
 */
public class CreateUpdateInstalledBaseServiceImpl implements ICreateUpdateInstalledBaseService {
    private final static Logger LOG= LoggerFactory.getLogger(CreateUpdateInstalledBaseServiceImpl.class);

    private IInstalledBaseRevisionManagementService revisionManagementService;
    private IDateTimeService dateTimeService;

    @Autowired
    public void setRevisionManagementService(IInstalledBaseRevisionManagementService service){
        this.revisionManagementService = service;
    }

    @Autowired
    public void setDateTimeService(IDateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public InstalledBaseUpdateResult manageCreateUpdate(CreateUpdateInstalledBaseRequest request, InstalledBase ref, CreateUpdateInstalledBaseRequest.Contract reqContract){
        CreateUpdateWorkingInfo contractUpdateWorkingInfo=buildWorkingInfoForContract(request,ref,reqContract);
        for(CreateUpdateItemWorkingInfo<?,?,?,?> workingInfo:contractUpdateWorkingInfo.getWorkingItems()){
            try{
                //TODO keep track of update of revision
                RevisionUpdateResult result = revisionManagementService.addOrReplaceRevision(contractUpdateWorkingInfo.getInstalledBase(),(InstalledItem<InstalledItemRevision>)workingInfo.getTargetItem(),workingInfo.getTargetRevision());
                workingInfo.setRevisionUpdateResult(result);
                if(!result.getAction().equals(RevisionUpdateResult.UpdateAction.UNCHANGED)){
                    workingInfo.getResult().addRevision(result);
                }
                if((workingInfo.getUpdateRequest().tempId!=null) || !result.getAction().equals(RevisionUpdateResult.UpdateAction.UNCHANGED)){
                    if(workingInfo instanceof OfferUpdateWorkingInfo){
                        contractUpdateWorkingInfo.getInstalledBaseUpdateResult().addOfferUpdate((InstalledItemUpdateResult)workingInfo.getResult());
                    }
                    else if(workingInfo instanceof TariffUpdateWorkingInfo){
                        contractUpdateWorkingInfo.getInstalledBaseUpdateResult().addTariffs((TariffUpdateResult) workingInfo.getResult());
                    }
                    else if(workingInfo instanceof DiscountUpdateWorkingInfo){
                        contractUpdateWorkingInfo.getInstalledBaseUpdateResult().addDiscounts((DiscountUpdateResult) workingInfo.getResult());
                    }
                    else if(workingInfo instanceof ProductServiceUpdateWorkingInfo){
                        contractUpdateWorkingInfo.getInstalledBaseUpdateResult().addProducts((InstalledItemUpdateResult) workingInfo.getResult());
                    }
                    else if(workingInfo instanceof ContractUpdateWorkingInfo){
                        contractUpdateWorkingInfo.getInstalledBaseUpdateResult().setContract((InstalledItemUpdateResult) workingInfo.getResult());
                    }
                    else{
                        throw new RuntimeException("Not managed working type "+workingInfo.getClass().getName());
                    }
                }
            }
            catch(Throwable e){
                LOG.error("error during the revision management of item "+workingInfo.getItemUid(),e);
                throw e;
            }
        }
        revisionManagementService.applyApplicableRevisions(contractUpdateWorkingInfo.getInstalledBaseUpdateResult(),contractUpdateWorkingInfo.getInstalledBase());
        return contractUpdateWorkingInfo.getInstalledBaseUpdateResult();
    }

    public CreateUpdateWorkingInfo buildWorkingInfoForContract(CreateUpdateInstalledBaseRequest request, InstalledBase ref, CreateUpdateInstalledBaseRequest.Contract reqContract ){
        CreateUpdateWorkingInfo globalWorkingInfos = new CreateUpdateWorkingInfo(request,ref,reqContract);

        /*
        * Manage contract update
        */
        if(reqContract.id==null){
            Preconditions.checkArgument(ref.getContract()==null,"The contract shouldn't be existing in the installed base when adding a new one");
            Preconditions.checkNotNull(reqContract.tempId,"The contract request creation should have a temporary id");
            ref.setContract(new InstalledContract());
        }

        ContractUpdateWorkingInfo contractProcessingInfo = new ContractUpdateWorkingInfo(new InstalledItemUpdateResult(),reqContract,ref.getContract());
        globalWorkingInfos.addWorkingInfo(contractProcessingInfo);
        buildExpectedRevisionForIdentifiedItem(contractProcessingInfo);

        /*
        * Perform a first loop to setup offers, ps, and attributes
         */
        for(CreateUpdateInstalledBaseRequest.Offer offer:request.offers){
            OfferUpdateWorkingInfo<? extends InstalledOffer> workingInfo=null;
            //Find applicable elements for given contract (beware of move of elements from one contract to another)
            // loop on all applicable offer (even the one which are moving to another contract)
            if(globalWorkingInfos.findPath(offer,reqContract).size()>0){
                workingInfo=initOfferWorkingInfo(offer,globalWorkingInfos);
            }
            else if(offer.comOp.equals(CreateUpdateInstalledBaseRequest.CommercialOperation.MOVE)){
                //TODO manage move operation if from one contract to another?!?
            }

            if(workingInfo!=null){
                globalWorkingInfos.addWorkingInfo(workingInfo);
            }
        }

        /*
        * Loop again to manage tariffs, discounts, parent/child and links within current installed base
         */
        for(CreateUpdateItemWorkingInfo<? extends InstalledItemRevision,? extends IdentifiedItemUpdateResult,? extends CreateUpdateInstalledBaseRequest.IdentifiedItem,? extends InstalledItem<? extends InstalledItemRevision>> workingInfo:globalWorkingInfos.getWorkingItems()){
            if(workingInfo instanceof OfferUpdateWorkingInfo<?>){
                OfferUpdateWorkingInfo<?> offerWorkingInfo=(OfferUpdateWorkingInfo<?>)workingInfo;
                manageParentUpdate(offerWorkingInfo,globalWorkingInfos);
                //Manage links updates
                for(CreateUpdateInstalledBaseRequest.IdentifiedItemLink link :offerWorkingInfo.getUpdateRequest().links){
                    InstalledItemLinkRevision linkRevision = buildLinkRevision(offerWorkingInfo,link,globalWorkingInfos);
                    if(linkRevision!=null){
                        offerWorkingInfo.getTargetRevision().addLink(linkRevision);
                    }
                }
            }
            else if(workingInfo instanceof ProductServiceUpdateWorkingInfo){
                ProductServiceUpdateWorkingInfo psWorkingInfo = (ProductServiceUpdateWorkingInfo)workingInfo;
                //Manage links updates
                for(CreateUpdateInstalledBaseRequest.IdentifiedItemLink link :psWorkingInfo.getUpdateRequest().links){
                    InstalledItemLinkRevision linkRevision = buildLinkRevision(psWorkingInfo,link,globalWorkingInfos);
                    if(linkRevision!=null){
                        psWorkingInfo.getTargetRevision().addLink(linkRevision);
                    }
                }
            }
        }

        /**
         *  Update strategy :
         *      * buildFromInternal target revision and create empty element
         *          * "ignore" child change or "unchanged"
         *          * check target revision consistency between items
         *      * find existing revision if any, check inconsistencies with "new target item"
         *      * check revisions consistencies :
         *          * compare item per item
         *          * if revisions doesn't have dates, they must modify "compatible items" :
         *                  * it is possible to setup de "merged revision"
         *                  * remove duplicates?
         *          * if revisions have dates, short them an check applicability :
         *              * date must be placed at one place for each revision (effective or status, link, ...)
         *              * simulate future by advancing time at whole installed base for "timed" items.
         *              * apply separately the merged revision to check consistency for each "applicable time"
         */

        return globalWorkingInfos;
    }

    public void manageParentUpdate(OfferUpdateWorkingInfo<?> workingInfo, CreateUpdateWorkingInfo globalWorkingInfos){
        CreateUpdateInstalledBaseRequest.Offer offerRequest=workingInfo.getUpdateRequest();
        if(offerRequest.parent!=null){
            if(offerRequest.parent.id!=null) {
                if(!offerRequest.parent.id.equals(globalWorkingInfos.getInstalledBase().getContract().getId())){
                    InstalledOffer parentOffer=globalWorkingInfos.getInstalledBaseIndexer().getInstalledOffer(offerRequest.parent.id);
                    Preconditions.checkNotNull(parentOffer,"The id %s isn't existing in the installed base",offerRequest.parent.id);
                    Preconditions.checkArgument(parentOffer instanceof InstalledCompositeOffer,"The id %s isn't an Composite offer in the installed base but %s",offerRequest.parent.id,parentOffer.getClass().getSimpleName());
                    workingInfo.getTargetRevision().setParent(offerRequest.parent.id);
                }
            }
            else{
                CreateUpdateItemWorkingInfo<?,?,?,?> parent=globalWorkingInfos.getWorkingItemByTempId(offerRequest.parent.tempId);
                Preconditions.checkNotNull(parent,"The parent with temp id %s for item %s isn't found",offerRequest.parent.tempId,workingInfo.getItemUid());
                Preconditions.checkArgument((parent.getTargetItem() instanceof InstalledCompositeOffer) || (parent.getTargetItem() instanceof InstalledContract),"The parent %s of %s must be either an Composite offer of an Contract but is %s",parent.getItemUid(),workingInfo.getItemUid(),parent.getTargetItem().getClass().getSimpleName());
                workingInfo.getTargetRevision().setParent(parent.getTargetItem().getId());
            }
        }
        else{
            Preconditions.checkNotNull(workingInfo.getTargetItem().getParent(),"The parent element of item %s isn't defined",workingInfo.getItemUid());
        }

    }

    public InstalledItemLinkRevision buildLinkRevision(CreateUpdateItemWorkingInfo workingInfo,CreateUpdateInstalledBaseRequest.IdentifiedItemLink linkRequest, CreateUpdateWorkingInfo globalWorkingInfos) {
        InstalledItemLinkRevision link = new InstalledItemLinkRevision();

        if(linkRequest.comOp!=null){
            switch (linkRequest.comOp){
                case UNCHANGE: return null;
                case ADD: link.setAction(InstalledBaseRevisionAction.ADD);break;
                case REMOVE: link.setAction(InstalledBaseRevisionAction.REMOVE);break;
                case MODIFY: link.setAction(InstalledBaseRevisionAction.MODIFY);break;
            }
        }

        //Manage type
        Preconditions.checkNotNull(linkRequest.linkType,"The type of link for element %s isn't defined",workingInfo.getItemUid());
        link.setType(linkRequest.linkType.getType());

        //Manage target
        Preconditions.checkNotNull(linkRequest.target,"The target of link %s for element %s isn't defined",linkRequest.linkType,workingInfo.getItemUid());
        if(linkRequest.target.id!=null){
            if(workingInfo.getTargetItem() instanceof InstalledOffer){
                InstalledOffer offer=globalWorkingInfos.getInstalledBaseIndexer().getInstalledOffer(linkRequest.target.id);
                Preconditions.checkNotNull(offer,"The target offer id %s of link %s for element %s isn't found in installed base",linkRequest.target.id,linkRequest.linkType,workingInfo.getItemUid(),globalWorkingInfos.getInstalledBase().getUid());
                link.setTargetId(linkRequest.target.id);
            }
            else{
                InstalledProductService productService=globalWorkingInfos.getInstalledBaseIndexer().getInstalledProductService(linkRequest.target.id);
                Preconditions.checkNotNull(productService,"The target product service id %s of link %s for element %s isn't found in installed base",linkRequest.target.id,linkRequest.linkType,workingInfo.getItemUid(),globalWorkingInfos.getInstalledBase().getUid());
                link.setTargetId(linkRequest.target.id);
            }
        }
        else{
            Preconditions.checkNotNull(linkRequest.target.tempId,"The target of link %s for element %s isn't well defined (id or tempId must be given",linkRequest.linkType,workingInfo.getItemUid());
            CreateUpdateItemWorkingInfo<?,?,?,?> target = globalWorkingInfos.getWorkingItem(linkRequest.target);
            Preconditions.checkNotNull(target,"The target with tempId %s of link %s for element %s isn't defined",linkRequest.target.tempId,linkRequest.linkType,workingInfo.getItemUid());
            link.setTargetId(target.getTargetItem().getId());
            if(workingInfo.getTargetItem() instanceof InstalledOffer){
                Preconditions.checkArgument(target.getTargetItem() instanceof InstalledOffer,"The target %s of link %s for element %s ins't of the proper type (expecting product Installed Offer)",target.getItemUid(),linkRequest.linkType,workingInfo.getItemUid());
            }
            else{
                Preconditions.checkArgument(target.getTargetItem() instanceof InstalledProductService,"The target %s of link %s for element %s ins't of the proper type (expecting Installed Product Service)",target.getItemUid(),linkRequest.linkType,workingInfo.getItemUid());
            }
        }

        //Manage direction if given
        if(linkRequest.direction!=null){
            link.isReverse(linkRequest.direction.isReverse());
        }

        //Manage status if given
        if(linkRequest.status!=null &&linkRequest.status.code!=null){
            link.setStatus(linkRequest.status.code.getMappedCode());
        }
        else if(InstalledBaseRevisionAction.ADD.equals(link.getAction())) {
            link.setStatus(InstalledStatus.Code.ACTIVE);
        }
        else if(InstalledBaseRevisionAction.REMOVE.equals(link.getAction())) {
            link.setStatus(InstalledStatus.Code.REMOVED);
        }
        else{
            throw new RuntimeException("The status or valid action should be given");
        }

        if(linkRequest.status!=null && linkRequest.status.effectiveDate!=null){
            link.setStatusDate(linkRequest.status.effectiveDate);
        }

        return link;
    }

    public OfferUpdateWorkingInfo<? extends InstalledOffer> initOfferWorkingInfo(CreateUpdateInstalledBaseRequest.Offer offer,CreateUpdateWorkingInfo globalWorkingInfos){
        OfferUpdateWorkingInfo<? extends InstalledOffer> workingInfo;
        InstalledOffer foundInstalledOffer;
        if(offer.id!=null) {
            foundInstalledOffer = globalWorkingInfos.getInstalledBaseIndexer().getInstalledOffer(offer.id);
            Preconditions.checkNotNull(foundInstalledOffer,"Cannot find the installed Offer id %s in the installed base",offer.id);
        }
        else{
            Preconditions.checkNotNull(offer.tempId,"The offer request creation should have a temporary id");
            if(offer.type== CreateUpdateInstalledBaseRequest.OfferType.ATOMIC_OFFER) {
                Preconditions.checkNotNull(offer.ps, "Cannot find the ps for id %s in the installed base", offer.tempId);
                foundInstalledOffer = new InstalledAtomicOffer();
            }
            else{
                foundInstalledOffer = new InstalledCompositeOffer();
            }
            globalWorkingInfos.getInstalledBaseIndexer().addToInstalledBase(foundInstalledOffer);
        }

        //Create working info
        if(offer.type== CreateUpdateInstalledBaseRequest.OfferType.ATOMIC_OFFER){
            workingInfo=new AtomicOfferUpdateWorkingInfo(new InstalledItemUpdateResult(),offer,(InstalledAtomicOffer)foundInstalledOffer);
        }
        else{
            workingInfo=new CompositeOfferUpdateWorkingInfo(new InstalledItemUpdateResult(),offer,(InstalledCompositeOffer)foundInstalledOffer);
        }
        //Add working info
        globalWorkingInfos.addWorkingInfo(workingInfo);
        buildExpectedRevisionForIdentifiedItem(workingInfo);
        manageCommercialAttributeUpdates(workingInfo);
        //Manage Ps updates if required
        if((offer.type== CreateUpdateInstalledBaseRequest.OfferType.ATOMIC_OFFER) &&(offer.ps!=null)) {
            initProductServiceWorkingInfoForAtomicOffer((AtomicOfferUpdateWorkingInfo) workingInfo, globalWorkingInfos);
        }

        for(CreateUpdateInstalledBaseRequest.Tariff tariffRequest:workingInfo.getUpdateRequest().tariffs){
            initTariffUpdateWorkingInfoForOffer(workingInfo,tariffRequest,globalWorkingInfos);
        }

        return workingInfo;
    }

    public TariffUpdateWorkingInfo initTariffUpdateWorkingInfoForOffer(OfferUpdateWorkingInfo<?> parentOfferWorkingInfo, CreateUpdateInstalledBaseRequest.Tariff tariff, CreateUpdateWorkingInfo globalWorkingInfos){
        InstalledTariff tariffItem=null;
        if(tariff.id==null){
            Preconditions.checkNotNull(tariff.tempId,"Cannot find and id for the tariff %s for offer %s/%s",tariff.code,parentOfferWorkingInfo.getTargetItem().getCode(),parentOfferWorkingInfo.getItemUid());
            tariffItem = new InstalledTariff();
            parentOfferWorkingInfo.getTargetItem().addTariff(tariffItem);
        }
        else{
            for(InstalledTariff currTarr:parentOfferWorkingInfo.getTargetItem().getTariffs()){
                if(currTarr.getId().equals(tariff.id)){
                    tariffItem=currTarr;
                    break;
                }
            }
            Preconditions.checkNotNull(tariffItem,"Cannot find the tariff %s for the tariff %s for offer %s/%s",tariff.code,tariff.id,parentOfferWorkingInfo.getTargetItem().getCode(),parentOfferWorkingInfo.getItemUid());
        }
        
        TariffUpdateWorkingInfo workingInfo=new TariffUpdateWorkingInfo (new TariffUpdateResult(),tariff,tariffItem,parentOfferWorkingInfo);
        globalWorkingInfos.addWorkingInfo(workingInfo);
        buildExpectedRevisionForIdentifiedItem(workingInfo);

        for(CreateUpdateInstalledBaseRequest.Discount discount:workingInfo.getUpdateRequest().discounts){
            initDiscountUpdateWorkingInfoForOffer(workingInfo,discount,globalWorkingInfos);
        }
        return workingInfo;
    }
    
    public DiscountUpdateWorkingInfo initDiscountUpdateWorkingInfoForOffer(TariffUpdateWorkingInfo parentTariff, CreateUpdateInstalledBaseRequest.Discount discount, CreateUpdateWorkingInfo globalWorkingInfos){
        InstalledDiscount discountItem=null;
        if(discount.id==null){
            Preconditions.checkNotNull(discount.tempId,"Cannot find and id for the discount %s for offer %s/%s",discount.code,parentTariff.getTargetItem().getCode(),parentTariff.getItemUid());
            discountItem = new InstalledDiscount();
            parentTariff.getTargetItem().addDiscounts(discountItem);
        }
        else{
            for(InstalledDiscount currTarr:parentTariff.getTargetItem().getDiscounts()){
                if(currTarr.getId().equals(discount.id)){
                    discountItem=currTarr;
                    break;
                }
            }
            Preconditions.checkNotNull(discountItem,"Cannot find the discount %s for the discount %s for offer %s/%s",discount.code,discount.id,parentTariff.getTargetItem().getCode(),parentTariff.getItemUid());
        }

        DiscountUpdateWorkingInfo workingInfo=new DiscountUpdateWorkingInfo(new DiscountUpdateResult(),discount,discountItem,parentTariff);
        globalWorkingInfos.addWorkingInfo(workingInfo);
        buildExpectedRevisionForIdentifiedItem(workingInfo);
        return workingInfo;
    }

    public ProductServiceUpdateWorkingInfo initProductServiceWorkingInfoForAtomicOffer(AtomicOfferUpdateWorkingInfo parentOfferWorkingInfo,CreateUpdateWorkingInfo globalWorkingInfos){
        CreateUpdateInstalledBaseRequest.ProductService ps = parentOfferWorkingInfo.getUpdateRequest().ps;
        InstalledProductService psItem;
        if(ps.id==null){
            Preconditions.checkNotNull(ps.tempId,"Cannot find and id for the ps %s for offer %s/%s",ps.code,parentOfferWorkingInfo.getTargetItem().getCode(),parentOfferWorkingInfo.getItemUid());
            psItem=new InstalledProductService();
            globalWorkingInfos.getInstalledBaseIndexer().addToInstalledBase(psItem);
            parentOfferWorkingInfo.getTargetItem().setPs(psItem.getId());
            globalWorkingInfos.getInstalledBase().addPs(psItem);
        }
        else{
            String psId=parentOfferWorkingInfo.getTargetItem().getPs();
            Preconditions.checkNotNull(psId,"Cannot find the ps for the ps %s for offer %s",ps.id,parentOfferWorkingInfo.getTargetItem().getCode(),parentOfferWorkingInfo.getItemUid());
            Preconditions.checkArgument(psId.equals(ps.id),"Cannot change the PS from %s to %s for installed offer %s",psId,ps.id,parentOfferWorkingInfo.getItemUid());
            psItem=globalWorkingInfos.getInstalledBaseIndexer().getInstalledProductService(psId);
            Preconditions.checkNotNull(psItem,"Cannot find the ps for the ps %s for offer %s",ps.id,parentOfferWorkingInfo.getTargetItem().getCode(),parentOfferWorkingInfo.getItemUid());
        }

        ProductServiceUpdateWorkingInfo workingInfo=new ProductServiceUpdateWorkingInfo(new InstalledItemUpdateResult(),ps,psItem,parentOfferWorkingInfo);
        globalWorkingInfos.addWorkingInfo(workingInfo);
        globalWorkingInfos.getInstalledBaseIndexer().addToInstalledBase(workingInfo.getTargetItem());
        buildExpectedRevisionForIdentifiedItem(workingInfo);
        manageFunctionsUpdates(workingInfo);
        return workingInfo;
    }

    public void manageFunctionsUpdates(ProductServiceUpdateWorkingInfo workingInfo){
        for(CreateUpdateInstalledBaseRequest.Attribute function:workingInfo.getUpdateRequest().attributes){
            InstalledAttributeRevision attrRevision=buildAttributeRevision(workingInfo,function);
            if(attrRevision!=null) {
                for(InstalledAttributeRevision alreadyDefinedRevision:workingInfo.getTargetRevision().getFunctions()){
                    Preconditions.checkArgument(alreadyDefinedRevision.getCode().equals(attrRevision.getCode()),"The attribute code %s is defined twice in offre %s",attrRevision.getCode(),workingInfo.getItemUid());
                }
                workingInfo.getTargetRevision().addFunctions(attrRevision);
            }
        }
    }

    public void manageCommercialAttributeUpdates(OfferUpdateWorkingInfo<?> offerWorkingInfo){
        if(offerWorkingInfo.isItemItselfUnchanged()){
            return;
        }
        for(CreateUpdateInstalledBaseRequest.Attribute attribute:offerWorkingInfo.getUpdateRequest().attributes){
            InstalledAttributeRevision attrRevision=buildAttributeRevision(offerWorkingInfo,attribute);
            if(attrRevision!=null) {
                for(InstalledAttributeRevision alreadyDefinedRevision:offerWorkingInfo.getTargetRevision().getCommercialParameters()){
                    Preconditions.checkArgument(alreadyDefinedRevision.getCode().equals(attrRevision.getCode()),"The attribute code %s is defined twice in offre %s",attrRevision.getCode(),offerWorkingInfo.getItemUid());
                }
                offerWorkingInfo.getTargetRevision().addCommercialParameters(attrRevision);
            }
        }
    }

    private InstalledAttributeRevision buildAttributeRevision(CreateUpdateItemWorkingInfo offerWorkingInfo,CreateUpdateInstalledBaseRequest.Attribute attribute){
        InstalledAttributeRevision revision=new InstalledAttributeRevision();
        Preconditions.checkNotNull(attribute.code,"The code isn't given in attribute for offer",offerWorkingInfo.getItemUid());
        //Set Code
        revision.setCode(attribute.code);
        //Set action if applicable
        if(attribute.comOp!=null){
            switch (attribute.comOp){
                case ADD: revision.setAction(InstalledBaseRevisionAction.ADD);break;
                case REMOVE: revision.setAction(InstalledBaseRevisionAction.REMOVE);break;
                case UNCHANGE:return null;
            }
        }

        for(CreateUpdateInstalledBaseRequest.Attribute.Value value:attribute.values){
            InstalledValueRevision valueRevision = new InstalledValueRevision();
            Preconditions.checkNotNull(value.value==null,"No value nor code given for the attribute %s on offer",attribute.code,offerWorkingInfo.getItemUid());
            valueRevision.setValue(value.value);
            if(value.comOp!=null){
                switch (value.comOp){
                    case UNCHANGE:continue;
                    case ADD:valueRevision.setAction(InstalledBaseRevisionAction.ADD);break;
                    case REMOVE:valueRevision.setAction(InstalledBaseRevisionAction.REMOVE);break;
                }
            }
            //TODO : set the keyType from the catalogue
            valueRevision.setStartDate(value.startDate);
            valueRevision.setEndDate(value.endDate);

            revision.addValues(valueRevision);
        }

        return revision;
    }


    public void buildExpectedRevisionForIdentifiedItem(CreateUpdateItemWorkingInfo<? extends InstalledItemRevision, ? extends IdentifiedItemUpdateResult, ? extends CreateUpdateInstalledBaseRequest.IdentifiedItem, ? extends InstalledItem<?>> workingInfo) {
        //Perform some checks
        if(workingInfo.getUpdateRequest().id!=null){
            if(workingInfo.getUpdateRequest().code!=null){
                Preconditions.checkNotNull(workingInfo.getUpdateRequest().code.equals(workingInfo.getTargetItem().getCode()),"Cannot change to item %s from code %s to code %s",workingInfo.getItemUid(),workingInfo.getTargetItem().getCode(),workingInfo.getUpdateRequest().code);
            }
        }

        if(workingInfo.isItemItselfUnchanged()){
            Preconditions.checkNotNull(workingInfo.getUpdateRequest().id,"Cannot unchange an item (%s) without id",workingInfo.getItemUid());
            return;
        }

        // init for add
        if (workingInfo.getUpdateRequest().id==null){
            Preconditions.checkArgument(
                    (workingInfo.getUpdateRequest().comOp==null)
                    ||(workingInfo.getUpdateRequest().comOp==CreateUpdateInstalledBaseRequest.CommercialOperation.ADD),
                    "The commercial operation for element %s should be add or null but not %s",workingInfo.getItemUid(),workingInfo.getUpdateRequest().comOp
            );
            Preconditions.checkNotNull(workingInfo.getUpdateRequest().code,"The identified item %s must have a code",workingInfo.getItemUid());

            workingInfo.getTargetItem().setCode(workingInfo.getUpdateRequest().code);
            workingInfo.getTargetItem().setCreationDate(dateTimeService.getCurrentDate());
            workingInfo.getTargetItem().setLastModificationDate(workingInfo.getTargetItem().getCreationDate());
            workingInfo.getResult().setTempId(workingInfo.getUpdateRequest().tempId);
        }

        workingInfo.getResult().setId(workingInfo.getTargetItem().getId());

        updateIdentifiedItemStatus(workingInfo);
    }

    public void updateIdentifiedItemStatus(CreateUpdateItemWorkingInfo<? extends InstalledItemRevision,? extends IdentifiedItemUpdateResult,? extends CreateUpdateInstalledBaseRequest.IdentifiedItem,? extends InstalledItem<?>> mapElement){
        InstalledItemRevision targetRevision = mapElement.getTargetRevision();

        //if status given, use it
        if(mapElement.getUpdateRequest().status!=null && mapElement.getUpdateRequest().status.code!=null){
            switch(mapElement.getUpdateRequest().status.code){
                case ACTIVE: targetRevision.setStatus(InstalledStatus.Code.ACTIVE);break;
                case CLOSED: targetRevision.setStatus(InstalledStatus.Code.CLOSED);break;
                case SUSPENDED: targetRevision.setStatus(InstalledStatus.Code.SUSPENDED);break;
                case ABORTED: targetRevision.setStatus(InstalledStatus.Code.ABORTED);break;
                case REMOVED: targetRevision.setStatus(InstalledStatus.Code.REMOVED);break;
                default://TODO throw an error
            }
            if(mapElement.getApplicableOrderInfo()!=null) {
                targetRevision.setEffectiveDate(mapElement.getApplicableOrderInfo().effectiveDate);
            }
        }
        //if not, map using commercial operator if given
        else if(mapElement.getUpdateRequest().comOp!=null) {
            switch (mapElement.getUpdateRequest().comOp) {
                case ADD:case ACTIVATE: targetRevision.setStatus(InstalledStatus.Code.ACTIVE);break;
                case REMOVE: targetRevision.setStatus(InstalledStatus.Code.CLOSED);break;
                case SUSPEND: targetRevision.setStatus(InstalledStatus.Code.SUSPENDED);break;
                case CANCEL: targetRevision.setStatus(InstalledStatus.Code.REMOVED);break;
                case MIGRATE: targetRevision.setStatus(InstalledStatus.Code.CLOSED);break;
                default://ignore
            }
            if(mapElement.getApplicableOrderInfo()!=null) {
                targetRevision.setEffectiveDate(mapElement.getApplicableOrderInfo().effectiveDate);
            }
        }
    }
}
