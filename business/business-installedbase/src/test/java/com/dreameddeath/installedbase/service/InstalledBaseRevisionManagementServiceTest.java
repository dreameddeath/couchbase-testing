package com.dreameddeath.installedbase.service;

import com.dreameddeath.core.date.MockDateTimeServiceImpl;
import com.dreameddeath.installedbase.model.offer.InstalledCompositeOffer;
import com.dreameddeath.installedbase.process.model.InstalledBaseUpdateResult;
import com.dreameddeath.installedbase.service.utils.InstalledItemRevisionsToApply;
import com.dreameddeath.testing.dataset.DatasetManager;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Christophe Jeunesse on 11/04/2016.
 */
public class InstalledBaseRevisionManagementServiceTest {
    private static final String DATASET_NAME="installed_offer_revision_test";
    private static final DateTime REFERENCE_DATE = DateTime.parse("2016-01-01T00:00:00");

    private final AtomicReference<DateTime> dateTimeRef=new AtomicReference<>(REFERENCE_DATE);
    private InstalledBaseRevisionManagementService service;
    private DatasetManager manager;

    @Before
    public void buildInstalledBaseRevisionManagementService(){
        service = new InstalledBaseRevisionManagementService();
        service.setDateTimeService(new MockDateTimeServiceImpl(MockDateTimeServiceImpl.Calculator.fixedCalculator(dateTimeRef)));
        manager = new DatasetManager();
        manager.addDatasetsFromResourceFilename("datasets/installedOfferRevisionDataset.json_dataset");
        manager.prepareDatasets();

    }

    @Test
    public void testFindApplicableRevision(){
        InstalledBaseUpdateResult result = new InstalledBaseUpdateResult();
        InstalledCompositeOffer offer = manager.build(InstalledCompositeOffer.class, DATASET_NAME, "base_installed_offer", Collections.singletonMap("origDate", dateTimeRef.get()));
        assertNotNull(offer);
        {
            InstalledItemRevisionsToApply<?, ?> revs = service.findApplicableRevisions(result, offer);
            assertEquals(2, revs.getRevisionsToApply().size());
            assertEquals("ccoItem4", revs.getRevisionsToApply().get(0).getOrderItemId());
            assertEquals("ccoItem4.1", revs.getRevisionsToApply().get(1).getOrderItemId());
        }

        dateTimeRef.getAndUpdate(dt->dt.plus(1));
        {
            InstalledItemRevisionsToApply<?,?> revs=service.findApplicableRevisions(result,offer);
            assertEquals(3,revs.getRevisionsToApply().size());
            assertEquals("ccoItem3",revs.getRevisionsToApply().get(0).getOrderItemId());
            assertEquals("ccoItem4",revs.getRevisionsToApply().get(1).getOrderItemId());
            assertEquals("ccoItem4.1",revs.getRevisionsToApply().get(2).getOrderItemId());
        }


    }




}