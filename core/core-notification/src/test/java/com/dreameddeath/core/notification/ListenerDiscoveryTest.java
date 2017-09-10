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

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.factory.DaoUtils;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.depinjection.IDependencyInjector;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.notification.bus.EventFireResult;
import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.notification.bus.IEventBusLifeCycleListener;
import com.dreameddeath.core.notification.bus.impl.EventBusImpl;
import com.dreameddeath.core.notification.dao.CrossDomainBridgeDao;
import com.dreameddeath.core.notification.dao.EventDao;
import com.dreameddeath.core.notification.dao.NotificationDao;
import com.dreameddeath.core.notification.discoverer.ListenerAutoSubscribe;
import com.dreameddeath.core.notification.discoverer.ListenerDiscoverer;
import com.dreameddeath.core.notification.listener.IEventListener;
import com.dreameddeath.core.notification.listener.impl.AbstractNotificationProcessor;
import com.dreameddeath.core.notification.listener.impl.DefaultDiscoverableDeferringListener;
import com.dreameddeath.core.notification.listener.impl.DiscoverableDefaultBlockingListener;
import com.dreameddeath.core.notification.listener.impl.factory.EventListenerFactory;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.EventListenerLink;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.core.notification.model.v1.listener.ListenedEvent;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;
import com.dreameddeath.core.notification.registrar.ListenerRegistrar;
import com.dreameddeath.core.notification.utils.ListenerInfoManager;
import com.dreameddeath.core.session.impl.CouchbaseSessionFactory;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.testing.couchbase.CouchbaseBucketSimulator;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.dreameddeath.core.notification.config.NotificationConfigProperties.EVENTBUS_THREAD_POOL_SIZE;

/**
 * Created by Christophe Jeunesse on 17/08/2016.
 */
