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

package com.dreameddeath.core.notification;

import com.dreameddeath.core.dao.factory.DaoUtils;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.dto.converter.DtoConverterFactory;
import com.dreameddeath.core.model.v2.DocumentState;
import com.dreameddeath.core.notification.bus.EventFireResult;
import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.notification.bus.impl.EventBusImpl;
import com.dreameddeath.core.notification.dao.CrossDomainBridgeDao;
import com.dreameddeath.core.notification.dao.EventDao;
import com.dreameddeath.core.notification.dao.NotificationDao;
import com.dreameddeath.core.notification.listener.impl.crossdomain.LocalCrossDomainListener;
import com.dreameddeath.core.notification.model.v1.CrossDomainBridge;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.EventListenerLink;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.core.session.impl.CouchbaseSessionFactory;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.testing.couchbase.CouchbaseBucketSimulator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.dreameddeath.core.notification.config.NotificationConfigProperties.EVENTBUS_THREAD_POOL_SIZE;

/**
 * Created by Christophe Jeunesse on 01/07/2016.
 */
public class EventNotificationTest extends Assert{
    private final static Logger LOG = LoggerFactory.getLogger(EventNotificationTest.class);
    private CouchbaseBucketSimulator cbSimulator;
    private CouchbaseSessionFactory sessionFactory;
    //private IEventBus bus;
    //private NotificationTestListener testListener;


    @Before
    public void initialise() throws Exception{
        cbSimulator = new CouchbaseBucketSimulator("test");
        cbSimulator.start();
        sessionFactory = new CouchbaseSessionFactory.Builder().build();
        DaoUtils.buildAndAddDaosForDomains(sessionFactory.getDocumentDaoFactory(),Event.class,EventDao.class,cbSimulator);
        DaoUtils.buildAndAddDaosForDomains(sessionFactory.getDocumentDaoFactory(),Notification.class,NotificationDao.class,cbSimulator);
        DaoUtils.buildAndAddDaosForDomains(sessionFactory.getDocumentDaoFactory(),CrossDomainBridge.class,CrossDomainBridgeDao.class,cbSimulator);
        Thread.sleep(100);
    }

