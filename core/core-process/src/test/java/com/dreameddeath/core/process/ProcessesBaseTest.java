/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.process;

import com.dreameddeath.core.couchbase.exception.DocumentNotFoundException;
import com.dreameddeath.core.dao.factory.DaoUtils;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.depinjection.IDependencyInjector;
import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.notification.bus.impl.EventBusImpl;
import com.dreameddeath.core.notification.dao.EventDao;
import com.dreameddeath.core.notification.dao.NotificationDao;
import com.dreameddeath.core.notification.discoverer.ListenerAutoSubscribe;
import com.dreameddeath.core.notification.discoverer.ListenerDiscoverer;
import com.dreameddeath.core.notification.listener.impl.AbstractNotificationProcessor;
import com.dreameddeath.core.notification.listener.impl.EventListenerFactory;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.EventLink;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.core.notification.model.v1.NotificationLink;
import com.dreameddeath.core.notification.utils.ListenerInfoManager;
import com.dreameddeath.core.process.dao.JobDao;
import com.dreameddeath.core.process.dao.TaskDao;
import com.dreameddeath.core.process.dao.TestChildDocDao;
import com.dreameddeath.core.process.dao.TestDocDao;
import com.dreameddeath.core.process.model.TestDoc;
import com.dreameddeath.core.process.model.TestDocEvent;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import com.dreameddeath.core.process.registrar.JobExecutorClientRegistrar;
import com.dreameddeath.core.process.registrar.TaskExecutorClientRegistrar;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.factory.impl.ExecutorClientFactory;
import com.dreameddeath.core.process.service.factory.impl.ExecutorServiceFactory;
import com.dreameddeath.core.process.service.factory.impl.ProcessingServiceFactory;
import com.dreameddeath.core.process.services.TestDocNotificationListener;
import com.dreameddeath.core.process.services.TestJobCreateService;
import com.dreameddeath.core.process.services.model.TestDocJobCreate;
import com.dreameddeath.core.process.testing.TestingJobExcecutorServiceImpl;
import com.dreameddeath.core.process.testing.TestingTaskExecutionServiceImpl;
import com.dreameddeath.core.session.impl.CouchbaseSessionFactory;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.testing.couchbase.CouchbaseBucketSimulator;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 01/11/2016.
 */
public class ProcessesBaseTest extends Assert {
    public static Logger LOG= LoggerFactory.getLogger(ProcessesBaseTest.class);
    public static final String BASE_PATH = "testEventsProcess";
    private static CuratorTestUtils curatorUtils;
    private static ExecutorClientFactory executorClientFactory;
    private static CouchbaseBucketSimulator cbSimulator;
    private static CuratorFramework curatorFramework;
    private static IEventBus bus;
    private static ListenerDiscoverer discoverer;
    private static CouchbaseSessionFactory sessionFactory;
    private static TestDocNotificationListener listener;
    private static ExecutorServiceFactory execFactory;
    private static ProcessingServiceFactory processFactory;

    @BeforeClass
    public static void initialise() throws Exception {
        curatorUtils = new CuratorTestUtils().prepare(1);

        cbSimulator = new CouchbaseBucketSimulator("test");
        cbSimulator.start();
        sessionFactory = new CouchbaseSessionFactory.Builder().build();
        DaoUtils.buildAndAddDaosForDomains(sessionFactory.getDocumentDaoFactory(),AbstractJob.class,JobDao.class,cbSimulator);
        DaoUtils.buildAndAddDaosForDomains(sessionFactory.getDocumentDaoFactory(),AbstractTask.class,TaskDao.class,cbSimulator);
        DaoUtils.buildAndAddDaosForDomains(sessionFactory.getDocumentDaoFactory(),Event.class,EventDao.class,cbSimulator);
        DaoUtils.buildAndAddDaosForDomains(sessionFactory.getDocumentDaoFactory(),Event.class,NotificationDao.class,cbSimulator);
        sessionFactory.getDocumentDaoFactory().addDao(new TestDocDao().setClient(cbSimulator));
        sessionFactory.getDocumentDaoFactory().addDao(new TestChildDocDao().setClient(cbSimulator));

        execFactory = new ExecutorServiceFactory();
        processFactory = new ProcessingServiceFactory();
        processFactory.addJobProcessingService(TestJobCreateService.class);

        curatorFramework=curatorUtils.getClient("testProcesses");
        JobExecutorClientRegistrar jobRegistrar=new JobExecutorClientRegistrar(curatorFramework,"D1","W1");
        TaskExecutorClientRegistrar taskRegistrar=new TaskExecutorClientRegistrar(curatorFramework,"D1","W1");

        bus = new EventBusImpl();
        listener = new TestDocNotificationListener();
        listener.setDefaultSessionUser(AnonymousUser.INSTANCE);
        listener.setSessionFactory(sessionFactory);
        bus.addListener(listener);
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
        //.setSessionFactory(sessionFactory)).setSessionFactory(sessionFactory)
        discoverer.addListener(new ListenerAutoSubscribe(bus,listenerFactory).setSessionFactory(sessionFactory));
        discoverer.start();

        bus.start();
        executorClientFactory = new ExecutorClientFactory(sessionFactory,execFactory,processFactory,bus,null,jobRegistrar,taskRegistrar);
    }

