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

package com.dreameddeath.couchbase.core.process.rest;

import com.dreameddeath.core.process.dao.JobDao;
import com.dreameddeath.core.process.dao.TaskDao;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.factory.impl.ExecutorClientFactory;
import com.dreameddeath.core.process.service.factory.impl.ExecutorServiceFactory;
import com.dreameddeath.core.service.annotation.processor.ServiceExposeAnnotationProcessor;
import com.dreameddeath.core.service.testing.TestingRestServer;
import com.dreameddeath.core.session.impl.CouchbaseSessionFactory;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.couchbase.core.process.remote.annotation.processor.ProcessRestServiceProcessor;
import com.dreameddeath.couchbase.core.process.remote.dao.TestDocDao;
import com.dreameddeath.couchbase.core.process.remote.factory.BaseRemoteClientFactory;
import com.dreameddeath.couchbase.core.process.remote.factory.ProcessingServiceWithRemoteCapabiltyFactory;
import com.dreameddeath.couchbase.core.process.remote.model.RemoteCreateJob;
import com.dreameddeath.couchbase.core.process.remote.model.RemoteUpdateGenJob;
import com.dreameddeath.couchbase.core.process.remote.model.RemoteUpdateJob;
import com.dreameddeath.couchbase.core.process.remote.model.TestDoc;
import com.dreameddeath.couchbase.core.process.remote.service.rest.RemoteTestDocJobUpdateGenService;
import com.dreameddeath.couchbase.core.process.rest.process.*;
import com.dreameddeath.couchbase.core.process.rest.process.rest.RestTestDocJobCreateService;
import com.dreameddeath.couchbase.core.process.rest.process.rest.RestTestDocJobUpdateService;
import com.dreameddeath.testing.AnnotationProcessorTestingWrapper;
import com.dreameddeath.testing.couchbase.CouchbaseBucketSimulator;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 04/01/2016.
 */
public class RefRestServiceTest extends Assert {
    private static TestingRestServer server;

    private static AnnotationProcessorTestingWrapper.Result generatorResult;
    private static CuratorTestUtils curatorUtils;
    private static ExecutorClientFactory executorClientFactory;
    private static CouchbaseBucketSimulator cbSimulator;

    public static void compileTestServiceGen() throws Exception{
        AnnotationProcessorTestingWrapper annotTester = new AnnotationProcessorTestingWrapper();
        annotTester.
                withAnnotationProcessor(new ServiceExposeAnnotationProcessor()).
                withAnnotationProcessor(new ProcessRestServiceProcessor()).
                withTempDirectoryPrefix("ProcessRestServiceGeneratorTest");
        generatorResult = annotTester.run(RefRestServiceTest.class.getClassLoader().getResource("processGenSourceFiles").getPath());
        assertTrue(generatorResult.getResult());
    }

    @BeforeClass
    public static void initialise() throws Exception{
        compileTestServiceGen();
        curatorUtils = new CuratorTestUtils().prepare(1);
        server = new TestingRestServer("serverTesting", curatorUtils.getClient("TestServicesTest"));
        BaseRemoteClientFactory remoteClientFactory = new BaseRemoteClientFactory();
        remoteClientFactory.setClientFactory(server.getClientFactory());

        cbSimulator = new CouchbaseBucketSimulator("test");
        cbSimulator.start();
        CouchbaseSessionFactory sessionFactory = new CouchbaseSessionFactory.Builder().build();
        sessionFactory.getDocumentDaoFactory().addDao(new JobDao().setClient(cbSimulator));
        sessionFactory.getDocumentDaoFactory().addDao(new TaskDao().setClient(cbSimulator));
        sessionFactory.getDocumentDaoFactory().addDao(new TestDocDao().setClient(cbSimulator));

        ExecutorServiceFactory execFactory=new ExecutorServiceFactory();
        ProcessingServiceWithRemoteCapabiltyFactory processFactory=new ProcessingServiceWithRemoteCapabiltyFactory();
        processFactory.setRemoteClientFactory(remoteClientFactory);

        processFactory.addJobProcessingService(TestJobCreateService.class);
        processFactory.addJobProcessingService(TestJobUpdateService.class);
        processFactory.addJobProcessingService(TestJobUpdateGenService.class);
        processFactory.addJobProcessingService(RemoteTestJobCreateService.class);
        processFactory.addJobProcessingService(RemoteTestJobUpdateService.class);
        processFactory.addJobProcessingService(RemoteTestJobUpdateGenService.class);
        server.registerBeanClass("testJobCreate",RestTestDocJobCreateService.class);
        server.registerBeanClass("testJobUpdate",RestTestDocJobUpdateService.class);
        server.registerBeanClass("testJobUpdateGen",RemoteTestDocJobUpdateGenService.class);
        server.registerBeanObject("couchbaseSessionFactory",sessionFactory);
        executorClientFactory = new ExecutorClientFactory(sessionFactory,execFactory,processFactory);
        server.registerBeanObject("stdJobExecutorFactory", executorClientFactory);
        server.start();
        Thread.sleep(100);
    }


