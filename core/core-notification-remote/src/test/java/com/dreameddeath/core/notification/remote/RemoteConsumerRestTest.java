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

package com.dreameddeath.core.notification.remote;

import com.codahale.metrics.MetricRegistry;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.depinjection.IDependencyInjector;
import com.dreameddeath.core.notification.EventTest;
import com.dreameddeath.core.notification.NotificationTestListener;
import com.dreameddeath.core.notification.bus.EventFireResult;
import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.notification.bus.impl.EventBusImpl;
import com.dreameddeath.core.notification.dao.EventDao;
import com.dreameddeath.core.notification.dao.NotificationDao;
import com.dreameddeath.core.notification.discoverer.ListenerAutoSubscribe;
import com.dreameddeath.core.notification.discoverer.ListenerDiscoverer;
import com.dreameddeath.core.notification.listener.impl.AbstractNotificationProcessor;
import com.dreameddeath.core.notification.listener.impl.EventListenerFactory;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.core.notification.registrar.ListenerRegistrar;
import com.dreameddeath.core.notification.utils.ListenerInfoManager;
import com.dreameddeath.core.service.testing.TestingRestServer;
import com.dreameddeath.core.session.impl.CouchbaseSessionFactory;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.testing.couchbase.CouchbaseBucketSimulator;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;

import static com.dreameddeath.core.notification.config.NotificationConfigProperties.EVENTBUS_THREAD_POOL_SIZE;


/**
 * Created by Christophe Jeunesse on 14/10/2016.
 */
public class RemoteConsumerRestTest extends Assert{
    private static TestingRestServer server;
    public static final String NAME_SPACE_PREFIX = "test";
    public static final String BASE_PATH = "testEvents";

    private static CuratorTestUtils curatorUtils;
    private static CouchbaseBucketSimulator cbSimulator;
    private static CouchbaseSessionFactory sessionFactory;
    private static CuratorFramework client;
    private static NotificationTestListener testListener;

    private IEventBus bus;
    private MetricRegistry metricRegistry;
    private ListenerDiscoverer discoverer;

    @BeforeClass
    public static void initialise() throws Exception{
        curatorUtils = new CuratorTestUtils().prepare(1);
        client = curatorUtils.getClient(NAME_SPACE_PREFIX);
        server = new TestingRestServer("serverTesting",client );

        cbSimulator = new CouchbaseBucketSimulator("test");
        cbSimulator.start();
        sessionFactory = new CouchbaseSessionFactory.Builder().build();
        sessionFactory.getDocumentDaoFactory().addDao(new EventDao().setClient(cbSimulator));
        sessionFactory.getDocumentDaoFactory().addDao(new NotificationDao().setClient(cbSimulator));
        NotificationTestConsumerRest consumerRest=new NotificationTestConsumerRest();
        testListener= new NotificationTestListener("testingNotification");
        testListener.setSessionFactory(sessionFactory);
        consumerRest.setEventListener(testListener);
        consumerRest.setListenerRegistrar(new ListenerRegistrar(client,BASE_PATH));
        consumerRest.setServiceRegistrar(server.getServiceRegistrar());
        server.registerBeanObject("restConsumer", consumerRest);

        server.start();
        Thread.sleep(100);
    }

    @Before
    public void setupBus() throws Exception{
        //ConfigManagerFactory.getConfig(ConfigManagerFactory.PriorityDomain.LOCAL_OVERRIDE).setProperty(EVENTBUS_THREAD_POOL_SIZE.getName(),4);
        metricRegistry=new MetricRegistry();
        bus = new EventBusImpl(metricRegistry);
        //EventListenerFactory factory = new EventListenerFactory();
        //client = curatorUtils.getClient(NAME_SPACE_PREFIX);
        discoverer = new ListenerDiscoverer(client, BASE_PATH);
        ListenerInfoManager manager = new ListenerInfoManager();
        EventListenerFactory listenerFactory = new EventListenerFactory();
        listenerFactory.setListenerInfoManager(manager);
        listenerFactory.setDependencyInjector(new IDependencyInjector() {
            @Override
            public <T> T getBeanOfType(Class<T> clazz) {return null;}

            @Override
            public <T> T autowireBean(T bean,String beanName) {
                if(bean instanceof AbstractNotificationProcessor){
                    ((AbstractNotificationProcessor)bean).setSessionFactory(sessionFactory);
                }
                if(bean instanceof RemoteProducerListener){
                    ((RemoteProducerListener)bean).setClientFactory((domain, name, version) -> server.getClientFactory().getClient(name,version));
                    ((RemoteProducerListener)bean).init();
                }
                return bean;
            }
        });
        listenerFactory.registerFromManager();
        discoverer.addListener(new ListenerAutoSubscribe(bus,listenerFactory).setSessionFactory(sessionFactory));

        discoverer.start();
        bus.start();
    }

    @Test
    public void testRemote() throws Exception{
        NotificationTestListener.clear();
        List<EventTest> submittedEvents = new ArrayList<>();
        int nbEvent = EVENTBUS_THREAD_POOL_SIZE.get() * 5;
        {
            ICouchbaseSession session = sessionFactory.newReadWriteSession(AnonymousUser.INSTANCE);
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
        //assertTrue(new Double(EVENTBUS_THREAD_POOL_SIZE.get()*((EventBusImpl)bus).getListeners().size()*0.95).longValue()<testListener.getThreadCounter().keySet().size());
        assertEquals(nbEvent,nbReceived);
        assertEquals((nbEvent+1)*nbEvent/2,testListener.getTotalCounter());
        Thread.sleep(50);//Wait for all updates
        {
            ICouchbaseSession checkSession = sessionFactory.newReadOnlySession(AnonymousUser.INSTANCE);
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
    }


    @After
    public void closeBus() throws Exception{
        if(discoverer!=null){
            discoverer.stop();
        }
        bus.stop();
        if(client!=null && client.getState()== CuratorFrameworkState.STARTED){
            client.close();
        }
    }

    @AfterClass
    public static void clear() throws Exception{
        if(cbSimulator!=null)cbSimulator.shutdown();
        if(curatorUtils!=null)curatorUtils.stop();
    }
}