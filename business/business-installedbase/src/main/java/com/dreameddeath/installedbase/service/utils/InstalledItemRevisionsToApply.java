package com.dreameddeath.installedbase.service.utils;

import com.dreameddeath.installedbase.model.v1.common.InstalledItem;
import com.dreameddeath.installedbase.model.v1.common.InstalledItemRevision;
import com.dreameddeath.installedbase.model.v1.contract.InstalledContract;
import com.dreameddeath.installedbase.model.v1.offer.InstalledOffer;
import com.dreameddeath.installedbase.model.v1.productservice.InstalledProductService;
import com.dreameddeath.installedbase.model.v1.tariff.InstalledDiscount;
import com.dreameddeath.installedbase.model.v1.tariff.InstalledTariff;
import com.dreameddeath.installedbase.process.model.v1.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 02/04/2016.
 */
public class InstalledItemRevisionsToApply<TREV extends InstalledItemRevision,TITEM extends InstalledItem<TREV>> {
    private final TITEM parent;
    private final List<TREV> revisionsToApply;
    private final IdentifiedItemUpdateResult updateResult;
    private final boolean isNewUpdateResult;
    private final Type itemType;

    public InstalledItemRevisionsToApply(TITEM item){
        this(item,null);
    }

    public InstalledItemRevisionsToApply(TITEM item,InstalledBaseUpdateResult baseUpdateResult){
        parent=item;
        revisionsToApply=new ArrayList<>(item.getRevisions().size());
        if(parent instanceof InstalledProductService){ itemType=Type.PS;}
        else if(parent instanceof InstalledContract) { itemType=Type.CONTRACT;}
        else if(parent instanceof InstalledTariff)   { itemType=Type.TARIFF; }
        else if(parent instanceof InstalledDiscount) { itemType=Type.DISCOUNT;}
        else if(parent instanceof InstalledOffer)    { itemType=Type.OFFER;}
        else{
            throw new RuntimeException("Unknow parent type "+parent.getClass().getName());
        }

        IdentifiedItemUpdateResult foundUpdateResult=null;
        if(baseUpdateResult!=null){
            List<? extends IdentifiedItemUpdateResult> existingList=null;
            if(itemType==Type.CONTRACT){
                foundUpdateResult=baseUpdateResult.getContract();
            }
            else{
                switch (itemType){
                    case OFFER:existingList=baseUpdateResult.getOffersUpdates();break;
                    case PS:existingList=baseUpdateResult.getProducts();break;
                    case TARIFF:existingList=baseUpdateResult.getTariffsUpdates();break;
                    case DISCOUNT:existingList=baseUpdateResult.getDiscountsUpdates();break;
                }
                if(existingList!=null){
                    for(IdentifiedItemUpdateResult itemUpdateResult:existingList){
                        if(itemUpdateResult.getId().equals(item.getId())){
                            foundUpdateResult=itemUpdateResult;
                            break;
                        }
                    }
                }
            }
        }
        if(foundUpdateResult!=null){
            updateResult = foundUpdateResult;
            isNewUpdateResult=false;
        }
        else {
            updateResult = newResult();
            updateResult.setId(item.getId());
            isNewUpdateResult=true;
        }
    }


    public TITEM getParent() {
        return parent;
    }

    public void addRevisionToApply(TREV revision){
        revisionsToApply.add(revision);
    }

    public List<TREV> getRevisionsToApply(){
        return Collections.unmodifiableList(revisionsToApply);
    }

    public String getItemUid(){
        return parent.getId()+"["+parent.getCode()+"]";
    }

    public IdentifiedItemUpdateResult getUpdateResult() {
        return updateResult;
    }

    public <T extends IdentifiedItemUpdateResult> T  getUpdateResult(Class<T> clazz) {
        return (T)updateResult;
    }

    public boolean isNewUpdateResult() {
        return isNewUpdateResult;
    }

    protected IdentifiedItemUpdateResult newResult(){
        switch (itemType){
            case TARIFF:   return new TariffUpdateResult();
            case DISCOUNT: return new DiscountUpdateResult();
            default : return new InstalledItemUpdateResult();
        }
    }

    public Type getItemType() {
        return itemType;
    }

    public void sortRevisions(){
        revisionsToApply.sort((a1,a2)->
            {
                if(a1.getEffectiveDate()!=null){
                    if(a2.getEffectiveDate()!=null){
                        return a1.getEffectiveDate().compareTo(a2.getEffectiveDate());
                    }
                    else{
                        return 1;
                    }
                }
                else if(a2.getEffectiveDate()!=null){
                    return -1;
                }
                else{
                    return 0;
                }
            }
        );
    }

    public enum Type{
        CONTRACT(true),
        OFFER(true),
        PS(true),
        TARIFF(false),
        DISCOUNT(false);

        private final boolean hasLink;

        Type(boolean hasLink) {
            this.hasLink = hasLink;
        }

        public boolean isHasLink() {
            return hasLink;
        }
    }
}