    @Test
    public void eventBusTest() throws Exception{
        final int nbListener = 4;

        IEventBus bus = new EventBusImpl();
        {
            NotificationTestListener testListener = new NotificationTestListener("singleThreaded");
            testListener.setSessionFactory(sessionFactory);
            bus.addListener(testListener);
        }
        {
            NotificationTestListener testListener = new NotificationTestListener("multiThreaded");
            testListener.setSessionFactory(sessionFactory);
            bus.addListener(testListener);
        }
        {
            AutoDiscoveryListener2Params autoDiscoveryListener2Params = new AutoDiscoveryListener2Params("2Params");
            autoDiscoveryListener2Params.setSessionFactory(sessionFactory);
            bus.addListener(autoDiscoveryListener2Params);
        }
        {
            AutoDiscoveryListener3Params autoDiscoveryListener3Params = new AutoDiscoveryListener3Params("3Params");
            autoDiscoveryListener3Params.setSessionFactory(sessionFactory);
            bus.addListener(autoDiscoveryListener3Params);
        }

        bus.start();
        TestNotificationQueue.INSTANCE().clear();

        {
            ICouchbaseSession session = sessionFactory.newReadWriteSession("test",AnonymousUser.INSTANCE);
            EventFireResult<NoListenerTestEvent,?> result = bus.blockingFireEvent(new NoListenerTestEvent(), session);
            assertTrue(result.isSuccess());
            assertTrue(result.getResults().size()==0);
            assertTrue(result.getEvent().getBaseMeta().getState().equals(DocumentState.NEW));
        }

        List<TestEvent> submittedEvents = new ArrayList<>();
        int nbEvent = EVENTBUS_THREAD_POOL_SIZE.get() * 5;
        {
            ICouchbaseSession session = sessionFactory.newReadWriteSession("test",AnonymousUser.INSTANCE);
            for (int i = 1; i <= nbEvent; ++i) {
                TestEvent test = new TestEvent();
                test.toAdd = i;
                EventFireResult<TestEvent,?> result = bus.blockingFireEvent(test, session);
                assertTrue(result.isSuccess());
                submittedEvents.add(result.getEvent());
            }
        }

        List<Notification> notificationList = new ArrayList<>();
        int nbReceived = 0;
        {
            Notification resultNotif;
            do {
                resultNotif = TestNotificationQueue.INSTANCE().poll();
                if (resultNotif != null) {
                    nbReceived++;
                    notificationList.add(resultNotif);
                }
            } while (resultNotif != null && (nbReceived< (nbEvent*nbListener)));
        }

        assertTrue(new Double(EVENTBUS_THREAD_POOL_SIZE.get()*((EventBusImpl)bus).getListeners().size()*0.95).longValue()<TestNotificationQueue.INSTANCE().getThreadCounter().keySet().size());
        assertEquals(nbEvent*nbListener,nbReceived);
        assertEquals(((nbEvent+1)*nbEvent/2)*nbListener,TestNotificationQueue.INSTANCE().getTotalCounter());
        Thread.sleep(50);//Wait for all updates
        {
            ICouchbaseSession checkSession = sessionFactory.newReadOnlySession("test",AnonymousUser.INSTANCE);
            for(TestEvent submittedEvent:submittedEvents){
                List<EventListenerLink> listeners = new ArrayList<>();
                listeners.addAll(submittedEvent.getListeners());
                int nbListeners = listeners.size();
                for(int listenerPos=0;listenerPos<nbListeners;listenerPos++){
                    Notification subNotification = checkSession.toBlocking().blockingGetFromKeyParams(Notification.class,submittedEvent.getId().toString(),listenerPos+1);
                    listeners.removeIf(listener->listener.getName().equals(subNotification.getListenerLink().getName()));
                    assertEquals(1L,(long)subNotification.getNbAttempts());
                    assertEquals(Notification.Status.PROCESSED,subNotification.getStatus());
                }

                assertEquals(0,listeners.size());
            }
            for (Notification srcNotif : notificationList) {
                if(srcNotif.getId()==null || srcNotif.getId()!=-1L) {
                    Notification notif = checkSession.toBlocking().blockingGet(srcNotif.getBaseMeta().getKey(), Notification.class);
                    assertEquals(Notification.Status.PROCESSED, notif.getStatus());
                    TestEvent eventTest = checkSession.toBlocking().blockingGetFromKeyParams(TestEvent.class, notif.getEventId().toString());
                    assertTrue(eventTest.getListeners().stream().anyMatch(listener->listener.getName().equals(notif.getListenerLink().getName())));
                }
            }
        }

        /*
           Resubmission attempt
         */
        {
            ICouchbaseSession resubmitSession = sessionFactory.newReadWriteSession("test",AnonymousUser.INSTANCE);
            for(EventFireResult<TestEvent,?> resumitResult: submittedEvents.stream().map(eventTest -> bus.blockingFireEvent(eventTest,resubmitSession)).collect(Collectors.toList())) {
                assertTrue(resumitResult.isSuccess());
                assertEquals(2,(long)resumitResult.getEvent().getSubmissionAttempt());
            }
            Thread.sleep(100);

            Notification resultNotif;
            resultNotif = TestNotificationQueue.INSTANCE().poll();
            if(resultNotif!=null) {
                fail();//should not occur
            }
            assertEquals(nbReceived,TestNotificationQueue.INSTANCE().getNbEventProcessed().get());

            ICouchbaseSession checkSession = sessionFactory.newReadOnlySession("test",AnonymousUser.INSTANCE);
            for (Notification srcNotif : notificationList) {
                if(srcNotif.getId()==null || srcNotif.getId()!=-1) {
                    Notification notif = checkSession.toBlocking().blockingGet(srcNotif.getBaseMeta().getKey(), Notification.class);
                    assertEquals(Notification.Status.PROCESSED, notif.getStatus());
                    assertEquals(1, (long) notif.getNbAttempts());//Simple re-submission, no new attempt
                    TestEvent eventTest = checkSession.toBlocking().blockingGetFromKeyParams(TestEvent.class, notif.getEventId().toString());
                    assertTrue(eventTest.getListeners().stream().anyMatch(listener->listener.getName().equals(notif.getListenerLink().getName())));
                    assertEquals(2, (long) eventTest.getSubmissionAttempt());
                }
            }
        }



        bus.stop();
    }


