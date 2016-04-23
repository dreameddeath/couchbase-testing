package com.dreameddeath.installedbase.service;

import com.dreameddeath.core.date.IDateTimeService;
import com.dreameddeath.core.date.MockDateTimeServiceImpl;
import com.dreameddeath.installedbase.model.common.InstalledItemRevision;
import com.dreameddeath.installedbase.model.common.InstalledStatus;
import com.dreameddeath.installedbase.model.offer.InstalledCompositeOffer;
import com.dreameddeath.installedbase.model.offer.InstalledOfferRevision;
import com.dreameddeath.installedbase.process.model.InstalledBaseUpdateResult;
import com.dreameddeath.installedbase.process.model.StatusUpdateResult;
import com.dreameddeath.installedbase.service.utils.InstalledItemRevisionsToApply;
import com.dreameddeath.testing.dataset.DatasetManager;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

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
            InstalledItemRevisionsToApply<InstalledOfferRevision,InstalledCompositeOffer> revs = service.findApplicableRevisions(result, offer);
            assertEquals(2, revs.getRevisionsToApply().size());
            assertEquals("ccoItem4", revs.getRevisionsToApply().get(0).getOrderItemId());
            assertEquals("ccoItem4.1", revs.getRevisionsToApply().get(1).getOrderItemId());
        }

        dateTimeRef.getAndUpdate(dt->dt.plus(1));
        {
            InstalledItemRevisionsToApply<InstalledOfferRevision,InstalledCompositeOffer> revs=service.findApplicableRevisions(result,offer);
            assertEquals(3,revs.getRevisionsToApply().size());
            assertEquals("ccoItem3",revs.getRevisionsToApply().get(0).getOrderItemId());
            assertEquals("ccoItem4",revs.getRevisionsToApply().get(1).getOrderItemId());
            assertEquals("ccoItem4.1",revs.getRevisionsToApply().get(2).getOrderItemId());
        }
    }




    @Test
    public void applyStatusesFromRevision() throws Exception {
        InstalledBaseUpdateResult result = new InstalledBaseUpdateResult();
        InstalledCompositeOffer offer = manager.build(InstalledCompositeOffer.class, DATASET_NAME, "base_installed_offer", Collections.singletonMap("origDate", REFERENCE_DATE));
        assertNotNull(offer);

        /*
        *  No revision, no change
         */
        dateTimeRef.getAndUpdate(dt -> dt.minus(10));
        {
            InstalledItemRevisionsToApply<InstalledOfferRevision, InstalledCompositeOffer> revs = service.findApplicableRevisions(result, offer);
            assertEquals(0, revs.getRevisionsToApply().size());
            boolean modified = service.applyStatusesFromRevision(revs);
            assertFalse(modified);
            assertEquals(InstalledStatus.Code.INEXISTING, offer.getStatus(REFERENCE_DATE).getCode());
        }

        /*
        *  Multiple inits at the same time
         */
        dateTimeRef.getAndUpdate(dt -> dt.plus(20));
        {
            InstalledItemRevisionsToApply<InstalledOfferRevision, InstalledCompositeOffer> revs = service.findApplicableRevisions(result, offer);
            revs.sortRevisions();
            assertEquals(3, revs.getRevisionsToApply().size());
            boolean modified = service.applyStatusesFromRevision(revs);
            assertTrue(modified);
            assertEquals(3, revs.getUpdateResult().getStatuses().size());
            assertEquals(3, offer.getStatuses().size());
            int pos = 0;
            {
                StatusUpdateResult revUpdateResult = revs.getUpdateResult().getStatuses().get(pos);
                assertEquals(StatusUpdateResult.Action.NEW, revUpdateResult.getAction());
                assertEquals(InstalledStatus.Code.ACTIVE, revUpdateResult.getCode());
                assertEquals(REFERENCE_DATE.minus(5), revUpdateResult.getStartDate());
                assertEquals(REFERENCE_DATE, revUpdateResult.getEndDate());
                assertNull(revUpdateResult.getOldEndDate());

                InstalledStatus status = revs.getParent().getStatuses().get(pos);
                assertEquals(InstalledStatus.Code.ACTIVE, status.getCode());
                assertEquals(REFERENCE_DATE.minus(5), status.getStartDate());
                assertEquals(REFERENCE_DATE, status.getEndDate());
            }
            pos++;
            {
                StatusUpdateResult revUpdateResult = revs.getUpdateResult().getStatuses().get(pos);
                assertEquals(StatusUpdateResult.Action.NEW, revUpdateResult.getAction());
                assertEquals(InstalledStatus.Code.SUSPENDED, revUpdateResult.getCode());
                assertEquals(REFERENCE_DATE, revUpdateResult.getStartDate());
                assertEquals(REFERENCE_DATE.plus(1), revUpdateResult.getEndDate());
                assertNull(revUpdateResult.getOldEndDate());

                InstalledStatus status = revs.getParent().getStatuses().get(pos);
                assertEquals(InstalledStatus.Code.SUSPENDED, status.getCode());
                assertEquals(REFERENCE_DATE, status.getStartDate());
                assertEquals(REFERENCE_DATE.plus(1), status.getEndDate());
            }
            pos++;
            {
                StatusUpdateResult revUpdateResult = revs.getUpdateResult().getStatuses().get(pos);
                assertEquals(StatusUpdateResult.Action.NEW, revUpdateResult.getAction());
                assertEquals(InstalledStatus.Code.CLOSED, revUpdateResult.getCode());
                assertEquals(REFERENCE_DATE.plus(1), revUpdateResult.getStartDate());
                assertEquals(IDateTimeService.MAX_TIME, revUpdateResult.getEndDate());
                assertNull(revUpdateResult.getOldEndDate());

                InstalledStatus status = revs.getParent().getStatuses().get(pos);
                assertEquals(InstalledStatus.Code.CLOSED, status.getCode());
                assertEquals(REFERENCE_DATE.plus(1), status.getStartDate());
                assertEquals(IDateTimeService.MAX_TIME, status.getEndDate());
            }
            service.updateRevisions(revs.getParent(),revs.getRevisionsToApply());
        }

        /*
        *  New status added
         */
        dateTimeRef.getAndUpdate(dt -> REFERENCE_DATE.plusDays(2));
        {
            InstalledItemRevisionsToApply<InstalledOfferRevision, InstalledCompositeOffer> revs = service.findApplicableRevisions(result, offer);
            revs.sortRevisions();
            assertEquals(1, revs.getRevisionsToApply().size());
            boolean modified = service.applyStatusesFromRevision(revs);
            assertTrue(modified);
            assertEquals(2, revs.getUpdateResult().getStatuses().size());
            assertEquals(4, offer.getStatuses().size());
            int pos = 0;
            {
                StatusUpdateResult revUpdateResult = revs.getUpdateResult().getStatuses().get(pos);
                assertEquals(StatusUpdateResult.Action.MODIFIED, revUpdateResult.getAction());
                assertEquals(InstalledStatus.Code.CLOSED, revUpdateResult.getCode());
                assertEquals(REFERENCE_DATE.plus(1), revUpdateResult.getStartDate());
                assertEquals(REFERENCE_DATE.plusDays(1), revUpdateResult.getEndDate());
                assertEquals(IDateTimeService.MAX_TIME,revUpdateResult.getOldEndDate());

                InstalledStatus status = revs.getParent().getStatuses().get(pos+2);
                assertEquals(InstalledStatus.Code.CLOSED, status.getCode());
                assertEquals(REFERENCE_DATE.plus(1), status.getStartDate());
                assertEquals(REFERENCE_DATE.plusDays(1), status.getEndDate());
            }
            pos++;
            {
                StatusUpdateResult revUpdateResult = revs.getUpdateResult().getStatuses().get(pos);
                assertEquals(StatusUpdateResult.Action.NEW, revUpdateResult.getAction());
                assertEquals(InstalledStatus.Code.ACTIVE, revUpdateResult.getCode());
                assertEquals(REFERENCE_DATE.plusDays(1), revUpdateResult.getStartDate());
                assertEquals(IDateTimeService.MAX_TIME,revUpdateResult.getEndDate());
                assertNull(revUpdateResult.getOldEndDate());

                InstalledStatus status = revs.getParent().getStatuses().get(pos+2);
                assertEquals(InstalledStatus.Code.ACTIVE, status.getCode());
                assertEquals(REFERENCE_DATE.plusDays(1), status.getStartDate());
                assertEquals(IDateTimeService.MAX_TIME, status.getEndDate());
            }
            service.updateRevisions(revs.getParent(),revs.getRevisionsToApply());
        }

        /*
        *  Backdated change added
         */
        dateTimeRef.getAndUpdate(dt -> REFERENCE_DATE.plusDays(3));
        {
            InstalledItemRevisionsToApply<InstalledOfferRevision, InstalledCompositeOffer> revs = service.findApplicableRevisions(result, offer);
            revs.sortRevisions();
            assertEquals(1, revs.getRevisionsToApply().size());
            revs.getRevisionsToApply().get(0).setEffectiveDate(REFERENCE_DATE.plusHours(12));
            boolean modified = service.applyStatusesFromRevision(revs);
            assertTrue(modified);
            assertEquals(3, revs.getUpdateResult().getStatuses().size());
            assertEquals(4, offer.getStatuses().size());
            int pos = 0;
            {
                StatusUpdateResult revUpdateResult = revs.getUpdateResult().getStatuses().get(pos);
                assertEquals(StatusUpdateResult.Action.MODIFIED, revUpdateResult.getAction());
                assertEquals(InstalledStatus.Code.CLOSED, revUpdateResult.getCode());
                assertEquals(REFERENCE_DATE.plus(1), revUpdateResult.getStartDate());
                assertEquals(REFERENCE_DATE.plusHours(12), revUpdateResult.getEndDate());
                assertEquals(REFERENCE_DATE.plusDays(1),revUpdateResult.getOldEndDate());

                InstalledStatus status = revs.getParent().getStatuses().get(pos+2);
                assertEquals(InstalledStatus.Code.CLOSED, status.getCode());
                assertEquals(REFERENCE_DATE.plus(1), status.getStartDate());
                assertEquals(REFERENCE_DATE.plusHours(12), status.getEndDate());
            }
            pos++;
            {
                StatusUpdateResult revUpdateResult = revs.getUpdateResult().getStatuses().get(pos);
                assertEquals(StatusUpdateResult.Action.DELETED, revUpdateResult.getAction());
                assertEquals(InstalledStatus.Code.ACTIVE, revUpdateResult.getCode());
                assertEquals(REFERENCE_DATE.plusDays(1), revUpdateResult.getStartDate());
                assertNull(revUpdateResult.getEndDate());
                assertEquals(IDateTimeService.MAX_TIME,revUpdateResult.getOldEndDate());
            }
            pos++;
            {
                StatusUpdateResult revUpdateResult = revs.getUpdateResult().getStatuses().get(pos);
                assertEquals(StatusUpdateResult.Action.NEW, revUpdateResult.getAction());
                assertEquals(InstalledStatus.Code.SUSPENDED, revUpdateResult.getCode());
                assertEquals(REFERENCE_DATE.plusHours(12), revUpdateResult.getStartDate());
                assertEquals(IDateTimeService.MAX_TIME,revUpdateResult.getEndDate());
                assertNull(revUpdateResult.getOldEndDate());

                InstalledStatus status = revs.getParent().getStatuses().get(pos+1);
                assertEquals(InstalledStatus.Code.SUSPENDED, status.getCode());
                assertEquals(REFERENCE_DATE.plusHours(12), status.getStartDate());
                assertEquals(IDateTimeService.MAX_TIME, status.getEndDate());
            }

            service.updateRevisions(revs.getParent(),revs.getRevisionsToApply());
        }

        /*
        *  Backdated with merge and remove
         */
        dateTimeRef.getAndUpdate(dt -> REFERENCE_DATE.plusDays(5));
        {
            InstalledItemRevisionsToApply<InstalledOfferRevision, InstalledCompositeOffer> revs = service.findApplicableRevisions(result, offer);
            revs.sortRevisions();
            assertEquals(1, revs.getRevisionsToApply().size());
            revs.getRevisionsToApply().get(0).setEffectiveDate(REFERENCE_DATE.plusHours(10));
            boolean modified = service.applyStatusesFromRevision(revs);
            assertTrue(modified);
            assertEquals(2, revs.getUpdateResult().getStatuses().size());
            assertEquals(3, offer.getStatuses().size());
            int pos = 0;
            {
                StatusUpdateResult revUpdateResult = revs.getUpdateResult().getStatuses().get(pos);
                assertEquals(StatusUpdateResult.Action.MODIFIED, revUpdateResult.getAction());
                assertEquals(InstalledStatus.Code.CLOSED, revUpdateResult.getCode());
                assertEquals(REFERENCE_DATE.plus(1), revUpdateResult.getStartDate());
                assertEquals(IDateTimeService.MAX_TIME, revUpdateResult.getEndDate());
                assertEquals(REFERENCE_DATE.plusHours(12),revUpdateResult.getOldEndDate());

                InstalledStatus status = revs.getParent().getStatuses().get(pos+2);
                assertEquals(InstalledStatus.Code.CLOSED, status.getCode());
                assertEquals(REFERENCE_DATE.plus(1), status.getStartDate());
                assertEquals(IDateTimeService.MAX_TIME, status.getEndDate());
            }
            pos++;
            {
                StatusUpdateResult revUpdateResult = revs.getUpdateResult().getStatuses().get(pos);
                assertEquals(StatusUpdateResult.Action.DELETED, revUpdateResult.getAction());
                assertEquals(InstalledStatus.Code.SUSPENDED, revUpdateResult.getCode());
                assertEquals(REFERENCE_DATE.plusHours(12), revUpdateResult.getStartDate());
                assertNull(revUpdateResult.getEndDate());
                assertEquals(IDateTimeService.MAX_TIME,revUpdateResult.getOldEndDate());
            }

            service.updateRevisions(revs.getParent(),revs.getRevisionsToApply());
        }
    }

    @Test
    public void updateRevisions() throws Exception {
        InstalledBaseUpdateResult result = new InstalledBaseUpdateResult();
        InstalledCompositeOffer offer = manager.build(InstalledCompositeOffer.class, DATASET_NAME, "base_installed_offer", Collections.singletonMap("origDate", REFERENCE_DATE));
        assertNotNull(offer);
        {
            InstalledItemRevisionsToApply<InstalledOfferRevision,InstalledCompositeOffer> revs = service.findApplicableRevisions(result, offer);
            revs.sortRevisions();
            assertEquals(2, revs.getRevisionsToApply().size());
            service.updateRevisions(revs.getParent(),revs.getRevisionsToApply());
            for(InstalledOfferRevision rev:offer.getRevisions()){
                switch (rev.getOrderItemId()){
                    case "ccoItem4.1":
                        assertEquals(0,rev.getRank().intValue());
                        assertEquals(InstalledItemRevision.RevState.DONE,rev.getRevState());
                        assertEquals(REFERENCE_DATE,rev.getRunDate());
                        break;
                    case "ccoItem4":
                        assertEquals(1,rev.getRank().intValue());
                        assertEquals(InstalledItemRevision.RevState.DONE,rev.getRevState());
                        assertEquals(REFERENCE_DATE,rev.getRunDate());
                        break;
                    case "ccoItem3":
                        assertNull(rev.getRank());
                        assertEquals(InstalledItemRevision.RevState.PLANNED,rev.getRevState());
                        assertNull(rev.getRunDate());
                        break;
                    case "ccoItem2":
                        assertNull(rev.getRank());
                        assertEquals(InstalledItemRevision.RevState.PLANNED,rev.getRevState());
                        assertNull(rev.getRunDate());
                        break;
                }
            }
        }

        dateTimeRef.getAndUpdate(dt->dt.plusDays(2));
        {
            InstalledItemRevisionsToApply<InstalledOfferRevision,InstalledCompositeOffer> revs=service.findApplicableRevisions(result,offer);
            assertEquals(2,revs.getRevisionsToApply().size());
            service.updateRevisions(revs.getParent(),revs.getRevisionsToApply());
            for(InstalledOfferRevision rev:offer.getRevisions()){
                switch (rev.getOrderItemId()){
                    case "ccoItem4.1":
                        assertEquals(0,rev.getRank().intValue());
                        assertEquals(InstalledItemRevision.RevState.DONE,rev.getRevState());
                        assertEquals(REFERENCE_DATE,rev.getRunDate());
                        break;
                    case "ccoItem4":
                        assertEquals(1,rev.getRank().intValue());
                        assertEquals(InstalledItemRevision.RevState.DONE,rev.getRevState());
                        assertEquals(REFERENCE_DATE,rev.getRunDate());
                        break;
                    case "ccoItem2":
                        assertEquals(2,rev.getRank().intValue());//no sorting , execute in appearance order
                        assertEquals(InstalledItemRevision.RevState.DONE,rev.getRevState());
                        assertEquals(REFERENCE_DATE.plusDays(2),rev.getRunDate());
                        break;
                    case "ccoItem3":
                        assertEquals(3,rev.getRank().intValue());//no sorting , execute in appearance order
                        assertEquals(InstalledItemRevision.RevState.DONE,rev.getRevState());
                        assertEquals(REFERENCE_DATE.plusDays(2),rev.getRunDate());
                        break;

                }
            }
        }
    }

}