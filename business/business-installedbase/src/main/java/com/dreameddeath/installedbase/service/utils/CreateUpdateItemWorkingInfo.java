package com.dreameddeath.installedbase.service.utils;

import com.dreameddeath.installedbase.model.v1.common.InstalledItem;
import com.dreameddeath.installedbase.model.v1.common.InstalledItemRevision;
import com.dreameddeath.installedbase.process.model.v1.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.process.model.v1.IdentifiedItemUpdateResult;
import com.dreameddeath.installedbase.process.model.v1.RevisionUpdateResult;

/**
 * Created by Christophe Jeunesse on 24/03/2016.
 */
public abstract class CreateUpdateItemWorkingInfo<
        TREV extends InstalledItemRevision,
        TRES extends IdentifiedItemUpdateResult,
        TREQ extends CreateUpdateInstalledBaseRequest.IdentifiedItem,
        TTARGET extends InstalledItem<TREV>
        > {
    private final CreateUpdateItemWorkingInfo<?,?,?,?> parent;
    private final TRES result;
    private final TREQ updateRequest;
    private final TTARGET targetItem;
    private final TREV currRevision;
    private final TREV targetRevision;
    private RevisionUpdateResult revisionUpdateResult;

    public CreateUpdateItemWorkingInfo(TRES result, TREQ updateRequest, TTARGET targetItem){
        this(result,updateRequest,targetItem,null);
    }

    public CreateUpdateItemWorkingInfo(TRES result, TREQ updateRequest, TTARGET targetItem,CreateUpdateItemWorkingInfo parent) {
        this.parent=parent;
        this.result = result;
        this.updateRequest = updateRequest;
        this.targetItem = targetItem;
        this.targetRevision = newRevision();
        TREV foundRevision = null;
        revisionUpdateResult=null;
        this.targetRevision.setRevState(InstalledItemRevision.RevState.PLANNED);
        CreateUpdateInstalledBaseRequest.OrderItemInfo orderItemInfo=getApplicableOrderInfo();
        if (orderItemInfo != null) {
            this.targetRevision.setOrderId(orderItemInfo.orderId);
            this.targetRevision.setOrderItemId(orderItemInfo.orderItemId);
            this.targetRevision.setEffectiveDate(orderItemInfo.effectiveDate);
            if(orderItemInfo.status!=null) {
                this.targetRevision.setRevState(orderItemInfo.status.toRevState());
            }
            //Lookup for existing revision if any
            for (TREV revision : targetItem.getRevisions()) {
                if (orderItemInfo.orderId.equals(revision.getOrderId()) &&
                        orderItemInfo.orderItemId.equals(revision.getOrderItemId())) {
                    foundRevision = revision;
                    break;
                }
            }
        }
        currRevision = foundRevision;
    }

    protected abstract TREV newRevision();

    public TRES getResult() {
        return result;
    }

    public String getItemUid() {
        return targetItem.getId() +
                ((updateRequest.tempId != null) ? "[temp:" + updateRequest.tempId + "]" : "");
    }

    public TREQ getUpdateRequest() {
        return updateRequest;
    }

    public CreateUpdateInstalledBaseRequest.OrderItemInfo getApplicableOrderInfo(){
        if(updateRequest!=null && updateRequest.orderInfo!=null && updateRequest.orderInfo.orderId!=null){
            return updateRequest.orderInfo;
        }

        if(parent!=null){
            return parent.getApplicableOrderInfo();
        }
        return null;
    }

    public TTARGET getTargetItem() {
        return targetItem;
    }

    public TREV getTargetRevision() {
        return targetRevision;
    }

    public boolean isItemItselfUnchanged(){
        return (updateRequest.comOp!=null) &&
                (updateRequest.comOp.equals(CreateUpdateInstalledBaseRequest.CommercialOperation.UNCHANGE) ||
                updateRequest.comOp.equals(CreateUpdateInstalledBaseRequest.CommercialOperation.CHILD_CHANGE))
        ;
    }

    public RevisionUpdateResult getRevisionUpdateResult() {
        return revisionUpdateResult;
    }

    public void setRevisionUpdateResult(RevisionUpdateResult revisionUpdateResult) {
        this.revisionUpdateResult = revisionUpdateResult;
    }
}
