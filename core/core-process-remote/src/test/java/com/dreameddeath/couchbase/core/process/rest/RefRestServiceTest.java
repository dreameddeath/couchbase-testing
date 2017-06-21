/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.couchbase.core.process.rest;

import com.dreameddeath.core.dao.factory.DaoUtils;
import com.dreameddeath.core.depinjection.IDependencyInjector;
import com.dreameddeath.core.model.dto.converter.DtoConverterFactory;
import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.notification.bus.impl.EventBusImpl;
import com.dreameddeath.core.notification.dao.EventDao;
import com.dreameddeath.core.notification.dao.NotificationDao;
import com.dreameddeath.core.notification.discoverer.ListenerAutoSubscribe;
import com.dreameddeath.core.notification.discoverer.ListenerDiscoverer;
import com.dreameddeath.core.notification.listener.impl.AbstractNotificationProcessor;
import com.dreameddeath.core.notification.listener.impl.EventListenerFactory;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.utils.ListenerInfoManager;
import com.dreameddeath.core.process.dao.JobDao;
import com.dreameddeath.core.process.dao.TaskDao;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
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
import com.dreameddeath.couchbase.core.process.remote.factory.BaseRemoteProcessClientFactory;
import com.dreameddeath.couchbase.core.process.remote.factory.ProcessingServiceWithRemoteCapabilityFactory;
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
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 04/01/2016.
 */
public class RefRestServiceTest extends Assert {
    private static final String BASE_PATH = "testRest";
    private static TestingRestServer server;

    private static AnnotationProcessorTestingWrapper.Result generatorResult;
    private static CuratorTestUtils curatorUtils;
    private static ExecutorClientFactory executorClientFactory;
    private static CouchbaseBucketSimulator cbSimulator;
    private static CuratorFramework curatorFramework;
    private static IEventBus bus;
    private static ListenerDiscoverer discoverer;


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
        curatorFramework = curatorUtils.getClient("TestServicesTest");

        server = new TestingRestServer("serverTesting", curatorFramework);
        BaseRemoteProcessClientFactory remoteClientFactory = new BaseRemoteProcessClientFactory();
        remoteClientFactory.setClientFactory(server.getClientFactory());

        cbSimulator = new CouchbaseBucketSimulator("test");
        cbSimulator.start();
        CouchbaseSessionFactory sessionFactory = new CouchbaseSessionFactory.Builder().build();
        DaoUtils.buildAndAddDaosForDomains(sessionFactory.getDocumentDaoFactory(),AbstractJob.class,JobDao.class,cbSimulator);
        DaoUtils.buildAndAddDaosForDomains(sessionFactory.getDocumentDaoFactory(),AbstractTask.class,TaskDao.class,cbSimulator);
        DaoUtils.buildAndAddDaosForDomains(sessionFactory.getDocumentDaoFactory(),Event.class,EventDao.class,cbSimulator);
        DaoUtils.buildAndAddDaosForDomains(sessionFactory.getDocumentDaoFactory(),Event.class,NotificationDao.class,cbSimulator);
        sessionFactory.getDocumentDaoFactory().addDao(new TestDocDao().setClient(cbSimulator));

        bus = new EventBusImpl();
        discoverer = new ListenerDiscoverer(curatorFramework, BASE_PATH);
        ListenerInfoManager manager = new ListenerInfoManager();
        EventListenerFactory listenerFactory = new EventListenerFactory();
        listenerFactory.setListenerInfoManager(manager);
        listenerFactory.setDependencyInjector(new IDependencyInjector() {
            @Override public <T> T getBeanOfType(Class<T> clazz) {return null;}
            @Override public <T> T autowireBean(T bean,String beanName) {
                if(bean instanceof AbstractNotificationProcessor){
                    ((AbstractNotificationProcessor)bean).setSessionFactory(sessionFactory);
                }
                return bean;
            }
        });
        listenerFactory.registerFromManager();
        discoverer.addListener(new ListenerAutoSubscribe(bus,listenerFactory).setSessionFactory(sessionFactory));
        discoverer.start();
        bus.start();


        ExecutorServiceFactory execFactory=new ExecutorServiceFactory();
        ProcessingServiceWithRemoteCapabilityFactory processFactory=new ProcessingServiceWithRemoteCapabilityFactory();
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
        server.registerBeanClass("DtoConverterFactory",DtoConverterFactory.class);
        server.registerBeanObject("couchbaseSessionFactory",sessionFactory);
        executorClientFactory = new ExecutorClientFactory(sessionFactory,execFactory,processFactory,bus);
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
        JobContext<RemoteCreateJob> context = jobClient.executeJob(job, AnonymousUser.INSTANCE).blockingGet();

        assertTrue(context.getJobState().isDone());
        String createdKey = context.getTasks(RemoteCreateJob.RemoteTestJobCreateTask.class).get(0).getInternalTask().key;
        TestDoc createdDoc = cbSimulator.toBlocking().get(createdKey,TestDoc.class);
        assertEquals(job.initIntValue,createdDoc.intValue);
        assertEquals(job.name,createdDoc.name);

        {
            IJobExecutorClient<RemoteUpdateJob> updateJobClient = executorClientFactory.buildJobClient(RemoteUpdateJob.class);
            RemoteUpdateJob updateJob = new RemoteUpdateJob();
            updateJob.incrIntValue = 20;
            updateJob.key = createdKey;
            JobContext<RemoteUpdateJob> updateContext = updateJobClient.executeJob(updateJob, AnonymousUser.INSTANCE).blockingGet();
            assertTrue(updateContext.getJobState().isDone());
            TestDoc updatedDoc = cbSimulator.toBlocking().get(createdKey, TestDoc.class);
            assertEquals(job.initIntValue + updateJob.incrIntValue, (long) updatedDoc.intValue);
        }
        {
            IJobExecutorClient<RemoteUpdateGenJob> updateJobClient = executorClientFactory.buildJobClient(RemoteUpdateGenJob.class);
            RemoteUpdateGenJob updateJob = new RemoteUpdateGenJob();
            updateJob.descrIntValue = 3;
            updateJob.key = createdKey;
            JobContext<RemoteUpdateGenJob> updateContext = updateJobClient.submitJob(updateJob, AnonymousUser.INSTANCE).blockingGet();
            try{
                updateContext = updateJobClient.toBlocking().resumeJob(updateContext.getInternalJob(), AnonymousUser.INSTANCE);
                fail();
            }catch (JobExecutionException e){
                //Ignore
            }
            updateContext = updateJobClient.resumeJob(updateContext.getInternalJob(), AnonymousUser.INSTANCE).blockingGet();

            assertTrue(updateContext.getJobState().isDone());
            TestDoc updatedDoc = cbSimulator.toBlocking().get(createdKey, TestDoc.class);
            assertEquals(job.initIntValue + 20 - 3 , (long) updatedDoc.intValue);
        }
    }

    @AfterClass
    public static void clean() throws Throwable{
        if(discoverer!=null){
            discoverer.stop();
        }
        if(bus!=null) {
            bus.stop();
        }
        if(curatorFramework!=null && curatorFramework.getState()== CuratorFrameworkState.STARTED){
            curatorFramework.close();
        }
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