package com.dreameddeath.installedbase.service;

import com.dreameddeath.core.process.service.TaskContext;
import com.dreameddeath.installedbase.model.common.InstalledBase;
import com.dreameddeath.installedbase.model.common.InstalledItem;
import com.dreameddeath.installedbase.model.common.InstalledItemRevision;
import com.dreameddeath.installedbase.model.common.InstalledStatus;
import com.dreameddeath.installedbase.model.contract.InstalledContract;
import com.dreameddeath.installedbase.model.contract.InstalledContractRevision;
import com.dreameddeath.installedbase.model.offer.InstalledAtomicOffer;
import com.dreameddeath.installedbase.model.offer.InstalledOffer;
import com.dreameddeath.installedbase.model.process.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.model.process.CreateUpdateInstalledBaseResponse;
import com.dreameddeath.installedbase.model.productservice.InstalledProductService;
import com.dreameddeath.installedbase.model.tariff.InstalledDiscount;
import com.dreameddeath.installedbase.model.tariff.InstalledTariff;


import java.util.ArrayList;

/**
 * Created by ceaj8230 on 16/10/2014.
 */
public class CreateUpdateInstalledBaseService {
    public CreateUpdateInstalledBaseResponse createUpdateInstalledBase(TaskContext ctxt,InstalledBase ref, CreateUpdateInstalledBaseRequest.Contract reqContract ){
        CreateUpdateInstalledBaseResponse res = new CreateUpdateInstalledBaseResponse();
        if(reqContract.comOp.equals(CreateUpdateInstalledBaseRequest.CommercialOperation.ADD)){
            createContract(ctxt,res,ref,reqContract);
        }

        updateContract(ctxt,ref.getContract(),reqContract);


        return res;
    }

    public void initItem(TaskContext ctxt,InstalledItem targetItem,CreateUpdateInstalledBaseRequest.IdentifiedItem item){
        InstalledStatus newItemDefaultStatus = new InstalledStatus();
        newItemDefaultStatus.setCode(InstalledStatus.Code.INITIALIZED);
        newItemDefaultStatus.setStartDate(ctxt.getSession().getCurrentDate());
        targetItem.setStatus(newItemDefaultStatus);


    }

    public InstalledContract createContract(TaskContext ctxt,CreateUpdateInstalledBaseResponse res, InstalledBase ref, CreateUpdateInstalledBaseRequest.Contract refContract  ){
        CreateUpdateInstalledBaseResponse.Contract resContract = new CreateUpdateInstalledBaseResponse.Contract();
        //Build New Contract
        InstalledContract newContract = new InstalledContract();
        InstalledStatus newContractDefaultStatus = new InstalledStatus();
        newContractDefaultStatus.setCode(InstalledStatus.Code.INITIALIZED);
        newContractDefaultStatus.setStartDate(ctxt.getSession().getCurrentDate());
        newContract.setStatus(newContractDefaultStatus);


        resContract.id = newContract.getId();
        resContract.tempId =  refContract.tempId;

        //Add contract to response
        if(res.contracts==null){res.contracts=new ArrayList<CreateUpdateInstalledBaseResponse.Contract>();}
        res.contracts.add(resContract);

        ref.setContract(newContract);
        return newContract;
    }

    public void updateContract(TaskContext ctxt,InstalledContract contract, CreateUpdateInstalledBaseRequest.Contract reqContract){
        InstalledContractRevision rev = getContractRevision(ctxt,contract,reqContract);


    }

    public InstalledContractRevision getContractRevision(TaskContext ctxt,InstalledContract contract, CreateUpdateInstalledBaseRequest.Contract reqContract){
        for(InstalledContractRevision revision:contract.getRevisions()){
            if(revision.getOrderId().equals(reqContract.orderInfo.orderId) &&
               revision.getOrderItemId().equals(reqContract.orderInfo.orderItemId))
            {
                updateRevision(ctxt,revision,reqContract);
                return revision;
            }
        }
        InstalledContractRevision revision = new InstalledContractRevision();
        revision.setStatus(new InstalledStatus());
        updateRevision(ctxt,revision,reqContract);
        return revision;
    }

    public void updateRevision(TaskContext ctxt,InstalledItemRevision rev, CreateUpdateInstalledBaseRequest.IdentifiedItem item){
        rev.setOrderId(item.orderInfo.orderId);
        rev.setOrderItemId(item.orderInfo.orderItemId);

        if(item.comOp!=null) {
            switch (item.comOp) {
                case ADD:case ACTIVATE: rev.getStatus().setCode(InstalledStatus.Code.ACTIVE);break;
                case REMOVE: rev.getStatus().setCode(InstalledStatus.Code.CLOSED);break;
                case SUSPEND: rev.getStatus().setCode(InstalledStatus.Code.SUSPENDED);break;
                case CANCEL: rev.getStatus().setCode(InstalledStatus.Code.CANCELLED);break;
                case MIGRATE: rev.getStatus().setCode(InstalledStatus.Code.CLOSED);break;
                default://TODO throw an error
            }
            //TODO manage planned dates
        }
        else if(item.status!=null){
            switch(item.status.statusCode){
                case ACTIVE: rev.getStatus().setCode(InstalledStatus.Code.ACTIVE);break;
                case CLOSED: rev.getStatus().setCode(InstalledStatus.Code.CLOSED);break;
                case SUSPENDED: rev.getStatus().setCode(InstalledStatus.Code.SUSPENDED);break;
                case CANCELLED: rev.getStatus().setCode(InstalledStatus.Code.CANCELLED);break;
                default://TODO throw an error
            }
            rev.getStatus().setStartDate(item.status.startDate);
            rev.getStatus().setEndDate(item.status.endDate);
        }
    }

    public InstalledItem findMatchingItem(TaskContext ctxt,InstalledBase ref, CreateUpdateInstalledBaseRequest.IdentifiedItem item){
        if(ref.getContract().getId().equals(item.id)){
            return ref.getContract();
        }

        for(InstalledOffer existingOffer:ref.getOffers()){
            if(existingOffer.getId().equals(item.id)) {
                return existingOffer;
            }
            for(InstalledTariff existingTariff : existingOffer.getTariffs()) {
                if (existingTariff.getId().equals(item.id)) {
                    return existingTariff;
                }
                for (InstalledDiscount existingDiscount : existingTariff.getDiscounts()) {
                    if (existingDiscount.getId().equals(item.id)) {
                        return existingDiscount;
                    }
                }
            }
            if(existingOffer instanceof InstalledAtomicOffer) {
                InstalledProductService installedPS = ((InstalledAtomicOffer) existingOffer).getProduct();
                if ((installedPS != null) && installedPS.getId().equals(item.id)) {
                    return installedPS;
                }
            }
        }

        return null;
    }

    public InstalledItemRevision findMatchingRevision(TaskContext ctxt,InstalledItem ref, CreateUpdateInstalledBaseRequest.IdentifiedItem item){
        return null;
    }

    public InstalledItemRevision createRevision(TaskContext ctxt,InstalledItem ref, CreateUpdateInstalledBaseRequest.IdentifiedItem item){
        return null;
    }

}
