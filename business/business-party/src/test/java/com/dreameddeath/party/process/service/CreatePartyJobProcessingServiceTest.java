package com.dreameddeath.party.process.service;

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
import org.junit.Test;

import static org.junit.Assert.*;

public class CreatePartyJobProcessingServiceTest {
    private final static CouchbaseSessionFactory _sessionFactory ;
    static {
        CouchbaseBucketSimulator client = new CouchbaseBucketSimulator("test");

        _sessionFactory = (new CouchbaseSessionFactory.Builder()).build();
        _sessionFactory.getUniqueKeyDaoFactory().setDefaultTranscoder(new GenericJacksonTranscoder<>(CouchbaseUniqueKey.class));
        _sessionFactory.getDocumentDaoFactory().addDao(new PartyDao().setClient(client), new GenericJacksonTranscoder<>(Party.class));
        _sessionFactory.getDocumentDaoFactory().addDao(new JobDao().setClient(client),new GenericJacksonTranscoder<>(AbstractJob.class));

        client.start();
    }

    private final static ExecutorServiceFactory _execFactory=new ExecutorServiceFactory();
    private final static ProcessingServiceFactory _processFactory=new ProcessingServiceFactory();
    static {
        _processFactory.addJobProcessingService(CreatePartyJobProcessingService.class);

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
        ICouchbaseSession controlSession = _sessionFactory.newReadOnlySession(null);
        CreatePartyJob inDbJob = controlSession.get(createPartyJob.getBaseMeta().getKey(),CreatePartyJob.class);
        assertEquals(inDbJob.getJobState(), AbstractJob.State.DONE);
        Party inDbParty = controlSession.get(inDbJob.getTask(0, CreatePartyJob.CreatePartyTask.class).getDocKey(),Party.class);
        assertEquals(inDbParty.getClass(),Person.class);
        if(inDbParty instanceof Person){
            Person inDbPerson = (Person)inDbParty;
            CreatePartyRequest req = createPartyJob.getRequest();
            assertEquals(inDbPerson.getFirstName(),req.person.firstName);
            assertEquals(inDbPerson.getLastName(),req.person.lastName);
        }
    }

}