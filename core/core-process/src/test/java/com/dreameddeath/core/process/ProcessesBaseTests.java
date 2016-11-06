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

package com.dreameddeath.core.process;

import com.dreameddeath.core.process.dao.JobDao;
import com.dreameddeath.core.process.dao.TaskDao;
import com.dreameddeath.core.process.dao.TestChildDocDao;
import com.dreameddeath.core.process.dao.TestDocDao;
import com.dreameddeath.core.process.model.TestDoc;
import com.dreameddeath.core.process.registrar.JobExecutorClientRegistrar;
import com.dreameddeath.core.process.registrar.TaskExecutorClientRegistrar;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.factory.impl.ExecutorClientFactory;
import com.dreameddeath.core.process.service.factory.impl.ExecutorServiceFactory;
import com.dreameddeath.core.process.service.factory.impl.ProcessingServiceFactory;
import com.dreameddeath.core.process.services.TestJobCreateService;
import com.dreameddeath.core.process.services.model.TestDocJobCreate;
import com.dreameddeath.core.session.impl.CouchbaseSessionFactory;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.testing.couchbase.CouchbaseBucketSimulator;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.apache.curator.framework.CuratorFramework;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 01/11/2016.
 */
public class ProcessesBaseTests extends Assert {
    private static CuratorTestUtils curatorUtils;
    private static ExecutorClientFactory executorClientFactory;
    private static CouchbaseBucketSimulator cbSimulator;
    private static CuratorFramework curatorFramework;

    @BeforeClass
    public static void initialise() throws Exception {
        curatorUtils = new CuratorTestUtils().prepare(1);

        cbSimulator = new CouchbaseBucketSimulator("test");
        cbSimulator.start();
        CouchbaseSessionFactory sessionFactory = new CouchbaseSessionFactory.Builder().build();
        sessionFactory.getDocumentDaoFactory().addDao(new JobDao().setClient(cbSimulator));
        sessionFactory.getDocumentDaoFactory().addDao(new TaskDao().setClient(cbSimulator));
        sessionFactory.getDocumentDaoFactory().addDao(new TestDocDao().setClient(cbSimulator));
        sessionFactory.getDocumentDaoFactory().addDao(new TestChildDocDao().setClient(cbSimulator));

        ExecutorServiceFactory execFactory = new ExecutorServiceFactory();
        ProcessingServiceFactory processFactory = new ProcessingServiceFactory();
        processFactory.addJobProcessingService(TestJobCreateService.class);
        curatorFramework=curatorUtils.getClient("testProcesses");
        JobExecutorClientRegistrar jobRegistrar=new JobExecutorClientRegistrar(curatorFramework,"D1","W1");
        TaskExecutorClientRegistrar taskRegistrar=new TaskExecutorClientRegistrar(curatorFramework,"D1","W1");
        executorClientFactory = new ExecutorClientFactory(sessionFactory,execFactory,processFactory,null,jobRegistrar,taskRegistrar);
    }


    @Test
    public void runTest() throws Exception {
        IJobExecutorClient<TestDocJobCreate> jobClient = executorClientFactory.buildJobClient(TestDocJobCreate.class);
        TestDocJobCreate job = new TestDocJobCreate();
        job.initIntValue = 10;
        job.name = "testValu1";
        job.tempUid = UUID.randomUUID().toString();
        JobContext<TestDocJobCreate> context = jobClient.executeJob(job, AnonymousUser.INSTANCE).toBlocking().single();

        assertTrue(context.getJobState().isDone());
        TestDocJobCreate.TestJobCreateTask createJobTask = context.getTasks(TestDocJobCreate.TestJobCreateTask.class).get(0).getInternalTask();
        assertEquals(createJobTask.getDocKey(),context.getInternalJob().createdKey);
        TestDoc createdDoc = cbSimulator.toBlocking().get(createJobTask.getDocKey(), TestDoc.class);
        assertEquals(job.initIntValue, createdDoc.intValue);
        assertEquals(job.name, createdDoc.name);
    }

    @AfterClass
    public static void clean() throws Throwable{
        if(curatorUtils!=null){
            curatorUtils.stop();
        }
        if(cbSimulator!=null && cbSimulator.isStarted()){
            cbSimulator.shutdown();
        }
    }
}