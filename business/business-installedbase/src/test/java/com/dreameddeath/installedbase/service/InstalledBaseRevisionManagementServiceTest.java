package com.dreameddeath.installedbase.service;

import com.dreameddeath.core.date.IDateTimeService;
import com.dreameddeath.core.date.MockDateTimeServiceImpl;
import com.dreameddeath.installedbase.model.InstalledBase;
import com.dreameddeath.installedbase.model.common.*;
import com.dreameddeath.installedbase.model.contract.InstalledContract;
import com.dreameddeath.installedbase.model.contract.InstalledContractRevision;
import com.dreameddeath.installedbase.model.offer.InstalledAtomicOffer;
import com.dreameddeath.installedbase.model.offer.InstalledCompositeOffer;
import com.dreameddeath.installedbase.model.offer.InstalledOffer;
import com.dreameddeath.installedbase.model.offer.InstalledOfferRevision;
import com.dreameddeath.installedbase.model.productservice.InstalledProductService;
import com.dreameddeath.installedbase.model.productservice.InstalledProductServiceRevision;
import com.dreameddeath.installedbase.model.tariff.InstalledDiscount;
import com.dreameddeath.installedbase.model.tariff.InstalledDiscountRevision;
import com.dreameddeath.installedbase.model.tariff.InstalledTariff;
import com.dreameddeath.installedbase.model.tariff.InstalledTariffRevision;
import com.dreameddeath.installedbase.process.model.InstalledBaseUpdateResult;
import com.dreameddeath.installedbase.process.model.InstalledItemUpdateResult;
import com.dreameddeath.installedbase.process.model.LinkUpdateResult;
import com.dreameddeath.installedbase.process.model.StatusUpdateResult;
import com.dreameddeath.installedbase.service.utils.InstalledItemRevisionsToApply;
import com.dreameddeath.testing.dataset.DatasetManager;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * Created by Christophe Jeunesse on 11/04/2016.
 */
@RunWith(JUnitParamsRunner.class)
public class InstalledBaseRevisionManagementServiceTest {
    private static final String DATASET_NAME="installed_offer_revision_test";
    private static final String DATASET_ELT_FOR_STATUS="installed_offer_for_status";
    private static final String DATASET_ELT_FOR_LINKS="installed_offer_for_links";
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

