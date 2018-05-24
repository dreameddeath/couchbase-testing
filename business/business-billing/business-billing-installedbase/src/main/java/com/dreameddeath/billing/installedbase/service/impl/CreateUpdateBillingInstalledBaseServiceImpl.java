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

import com.dreameddeath.billing.installedbase.model.v1.BillingInstalledBase;
import com.dreameddeath.billing.installedbase.model.v1.BillingInstalledBaseItem;
import com.dreameddeath.billing.installedbase.model.v1.BillingInstalledBaseItemDiscount;
import com.dreameddeath.billing.installedbase.model.v1.BillingInstalledBaseItemFee;
import com.dreameddeath.billing.installedbase.service.ICreateUpdateBillingInstalledBaseItemStatusService;
import com.dreameddeath.billing.installedbase.service.ICreateUpdateBillingInstalledBaseService;
import com.dreameddeath.billing.installedbase.service.model.v1.CreateUpdateBillingInstalledBaseAction;
import com.dreameddeath.billing.installedbase.service.model.v1.CreateUpdateBillingInstalledBaseDiscountItemResult;
import com.dreameddeath.billing.installedbase.service.model.v1.CreateUpdateBillingInstalledBaseItemResult;
import com.dreameddeath.billing.installedbase.service.model.v1.CreateUpdateBillingInstalledBaseResult;
import com.dreameddeath.billing.installedbase.service.model.v1.CreateUpdateBillingInstalledBaseTariffItemResult;
import com.dreameddeath.installedbase.model.v1.offer.published.query.InstalledOfferResponse;
import com.dreameddeath.installedbase.model.v1.productservice.published.query.InstalledProductServiceResponse;
import com.dreameddeath.installedbase.model.v1.published.query.InstalledBaseResponse;
import com.dreameddeath.installedbase.model.v1.tariff.published.query.InstalledDiscountResponse;
import com.dreameddeath.installedbase.model.v1.tariff.published.query.InstalledTariffResponse;

import javax.inject.Inject;

/**
 * Created by Christophe Jeunesse on 22/09/2017.
 */
public class CreateUpdateBillingInstalledBaseServiceImpl implements ICreateUpdateBillingInstalledBaseService {

    @Inject
    private ICreateUpdateBillingInstalledBaseItemStatusService updateStatusService;

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

                CreateUpdateBillingInstalledBaseAction statusUpdateAction = updateStatusService.manageUpdateOfStatuses(billingInstalledBaseFee,tariff.getStatuses(),tariffUpdateResult);
                if(!tariffUpdateResult.getAction().equals(CreateUpdateBillingInstalledBaseAction.CREATED)){
                    result.setAction(statusUpdateAction);
                }


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


                    if(!discountUpdateResult.getAction().equals(CreateUpdateBillingInstalledBaseAction.CREATED)){
                        //TODO check unmodifiable values
                    }

                    CreateUpdateBillingInstalledBaseAction discountStatusUpdateAction = updateStatusService.manageUpdateOfStatuses(billingInstalledBaseDiscount,discount.getStatuses(),discountUpdateResult);
                    if(!discountUpdateResult.getAction().equals(CreateUpdateBillingInstalledBaseAction.CREATED)){
                        result.setAction(discountStatusUpdateAction);
                    }
                }
            }
        }

        for(InstalledProductServiceResponse installedProductService:installedBase.getPsList()){

        }

        return result;
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

}
