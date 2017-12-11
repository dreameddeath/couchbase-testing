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

package com.dreameddeath.billing.installedbase.service.impl;

import com.dreameddeath.billing.installedbase.model.v1.*;
import com.dreameddeath.billing.installedbase.service.ICreateUpdateBillingInstalledBaseService;
import com.dreameddeath.billing.installedbase.service.model.v1.*;
import com.dreameddeath.installedbase.model.v1.common.published.query.CodeResponse;
import com.dreameddeath.installedbase.model.v1.common.published.query.InstalledStatusResponse;
import com.dreameddeath.installedbase.model.v1.offer.published.query.InstalledOfferResponse;
import com.dreameddeath.installedbase.model.v1.productservice.published.query.InstalledProductServiceResponse;
import com.dreameddeath.installedbase.model.v1.published.query.InstalledBaseResponse;
import com.dreameddeath.installedbase.model.v1.tariff.published.query.InstalledDiscountResponse;
import com.dreameddeath.installedbase.model.v1.tariff.published.query.InstalledTariffResponse;
import com.google.common.base.Preconditions;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 22/09/2017.
 */
public class CreateUpdateBillingInstalledBaseServiceImpl implements ICreateUpdateBillingInstalledBaseService {
    public CreateUpdateBillingInstalledBaseResult createUpdateBillingInstalledBase(InstalledBaseResponse installedBase, BillingInstalledBase origBillingInstalledBase){
        CreateUpdateBillingInstalledBaseResult result = new CreateUpdateBillingInstalledBaseResult();
        if(origBillingInstalledBase.getInstalledBaseRevision()!=null && installedBase.getRevision()>= origBillingInstalledBase.getInstalledBaseRevision()){
            //Already up to date
            result.setAction(CreateUpdateBillingInstalledBaseAction.IGNORED);
            return result;
        }

        for(InstalledOfferResponse offer :installedBase.getOffers()){
            for(InstalledTariffResponse tariff :offer.getTariffs()){
                CreateUpdateBillingInstalledBaseTariffItemResult tariffUpdateResult = new CreateUpdateBillingInstalledBaseTariffItemResult();
                result.addItem(tariffUpdateResult);
                BillingInstalledBaseItemFee billingInstalledBaseFee =
                        getOrCreateBillingItem(
                                origBillingInstalledBase,
                                tariffUpdateResult,
                                BillingInstalledBaseItemFee.class,
                                itemFee->itemFee.getTariffId().equals(tariff.getId()),
                                newKey-> buildNewFee(newKey,tariff)
                        );
                if(!tariffUpdateResult.getAction().equals(CreateUpdateBillingInstalledBaseAction.CREATED)){
                    //TODO check unmodifiable values
                }

                CreateUpdateBillingInstalledBaseAction statusUpdateAction = manageUpdateOfStatuses(billingInstalledBaseFee,tariff.getStatuses(),tariffUpdateResult);


                for(InstalledDiscountResponse discount:tariff.getDiscounts()){
                    CreateUpdateBillingInstalledBaseDiscountItemResult discountUpdateResult = new CreateUpdateBillingInstalledBaseDiscountItemResult();
                    result.addItem(discountUpdateResult);
                    BillingInstalledBaseItemDiscount billingInstalledBaseDiscount =
                            getOrCreateBillingItem(
                                    origBillingInstalledBase,
                                    discountUpdateResult,
                                    BillingInstalledBaseItemDiscount.class,
                                    item->item.getDiscountId().equals(discount.getId()),
                                    key->buildNewDiscount(key,billingInstalledBaseFee,discount)

                            );

                    //Update statuses
                    for(InstalledStatusResponse discountStatus :discount.getStatuses()){
                        //billingInstalledBaseDiscount.getStatuses();
                    }
                }
            }
        }

        for(InstalledProductServiceResponse installedProductService:installedBase.getPsList()){

        }

        return result;
    }

