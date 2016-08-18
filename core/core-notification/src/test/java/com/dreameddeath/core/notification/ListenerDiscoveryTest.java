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

package com.dreameddeath.core.notification;

import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.notification.bus.IEventBusLifeCycleListener;
import com.dreameddeath.core.notification.bus.impl.EventBusImpl;
import com.dreameddeath.core.notification.dao.EventDao;
import com.dreameddeath.core.notification.dao.NotificationDao;
import com.dreameddeath.core.notification.discoverer.ListenerAutoSubscribe;
import com.dreameddeath.core.notification.discoverer.ListenerDiscoverer;
import com.dreameddeath.core.notification.listener.IEventListener;
import com.dreameddeath.core.notification.listener.impl.DiscoverableDefaultBlockingListener;
import com.dreameddeath.core.notification.listener.impl.EventListenerFactory;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;
import com.dreameddeath.core.notification.registrar.ListenerRegistrar;
import com.dreameddeath.core.session.impl.CouchbaseSessionFactory;
import com.dreameddeath.testing.couchbase.CouchbaseBucketSimulator;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
    private CuratorFramework client;
    private NotificationTestListener testListener;

    @BeforeClass
    public static void initialise() throws Exception{
        curatorUtils = new CuratorTestUtils().prepare(1);

        cbSimulator = new CouchbaseBucketSimulator("test");
        cbSimulator.start();
        sessionFactory = new CouchbaseSessionFactory.Builder().build();
        sessionFactory.getDocumentDaoFactory().addDao(new EventDao().setClient(cbSimulator));
        sessionFactory.getDocumentDaoFactory().addDao(new NotificationDao().setClient(cbSimulator));
        Thread.sleep(100);
    }


    @Before
    public void setupBus() throws Exception{
        //ConfigManagerFactory.getConfig(ConfigManagerFactory.PriorityDomain.LOCAL_OVERRIDE).setProperty(EVENTBUS_THREAD_POOL_SIZE.getName(),4);
        bus = new EventBusImpl();
        //EventListenerFactory factory = new EventListenerFactory();
        client = curatorUtils.getClient(NAME_SPACE_PREFIX);
        ListenerDiscoverer discoverer = new ListenerDiscoverer(client, BASE_PATH);
        discoverer.addListener(new ListenerAutoSubscribe(bus,new EventListenerFactory()));
        discoverer.start();
        //autoSubscribe.setListenerFactory(factory);

        /*testListener = new NotificationTestListener("singleThreaded");
        testListener.setSessionFactory(sessionFactory);
        bus.addListener(testListener);

        testListener = new NotificationTestListener("multiThreaded");
        testListener.setSessionFactory(sessionFactory);
        bus.addListener(testListener);*/
        //bus.addMultiThreaded(testListener);
        bus.start();
    }

    @Test
    public void testRegistrar() throws Exception{
        assertEquals(0L,((EventBusImpl)bus).getListeners().size());
        ListenerRegistrar registrar = new ListenerRegistrar(client,BASE_PATH);
        ListenerDescription description = new ListenerDescription();
        description.setName("test");
        description.setType("testListenerDiscovery");
        {
            CountDownLatch counter = new CountDownLatch(1);
            bus.addLifeCycleListener(new IEventBusLifeCycleListener() {
                @Override
                public void onStart() {
                }

                @Override
                public void onStop() {
                }

                @Override
                public void onAddListener(IEventListener listener) {
                    counter.countDown();
                }

                @Override
                public void onRemoveListener(IEventListener listener) {

                }
            });
            registrar.register(description);

            counter.await(10, TimeUnit.SECONDS);
        }
        assertEquals(1L,((EventBusImpl)bus).getListeners().size());
        assertTrue(((EventBusImpl)bus).getListeners().iterator().next() instanceof DiscoverableDefaultBlockingListener);

        {
            CountDownLatch counter = new CountDownLatch(1);
            bus.addLifeCycleListener(new IEventBusLifeCycleListener() {
                @Override
                public void onStart() {
                }

                @Override
                public void onStop() {
                }

                @Override
                public void onAddListener(IEventListener listener) {
                    counter.countDown();
                }

                @Override
                public void onRemoveListener(IEventListener listener) {

                }
            });
            registrar.deregister(description);

            counter.await(10, TimeUnit.SECONDS);
        }
        assertEquals(0L,((EventBusImpl)bus).getListeners().size());

    }


    @After
    public void closeBus(){
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
