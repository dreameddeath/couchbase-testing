package com.dreameddeath.installedbase.service.utils;

import com.dreameddeath.installedbase.model.common.InstalledItem;
import com.dreameddeath.installedbase.model.common.InstalledItemRevision;
import com.dreameddeath.installedbase.model.process.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.process.model.IdentifiedItemUpdateResult;
import com.dreameddeath.installedbase.process.model.RevisionUpdateResult;

/**
 * Created by Christophe Jeunesse on 24/03/2016.
 */
public abstract class CreateUpdateItemWorkingInfo<
        TREV extends InstalledItemRevision,
        TRES extends IdentifiedItemUpdateResult,
        TREQ extends CreateUpdateInstalledBaseRequest.IdentifiedItem,
        TTARGET extends InstalledItem<TREV>
        > {
    private final TRES result;
    private final TREQ updateRequest;
    private final TTARGET targetItem;
    private final TREV currRevision;
    private final TREV targetRevision;
    private RevisionUpdateResult revisionUpdateResult;

    public CreateUpdateItemWorkingInfo(TRES result, TREQ updateRequest, TTARGET targetItem) {
        this.result = result;
        this.updateRequest = updateRequest;
        this.targetItem = targetItem;
        this.targetRevision = newRevision();
        TREV foundRevision = null;
        revisionUpdateResult=null;
        if (updateRequest.orderInfo != null) {
            this.targetRevision.setOrderId(updateRequest.orderInfo.orderId);
            this.targetRevision.setOrderItemId(updateRequest.orderInfo.orderItemId);
            this.targetRevision.setEffectiveDate(updateRequest.orderInfo.effectiveDate);
            //Lookup for existing revision if any
            for (TREV revision : targetItem.getRevisions()) {
                if (updateRequest.orderInfo.orderId.equals(revision.getOrderId()) &&
                        updateRequest.orderInfo.orderItemId.equals(revision.getOrderItemId())) {
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
