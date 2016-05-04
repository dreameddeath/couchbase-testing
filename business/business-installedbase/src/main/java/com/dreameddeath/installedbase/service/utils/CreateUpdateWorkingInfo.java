package com.dreameddeath.installedbase.service.utils;

import com.dreameddeath.installedbase.model.v1.InstalledBase;
import com.dreameddeath.installedbase.model.v1.process.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.process.model.v1.InstalledBaseUpdateResult;
import com.dreameddeath.installedbase.utils.InstalledBaseTools;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.*;

/**
 * Created by Christophe Jeunesse on 24/03/2016.
 */
public class CreateUpdateWorkingInfo {
    private final Map<String, CreateUpdateItemWorkingInfo<?,?,?,?>> workingInfoPerIdMap = new TreeMap<>();
    private final Map<String, CreateUpdateItemWorkingInfo<?,?,?,?>> workingInfoPerTempIdMap = new TreeMap<>();

    private final Multimap<String,CreateUpdateInstalledBaseRequest.IdentifiedItem> reguestOfferPerContract = ArrayListMultimap.create();
    private final Map<String,CreateUpdateInstalledBaseRequest.IdentifiedItem> requestItemPerId = new HashMap<>();
    private final Map<String,CreateUpdateInstalledBaseRequest.IdentifiedItem> requestItemPerTempId = new HashMap<>();
    private final CreateUpdateInstalledBaseRequest.Contract requestedContract;
    private final InstalledBase installedBase;
    private final InstalledBaseTools.InstalledBaseIndexer installedBaseIndexer;
    private final InstalledBaseUpdateResult installedBaseUpdateResult;


    public CreateUpdateWorkingInfo(CreateUpdateInstalledBaseRequest request,InstalledBase ref,CreateUpdateInstalledBaseRequest.Contract contract) {
        this.requestedContract = contract;
        this.installedBase = ref;
        this.installedBaseUpdateResult=new InstalledBaseUpdateResult();
        if(requestedContract.tempId!=null){
            requestItemPerTempId.put(requestedContract.tempId,requestedContract);
        }
        if(requestedContract.id!=null){
            requestItemPerId.put(requestedContract.id,requestedContract);
        }
        for(CreateUpdateInstalledBaseRequest.IdentifiedItem item:request.offers){
            if(item.tempId!=null){
                requestItemPerTempId.put(item.tempId,item);
            }
            if(item.id!=null){
                requestItemPerTempId.put(item.id,item);
            }
        }
        installedBaseIndexer = new InstalledBaseTools.InstalledBaseIndexer(installedBase);
    }


    public CreateUpdateInstalledBaseRequest.Contract getRequestedContract() {
        return requestedContract;
    }

    public void addWorkingInfo(CreateUpdateItemWorkingInfo<?, ?, ?, ?> workingInfoItem) {
        Preconditions.checkNotNull(workingInfoItem.getTargetItem().getId(),"The target item %s doesn't have any id",workingInfoItem.getItemUid());
        //attach new item id if id has just been created
        requestItemPerId.putIfAbsent(workingInfoItem.getTargetItem().getId(),workingInfoItem.getUpdateRequest());
        workingInfoPerIdMap.put(workingInfoItem.getTargetItem().getId(), workingInfoItem);
        if (workingInfoItem.getUpdateRequest().tempId != null) {
            workingInfoPerTempIdMap.put(workingInfoItem.getUpdateRequest().tempId, workingInfoItem);
        }
    }


    public InstalledBaseUpdateResult getInstalledBaseUpdateResult() {
        return installedBaseUpdateResult;
    }

    public InstalledBase getInstalledBase() {
        return installedBase;
    }

    public InstalledBaseTools.InstalledBaseIndexer getInstalledBaseIndexer() {
        return installedBaseIndexer;
    }

    public CreateUpdateItemWorkingInfo getWorkingItemByTempId(String tempId) {
        return workingInfoPerTempIdMap.get(tempId);
    }

    public CreateUpdateItemWorkingInfo<?,?,?,?> getWorkingItemById(String id) {
        return workingInfoPerIdMap.get(id);
    }

    public Collection<CreateUpdateItemWorkingInfo<?,?,?,?>> getWorkingItems(){
        return workingInfoPerIdMap.values();
    }

    public CreateUpdateItemWorkingInfo<?,?,?,?> getWorkingItem(CreateUpdateInstalledBaseRequest.TargetIdentifiedItem target) {
        CreateUpdateItemWorkingInfo result = null;
        if (target.tempId != null) {
            result = workingInfoPerTempIdMap.get(target.tempId);
        }
        if ((result != null) && (target.id != null)) {
            result = workingInfoPerIdMap.get(target.id);
        }
        return result;
    }

    public List<CreateUpdateInstalledBaseRequest.IdentifiedItem> findPath(CreateUpdateInstalledBaseRequest.IdentifiedItem src, CreateUpdateInstalledBaseRequest.IdentifiedItem target){
        List<CreateUpdateInstalledBaseRequest.IdentifiedItem> result = new LinkedList<>();
        CreateUpdateInstalledBaseRequest.IdentifiedItem current=src;
        while(current!=null){
            result.add(current);
            if(    ((current.tempId!=null) && (current.tempId.equals(target.tempId)))
                || ((current.id!=null) && (current.id.equals(target.id))))
            {
                break;
            }

            if((current instanceof CreateUpdateInstalledBaseRequest.Offer)&& ((CreateUpdateInstalledBaseRequest.Offer)current).parent!=null){
                CreateUpdateInstalledBaseRequest.TargetIdentifiedItem parent = ((CreateUpdateInstalledBaseRequest.Offer)current).parent;

                if(parent.tempId!=null) { current=requestItemPerTempId.get(parent.tempId); }
                else if(parent.id!=null){ current=requestItemPerId.get(parent.id); }
                else                    { current=null; }
            }
            else{
                current=null;
            }
        }

        if(current!=null){
            return result;
        }
        else{
            return Collections.emptyList();
        }
    }


}
