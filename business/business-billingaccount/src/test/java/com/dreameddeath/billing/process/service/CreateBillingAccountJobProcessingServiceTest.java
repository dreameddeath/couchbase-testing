package com.dreameddeath.billing.process.service;

import com.dreameddeath.billing.dao.BillingAccountDao;
import com.dreameddeath.billing.dao.BillingCycleDao;
import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.billing.model.cycle.BillingCycle;
import com.dreameddeath.billing.process.model.CreateBillingAccountJob;
import com.dreameddeath.core.dao.process.JobDao;
import com.dreameddeath.core.model.process.AbstractJob;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;
import com.dreameddeath.core.process.service.ExecutorServiceFactory;
import com.dreameddeath.core.process.service.JobContext;
import com.dreameddeath.core.process.service.ProcessingServiceFactory;
import com.dreameddeath.core.session.ICouchbaseSession;
import com.dreameddeath.core.session.impl.CouchbaseSessionFactory;
import com.dreameddeath.core.storage.impl.CouchbaseBucketSimulator;
import com.dreameddeath.core.transcoder.json.GenericJacksonTranscoder;
import com.dreameddeath.party.dao.PartyDao;
import com.dreameddeath.party.model.base.Party;
import com.dreameddeath.party.model.base.Person;
import com.dreameddeath.party.process.model.CreatePartyJob;
import com.dreameddeath.party.process.model.CreatePartyRequest;
import com.dreameddeath.party.process.service.CreatePartyJobProcessingService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class CreateBillingAccountJobProcessingServiceTest {
    private final static CouchbaseSessionFactory _sessionFactory ;
    static {
        CouchbaseBucketSimulator client = new CouchbaseBucketSimulator("test");

        _sessionFactory = (new CouchbaseSessionFactory.Builder()).build();
        _sessionFactory.getUniqueKeyDaoFactory().setDefaultTranscoder(new GenericJacksonTranscoder<>(CouchbaseUniqueKey.class));
        _sessionFactory.getDocumentDaoFactory().addDao(new PartyDao().setClient(client), new GenericJacksonTranscoder<>(Party.class));
        _sessionFactory.getDocumentDaoFactory().addDao(new JobDao().setClient(client),new GenericJacksonTranscoder<>(AbstractJob.class));
        _sessionFactory.getDocumentDaoFactory().addDao(new BillingAccountDao().setClient(client),new GenericJacksonTranscoder<>(BillingAccount.class));
        _sessionFactory.getDocumentDaoFactory().addDao(new BillingCycleDao().setClient(client),new GenericJacksonTranscoder<>(BillingCycle.class));

        client.start();
    }

    private final static ExecutorServiceFactory _execFactory=new ExecutorServiceFactory();
    private final static ProcessingServiceFactory _processFactory=new ProcessingServiceFactory();
    static {
        _processFactory.addJobProcessingService(CreatePartyJobProcessingService.class);
        _processFactory.addJobProcessingService(CreateBillingAccountJobProcessingService.class);
        _processFactory.addJobProcessingService(CreateBillingCycleJobProcessingService.class);
    }

    @Before
    public void Init(){

    }

    @Test
    public void JobTest() throws Exception{
        ICouchbaseSession session = _sessionFactory.newReadWriteSession(null);
        CreatePartyJob createPartyJob = session.newEntity(CreatePartyJob.class);
        createPartyJob.getRequest().type = CreatePartyRequest.Type.person;
        createPartyJob.getRequest().person = new CreatePartyRequest.Person();
        createPartyJob.getRequest().person.firstName = "christophe";
        createPartyJob.getRequest().person.lastName = "jeunesse";

        _execFactory.execute(JobContext.newContext(session, _execFactory, _processFactory), createPartyJob);

        CreateBillingAccountJob createBaJob = session.newEntity(CreateBillingAccountJob.class);
        createBaJob.getRequest().billDay=2;
        createBaJob.getRequest().partyId = createPartyJob.getTask(0,CreatePartyJob.CreatePartyTask.class).getDocument(session).getUid();
        _execFactory.execute(JobContext.newContext(session, _execFactory, _processFactory),createBaJob);


        ICouchbaseSession controlSession = _sessionFactory.newReadOnlySession(null);
        CreateBillingAccountJob inDbJob = controlSession.get(createBaJob.getBaseMeta().getKey(),CreateBillingAccountJob.class);
        assertEquals(inDbJob.getJobState(), AbstractJob.State.DONE);
        BillingAccount inDbBA = controlSession.get(inDbJob.getTask(0, CreateBillingAccountJob.CreateBillingAccountTask.class).getDocKey(),BillingAccount.class);

        assertEquals(inDbBA.getBillDay(),createBaJob.getRequest().billDay);
        assertEquals((long)inDbBA.getBillCycleLength(),1);
        assertEquals(1,inDbBA.getBillingCycleLinks().size());
        assertEquals(1,inDbBA.getPartyLinks().size());
        assertEquals(createPartyJob.getTask(0, CreatePartyJob.CreatePartyTask.class).getDocKey(),inDbBA.getPartyLinks().get(0).getKey());


    }


}