    private CreateUpdateBillingInstalledBaseAction manageUpdateOfStatuses(BillingInstalledBaseItem billingInstalledBaseItem, List<InstalledStatusResponse> statuses, CreateUpdateBillingInstalledBaseItemResult itemUpdateResult) {
        CreateUpdateBillingInstalledBaseAction resultAction = CreateUpdateBillingInstalledBaseAction.UNCHANGED;
        List<InstalledStatusResponse> sortedStatusResponses = new ArrayList<>(statuses);
        sortedStatusResponses.sort(Comparator.comparing(InstalledStatusResponse::getStartDate));
        Iterator<InstalledStatusResponse> iteratorExpected = sortedStatusResponses.iterator();
        CodeResponse lastResponseCode=null;
        DateTime lastEndDate=null;
        while(iteratorExpected.hasNext()){
            InstalledStatusResponse currResponse = iteratorExpected.next();
            Preconditions.checkArgument(lastResponseCode==null || !lastResponseCode.equals(currResponse.getCode()),"Couldn't have two times the same status consecutively");
            Preconditions.checkArgument(lastEndDate==null || lastEndDate.isEqual(currResponse.getStartDate().minusSeconds(1)),"No time hole between start time and end times");
            lastResponseCode =currResponse.getCode();
            lastEndDate=currResponse.getEndDate();
        }


        List<BillingInstalledBaseItemStatus> existingStatuses= new ArrayList<>(billingInstalledBaseItem.getStatuses());
        existingStatuses.sort(Comparator.comparing(BillingInstalledBaseItemStatus::getStartDate));

        int currPosExpected = 0;
        int currPosExisting = 0;
        while(currPosExpected<sortedStatusResponses.size() && currPosExisting<existingStatuses.size()){
            InstalledStatusResponse currExpected = sortedStatusResponses.get(currPosExpected);
            BillingInstalledBaseItemStatus currExistingStatus = existingStatuses.get(currPosExisting);

            if(currExpected.getStartDate().isEqual(currExpected.getEndDate())){
                currPosExpected++;
                continue;
            }
            BillingInstalledBaseItemStatus.Status expectedStatus = mapStatus(currExpected.getCode());
            Preconditions.checkNotNull(expectedStatus,"The status %s isn't managed",currExpected.getCode());

            //Changing of status
            if(expectedStatus!=currExistingStatus.getStatus()){
                CompareDateRangeResult compareDateRangeResult = compareDateRange(currExpected, currExistingStatus);
                if(compareDateRangeResult.needCreateNewItemWhenStatusDiffers){
                    buildNewStatus(currExpected,billingInstalledBaseItem,existingStatuses,currPosExisting);
                }
                switch (compareDateRangeResult){
                    //nothing to do
                    case BEFORE:
                        break;
                    //need to "restart" from the expected item end date(+1)
                    case OVERLAPPING_START:
                    case EARLIER_END_DATE:
                        currExistingStatus.setStartDate(currExpected.getEndDate().plusSeconds(1));
                        break;
                    //need to clean curr existing item
                    case OLDER_END_DATE:
                    case OVERLAPPING_BOTH:
                    case EARLIER_START_DATE:
                    case EXACT:
                    case AFTER:
                        currExistingStatus.setStatus(BillingInstalledBaseItemStatus.Status.CANCELLED);
                        ++currPosExisting;
                        break;
                    //need to close the current item
                    case OVERLAPPING_END:
                    case OLDER_START_DATE:
                    case CONTAINED_INTO:
                        currExistingStatus.setEndDate(currExpected.getStartDate());
                        ++currPosExisting;
                        break;
                }
                if(compareDateRangeResult.moveForwardOnExpected){
                    ++currPosExpected;
                }
            }
            //Change of dates
            else{
                switch (compareDateRange(currExpected,currExistingStatus)){
                    case EXACT:
                        ++currPosExpected;
                        ++currPosExisting;
                        break;
                    case AFTER:
                        currExistingStatus.setStatus(BillingInstalledBaseItemStatus.Status.CANCELLED);
                        ++currPosExisting;
                        break;
                    case BEFORE:
                        buildNewStatus(currExpected,billingInstalledBaseItem,existingStatuses,currPosExisting);
                        ++currPosExpected;
                        break;
                    case OVERLAPPING_START:
                    case EARLIER_END_DATE:
                    case EARLIER_START_DATE:
                    case OLDER_START_DATE:
                    case CONTAINED_INTO:
                        currExistingStatus.setStartDate(currExpected.getStartDate());
                        currExistingStatus.setEndDate(currExpected.getEndDate());
                        ++currPosExpected;
                        ++currPosExisting;
                        break;
                    case OLDER_END_DATE:
                    case OVERLAPPING_BOTH:
                    case OVERLAPPING_END:
                        currExistingStatus.setStartDate(currExpected.getStartDate());
                        currExistingStatus.setEndDate(currExpected.getEndDate());
                        ++currPosExisting;
                        break;
                }
            }
        }
        return resultAction;
    }

