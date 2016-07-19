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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Christophe Jeunesse on 01/07/2016.
 */
public class EventNotificationTest extends Assert{
    private final static Logger LOG = LoggerFactory.getLogger(EventNotificationTest.class);
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
        @DocumentProperty
        public Integer toAdd;
    }

    public static class NotificationTestListener extends AbstractLocalListener{
        private static final Logger LOG = LoggerFactory.getLogger(NotificationTestListener.class);
        private static final AtomicInteger nbEventProcessed= new AtomicInteger(0);
        private static final AtomicInteger totalCounter= new AtomicInteger(0);
        private static final BlockingQueue<Notification> processedNotification = new ArrayBlockingQueue<>(100);
        private static final Map<String,Integer> threadCounter=new ConcurrentHashMap<>();

        private final String name;

        public NotificationTestListener(String name) {
            this.name = name;
        }

        public Notification poll() throws InterruptedException{
            return processedNotification.poll(2,TimeUnit.SECONDS);
        }

        public int getTotalCounter() {
            return totalCounter.get();
        }

        public Map<String,Integer> getThreadCounter(){
            return Collections.unmodifiableMap(threadCounter);
        }

        @Override
        protected <T extends Event> Observable<Boolean> process(T event, Notification notification,ICouchbaseSession session) {
            //LOG.error("Received event {} on thread {}",((EventTest)event).toAdd,Thread.currentThread());
            nbEventProcessed.incrementAndGet();
            if(!threadCounter.containsKey(Thread.currentThread().getName())){
                threadCounter.put(Thread.currentThread().getName(),0);
            }
            threadCounter.put(Thread.currentThread().getName(),threadCounter.get(Thread.currentThread().getName())+1);
            totalCounter.addAndGet(((EventTest)event).toAdd);
            try {
                //Thread.sleep(100);
                processedNotification.offer(notification, 20, TimeUnit.SECONDS);
                return Observable.just(true);
            }
            catch(InterruptedException e){
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getName() {
            return this.name+this.getClass().getSimpleName();
        }

        @Override
        public <T extends Event> boolean isApplicable(T event) {
            return event instanceof EventTest;
        }
    }


    @Before
    public void setupBus(){
        bus = new EventBusImpl();

        testListener = new NotificationTestListener("singleThreaded");
        testListener.setSessionFactory(sessionFactory);
        bus.addListener(testListener);

        testListener = new NotificationTestListener("multiThreaded");
        testListener.setSessionFactory(sessionFactory);
        bus.addMultiThreadedListener(testListener);
        bus.start();
    }

    @Test
    public void eventBusTest() throws Exception{
        ICouchbaseSession session = sessionFactory.newReadWriteSession(AnonymousUser.INSTANCE);
        //int i=1;
        for(int i=1;i<=20;++i) {
            EventTest test = new EventTest();
            test.toAdd = i;
            EventFireResult<EventTest> result = bus.fireEvent(test, session);
            assertTrue(result.isSuccess());
        }

        int nbReceived = 0;
        Notification resultNotif;
        do{
            resultNotif = testListener.poll();
            if(resultNotif!=null) {
                nbReceived++;
            }
        }while(resultNotif!=null ||(nbReceived<40));

        assertTrue(testListener.getThreadCounter().keySet().size()>1);
        assertEquals(20*2,nbReceived);
        assertEquals(((20+1)*20/2)*2,testListener.getTotalCounter());
    }
}
