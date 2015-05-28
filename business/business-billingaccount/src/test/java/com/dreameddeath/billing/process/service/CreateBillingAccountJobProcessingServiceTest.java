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

package com.dreameddeath.billing.process.service;


import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.billing.process.model.CreateBillingAccountJob;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.process.dao.JobDao;
import com.dreameddeath.core.process.model.AbstractJob;
import com.dreameddeath.core.process.service.ExecutorServiceFactory;
import com.dreameddeath.core.process.service.JobContext;
import com.dreameddeath.core.process.service.ProcessingServiceFactory;
import com.dreameddeath.party.dao.PartyDao;
import com.dreameddeath.party.process.model.CreatePartyJob;
import com.dreameddeath.party.process.model.CreatePartyRequest;
import com.dreameddeath.party.process.service.CreatePartyJobProcessingService;
import com.dreameddeath.testing.Utils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CreateBillingAccountJobProcessingServiceTest {
    Utils.TestEnvironment _env;
    @Before
    public void initTest() throws  Exception{
        _env = new Utils.TestEnvironment("billingOrder");
        _env.addDocumentDao(new PartyDao());
        _env.addDocumentDao(new JobDao());
        _env.addDocumentDao((CouchbaseDocumentDao) CreateBillingAccountJobProcessingServiceTest.class.getClassLoader().loadClass("com.dreameddeath.billing.dao.account.BillingAccountDao").newInstance());
        _env.addDocumentDao((CouchbaseDocumentDao) CreateBillingAccountJobProcessingServiceTest.class.getClassLoader().loadClass("com.dreameddeath.billing.dao.cycle.BillingCycleDao").newInstance());
        _env.addDocumentDao((CouchbaseDocumentDao) CreateBillingAccountJobProcessingServiceTest.class.getClassLoader().loadClass("com.dreameddeath.billing.dao.order.BillingOrderDao").newInstance());
        //_env.addDocumentDao((CouchbaseDocumentDao) CreateBillingAccountJobProcessingServiceTest.class.getClassLoader().loadClass("com.dreameddeath.billing.dao.account.BillingAccountDao").newInstance());
        // _env.addDocumentDao(new TestDaoProcesorDao(),TestDaoProcessor.class);
        _env.start();
    }

    private final static ExecutorServiceFactory _execFactory=new ExecutorServiceFactory();
    private final static ProcessingServiceFactory _processFactory=new ProcessingServiceFactory();
    static {
        _processFactory.addJobProcessingService(CreatePartyJobProcessingService.class);
        _processFactory.addJobProcessingService(CreateBillingAccountJobProcessingService.class);
        _processFactory.addJobProcessingService(CreateBillingCycleJobProcessingService.class);
    }

    @Test
    public void JobTest() throws Exception{
        ICouchbaseSession session =_env.getSessionFactory().newReadWriteSession(null);
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


        //ICouchbaseSession controlSession = _sessionFactory.newReadOnlySession(null);
        session.reset();
        CreateBillingAccountJob inDbJob = session.get(createBaJob.getBaseMeta().getKey(),CreateBillingAccountJob.class);
        assertEquals(inDbJob.getJobState(), AbstractJob.State.DONE);
        BillingAccount inDbBA = session.get(inDbJob.getTask(0, CreateBillingAccountJob.CreateBillingAccountTask.class).getDocKey(),BillingAccount.class);

        assertEquals(inDbBA.getBillDay(),createBaJob.getRequest().billDay);
        assertEquals((long)inDbBA.getBillCycleLength(),1);
        assertEquals(1,inDbBA.getBillingCycleLinks().size());
        assertEquals(1,inDbBA.getPartyLinks().size());
        assertEquals(createPartyJob.getTask(0, CreatePartyJob.CreatePartyTask.class).getDocKey(),inDbBA.getPartyLinks().get(0).getKey());


    }


}