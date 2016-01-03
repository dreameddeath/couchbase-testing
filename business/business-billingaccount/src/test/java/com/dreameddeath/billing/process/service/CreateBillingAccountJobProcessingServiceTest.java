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
import com.dreameddeath.core.process.dao.TaskDao;
import com.dreameddeath.core.process.model.ProcessState;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.factory.impl.ExecutorClientFactory;
import com.dreameddeath.core.process.service.factory.impl.ExecutorServiceFactory;
import com.dreameddeath.core.process.service.factory.impl.ProcessingServiceFactory;
import com.dreameddeath.core.process.utils.ProcessUtils;
import com.dreameddeath.party.dao.base.PartyDao;
import com.dreameddeath.party.process.model.CreatePartyJob;
import com.dreameddeath.party.process.service.CreatePartyJobProcessingService;
import com.dreameddeath.testing.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CreateBillingAccountJobProcessingServiceTest {
    private Utils.TestEnvironment env;
    private ExecutorClientFactory executorClientFactory;

    @Before
    public void initTest() throws  Exception{
        env = new Utils.TestEnvironment("billingOrder", Utils.TestEnvironment.TestEnvType.COUCHBASE_ELASTICSEARCH);
        env.addDocumentDao(new PartyDao());
        env.addDocumentDao(new JobDao());
        env.addDocumentDao(new TaskDao());
        env.addDocumentDao((CouchbaseDocumentDao) CreateBillingAccountJobProcessingServiceTest.class.getClassLoader().loadClass("com.dreameddeath.billing.dao.account.BillingAccountDao").newInstance());
        env.addDocumentDao((CouchbaseDocumentDao) CreateBillingAccountJobProcessingServiceTest.class.getClassLoader().loadClass("com.dreameddeath.billing.dao.cycle.BillingCycleDao").newInstance());
        env.addDocumentDao((CouchbaseDocumentDao) CreateBillingAccountJobProcessingServiceTest.class.getClassLoader().loadClass("com.dreameddeath.billing.dao.order.BillingOrderDao").newInstance());
        //_env.addDocumentDao((CouchbaseDocumentDao) CreateBillingAccountJobProcessingServiceTest.class.getClassLoader().loadClass("com.dreameddeath.billing.dao.account.BillingAccountDao").newInstance());
        // _env.addDocumentDao(new TestDaoProcesorDao(),TestDaoProcessor.class);
        env.start();
        ExecutorServiceFactory execFactory=new ExecutorServiceFactory();
        ProcessingServiceFactory processFactory=new ProcessingServiceFactory();

        processFactory.addJobProcessingService(CreatePartyJobProcessingService.class);
        processFactory.addJobProcessingService(CreateBillingAccountJobProcessingService.class);
        processFactory.addJobProcessingService(CreateBillingCycleJobProcessingService.class);
        executorClientFactory = new ExecutorClientFactory(env.getSessionFactory(),execFactory,processFactory);

    }

    @Test
    public void JobTest() throws Exception{
        ICouchbaseSession session =env.getSessionFactory().newReadWriteSession(null);
        CreatePartyJob createPartyJob = session.newEntity(CreatePartyJob.class);
        createPartyJob.type = CreatePartyJob.Type.person;
        createPartyJob.person = new CreatePartyJob.Person();
        createPartyJob.person.firstName = "christophe";
        createPartyJob.person.lastName = "jeunesse";

        executorClientFactory.buildJobClient(CreatePartyJob.class).executeJob(createPartyJob,null);
        CreatePartyJob.CreatePartyTask createPartyTask=ProcessUtils.loadTask(session,createPartyJob,1,CreatePartyJob.CreatePartyTask.class);
        CreateBillingAccountJob createBaJob = session.newEntity(CreateBillingAccountJob.class);
        createBaJob.billDay=2;

        createBaJob.partyId = createPartyTask.getDocument(session).getUid();
        JobContext<CreateBillingAccountJob> createBaJobContext =  executorClientFactory.buildJobClient(CreateBillingAccountJob.class).executeJob(createBaJob,null);

        session.reset();
        CreateBillingAccountJob inDbJob = session.get(createBaJob.getBaseMeta().getKey(),CreateBillingAccountJob.class);
        assertEquals(inDbJob.getStateInfo().getState(), ProcessState.State.DONE);
        CreateBillingAccountJob.CreateBillingAccountTask baTask = session.get(createBaJob.getBaseMeta().getKey()+"/task/1",CreateBillingAccountJob.CreateBillingAccountTask.class);

        BillingAccount inDbBA = session.get(baTask.getDocKey(),BillingAccount.class);

        assertEquals(inDbBA.getBillDay(),createBaJob.billDay);
        assertEquals((long)inDbBA.getBillCycleLength(),1);
        assertEquals(1,inDbBA.getBillingCycleLinks().size());
        assertEquals(1,inDbBA.getPartyLinks().size());
        assertEquals(createPartyTask.getDocKey(),inDbBA.getPartyLinks().get(0).getKey());
    }


    @After
    public void end() throws Exception{
        if(env!=null){
            env.shutdown(true);
        }
    }

}