/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.installedbase.service;

import com.dreameddeath.core.date.MockDateTimeServiceImpl;
import com.dreameddeath.installedbase.model.v1.InstalledBase;
import com.dreameddeath.installedbase.process.model.v1.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.process.model.v1.IdentifiedItemUpdateResult;
import com.dreameddeath.installedbase.process.model.v1.InstalledBaseUpdateResult;
import com.dreameddeath.installedbase.service.impl.CreateUpdateInstalledBaseServiceImpl;
import com.dreameddeath.installedbase.service.impl.InstalledBaseRevisionManagementServiceImpl;
import com.dreameddeath.testing.dataset.DatasetManager;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.dreameddeath.installedbase.process.model.v1.CreateUpdateInstalledBaseRequest.OrderStatus.COMPLETED;
import static com.dreameddeath.installedbase.process.model.v1.CreateUpdateInstalledBaseRequest.OrderStatus.IN_ORDER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Christophe Jeunesse on 26/04/2016.
 */
public class CreateUpdateInstalledBaseServiceTest {
    private static final String DATASET_NAME="create_update_inputs";
    private static final String DATASET_ELT_NOMINAL_CASE="create_update_simple_installed_base";

    private static final DateTime REFERENCE_DATE = DateTime.parse("2016-01-01T00:00:00");

    private final AtomicReference<DateTime> dateTimeRef=new AtomicReference<>(REFERENCE_DATE);
    private InstalledBaseRevisionManagementServiceImpl installedBaseRevisionManagementService;
    private CreateUpdateInstalledBaseServiceImpl service;
    private DatasetManager manager;

    @Before
    public void buildInstalledBaseRevisionManagementService(){
        installedBaseRevisionManagementService = new InstalledBaseRevisionManagementServiceImpl();
        installedBaseRevisionManagementService.setDateTimeService(new MockDateTimeServiceImpl(MockDateTimeServiceImpl.Calculator.fixedCalculator(dateTimeRef)));
        service=new CreateUpdateInstalledBaseServiceImpl();
        service.setDateTimeService(new MockDateTimeServiceImpl(MockDateTimeServiceImpl.Calculator.fixedCalculator(dateTimeRef)));
        service.setRevisionManagementService(installedBaseRevisionManagementService);
        manager = new DatasetManager();
        manager.addDatasetsFromResourceFilename("datasets/createUpdateInstalledBaseInput.json_dataset");
        manager.prepareDatasets();
    }


    @Test
    public void manageCreateUpdate(){
        InstalledBase installedBase = new InstalledBase();
        CreateUpdateInstalledBaseRequest request;
        final Map<String,String> tempIdsMap=new HashMap<>();
        {
            Map<String, Object> params = new HashMap<>();
            params.put("origDate", dateTimeRef.get());
            params.put("orderStatus",IN_ORDER.toString());
            params.put("tempIdsMap",tempIdsMap);
            request = manager.build(CreateUpdateInstalledBaseRequest.class, DATASET_NAME, DATASET_ELT_NOMINAL_CASE,params);
            InstalledBaseUpdateResult result = service.manageCreateUpdate(request,installedBase,request.contracts.get(0));
            assertNotNull(installedBase.getContract());
            assertEquals(4,installedBase.getOffers().size());
            assertEquals(2,installedBase.getPsList().size());
            assertNotNull(result.getContract());
            assertEquals(4,result.getOffersUpdates().size());
            assertEquals(2,result.getProducts().size());
            assertEquals(4,result.getTariffsUpdates().size());
            assertEquals(4,result.getDiscountsUpdates().size());
            //assertEquals(15,result.getRevisions().size());
            tempIdsMap.put(result.getContract().getTempId(),result.getContract().getId());
            List<IdentifiedItemUpdateResult> listForIds=new ArrayList<>();
            listForIds.add(result.getContract());
            listForIds.addAll(result.getProducts());
            listForIds.addAll(result.getOffersUpdates());
            listForIds.addAll(result.getTariffsUpdates());
            listForIds.addAll(result.getDiscountsUpdates());
            listForIds.forEach(it->tempIdsMap.put(it.getTempId(),it.getId()));
        }


        {
            Map<String, Object> params = new HashMap<>();
            params.put("origDate", dateTimeRef.get());
            params.put("orderStatus",COMPLETED.toString());
            params.put("tempIdsMap",tempIdsMap);
            request = manager.build(CreateUpdateInstalledBaseRequest.class, DATASET_NAME, DATASET_ELT_NOMINAL_CASE,params );
            InstalledBaseUpdateResult result = service.manageCreateUpdate(request,installedBase,request.contracts.get(0));
            assertNotNull(installedBase.getContract());
            assertEquals(4,installedBase.getOffers().size());
            assertEquals(2,installedBase.getPsList().size());
            assertNotNull(result.getContract());
            assertEquals(4,result.getOffersUpdates().size());
            assertEquals(2,result.getProducts().size());
            assertEquals(4,result.getTariffsUpdates().size());
            assertEquals(4,result.getDiscountsUpdates().size());
            //assertEquals(15,result.getRevisions().size());
        }

    }

}