public class ListenerDiscoveryTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(EventNotificationTest.class);
    public static final String NAME_SPACE_PREFIX = "test";
    public static final String BASE_PATH = "testEvents";
    private static CuratorTestUtils curatorUtils;
    private static CouchbaseBucketSimulator cbSimulator;

    private static CouchbaseSessionFactory sessionFactory;
    private IEventBus bus;
    private MetricRegistry metricRegistry;
    private CuratorFramework client;
    private ListenerDiscoverer discoverer;
    private volatile NotificationTestListener testListener;

    @BeforeClass
    public static void initialise() throws Exception{
        curatorUtils = new CuratorTestUtils().prepare(1);

        cbSimulator = new CouchbaseBucketSimulator("testDiscovery");
        cbSimulator.start();
        sessionFactory = new CouchbaseSessionFactory.Builder().build();
        DaoUtils.buildAndAddDaosForDomains(sessionFactory.getDocumentDaoFactory(),Event.class,EventDao.class,cbSimulator);
        DaoUtils.buildAndAddDaosForDomains(sessionFactory.getDocumentDaoFactory(),Event.class,NotificationDao.class,cbSimulator);
        DaoUtils.buildAndAddDaosForDomains(sessionFactory.getDocumentDaoFactory(),Event.class,CrossDomainBridgeDao.class,cbSimulator);
        Thread.sleep(100);
    }


    @Before
    public void setupBus() throws Exception{
        //ConfigManagerFactory.getConfig(ConfigManagerFactory.PriorityDomain.LOCAL_OVERRIDE).setProperty(EVENTBUS_THREAD_POOL_SIZE.getMethodParamName(),4);
        metricRegistry=new MetricRegistry();
        client = curatorUtils.getClient(NAME_SPACE_PREFIX);
        discoverer = new ListenerDiscoverer(client, BASE_PATH);
        //EventListenerFactory factory = new EventListenerFactory();
        bus = new EventBusImpl(metricRegistry);
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
                return bean;
            }
        });
        listenerFactory.registerFromManager();
        //.setSessionFactory(sessionFactory)).setSessionFactory(sessionFactory)
        discoverer.addListener(new ListenerAutoSubscribe(bus,listenerFactory).setSessionFactory(sessionFactory));
        discoverer.start();

        bus.start();
    }

    @Test
    public void testRegistrar() throws Exception{
        TestNotificationQueue.INSTANCE().clear();
        assertEquals(0L,((EventBusImpl)bus).getListeners().size());
        ListenerRegistrar registrar = new ListenerRegistrar(client,BASE_PATH);
        ListenedEvent listenedEvent = new ListenedEvent(CouchbaseDocumentReflection.getReflectionFromClass(TestEvent.class).getStructure().getEntityModelId());
        ListenerDescription descriptionBlocking = new ListenerDescription();
        descriptionBlocking.setDomain("test");
        descriptionBlocking.setName("testBlocking");
        descriptionBlocking.addListenedEvent(listenedEvent);
        descriptionBlocking.setType("blockingGeneric");

        ListenerDescription descriptionNonBlocking = new ListenerDescription();
        descriptionNonBlocking.setDomain("test");
        descriptionNonBlocking.setName("testNonBlocking");
        descriptionNonBlocking.setType("NonBlockingGeneric");
        descriptionNonBlocking.addListenedEvent(listenedEvent);
        descriptionNonBlocking.setAllowDeferred(true);

        ListenerDescription descriptionTestListener = new ListenerDescription();
        descriptionTestListener.setDomain("test");
        descriptionTestListener.setName("test");
        descriptionTestListener.setType("testListenerDiscovery");
        //Test Registrar blocking
        {
            final CountDownLatch counter = new CountDownLatch(1);
            final String expectedName = descriptionBlocking.getName();
            final String expectedType = descriptionBlocking.getType();
            bus.addLifeCycleListener(new IEventBusLifeCycleListener() {
                @Override public void onStart() {}
                @Override public void onStop() {}
                @Override public void onAddListener(IEventListener listener) {
                    if(counter.getCount()>0) {
                        assertTrue(listener instanceof DiscoverableDefaultBlockingListener);
                        assertEquals(expectedType,listener.getType());
                        assertEquals(expectedName,listener.getName());
                        counter.countDown();
                    }
                }
                @Override public void onRemoveListener(IEventListener listener) {}
            });
            registrar.register(descriptionBlocking);

            assertTrue(counter.await(10, TimeUnit.SECONDS));
            assertEquals(1L,((EventBusImpl)bus).getListeners().size());
            assertTrue(((EventBusImpl)bus).getListeners().get(0) instanceof DiscoverableDefaultBlockingListener);
        }


        //Test Registrar non blocking
        {
            final CountDownLatch counter = new CountDownLatch(1);
            final String expectedName = descriptionNonBlocking.getName();
            final String expectedType = descriptionNonBlocking.getType();
            bus.addLifeCycleListener(new IEventBusLifeCycleListener() {
                @Override public void onStart() {}
                @Override public void onStop() {}
                @Override public void onAddListener(IEventListener listener) {
                    if(counter.getCount()>0) {
                        assertTrue(listener instanceof DefaultDiscoverableDeferringListener);
                        assertEquals(expectedType,listener.getType());
                        assertEquals(expectedName,listener.getName());
                        counter.countDown();
                    }
                }
                @Override public void onRemoveListener(IEventListener listener) {}
            });
            registrar.register(descriptionNonBlocking);

            assertTrue(counter.await(10, TimeUnit.SECONDS));
            assertEquals(2L,((EventBusImpl)bus).getListeners().size());
            //assertTrue(((EventBusImpl)bus).getListeners().iterator().next().next() instanceof DiscoverableDefaultBlockingListener);
        }


        {
            final CountDownLatch counter = new CountDownLatch(1);
            final String expectedName = descriptionTestListener.getName();
            final String expectedType = descriptionTestListener.getType();
            bus.addLifeCycleListener(new IEventBusLifeCycleListener() {
                @Override public void onStart() {}
                @Override public void onStop() {}
                @Override public void onAddListener(IEventListener listener) {
                    if(counter.getCount()>0) {
                        assertTrue(listener instanceof NotificationTestListener);
                        assertEquals(expectedType,listener.getType());
                        assertEquals(expectedName,listener.getName());
                        counter.countDown();
                        testListener = (NotificationTestListener)listener;
                    }
                }
                @Override public void onRemoveListener(IEventListener listener) {}
            });
            registrar.register(descriptionTestListener);

            assertTrue(counter.await(10, TimeUnit.SECONDS));
            assertEquals(3L,((EventBusImpl)bus).getListeners().size());
            //assertTrue(((EventBusImpl)bus).getListeners().iterator().next().next() instanceof DiscoverableDefaultBlockingListener);
        }


        List<TestEvent> submittedEvents = new ArrayList<>();
        int nbEvent = EVENTBUS_THREAD_POOL_SIZE.get() * 5;
        {
            ICouchbaseSession session = sessionFactory.newReadWriteSession("test",AnonymousUser.INSTANCE);
            for (int i = 1; i <= nbEvent; ++i) {
                TestEvent test = new TestEvent();
                test.toAdd = i;
                //test.setCorrelationId(test.toAdd.toString());
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
                resultNotif = testListener.poll();
                if (resultNotif != null) {
                    nbReceived++;
                    notificationList.add(resultNotif);
                }
            } while (resultNotif != null && (nbReceived< (nbEvent)));
        }

        assertEquals(nbEvent,nbReceived);
        assertEquals(((nbEvent+1)*nbEvent/2),TestNotificationQueue.INSTANCE().getTotalCounter());
        Thread.sleep(50);//Wait for all updates
        {
            ICouchbaseSession checkSession = sessionFactory.newReadOnlySession("test",AnonymousUser.INSTANCE);
            for(TestEvent submittedEvent:submittedEvents){
                List<EventListenerLink> listeners = new ArrayList<>();
                listeners.addAll(submittedEvent.getListeners());
                int nbListeners = listeners.size();
                assertEquals(3L,nbListeners);
                for(int listenerPos=0;listenerPos<nbListeners;listenerPos++){
                    try {
                        Notification subNotification = checkSession.toBlocking().blockingGetFromKeyParams(Notification.class, submittedEvent.getId().toString(), listenerPos + 1);
                        listeners.removeIf(listener->listener.getName().equals(subNotification.getListenerLink().getName()));
                        assertEquals(1L, (long) subNotification.getNbAttempts());
                        switch (subNotification.getListenerLink().getName()) {
                            case "testNonBlocking":
                                assertEquals(Notification.Status.DEFERRED, subNotification.getStatus());
                                break;
                            case "test":
                                assertEquals(Notification.Status.PROCESSED, subNotification.getStatus());
                                break;
                            default:
                                fail("Unexpected name "+subNotification.getListenerLink().getName());
                        }
                    }
                    catch(StorageException e){

                    }
                }

                assertEquals(1,listeners.size());
                assertEquals("testBlocking",listeners.get(0).getName());
            }
        }
        //Test DeRegistrar
        {
            final CountDownLatch counter = new CountDownLatch(1);
            final String expectedName = descriptionBlocking.getName();
            final String expectedType = descriptionBlocking.getType();
            bus.addLifeCycleListener(new IEventBusLifeCycleListener() {
                @Override public void onStart() {}
                @Override public void onStop() {}
                @Override public void onAddListener(IEventListener listener) {}
                @Override public void onRemoveListener(IEventListener listener) {
                    if(counter.getCount()>0) {
                        assertTrue(listener instanceof DiscoverableDefaultBlockingListener);
                        assertEquals(expectedType,listener.getType());
                        assertEquals(expectedName,listener.getName());
                        counter.countDown();
                    }
                }
            });
            registrar.deregister(descriptionBlocking);
            assertTrue(counter.await(10, TimeUnit.SECONDS));
            assertEquals(2L,((EventBusImpl)bus).getListeners().size());
            Iterator<IEventListener> iterator = ((EventBusImpl)bus).getListeners().iterator();
            assertTrue(!(iterator.next() instanceof DiscoverableDefaultBlockingListener));
            assertTrue(!(iterator.next() instanceof DiscoverableDefaultBlockingListener));
        }

        registrar.close();
        ConsoleReporter.forRegistry(metricRegistry).build().report();
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