    private void buildNewStatus(InstalledStatusResponse currExpected, BillingInstalledBaseItem billingInstalledBaseItem, List<BillingInstalledBaseItemStatus> existingStatuses, int currPosExisting) {
        BillingInstalledBaseItemStatus newStatus = new BillingInstalledBaseItemStatus();
        newStatus.setStatus(mapStatus(currExpected.getCode()));
        newStatus.setStartDate(currExpected.getStartDate());
        newStatus.setEndDate(currExpected.getEndDate());

        billingInstalledBaseItem.addStatuses(newStatus);
        existingStatuses.add(currPosExisting,newStatus);
    }

    private BillingInstalledBaseItemDiscount buildNewDiscount(Long newDiscountId, BillingInstalledBaseItemFee parentTariff, InstalledDiscountResponse discount) {
        parentTariff.addDiscountsIds(newDiscountId);
        BillingInstalledBaseItemDiscount billingInstalledBaseItemDiscount = new BillingInstalledBaseItemDiscount();
        billingInstalledBaseItemDiscount.setId(newDiscountId);
        billingInstalledBaseItemDiscount.setDiscountId(discount.getId());
        billingInstalledBaseItemDiscount.setCode(discount.getCode());
        return billingInstalledBaseItemDiscount;
    }

    private BillingInstalledBaseItemFee buildNewFee(Long newTariffId,InstalledTariffResponse tariff) {
        BillingInstalledBaseItemFee newFee = new BillingInstalledBaseItemFee();
        newFee.setId(newTariffId);
        newFee.setTariffId(tariff.getId());
        newFee.setCode(tariff.getCode());
        return newFee;
    }

    private <T extends BillingInstalledBaseItem> T getOrCreateBillingItem(BillingInstalledBase billingInstalledBase, CreateUpdateBillingInstalledBaseItemResult result, Class<T> clazz, Matcher<T> matcher, Initializer<T> initializer) {
        return billingInstalledBase.getBillingItems()
                .stream()
                .filter(item->clazz.isAssignableFrom(item.getClass()))
                .map(item->(T)item)
                .filter(matcher::isApplicable)
                .findFirst()
                .map(item->updateBillingItemResult(item,result))
                .orElseGet(()->{
                    Long newKey = billingInstalledBase.getItemIdNextKey();
                    result.setAction(CreateUpdateBillingInstalledBaseAction.CREATED);
                    T initializedItem = initializer.initialize(newKey);
                    billingInstalledBase.addBillingItems(initializedItem);
                    return initializedItem;
                });
    }

    private <T extends BillingInstalledBaseItem> T updateBillingItemResult(T item, CreateUpdateBillingInstalledBaseItemResult result) {
        return item;
    }

    private interface Matcher<T>{
        boolean isApplicable(T item);
    }
    private interface Initializer<T>{
        T initialize(Long newKey);
    }

