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

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.notification.bus.EventFireResult;
import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.notification.bus.impl.EventBusImpl;
import com.dreameddeath.core.notification.dao.EventDao;
import com.dreameddeath.core.notification.dao.NotificationDao;
import com.dreameddeath.core.notification.listener.impl.AbstractLocalListener;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.core.session.impl.CouchbaseSessionFactory;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.testing.couchbase.CouchbaseBucketSimulator;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import rx.Observable;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Christophe Jeunesse on 01/07/2016.
 */
public class EventNotificationTest extends Assert{
    private static CuratorTestUtils curatorUtils;
    private static CouchbaseBucketSimulator cbSimulator;
    private static CouchbaseSessionFactory sessionFactory;
    private IEventBus bus;
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


    @DocumentEntity(domain = "test",version="1.0")
    public static class EventTest extends Event{
        @DocumentProperty
        public String eventId;
    }

    public static class NotificationTestListener extends AbstractLocalListener{
        private AtomicInteger nbEventProcessed= new AtomicInteger(0);
        private SynchronousQueue<Notification> processedNotification = new SynchronousQueue<>();

        public Notification poll() throws InterruptedException{
            return processedNotification.poll(10,TimeUnit.SECONDS);
        }
        @Override
        protected <T extends Event> Observable<Boolean> process(T event, Notification notification,ICouchbaseSession session) {
            nbEventProcessed.incrementAndGet();
            try {
                processedNotification.offer(notification, 10, TimeUnit.SECONDS);
                return Observable.just(true);
            }
            catch(InterruptedException e){
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getName() {
            return "test"+this.getClass().getSimpleName();
        }

        @Override
        public <T extends Event> boolean isApplicable(T event) {
            return event instanceof EventTest;
        }
    }


    @Before
    public void setupBus(){
        bus = new EventBusImpl();
        testListener = new NotificationTestListener();
        testListener.setSessionFactory(sessionFactory);
        ((EventBusImpl)bus).addListener(testListener);
    }

    @Test
    public void eventBusTest() throws Exception{
        ICouchbaseSession session = sessionFactory.newReadWriteSession(AnonymousUser.INSTANCE);
        EventTest test = new EventTest();
        EventFireResult<EventTest> result = bus.fireEvent(test,session);
        assertTrue(result.isSuccess());
        Notification resultNotif = testListener.poll();
        assertNotNull(resultNotif);
    }
}