    @Test
    public void runTest() throws Exception{
        IJobExecutorClient<RemoteCreateJob> jobClient = executorClientFactory.buildJobClient(RemoteCreateJob.class);
        RemoteCreateJob job = new RemoteCreateJob();
        job.initIntValue = 10;
        job.name = "testValu1";
        job.tempUid = UUID.randomUUID().toString();
        JobContext<RemoteCreateJob> context = jobClient.executeJob(job, AnonymousUser.INSTANCE);

        assertTrue(context.getJobState().isDone());
        String createdKey = context.getTasks(RemoteCreateJob.RemoteTestJobCreateTask.class).get(0).key;
        TestDoc createdDoc = cbSimulator.get(createdKey,TestDoc.class);
        assertEquals(job.initIntValue,createdDoc.intValue);
        assertEquals(job.name,createdDoc.name);

        {
            IJobExecutorClient<RemoteUpdateJob> updateJobClient = executorClientFactory.buildJobClient(RemoteUpdateJob.class);
            RemoteUpdateJob updateJob = new RemoteUpdateJob();
            updateJob.incrIntValue = 20;
            updateJob.key = createdKey;
            JobContext<RemoteUpdateJob> updateContext = updateJobClient.executeJob(updateJob, AnonymousUser.INSTANCE);
            assertTrue(updateContext.getJobState().isDone());
            TestDoc updatedDoc = cbSimulator.get(createdKey, TestDoc.class);
            assertEquals(job.initIntValue + updateJob.incrIntValue, (long) updatedDoc.intValue);
        }
        {
            IJobExecutorClient<RemoteUpdateGenJob> updateJobClient = executorClientFactory.buildJobClient(RemoteUpdateGenJob.class);
            RemoteUpdateGenJob updateJob = new RemoteUpdateGenJob();
            updateJob.descrIntValue = 3;
            updateJob.key = createdKey;
            JobContext<RemoteUpdateGenJob> updateContext = updateJobClient.submitJob(updateJob, AnonymousUser.INSTANCE);
            try{
                updateContext = updateJobClient.resumeJob(updateContext.getJob(), AnonymousUser.INSTANCE);
                fail();
            }catch (JobExecutionException e){
                //Ignore
            }
            updateContext = updateJobClient.resumeJob(updateContext.getJob(), AnonymousUser.INSTANCE);

            assertTrue(updateContext.getJobState().isDone());
            TestDoc updatedDoc = cbSimulator.get(createdKey, TestDoc.class);
            assertEquals(job.initIntValue + 20 - 3 , (long) updatedDoc.intValue);
        }
    }

    @AfterClass
    public static void clean() throws Throwable{
        if(curatorUtils!=null){
            curatorUtils.stop();
        }
        if(generatorResult!=null){
            generatorResult.cleanUp();
        }
        if(cbSimulator!=null && cbSimulator.isStarted()){
            cbSimulator.shutdown();
        }
    }
}