    @Test
    public void crossDomainListener() throws Exception{
        TestNotificationQueue.INSTANCE().clear();
        IEventBus bus = new EventBusImpl();
        DtoConverterFactory dtoConverterFactory = new DtoConverterFactory();
        NotificationCrossDomainListener crossDomainListener;

        {
            crossDomainListener = new NotificationCrossDomainListener("crossDomain");
            crossDomainListener.setSessionFactory(sessionFactory);
            bus.addListener(crossDomainListener);
            LocalCrossDomainListener<NotificationCrossDomainListener> localCrossDomainListenerBridge = new LocalCrossDomainListener<>("test",crossDomainListener);
            localCrossDomainListenerBridge.setEventBus(bus);
            localCrossDomainListenerBridge.setSessionFactory(sessionFactory);
            localCrossDomainListenerBridge.setDtoConverterFactory(dtoConverterFactory);
            bus.addListener(localCrossDomainListenerBridge);
        }
        bus.start();
        {
            ICouchbaseSession session = sessionFactory.newReadWriteSession("test",AnonymousUser.INSTANCE);
            EventFireResult<NoListenerTestEvent,?> result = bus.blockingFireEvent(new NoListenerTestEvent(), session);
            assertTrue(result.isSuccess());
            assertTrue(result.getResults().size()==0);
            assertTrue(result.getEvent().getBaseMeta().getState().equals(DocumentState.NEW));
        }

        List<AbstractTestEvent> submittedEvents = new ArrayList<>();
        int nbEvent = EVENTBUS_THREAD_POOL_SIZE.get() * 5;
        int nbCrossDomainEvents=0;
        {
            ICouchbaseSession testSession = sessionFactory.newReadWriteSession("test",AnonymousUser.INSTANCE);
            ICouchbaseSession test2Session = sessionFactory.newReadWriteSession("test2",AnonymousUser.INSTANCE);
            for (int i = 1; i <= nbEvent; ++i) {
                AbstractTestEvent test;
                switch (i%3){
                    case 2 : test = new TestEventSecondary();break;
                    case 0 : test = new TestEventCrossDomain();break;
                    default: test = new TestEvent();
                }
                test.toAdd= i;
                EventFireResult<AbstractTestEvent,?> result = bus.blockingFireEvent(test, test instanceof TestEventCrossDomain?test2Session:testSession);
                nbCrossDomainEvents+=result.getEvent().getDomain().equals(crossDomainListener.getDomain())?0:1;
                assertTrue(result.isSuccess());
                submittedEvents.add(result.getEvent());
            }
        }

        List<Notification> notificationList = new ArrayList<>();
        int nbReceived = 0;
        {
            Notification resultNotif;
            do {
                resultNotif = TestNotificationQueue.INSTANCE().poll();
                if (resultNotif != null) {
                    nbReceived++;
                    notificationList.add(resultNotif);
                }
            } while (resultNotif != null && (nbReceived<nbEvent));
        }

        assertEquals(nbEvent,nbReceived);
        assertEquals(((nbEvent+1)*nbEvent/2),TestNotificationQueue.INSTANCE().getTotalCounter());
        Thread.sleep(50);//Wait for all updates
        {
            ICouchbaseSession checkTestSession = sessionFactory.newReadWriteSession("test",AnonymousUser.INSTANCE);
            ICouchbaseSession checkTest2Session = sessionFactory.newReadWriteSession("test2",AnonymousUser.INSTANCE);

            for(AbstractTestEvent submittedEvent:submittedEvents){
                ICouchbaseSession checkSession = submittedEvent.getDomain().equals("test")?checkTestSession:checkTest2Session;
                assertEquals(submittedEvent.getListeners().size(),submittedEvent.getNotifications().keySet().size());
                List<EventListenerLink> listeners = new ArrayList<>();
                listeners.addAll(submittedEvent.getListeners());
                int nbListeners = listeners.size();

                for(int listenerPos=0;listenerPos<nbListeners;listenerPos++){
                    EventListenerLink listenerLink = submittedEvent.getListeners().get(listenerPos);
                    Notification subNotification;
                    //Cross domain notif
                    if(!listenerLink.getDomain().equals(crossDomainListener.getDomain())){
                        ICouchbaseSession targetCheckSession = crossDomainListener.getDomain().equals("test")?checkTestSession:checkTest2Session;
                        CrossDomainBridge crossDomainBridge = targetCheckSession.toBlocking().blockingGetFromKeyParams(CrossDomainBridge.class, submittedEvent.getId().toString(), crossDomainListener.getDomain());
                        assertEquals(crossDomainListener.getDomain(),crossDomainBridge.getEffectiveDomain());//The crossDomainBridge object must belong to the target domain
                        listeners.removeIf(listener->listener.getDomain().equals(crossDomainBridge.getEventDomain()));
                        assertEquals(crossDomainBridge.getListeners().size(),crossDomainBridge.getNotifications().keySet().size());
                        assertEquals(Event.Status.NOTIFICATIONS_IN_DB,crossDomainBridge.getStatus());
                        assertEquals(1L,(long)crossDomainBridge.getSubmissionAttempt());
                        for(EventListenerLink targetListenerLink:crossDomainBridge.getListeners()) {
                            subNotification = crossDomainBridge.getNotifications().get(targetListenerLink.getName()).getBlockingNotification(targetCheckSession);
                            assertEquals(1L,(long)subNotification.getNbAttempts());
                            assertEquals(Notification.Status.PROCESSED,subNotification.getStatus());
                        }
                    }
                    else {
                        subNotification = checkSession.toBlocking().blockingGetFromKeyParams(Notification.class, submittedEvent.getId().toString(), listenerPos + 1);
                        assertEquals(1L,(long)subNotification.getNbAttempts());
                        assertEquals(Notification.Status.PROCESSED,subNotification.getStatus());
                        listeners.removeIf(listener->listener.getName().equals(listenerLink.getName()));
                    }
                }

                assertEquals(0,listeners.size());
            }
            for (Notification srcNotif : notificationList) {
                if(srcNotif.getId()==null || srcNotif.getId()!=-1L) {
                    ICouchbaseSession eventCheckSession = srcNotif.getDomain().equals("test")?checkTestSession:checkTest2Session;
                    ICouchbaseSession notifCheckSession = srcNotif.getListenerLink().getDomain().equals("test")?checkTestSession:checkTest2Session;
                    Notification notif = notifCheckSession.toBlocking().blockingGet(srcNotif.getBaseMeta().getKey(), Notification.class);
                    assertEquals(Notification.Status.PROCESSED, notif.getStatus());
                    AbstractTestEvent eventTest = eventCheckSession.toBlocking().blockingGetFromKeyParams(AbstractTestEvent.class, notif.getEventId().toString());

                    if(eventTest.getDomain().equals(crossDomainListener.getDomain())) {
                        assertTrue(eventTest.getListeners().stream().anyMatch(listener -> listener.getName().equals(notif.getListenerLink().getName())));
                    }
                    else{
                        CrossDomainBridge crossDomainBridge = eventCheckSession.toBlocking().blockingGetFromKeyParams(CrossDomainBridge.class, notif.getEventId().toString(),srcNotif.getDomain());
                        assertEquals(crossDomainListener.getDomain(),crossDomainBridge.getEffectiveDomain());//The cross domain bridge belongs to the target domain
                        assertTrue(crossDomainBridge.getListeners().stream().anyMatch(listener -> listener.getName().equals(notif.getListenerLink().getName())));
                        assertTrue(eventTest.getListeners().stream().anyMatch(listener -> listener.getName().equals(LocalCrossDomainListener.buildCrossDomainListenerName(eventTest.getDomain(),notif.getListenerLink().getDomain()))));
                    }
                }
            }
        }


        bus.stop();
    }


    @After
    public void afterClass() throws Exception{
        if(cbSimulator!=null){
            cbSimulator.shutdown();
        }
    }
}
