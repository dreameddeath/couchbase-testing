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

package com.dreameddeath.core.notification;

import com.dreameddeath.core.dao.factory.DaoUtils;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.notification.bus.EventFireResult;
import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.notification.bus.impl.EventBusImpl;
import com.dreameddeath.core.notification.dao.EventDao;
import com.dreameddeath.core.notification.dao.NotificationDao;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.core.session.impl.CouchbaseSessionFactory;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.testing.couchbase.CouchbaseBucketSimulator;
import org.junit.*;
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
    private static CouchbaseBucketSimulator cbSimulator;
    private static CouchbaseSessionFactory sessionFactory;
    private IEventBus bus;
    private NotificationTestListener testListener;

    @BeforeClass
    public static void initialise() throws Exception{
        cbSimulator = new CouchbaseBucketSimulator("test");
        cbSimulator.start();
        sessionFactory = new CouchbaseSessionFactory.Builder().build();
        DaoUtils.buildAndAddDaosForDomains(sessionFactory.getDocumentDaoFactory(),Event.class,EventDao.class,cbSimulator);
        DaoUtils.buildAndAddDaosForDomains(sessionFactory.getDocumentDaoFactory(),Event.class,NotificationDao.class,cbSimulator);
        Thread.sleep(100);
    }


    @Before
    public void setupBus(){
        //ConfigManagerFactory.getConfig(ConfigManagerFactory.PriorityDomain.LOCAL_OVERRIDE).setProperty(EVENTBUS_THREAD_POOL_SIZE.getName(),4);
        bus = new EventBusImpl();

        testListener = new NotificationTestListener("singleThreaded");
        testListener.setSessionFactory(sessionFactory);
        bus.addListener(testListener);

        testListener = new NotificationTestListener("multiThreaded");
        testListener.setSessionFactory(sessionFactory);
        bus.addListener(testListener);
        //bus.addMultiThreaded(testListener);
        bus.start();
    }

    @Test
    public void eventBusTest() throws Exception{
        NotificationTestListener.clear();

        //test.setCorrelationId(test.toAdd.toString());
        {
            ICouchbaseSession session = sessionFactory.newReadWriteSession("test",AnonymousUser.INSTANCE);
            EventFireResult<NoListenerEventTest> result = bus.fireEvent(new NoListenerEventTest(), session);
            assertTrue(result.isSuccess());
            assertTrue(result.getResults().size()==0);
            assertTrue(result.getEvent().getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.NEW));
        }

        List<EventTest> submittedEvents = new ArrayList<>();
        int nbEvent = EVENTBUS_THREAD_POOL_SIZE.get() * 5;
        {
            ICouchbaseSession session = sessionFactory.newReadWriteSession("test",AnonymousUser.INSTANCE);
            for (int i = 1; i <= nbEvent; ++i) {
                EventTest test = new EventTest();
                test.toAdd = i;
                //test.setCorrelationId(test.toAdd.toString());
                EventFireResult<EventTest> result = bus.fireEvent(test, session);
                assertTrue(result.isSuccess());
                submittedEvents.add(result.getEvent());
            }
        }

        List<Notification> notificationList = new ArrayList<>();
        int nbReceived = 0;
        {
            Notification resultNotif;

            do {
                resultNotif = testListener.poll();
                if (resultNotif != null) {
                    nbReceived++;
                    notificationList.add(resultNotif);
                }
            } while (resultNotif != null && (nbReceived< (nbEvent*2)));
        }
        assertTrue(new Double(EVENTBUS_THREAD_POOL_SIZE.get()*((EventBusImpl)bus).getListeners().size()*0.95).longValue()<testListener.getThreadCounter().keySet().size());
        assertEquals(nbEvent*2,nbReceived);
        assertEquals(((nbEvent+1)*nbEvent/2)*2,testListener.getTotalCounter());
        Thread.sleep(50);//Wait for all updates
        {
            ICouchbaseSession checkSession = sessionFactory.newReadOnlySession("test",AnonymousUser.INSTANCE);
            for(EventTest submittedEvent:submittedEvents){
                List<String> listeners = new ArrayList<>();
                listeners.addAll(submittedEvent.getListeners());
                int nbListeners = listeners.size();
                for(int listenerPos=0;listenerPos<nbListeners;listenerPos++){
                    Notification subNotification = checkSession.toBlocking().blockingGetFromKeyParams(Notification.class,submittedEvent.getId().toString(),listenerPos+1);
                    listeners.remove(subNotification.getListenerName());
                    assertEquals(1L,(long)subNotification.getNbAttempts());
                    assertEquals(Notification.Status.PROCESSED,subNotification.getStatus());
                }

                assertEquals(0,listeners.size());
            }
            for (Notification srcNotif : notificationList) {
                Notification notif = checkSession.toBlocking().blockingGet(srcNotif.getBaseMeta().getKey(),Notification.class);
                assertEquals(Notification.Status.PROCESSED,notif.getStatus());
                EventTest eventTest = checkSession.toBlocking().blockingGetFromKeyParams(EventTest.class,notif.getEventId().toString());
                assertTrue(eventTest.getListeners().contains(notif.getListenerName()));
            }
        }

        /*
           Resubmission attempt
         */
        {
            ICouchbaseSession resubmitSession = sessionFactory.newReadWriteSession("test",AnonymousUser.INSTANCE);
            for(EventFireResult<EventTest> resumitResult: submittedEvents.stream().map(eventTest -> bus.fireEvent(eventTest,resubmitSession)).collect(Collectors.toList())) {
                assertTrue(resumitResult.isSuccess());
                assertEquals(2,(long)resumitResult.getEvent().getSubmissionAttempt());
            }
            Thread.sleep(100);

            Notification resultNotif;
            resultNotif = testListener.poll();
            if(resultNotif!=null) {
                fail();//should not occur
            }
            assertEquals(nbReceived,NotificationTestListener.getNbEventProcessed().get());

            ICouchbaseSession checkSession = sessionFactory.newReadOnlySession("test",AnonymousUser.INSTANCE);
            for (Notification srcNotif : notificationList) {
                Notification notif = checkSession.toBlocking().blockingGet(srcNotif.getBaseMeta().getKey(),Notification.class);
                assertEquals(Notification.Status.PROCESSED,notif.getStatus());
                assertEquals(1,(long)notif.getNbAttempts());//Simple re-submission, no new attempt
                EventTest eventTest = checkSession.toBlocking().blockingGetFromKeyParams(EventTest.class,notif.getEventId().toString());
                assertTrue(eventTest.getListeners().contains(notif.getListenerName()));
                assertEquals(2,(long)eventTest.getSubmissionAttempt());
            }
        }
    }


    @After
    public void after(){
        if(bus!=null){
            bus.stop();
        }
    }

    @AfterClass
    public static void afterClass() throws Exception{
        if(cbSimulator!=null){
            cbSimulator.shutdown();
        }
    }
}
