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

package com.dreameddeath.infrastructure.plugin.notification;

import com.dreameddeath.core.config.ConfigManagerFactory;
import com.dreameddeath.core.curator.discovery.path.ICuratorPathDiscoveryListener;
import com.dreameddeath.core.dao.config.CouchbaseDaoConfigProperties;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.notification.EventTest;
import com.dreameddeath.core.notification.NotificationTestListener;
import com.dreameddeath.core.notification.bus.EventFireResult;
import com.dreameddeath.core.notification.bus.impl.EventBusImpl;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;
import com.dreameddeath.core.notification.registrar.ListenerRegistrar;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.core.user.StandardMockUserFactory;
import com.dreameddeath.infrastructure.common.CommonConfigProperties;
import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.lifecycle.IDaemonLifeCycle;
import com.dreameddeath.infrastructure.daemon.webserver.RestWebServer;
import com.dreameddeath.infrastructure.plugin.couchbase.CouchbaseDaemonPlugin;
import com.dreameddeath.infrastructure.plugin.couchbase.CouchbaseWebServerPlugin;
import com.dreameddeath.infrastructure.plugin.notification.config.InfrastructureNotificationPluginConfigProperties;
import com.dreameddeath.testing.couchbase.CouchbaseBucketFactorySimulator;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by Christophe Jeunesse on 22/08/2016.
 */
public class NotificationWebServerPluginTest extends Assert{
    private final static Logger LOG = LoggerFactory.getLogger(NotificationWebServerPlugin.class);
    private CuratorTestUtils testUtils;
    private CouchbaseBucketFactorySimulator couchbaseBucketFactory;

    @Before
    public void setup() throws Exception {
        testUtils = new CuratorTestUtils();
        testUtils.prepare(1);
        couchbaseBucketFactory = new CouchbaseBucketFactorySimulator();
    }

