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
import com.dreameddeath.core.process.dao.TaskDao;
import com.dreameddeath.core.process.model.v1.base.ProcessState;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.dreameddeath.core.process.service.factory.impl.ExecutorClientFactory;
import com.dreameddeath.core.process.service.factory.impl.ExecutorServiceFactory;
import com.dreameddeath.core.process.service.factory.impl.ProcessingServiceFactory;
import com.dreameddeath.party.dao.base.PartyDao;
import com.dreameddeath.party.model.base.Party;
import com.dreameddeath.party.model.base.Person;
import com.dreameddeath.party.process.model.CreatePartyJob;
import com.dreameddeath.testing.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CreatePartyJobProcessingServiceTest {
    //private final static CouchbaseSessionFactory _sessionFactory ;
    private Utils.TestEnvironment testEnvironment;
    private ExecutorClientFactory executorClientFactory;
    @Before
    public void setup() throws Exception{
        testEnvironment = new Utils.TestEnvironment("PartyTest", Utils.TestEnvironment.TestEnvType.COUCHBASE_ELASTICSEARCH);
        testEnvironment.addDocumentDao(new PartyDao());
        testEnvironment.addDocumentDao(new JobDao());
        testEnvironment.addDocumentDao(new TaskDao());
        testEnvironment.start();
        ExecutorServiceFactory execFactory=new ExecutorServiceFactory();
        ProcessingServiceFactory processFactory=new ProcessingServiceFactory();
        processFactory.addJobProcessingService(CreatePartyJobProcessingService.class);
        executorClientFactory = new ExecutorClientFactory(testEnvironment.getSessionFactory(),execFactory,processFactory);
    }

    @Test
    public void JobTest() throws Exception{
        ICouchbaseSession session = testEnvironment.getSessionFactory().newReadWriteSession(null);

        CreatePartyJob createPartyJob = session.newEntity(CreatePartyJob.class);
        createPartyJob.type = CreatePartyJob.Type.person;
        createPartyJob.person = new CreatePartyJob.Person();
        createPartyJob.person.firstName = "christophe";
        createPartyJob.person.lastName = "jeunesse";

        IJobExecutorClient<CreatePartyJob> executorClient = executorClientFactory.buildJobClient(CreatePartyJob.class);
        executorClient.executeJob(createPartyJob,null);

        //execFactory.execute(JobContext.newContext(session, execFactory, processFactory, createPartyJob));
        ICouchbaseSession controlSession = testEnvironment.getSessionFactory().newReadOnlySession(null);
        CreatePartyJob inDbJob = controlSession.get(createPartyJob.getBaseMeta().getKey(),CreatePartyJob.class);
        assertEquals(inDbJob.getStateInfo().getState(), ProcessState.State.DONE);
        CreatePartyJob.CreatePartyTask inDbTask = controlSession.get(createPartyJob.getBaseMeta().getKey()+"/task/1",CreatePartyJob.CreatePartyTask.class);
        Party inDbParty = controlSession.get(inDbTask.getDocKey(),Party.class);
        assertEquals(inDbParty.getClass(),Person.class);
        if(inDbParty instanceof Person){
            Person inDbPerson = (Person)inDbParty;
            assertEquals(inDbPerson.getFirstName(),createPartyJob.person.firstName);
            assertEquals(inDbPerson.getLastName(),createPartyJob.person.lastName);
        }
    }

    @After
    public void close(){
        if(testEnvironment!=null) {
            testEnvironment.shutdown(true);
        }
    }
}