    @Test
    public void runTest() throws Exception {
        execFactory.addJobExecutorService(AbstractJob.class, TestingJobExcecutorServiceImpl.class);
        execFactory.addTaskExecutorService(AbstractTask.class, TestingTaskExecutionServiceImpl.class);

        IJobExecutorClient<TestDocJobCreate> jobClient = executorClientFactory.buildJobClient(TestDocJobCreate.class);
        TestDocJobCreate job = new TestDocJobCreate();
        job.initIntValue = 10;
        job.name = "testValu1";
        job.tempUid = UUID.randomUUID().toString();
        JobContext<TestDocJobCreate> context = jobClient.toBlocking().executeJob(job, AnonymousUser.INSTANCE);
        assertTrue(context.getJobState().isDone());
        TestDocJobCreate.TestJobCreateTask createJobTask = context.getTasks(TestDocJobCreate.TestJobCreateTask.class).get(0).getInternalTask();
        assertEquals(createJobTask.getDocKey(),context.getInternalJob().createdKey);
        TestDoc createdDoc = cbSimulator.toBlocking().get(createJobTask.getDocKey(), TestDoc.class);
        assertEquals(new Integer(job.initIntValue/*create*/*2/*update*/+job.initIntValue/*updateFromRead*/+job.initIntValue/*updateFromDuplicate*/), createdDoc.intValue);
        assertEquals(job.name, createdDoc.name);
        assertEquals(2,createJobTask.getNotifications().size());
        Thread.sleep(50);
        ICouchbaseSession session = sessionFactory.newSession(ICouchbaseSession.SessionType.READ_ONLY,"test", AnonymousUser.INSTANCE);
        for(TaskContext taskContext:context.getTaskContexts().toList().blockingGet()) {
            AbstractTask currTask = taskContext.getInternalTask();
            for (EventLink link : new ArrayList<>(currTask.getNotifications())) {
                try {
                    Event event = link.getBlockingEvent(session);
                    assertNotNull(event);
                    assertTrue(event instanceof TestDocEvent);
                    assertEquals("test/testdocevent/1.0.0", link.getEventType());
                    assertEquals(event.getId(), link.getUid());
                    assertEquals(1, event.getListeners().size());
                    assertEquals(TestDocNotificationListener.NAME, event.getListeners().get(0));
                    assertEquals(1, event.getNotifications().keySet().size());
                    assertEquals(TestDocNotificationListener.NAME, event.getNotifications().keySet().iterator().next());
                    NotificationLink notificationLink = event.getNotifications().get(TestDocNotificationListener.NAME);
                    assertNotNull(notificationLink);
                    final Notification notification = notificationLink.getBlockingNotification(session);
                    assertNotNull(notification);
                    assertEquals(Notification.Status.PROCESSED,notification.getStatus());
                    assertEquals(TestDocNotificationListener.NAME, notification.getListenerName());
                    TestDocNotificationListener.EventNotifInfo eventNotifInfo = new TestDocNotificationListener.EventNotifInfo(((TestDocEvent) event).sourceTask, event.getId().toString());
                    assertTrue(TestDocNotificationListener.mapCounter.containsKey(eventNotifInfo));
                } catch (AssertionError e) {
                    throw e;
                } catch (Throwable e) {
                    assertTrue(e instanceof DocumentNotFoundException);
                    assertEquals("core/createdocumenttaskevent/1.0.0", link.getEventType());
                }
            }
        }
    }

    @AfterClass
    public static void clean() throws Throwable{
        if(discoverer!=null){
            discoverer.stop();
        }
        bus.stop();
        if(curatorFramework!=null && curatorFramework.getState()== CuratorFrameworkState.STARTED){
            curatorFramework.close();
        }
        if(curatorUtils!=null){
            curatorUtils.stop();
        }
        if(cbSimulator!=null && cbSimulator.isStarted()){
            cbSimulator.shutdown();
        }
    }
}