    /*
    *
    *  Testing on applicable revisions selection
    *
     */
    @Test
    public void testFindApplicableRevision(){
        InstalledBaseUpdateResult result = new InstalledBaseUpdateResult();
        InstalledCompositeOffer offer = manager.build(InstalledCompositeOffer.class, DATASET_NAME, DATASET_ELT_FOR_STATUS, Collections.singletonMap("origDate", dateTimeRef.get()));
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

    /*
    *
    *
    *  Tests on revisions status updates after execution
    *
    *
    */
    private Object[] revisionsForClasses() {
        return new Object[]{
                new Object[]{InstalledContract.class,InstalledContractRevision.class},
                new Object[]{InstalledCompositeOffer.class,InstalledOfferRevision.class},
                new Object[]{InstalledAtomicOffer.class,InstalledOfferRevision.class},
                new Object[]{InstalledProductService.class, InstalledProductServiceRevision.class},
                new Object[]{InstalledTariff.class, InstalledTariffRevision.class},
                new Object[]{InstalledDiscount.class, InstalledDiscountRevision.class}
        };
    }
    
    @Test
    @Parameters(method = "revisionsForClasses")
    @TestCaseName("RevisionTest for class {0} and  revision {1}")
    public <TREV extends InstalledItemRevision,TOBJ extends InstalledItem<TREV>> void updateRevisions(Class<TOBJ> objClass,Class<TREV> revClass) throws Exception {
        InstalledBaseUpdateResult result = new InstalledBaseUpdateResult();
        TOBJ offer = manager.build(objClass, DATASET_NAME, DATASET_ELT_FOR_STATUS, Collections.singletonMap("origDate", REFERENCE_DATE));
        assertNotNull(offer);
        {
            InstalledItemRevisionsToApply<TREV,TOBJ> revs = service.findApplicableRevisions(result, offer);
            revs.sortRevisions();
            assertEquals(2, revs.getRevisionsToApply().size());
            service.updateRevisions(revs.getParent(),revs.getRevisionsToApply());
            for(TREV rev:offer.getRevisions()){
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
            InstalledItemRevisionsToApply<TREV,TOBJ> revs=service.findApplicableRevisions(result,offer);
            assertEquals(2,revs.getRevisionsToApply().size());
            service.updateRevisions(revs.getParent(),revs.getRevisionsToApply());
            for(TREV rev:offer.getRevisions()){
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

    /*
    *
    *
    *   Statuses upgrades full tests
    *
    *
    */
    private Object[] statusesForClasses() {
        return new Object[]{
                new Object[]{InstalledContract.class,InstalledContractRevision.class},
                new Object[]{InstalledCompositeOffer.class,InstalledOfferRevision.class},
                new Object[]{InstalledAtomicOffer.class,InstalledOfferRevision.class},
                new Object[]{InstalledProductService.class, InstalledProductServiceRevision.class},
                new Object[]{InstalledTariff.class, InstalledTariffRevision.class},
                new Object[]{InstalledDiscount.class, InstalledDiscountRevision.class}
        };
    }

    @Test
    @Parameters(method = "statusesForClasses")
    @TestCaseName("StatusTest for class {0} and  revision {1}")
    public <TREV extends InstalledItemRevision,TOBJ extends InstalledItem<TREV>> void applyStatusesFromRevision(Class<TOBJ> objClass,Class<TREV> revClass) throws Exception {
        InstalledBaseUpdateResult result = new InstalledBaseUpdateResult();
        TOBJ offer = manager.build(objClass, DATASET_NAME, DATASET_ELT_FOR_STATUS, Collections.singletonMap("origDate", REFERENCE_DATE));
        assertNotNull(offer);

        /*
        *  No revision, no change
         */
        dateTimeRef.getAndUpdate(dt -> dt.minus(10));
        {
            InstalledItemRevisionsToApply<TREV, TOBJ> revs = service.findApplicableRevisions(result, offer);
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
            InstalledItemRevisionsToApply<TREV, TOBJ> revs = service.findApplicableRevisions(result, offer);
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
            InstalledItemRevisionsToApply<TREV, TOBJ> revs = service.findApplicableRevisions(result, offer);
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
            InstalledItemRevisionsToApply<TREV, TOBJ> revs = service.findApplicableRevisions(result, offer);
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
            InstalledItemRevisionsToApply<TREV, TOBJ> revs = service.findApplicableRevisions(result, offer);
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

    /*
    *
    *
    *   Testing on links
    *
    *
     */
    private Object[] linksForClasses() {
        return new Object[]{
                new Object[]{InstalledContract.class,InstalledContractRevision.class},
                new Object[]{InstalledCompositeOffer.class,InstalledOfferRevision.class},
                new Object[]{InstalledAtomicOffer.class,InstalledOfferRevision.class},
                new Object[]{InstalledProductService.class, InstalledProductServiceRevision.class}
        };
    }
    @Test
    @Parameters(method = "linksForClasses")
    @TestCaseName("LinksTest for class {0} and  revision {1}")
    public <TLINK extends InstalledItemLink,TREV extends InstalledItemRevision & IHasLinkRevision,TOBJ extends InstalledItem<TREV> & IHasInstalledItemLink<TLINK>> void applyLinksFromRevision(Class<TOBJ> objClass,Class<TREV> revClass) throws Exception {
        InstalledBaseUpdateResult result = new InstalledBaseUpdateResult();
        InstalledBase ref=new InstalledBase();
        TOBJ offer = manager.build(objClass, DATASET_NAME, DATASET_ELT_FOR_LINKS, Collections.singletonMap("origDate", dateTimeRef.get()));


        /*
        *
        *   Simple creation cases
        *
         */
        {
            InstalledItemRevisionsToApply<TREV,TOBJ> revs=service.findApplicableRevisions(result,offer);
            revs.sortRevisions();
            assertEquals(1,revs.getRevisionsToApply().size());
            boolean modified=service.applyLinksFromRevision(ref,revs);
            assertTrue(modified);
            assertEquals(4, revs.getUpdateResult(InstalledItemUpdateResult.class).getLinks().size());
            assertEquals(4, offer.getLinks().size());
            int pos = 0;
            {
                LinkUpdateResult linkUpdateResult = revs.getUpdateResult(InstalledItemUpdateResult.class).getLinks().get(pos);
                //update itself
                assertEquals(InstalledItemLink.Type.RELIES,linkUpdateResult.getType());
                assertEquals("tid1",linkUpdateResult.getTargetId());
                assertNull(linkUpdateResult.isReverse());
                assertEquals(1,linkUpdateResult.getStatuses().size());
                //Status check
                assertEquals(StatusUpdateResult.Action.NEW,linkUpdateResult.getStatuses().get(0).getAction());
                assertEquals(InstalledStatus.Code.SUSPENDED,linkUpdateResult.getStatuses().get(0).getCode());
                assertEquals(REFERENCE_DATE,linkUpdateResult.getStatuses().get(0).getStartDate());
                assertEquals(IDateTimeService.MAX_TIME,linkUpdateResult.getStatuses().get(0).getEndDate());

                TLINK link = revs.getParent().getLinks().get(pos);
                assertEquals(linkUpdateResult.getTargetId(),link.getTargetId());
                assertEquals(linkUpdateResult.getType(),link.getType());
                assertEquals(linkUpdateResult.isReverse(),link.isReverse());
                assertEquals(1,link.getStatuses().size());
                assertEquals(linkUpdateResult.getStatuses().get(0).getCode(),link.getStatuses().get(0).getCode());
                assertEquals(linkUpdateResult.getStatuses().get(0).getStartDate(),link.getStatuses().get(0).getStartDate());
                assertEquals(linkUpdateResult.getStatuses().get(0).getEndDate(),link.getStatuses().get(0).getEndDate());
            }
            ++pos;
            {
                LinkUpdateResult linkUpdateResult = revs.getUpdateResult(InstalledItemUpdateResult.class).getLinks().get(pos);
                //update itself
                assertEquals(InstalledItemLink.Type.MIGRATE,linkUpdateResult.getType());
                assertEquals("tid1",linkUpdateResult.getTargetId());
                assertTrue(linkUpdateResult.isReverse());
                assertEquals(1,linkUpdateResult.getStatuses().size());
                //Status check
                assertEquals(StatusUpdateResult.Action.NEW,linkUpdateResult.getStatuses().get(0).getAction());
                assertEquals(InstalledStatus.Code.SUSPENDED,linkUpdateResult.getStatuses().get(0).getCode());
                assertEquals(REFERENCE_DATE,linkUpdateResult.getStatuses().get(0).getStartDate());
                assertEquals(IDateTimeService.MAX_TIME,linkUpdateResult.getStatuses().get(0).getEndDate());

                TLINK link = revs.getParent().getLinks().get(pos);
                assertEquals(linkUpdateResult.getTargetId(),link.getTargetId());
                assertEquals(linkUpdateResult.getType(),link.getType());
                assertEquals(linkUpdateResult.isReverse(),link.isReverse());
                assertEquals(1,link.getStatuses().size());
                assertEquals(linkUpdateResult.getStatuses().get(0).getCode(),link.getStatuses().get(0).getCode());
                assertEquals(linkUpdateResult.getStatuses().get(0).getStartDate(),link.getStatuses().get(0).getStartDate());
                assertEquals(linkUpdateResult.getStatuses().get(0).getEndDate(),link.getStatuses().get(0).getEndDate());
            }
            ++pos;
            {
                LinkUpdateResult linkUpdateResult = revs.getUpdateResult(InstalledItemUpdateResult.class).getLinks().get(pos);
                //update itself
                assertEquals(InstalledItemLink.Type.MIGRATE,linkUpdateResult.getType());
                assertEquals("tid2",linkUpdateResult.getTargetId());
                assertNull(linkUpdateResult.isReverse());
                assertEquals(1,linkUpdateResult.getStatuses().size());
                //Status check
                assertEquals(StatusUpdateResult.Action.NEW,linkUpdateResult.getStatuses().get(0).getAction());
                assertEquals(InstalledStatus.Code.ACTIVE,linkUpdateResult.getStatuses().get(0).getCode());
                assertEquals(REFERENCE_DATE,linkUpdateResult.getStatuses().get(0).getStartDate());
                assertEquals(IDateTimeService.MAX_TIME,linkUpdateResult.getStatuses().get(0).getEndDate());

                TLINK link = revs.getParent().getLinks().get(pos);
                assertEquals(linkUpdateResult.getTargetId(),link.getTargetId());
                assertEquals(linkUpdateResult.getType(),link.getType());
                assertEquals(linkUpdateResult.isReverse(),link.isReverse());
                assertEquals(1,link.getStatuses().size());
                assertEquals(linkUpdateResult.getStatuses().get(0).getCode(),link.getStatuses().get(0).getCode());
                assertEquals(linkUpdateResult.getStatuses().get(0).getStartDate(),link.getStatuses().get(0).getStartDate());
                assertEquals(linkUpdateResult.getStatuses().get(0).getEndDate(),link.getStatuses().get(0).getEndDate());
            }
            ++pos;
            {
                LinkUpdateResult linkUpdateResult = revs.getUpdateResult(InstalledItemUpdateResult.class).getLinks().get(pos);
                //update itself
                assertEquals(InstalledItemLink.Type.AGGREGATE,linkUpdateResult.getType());
                assertEquals("tid3",linkUpdateResult.getTargetId());
                assertNull(linkUpdateResult.isReverse());
                assertEquals(1,linkUpdateResult.getStatuses().size());
                //Status check
                assertEquals(StatusUpdateResult.Action.NEW,linkUpdateResult.getStatuses().get(0).getAction());
                assertEquals(InstalledStatus.Code.ACTIVE,linkUpdateResult.getStatuses().get(0).getCode());
                assertEquals(REFERENCE_DATE.plus(5),linkUpdateResult.getStatuses().get(0).getStartDate());
                assertEquals(IDateTimeService.MAX_TIME,linkUpdateResult.getStatuses().get(0).getEndDate());

                TLINK link = revs.getParent().getLinks().get(pos);
                assertEquals(linkUpdateResult.getTargetId(),link.getTargetId());
                assertEquals(linkUpdateResult.getType(),link.getType());
                assertEquals(linkUpdateResult.isReverse(),link.isReverse());
                assertEquals(1,link.getStatuses().size());
                assertEquals(linkUpdateResult.getStatuses().get(0).getCode(),link.getStatuses().get(0).getCode());
                assertEquals(linkUpdateResult.getStatuses().get(0).getStartDate(),link.getStatuses().get(0).getStartDate());
                assertEquals(linkUpdateResult.getStatuses().get(0).getEndDate(),link.getStatuses().get(0).getEndDate());
            }
            service.updateRevisions(revs.getParent(),revs.getRevisionsToApply());
        }


        /*
        *
        *  Some update cases
        *
         */
        dateTimeRef.getAndUpdate(dt->dt.plusHours(14).plusMinutes(30));
        {
            InstalledItemRevisionsToApply<TREV,TOBJ> revs=service.findApplicableRevisions(result,offer);
            revs.sortRevisions();
            assertEquals(2,revs.getRevisionsToApply().size());
            boolean modified=service.applyLinksFromRevision(ref,revs);
            assertTrue(modified);
            assertEquals(1, revs.getUpdateResult(InstalledItemUpdateResult.class).getLinks().size());
            assertEquals(4, offer.getLinks().size());
            LinkUpdateResult linkUpdateResult = revs.getUpdateResult(InstalledItemUpdateResult.class).getLinks().get(0);
            //update itself
            assertEquals(InstalledItemLink.Type.RELIES,linkUpdateResult.getType());
            assertEquals("tid1",linkUpdateResult.getTargetId());
            assertNull(linkUpdateResult.isReverse());
            assertEquals(4,linkUpdateResult.getStatuses().size());

            TLINK link = revs.getParent().getLinks().get(0);
            assertEquals(linkUpdateResult.getTargetId(),link.getTargetId());
            assertEquals(linkUpdateResult.getType(),link.getType());
            assertEquals(linkUpdateResult.isReverse(),link.isReverse());
            assertEquals(4,link.getStatuses().size());

            int pos = 0;
            {
                //Status check
                assertEquals(StatusUpdateResult.Action.MODIFIED,linkUpdateResult.getStatuses().get(pos).getAction());
                assertEquals(InstalledStatus.Code.SUSPENDED,linkUpdateResult.getStatuses().get(pos).getCode());
                assertEquals(REFERENCE_DATE,linkUpdateResult.getStatuses().get(pos).getStartDate());
                assertEquals(REFERENCE_DATE.plusHours(12),linkUpdateResult.getStatuses().get(pos).getEndDate());
                assertEquals(IDateTimeService.MAX_TIME,linkUpdateResult.getStatuses().get(pos).getOldEndDate());
                //Effective link results
                assertEquals(linkUpdateResult.getStatuses().get(pos).getCode(),link.getStatuses().get(0).getCode());
                assertEquals(linkUpdateResult.getStatuses().get(pos).getStartDate(),link.getStatuses().get(0).getStartDate());
                assertEquals(linkUpdateResult.getStatuses().get(pos).getEndDate(),link.getStatuses().get(0).getEndDate());
            }
            ++pos;
            {
                //Status check
                assertEquals(StatusUpdateResult.Action.NEW,linkUpdateResult.getStatuses().get(pos).getAction());
                assertEquals(InstalledStatus.Code.ACTIVE,linkUpdateResult.getStatuses().get(pos).getCode());
                assertEquals(REFERENCE_DATE.plusHours(12),linkUpdateResult.getStatuses().get(pos).getStartDate());
                assertEquals(REFERENCE_DATE.plusHours(12).plusMinutes(30),linkUpdateResult.getStatuses().get(pos).getEndDate());
                assertNull(linkUpdateResult.getStatuses().get(pos).getOldEndDate());
                //Effective link results
                assertEquals(linkUpdateResult.getStatuses().get(pos).getCode(),link.getStatuses().get(pos).getCode());
                assertEquals(linkUpdateResult.getStatuses().get(pos).getStartDate(),link.getStatuses().get(pos).getStartDate());
                assertEquals(linkUpdateResult.getStatuses().get(pos).getEndDate(),link.getStatuses().get(pos).getEndDate());
            }
            ++pos;
            {
                //Status check
                assertEquals(StatusUpdateResult.Action.NEW,linkUpdateResult.getStatuses().get(pos).getAction());
                assertEquals(InstalledStatus.Code.SUSPENDED,linkUpdateResult.getStatuses().get(pos).getCode());
                assertEquals(REFERENCE_DATE.plusHours(12).plusMinutes(30),linkUpdateResult.getStatuses().get(pos).getStartDate());
                assertEquals(REFERENCE_DATE.plusHours(13),linkUpdateResult.getStatuses().get(pos).getEndDate());
                assertNull(linkUpdateResult.getStatuses().get(pos).getOldEndDate());
                //Effective link results
                assertEquals(linkUpdateResult.getStatuses().get(pos).getCode(),link.getStatuses().get(pos).getCode());
                assertEquals(linkUpdateResult.getStatuses().get(pos).getStartDate(),link.getStatuses().get(pos).getStartDate());
                assertEquals(linkUpdateResult.getStatuses().get(pos).getEndDate(),link.getStatuses().get(pos).getEndDate());
            }
            ++pos;
            {
                //Status check
                assertEquals(StatusUpdateResult.Action.NEW,linkUpdateResult.getStatuses().get(pos).getAction());
                assertEquals(InstalledStatus.Code.CLOSED,linkUpdateResult.getStatuses().get(pos).getCode());
                assertEquals(REFERENCE_DATE.plusHours(13),linkUpdateResult.getStatuses().get(pos).getStartDate());
                assertEquals(IDateTimeService.MAX_TIME,linkUpdateResult.getStatuses().get(pos).getEndDate());
                assertNull(linkUpdateResult.getStatuses().get(pos).getOldEndDate());
                //Effective link results
                assertEquals(linkUpdateResult.getStatuses().get(pos).getCode(),link.getStatuses().get(pos).getCode());
                assertEquals(linkUpdateResult.getStatuses().get(pos).getStartDate(),link.getStatuses().get(pos).getStartDate());
                assertEquals(linkUpdateResult.getStatuses().get(pos).getEndDate(),link.getStatuses().get(pos).getEndDate());
            }
            service.updateRevisions(revs.getParent(),revs.getRevisionsToApply());
        }

        /*
        *
        *  creation of already existing
        *
         */
        dateTimeRef.getAndUpdate(dt->REFERENCE_DATE.plusDays(1));
        {
            InstalledItemRevisionsToApply<TREV, TOBJ> revs = service.findApplicableRevisions(result, offer);
            revs.sortRevisions();
            assertEquals(1, revs.getRevisionsToApply().size());
            try {
                service.applyLinksFromRevision(ref, revs);
                fail();
            }
            catch(IllegalArgumentException e){
                assertTrue(e.getMessage().matches("The existing link .*? has bad action ADD"));
            }
            service.updateRevisions(revs.getParent(),revs.getRevisionsToApply());
        }

        /*
        *
        *  remove of not existing link
        *
        */
        dateTimeRef.getAndUpdate(dt->REFERENCE_DATE.plusDays(2));
        {
            InstalledItemRevisionsToApply<TREV, TOBJ> revs = service.findApplicableRevisions(result, offer);
            revs.sortRevisions();
            assertEquals(1, revs.getRevisionsToApply().size());
            try {
                service.applyLinksFromRevision(ref, revs);
                fail();
            }
            catch(IllegalArgumentException e){
                assertTrue(e.getMessage().matches("The new link .*? has bad action REMOVE"));
            }
            service.updateRevisions(revs.getParent(),revs.getRevisionsToApply());
        }
    }




    /*
    *
    *  Testing on attributes
    *
    *
    */
    private Object[] attributesForClasses() {
        return new Object[]{
                new Object[]{InstalledCompositeOffer.class,InstalledOfferRevision.class,"commercialParameters"},
                new Object[]{InstalledAtomicOffer.class,InstalledOfferRevision.class,"commercialParameters"},
                new Object[]{InstalledProductService.class, InstalledProductServiceRevision.class,"functions"}
        };
    }

    @Test
    @Parameters(method = "attributesForClasses")
    public <TREV extends InstalledItemRevision,TOBJ extends InstalledItem<TREV>> void applyAttributeUpdateFromRevision(Class<TOBJ> objClass,Class<TREV> revClass,String attrFieldName) throws Exception {
        InstalledBaseUpdateResult result = new InstalledBaseUpdateResult();
        InstalledBase ref = new InstalledBase();
        Map<String,Object> params=new HashMap<>();
        params.put("origDate",dateTimeRef.get());
        params.put("attrFieldName",attrFieldName);
        TOBJ installedItem = manager.build(objClass, DATASET_NAME, "installed_offer_for_attributes", params);

        /*
        *
        *   Simple creation cases
        *
        */
        dateTimeRef.getAndUpdate(dt->dt.plus(5));
        {
            InstalledItemRevisionsToApply<TREV, TOBJ> revs = service.findApplicableRevisions(result, installedItem);
            revs.sortRevisions();
            assertEquals(2, revs.getRevisionsToApply().size());
            boolean modified;
            List<? extends InstalledAttribute> attributesList;
            if (installedItem instanceof InstalledOffer) {
                modified = service.applyCommercialParametersFromRevision((InstalledItemRevisionsToApply<InstalledOfferRevision, InstalledOffer>) revs);
                attributesList = ((InstalledOffer) installedItem).getCommercialParameters();
            } else {
                modified = service.applyFunctionsFromRevision((InstalledItemRevisionsToApply<InstalledProductServiceRevision, InstalledProductService>) revs);
                attributesList = ((InstalledProductService) installedItem).getFunctions();
            }

            assertTrue(modified);
            assertEquals(3, revs.getUpdateResult(InstalledItemUpdateResult.class).getAttributes().size());
            assertEquals(3, attributesList.size());
        }
    }
}