    @Test
    public void testDaemon() throws Exception {
        final AtomicInteger nbErrors = new AtomicInteger(0);
        String connectionString = testUtils.getCluster().getConnectString();

        ConfigManagerFactory.addPersistentConfigurationEntry(CommonConfigProperties.ZOOKEEPER_CLUSTER_ADDREES.getName(), connectionString);
        //ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("test", "testdoc").getName(), "testBucketName");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("test", "testdocprocess").getName(), "testBucketName");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("test", "notification").getName(), "testBucketName");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("test", "event").getName(), "testBucketName");
        final AbstractDaemon daemon = AbstractDaemon.builder()
                .withName("testing Daemon")
                .withUserFactory(new StandardMockUserFactory())
                .withPlugin(CouchbaseDaemonPlugin.builder().withBucketFactory(couchbaseBucketFactory))
                .build();

        daemon.addWebServer(RestWebServer.builder().withName("tests")
                //.withServiceDiscoveryManager(true)
                .withPlugin(CouchbaseWebServerPlugin.builder())
                .withPlugin(NotificationWebServerPlugin.builder())
                .withApplicationContextConfig("applicationContext.xml"));

        Thread stopping_thread = new Thread(() -> {
            ListenerRegistrar globalRegistrar = new ListenerRegistrar(daemon.getCuratorClient(), InfrastructureNotificationPluginConfigProperties.LISTENERS_PATH.get());
            try {
                NotificationWebServerPlugin plugin=daemon.getAdditionalWebServers().get(0).getPlugin(NotificationWebServerPlugin.class);
                CouchbaseWebServerPlugin cbPlugin=daemon.getAdditionalWebServers().get(0).getPlugin(CouchbaseWebServerPlugin.class);
                assertEquals(0L,((EventBusImpl)plugin.getEventBus()).getListeners().size());
                assertEquals(1L,plugin.getListenerDiscoverer().getListeners().size());
                ListenerDescription descriptionTestListener = new ListenerDescription();
                descriptionTestListener.setName("test");
                descriptionTestListener.setType("testListenerDiscovery");

                final CountDownLatch registarDone = new CountDownLatch(1);
                plugin.getListenerDiscoverer().addListener(new ICuratorPathDiscoveryListener<ListenerDescription>() {
                    @Override public void onRegister(String uid, ListenerDescription obj) {registarDone.countDown();}
                    @Override public void onUnregister(String uid, ListenerDescription oldObj) {}
                    @Override public void onUpdate(String uid, ListenerDescription oldObj, ListenerDescription newObj) {}
                });
                globalRegistrar.register(descriptionTestListener);
                assertTrue(registarDone.await(1, TimeUnit.MINUTES));
                Thread.sleep(100);
                assertEquals(1L,((EventBusImpl)plugin.getEventBus()).getListeners().size());
                //assertEquals(1L,plugin.getListenerDiscoverer().getListeners().size());
                ICouchbaseSession session=cbPlugin.getSessionFactory().newReadWriteSession("test",AnonymousUser.INSTANCE);
                final int nbEvent = 20;
                for (int i = 1; i <= nbEvent; ++i) {
                    EventTest test = new EventTest();
                    test.toAdd = i;
                    //test.setCorrelationId(test.toAdd.toString());
                    EventFireResult<EventTest> result = plugin.getEventBus().fireEvent(test, session);
                    assertTrue(result.isSuccess());
                }

                List<Notification> notificationList = new ArrayList<>();
                int nbReceived = 0;
                {
                    Notification resultNotif;

                    do {
                        resultNotif = NotificationTestListener.pollNotif();
                        if (resultNotif != null) {
                            nbReceived++;
                            notificationList.add(resultNotif);
                        }
                    } while (resultNotif != null && (nbReceived< (nbEvent)));
                }
                assertEquals(nbEvent,nbReceived);
                assertEquals(((nbEvent+1)*nbEvent/2), NotificationTestListener.totalCounter());

            } catch (Throwable e) {
                nbErrors.incrementAndGet();
                LOG.error("!!!!! ERROR !!!!!Error during status read", e);
            }
            try {
                daemon.getAdditionalWebServers().get(0).stop();
            }
            catch(Throwable e){
                nbErrors.incrementAndGet();
                LOG.error("!!!!! ERROR !!!!!Error during server stop", e);
            }

            try {
                daemon.getAdditionalWebServers().get(0).start();

                NotificationWebServerPlugin plugin=daemon.getAdditionalWebServers().get(0).getPlugin(NotificationWebServerPlugin.class);
                CouchbaseWebServerPlugin cbPlugin=daemon.getAdditionalWebServers().get(0).getPlugin(CouchbaseWebServerPlugin.class);
                assertEquals(1L,((EventBusImpl)plugin.getEventBus()).getListeners().size());
                ICouchbaseSession session=cbPlugin.getSessionFactory().newReadWriteSession("test",AnonymousUser.INSTANCE);
                final int nbEvent = 20;
                for (int i = 1; i <= nbEvent; ++i) {
                    EventTest test = new EventTest();
                    test.toAdd = i;
                    //test.setCorrelationId(test.toAdd.toString());
                    EventFireResult<EventTest> result = plugin.getEventBus().fireEvent(test, session);
                    assertTrue(result.isSuccess());
                }

                List<Notification> notificationList = new ArrayList<>();
                int nbReceived = 0;
                {
                    Notification resultNotif;

                    do {
                        resultNotif = NotificationTestListener.pollNotif();
                        if (resultNotif != null) {
                            nbReceived++;
                            notificationList.add(resultNotif);
                        }
                    } while (resultNotif != null && (nbReceived< (nbEvent)));
                }
                assertEquals(nbEvent,nbReceived);

                globalRegistrar.close();
                //assertTrue(unRegistar.await(1, TimeUnit.MINUTES));
                Thread.sleep(100);//Wait for all unregistrar
                assertEquals(1L,((EventBusImpl)plugin.getEventBus()).getListeners().size());
                //assertEquals(0L,((EventBusImpl)plugin.getEventBus()).getListeners().size());

            }
            catch(Throwable e){
                nbErrors.incrementAndGet();
                LOG.error("!!!!! ERROR !!!!!Error during server rerun", e);
            }


            try {
                daemon.getDaemonLifeCycle().stop();
            }
            catch(Throwable e){

            }
        });
        daemon.getDaemonLifeCycle().addLifeCycleListener(new IDaemonLifeCycle.DefaultListener(1000000) {
            @Override
            public void lifeCycleStarted(final IDaemonLifeCycle lifeCycle) {
                stopping_thread.start();
            }
        });
        daemon.startAndJoin();
        stopping_thread.join();
        assertEquals(0L, nbErrors.get());
    }

    @After
    public void close() throws Exception {
        if (testUtils != null) testUtils.stop();
    }
}