    private BillingInstalledBaseItemStatus.Status mapStatus(CodeResponse origStatus){
        if(origStatus==null){
            return null;
        }

        switch (origStatus){
            case ACTIVE:return BillingInstalledBaseItemStatus.Status.ACTIVE;
            case ABORTED:return BillingInstalledBaseItemStatus.Status.CANCELLED;
            case SUSPENDED:return BillingInstalledBaseItemStatus.Status.SUSPENDED;
            case CLOSED:return BillingInstalledBaseItemStatus.Status.CLOSED;
            case REMOVED:return BillingInstalledBaseItemStatus.Status.CANCELLED;
            case INEXISTING:return BillingInstalledBaseItemStatus.Status.CANCELLED;
        }
        return null;
    }

    private enum CompareDateRangeResult{
        BEFORE(true,true),//new_end before or equal curr_start
        OVERLAPPING_START(true,true),//new_start before (strict), new_end before curr_end
        OVERLAPPING_BOTH(false,false),//new_start before (strict), new_end after(strictly) curr_end
        EARLIER_END_DATE(true,true),//new start equals curr_start, new_end before(strictly) curr_end
        EARLIER_START_DATE(true,true),//new start before(strict)curr_start, new_end equals curr_end
        EXACT(true,true),
        OLDER_START_DATE(true,true),//new start after(strict)curr_start, new_end equals curr_end
        CONTAINED_INTO(false,false),//new start after(strict) curr_start, new_end before(strictly) curr_end
        OVERLAPPING_END(false,false),//new start after(strict) curr_start, new_end after(strictly) curr_end
        OLDER_END_DATE(false,false),//new start equals curr_start, new_end after(strictly) curr_end
        AFTER(false,false);//new start after or equals curr_end

        private boolean needCreateNewItemWhenStatusDiffers;
        private boolean moveForwardOnExpected;

        CompareDateRangeResult(boolean needCreateNewItemWhenStatusDiffers, boolean moveForwardOnExpected){
            this.needCreateNewItemWhenStatusDiffers = needCreateNewItemWhenStatusDiffers;
            this.moveForwardOnExpected=moveForwardOnExpected;
        }

        public boolean isNeedCreateNewItemWhenStatusDiffers() {
            return needCreateNewItemWhenStatusDiffers;
        }

        public boolean isMoveForwardOnExpected() {
            return moveForwardOnExpected;
        }
    }
    public CompareDateRangeResult compareDateRange(InstalledStatusResponse expected,BillingInstalledBaseItemStatus currStatus){
        if(
                expected.getEndDate().isBefore(currStatus.getStartDate())
                        || expected.getEndDate().isEqual(currStatus.getStartDate())
                ){
            return CompareDateRangeResult.BEFORE;
        }
        else if(expected.getEndDate().isBefore(currStatus.getEndDate())){
            if(expected.getStartDate().isBefore(currStatus.getStartDate())) {
                return CompareDateRangeResult.OVERLAPPING_START;
            }
            else if(expected.getStartDate().isEqual(currStatus.getStartDate())) {
                return CompareDateRangeResult.EARLIER_END_DATE;
            }
            else {
                return CompareDateRangeResult.CONTAINED_INTO;
            }
        }
        else if(expected.getEndDate().isEqual(currStatus.getEndDate())){
            if(expected.getStartDate().isBefore(currStatus.getStartDate())) {
                return CompareDateRangeResult.EARLIER_START_DATE;
            }
            else if(expected.getStartDate().isEqual(currStatus.getStartDate())) {
                return CompareDateRangeResult.EXACT;
            }
            else {
                return CompareDateRangeResult.OLDER_START_DATE;
            }
        }
        else{
            if(expected.getStartDate().isBefore(currStatus.getStartDate())) {
                return CompareDateRangeResult.OVERLAPPING_BOTH;
            }
            else if(expected.getStartDate().isEqual(currStatus.getStartDate())) {
                return CompareDateRangeResult.OLDER_END_DATE;
            }
            else if(expected.getStartDate().isBefore(currStatus.getEndDate())){
                return CompareDateRangeResult.OVERLAPPING_END;
            }
            else{
                return CompareDateRangeResult.AFTER;
            }
        }
    }
}
