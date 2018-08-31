package com.dreameddeath.billing.installedbase.service.impl;

import com.dreameddeath.billing.installedbase.model.v1.BillingInstalledBase;
import com.dreameddeath.billing.installedbase.model.v1.BillingInstalledBaseItemFee;
import com.dreameddeath.billing.installedbase.service.model.v1.CreateUpdateBillingInstalledBaseAction;
import com.dreameddeath.billing.installedbase.service.model.v1.CreateUpdateBillingInstalledBaseResult;
import com.dreameddeath.installedbase.model.v1.offer.published.query.InstalledAtomicOfferResponse;
import com.dreameddeath.installedbase.model.v1.published.query.InstalledBaseResponse;
import com.dreameddeath.installedbase.model.v1.tariff.published.query.InstalledDiscountResponse;
import com.dreameddeath.installedbase.model.v1.tariff.published.query.InstalledTariffResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class CreateUpdateBillingInstalledBaseServiceImplTest {
    private CreateUpdateBillingInstalledBaseServiceImpl service;
    @Before
    public void setUp(){
        CreateUpdateBillingInstalledBaseItemStatusServiceImpl updateService = new CreateUpdateBillingInstalledBaseItemStatusServiceImpl();
        CreateUpdateBillingInstalledBaseServiceImpl service = new CreateUpdateBillingInstalledBaseServiceImpl();
        service.setCreateUpdateStatusService(updateService);

        this.service = service;
    }

    @Test
    public void ignoreHigherRevision(){
        InstalledBaseResponse response =new InstalledBaseResponse();
        response.setRevision(2L);
        BillingInstalledBase sourceInstalledBase = new BillingInstalledBase();
        sourceInstalledBase.setInstalledBaseRevision(2L);
        CreateUpdateBillingInstalledBaseResult updateBillingInstalledBase = this.service.createUpdateBillingInstalledBase(response, sourceInstalledBase);
        assertEquals(CreateUpdateBillingInstalledBaseAction.IGNORED,updateBillingInstalledBase.getAction());
    }

    @Test
    public void shouldAtLeastUpdateRevision(){
        InstalledBaseResponse response =new InstalledBaseResponse();
        response.setRevision(10L);
        BillingInstalledBase sourceInstalledBase = new BillingInstalledBase();
        sourceInstalledBase.setInstalledBaseRevision(1L);
        CreateUpdateBillingInstalledBaseResult updateBillingInstalledBase = this.service.createUpdateBillingInstalledBase(response, sourceInstalledBase);
        assertEquals(CreateUpdateBillingInstalledBaseAction.UNCHANGED,updateBillingInstalledBase.getAction());
        assertEquals(10L,(long)sourceInstalledBase.getInstalledBaseRevision());
    }


    @Test
    public void createFeeFromTariff(){
        InstalledBaseResponse response =new InstalledBaseResponse();
        response.setRevision(10L);
        InstalledAtomicOfferResponse offerInput = new InstalledAtomicOfferResponse();
        response.setOffers(Collections.singletonList(offerInput));
        ArrayList<InstalledTariffResponse> tariffs = new ArrayList<>();
        InstalledTariffResponse sourceTariff = new InstalledTariffResponse();
        tariffs.add(sourceTariff);
        sourceTariff.setId("1");
        sourceTariff.setCode("code");
        offerInput.setTariffs(tariffs);
        BillingInstalledBase sourceInstalledBase = new BillingInstalledBase();
        sourceInstalledBase.setInstalledBaseRevision(1L);
        {
            CreateUpdateBillingInstalledBaseResult updateBillingInstalledBase = this.service.createUpdateBillingInstalledBase(response, sourceInstalledBase);
            assertEquals(CreateUpdateBillingInstalledBaseAction.UPDATED, updateBillingInstalledBase.getAction());
            assertEquals(10L, (long) sourceInstalledBase.getInstalledBaseRevision());
            {
                assertEquals(1, updateBillingInstalledBase.getItems().size());
                assertEquals(1, sourceInstalledBase.getBillingItems().size());
            }
        }
        {
            response.setRevision(11L);
            CreateUpdateBillingInstalledBaseResult updateBillingInstalledBase = this.service.createUpdateBillingInstalledBase(response, sourceInstalledBase);
            assertEquals(CreateUpdateBillingInstalledBaseAction.UNCHANGED, updateBillingInstalledBase.getAction());
            assertEquals(11L, (long) sourceInstalledBase.getInstalledBaseRevision());
            {
                assertEquals(0, updateBillingInstalledBase.getItems().size());
                assertEquals(1, sourceInstalledBase.getBillingItems().size());
            }
        }

        {
            response.setRevision(12L);
            InstalledTariffResponse sourceTariff2 = new InstalledTariffResponse();
            tariffs.add(sourceTariff2);
            sourceTariff2.setId("2");
            sourceTariff2.setCode("code2");
            offerInput.setTariffs(tariffs);
            CreateUpdateBillingInstalledBaseResult updateBillingInstalledBase = this.service.createUpdateBillingInstalledBase(response, sourceInstalledBase);
            assertEquals(CreateUpdateBillingInstalledBaseAction.UPDATED, updateBillingInstalledBase.getAction());
            assertEquals(12L, (long) sourceInstalledBase.getInstalledBaseRevision());
            {
                assertEquals(1, updateBillingInstalledBase.getItems().size());
                assertEquals(2, sourceInstalledBase.getBillingItems().size());
            }
        }
    }


    @Test
    public void createDiscountItemFromTariffItem(){
        InstalledBaseResponse response =new InstalledBaseResponse();
        response.setRevision(10L);
        InstalledAtomicOfferResponse offerInput = new InstalledAtomicOfferResponse();
        response.setOffers(Collections.singletonList(offerInput));
        ArrayList<InstalledTariffResponse> tariffs = new ArrayList<>();
        InstalledTariffResponse sourceTariff = new InstalledTariffResponse();
        tariffs.add(sourceTariff);
        sourceTariff.setId("1");
        sourceTariff.setCode("code");
        InstalledDiscountResponse sourceDiscount = new InstalledDiscountResponse();
        sourceDiscount.setId("1");
        sourceDiscount.setCode("discCode");
        sourceTariff.getDiscounts().add(sourceDiscount);
        offerInput.setTariffs(tariffs);
        BillingInstalledBase sourceInstalledBase = new BillingInstalledBase();
        sourceInstalledBase.setInstalledBaseRevision(1L);
        BillingInstalledBaseItemFee billingInstalledBaseItemFee = new BillingInstalledBaseItemFee();
        billingInstalledBaseItemFee.setTariffId(sourceTariff.getId());
        billingInstalledBaseItemFee.setCode(sourceTariff.getCode());
        billingInstalledBaseItemFee.setId(sourceInstalledBase.getItemIdNextKey());
        sourceInstalledBase.addBillingItems(billingInstalledBaseItemFee);

        {
            CreateUpdateBillingInstalledBaseResult updateBillingInstalledBase = this.service.createUpdateBillingInstalledBase(response, sourceInstalledBase);
            assertEquals(CreateUpdateBillingInstalledBaseAction.UPDATED, updateBillingInstalledBase.getAction());
            assertEquals(10L, (long) sourceInstalledBase.getInstalledBaseRevision());
            {
                assertEquals(2, updateBillingInstalledBase.getItems().size());
                assertEquals(2, sourceInstalledBase.getBillingItems().size());
            }
        }
        {
            response.setRevision(11L);
            CreateUpdateBillingInstalledBaseResult updateBillingInstalledBase = this.service.createUpdateBillingInstalledBase(response, sourceInstalledBase);
            assertEquals(CreateUpdateBillingInstalledBaseAction.UNCHANGED, updateBillingInstalledBase.getAction());
            assertEquals(11L, (long) sourceInstalledBase.getInstalledBaseRevision());
            {
                assertEquals(0, updateBillingInstalledBase.getItems().size());
                assertEquals(2, sourceInstalledBase.getBillingItems().size());
            }
        }

        {
            response.setRevision(12L);
            InstalledDiscountResponse sourceDiscount2 = new InstalledDiscountResponse();
            sourceDiscount2.setId("2");
            sourceDiscount2.setCode("discCode");
            sourceTariff.getDiscounts().add(sourceDiscount2);

            CreateUpdateBillingInstalledBaseResult updateBillingInstalledBase = this.service.createUpdateBillingInstalledBase(response, sourceInstalledBase);
            assertEquals(CreateUpdateBillingInstalledBaseAction.UPDATED, updateBillingInstalledBase.getAction());
            assertEquals(12L, (long) sourceInstalledBase.getInstalledBaseRevision());
            {
                assertEquals(2, updateBillingInstalledBase.getItems().size());
                assertEquals(3, sourceInstalledBase.getBillingItems().size());
            }
        }
    }
}