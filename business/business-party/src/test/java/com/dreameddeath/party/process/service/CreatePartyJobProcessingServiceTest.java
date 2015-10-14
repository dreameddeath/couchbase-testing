/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.party.process.service;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.process.dao.JobDao;
import com.dreameddeath.core.process.model.AbstractJob;
import com.dreameddeath.core.process.service.ExecutorServiceFactory;
import com.dreameddeath.core.process.service.JobContext;
import com.dreameddeath.core.process.service.ProcessingServiceFactory;
import com.dreameddeath.party.dao.PartyDao;
import com.dreameddeath.party.model.base.Party;
import com.dreameddeath.party.model.base.Person;
import com.dreameddeath.party.process.model.CreatePartyJob;
import com.dreameddeath.party.process.model.CreatePartyRequest;
import com.dreameddeath.testing.Utils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CreatePartyJobProcessingServiceTest {
    //private final static CouchbaseSessionFactory _sessionFactory ;
    private final static Utils.TestEnvironment testEnvironment;
    static {
        try {
            testEnvironment = new Utils.TestEnvironment("PartyTest", Utils.TestEnvironment.TestEnvType.COUCHBASE_ELASTICSEARCH);
            testEnvironment.addDocumentDao(new PartyDao());
            testEnvironment.addDocumentDao(new JobDao());
            testEnvironment.start();
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
        //CouchbaseBucketSimulator client = new CouchbaseBucketSimulator("test");

        //_sessionFactory = (new CouchbaseSessionFactory.Builder()).build();
        //_sessionFactory.getUniqueKeyDaoFactory().setDefaultTranscoder(new GenericJacksonTranscoder<>(CouchbaseUniqueKey.class));
        //_sessionFactory.getDocumentDaoFactory().addDao(new PartyDao().setClient(client), new GenericJacksonTranscoder<>(Party.class));
        //_sessionFactory.getDocumentDaoFactory().addDao(new JobDao().setClient(client),new GenericJacksonTranscoder<>(AbstractJob.class));


    }

    private final static ExecutorServiceFactory execFactory=new ExecutorServiceFactory();
    private final static ProcessingServiceFactory processFactory=new ProcessingServiceFactory();
    static {
        processFactory.addJobProcessingService(CreatePartyJobProcessingService.class);

    }

    @Test
    public void JobTest() throws Exception{
        ICouchbaseSession session = testEnvironment.getSessionFactory().newReadWriteSession(null);
        CreatePartyJob createPartyJob = session.newEntity(CreatePartyJob.class);
        createPartyJob.getRequest().type = CreatePartyRequest.Type.person;
        createPartyJob.getRequest().person = new CreatePartyRequest.Person();
        createPartyJob.getRequest().person.firstName = "christophe";
        createPartyJob.getRequest().person.lastName = "jeunesse";

        execFactory.execute(JobContext.newContext(session, execFactory, processFactory), createPartyJob);
        ICouchbaseSession controlSession = testEnvironment.getSessionFactory().newReadOnlySession(null);
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