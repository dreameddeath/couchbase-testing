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

import com.dreameddeath.core.notification.common.IEvent;
import com.dreameddeath.core.notification.listener.impl.AbstractNotificationProcessor;
import com.dreameddeath.core.notification.model.v1.Notification;
import io.reactivex.Single;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Christophe Jeunesse on 30/07/2017.
 */
public class TestNotificationQueue {
    private final static TestNotificationQueue instance= new TestNotificationQueue();

    private final AtomicInteger nbEventProcessed = new AtomicInteger(0);
    private final AtomicInteger totalCounter = new AtomicInteger(0);
    private final BlockingQueue<Notification> processedNotification = new ArrayBlockingQueue<>(100);
    private final Map<String, Integer> threadCounter = new ConcurrentHashMap<>();

    public void clear(){
        nbEventProcessed.set(0);
        totalCounter.set(0);
        processedNotification.clear();
        threadCounter.clear();
    }

    private TestNotificationQueue(){

    }

    public static TestNotificationQueue INSTANCE(){
        return instance;
    }

    public AtomicInteger getNbEventProcessed() {
        return nbEventProcessed;
    }
    public Map<String, Integer> getThreadCounter() {
        return Collections.unmodifiableMap(threadCounter);
    }

    public int getTotalCounter() {
        return totalCounter.get();
    }

    public Notification poll() throws InterruptedException {
        return poll(5, TimeUnit.SECONDS);
    }

    public Notification poll(long timeout, TimeUnit unit) throws InterruptedException {
        return processedNotification.poll(timeout, unit);
    }

    private void manageInternalEvent(Logger LOG, IEvent event, Notification notification){
        nbEventProcessed.incrementAndGet();
        if (!threadCounter.containsKey(Thread.currentThread().getName())) {
            threadCounter.put(Thread.currentThread().getName(), 0);
        }
        threadCounter.put(Thread.currentThread().getName(), threadCounter.get(Thread.currentThread().getName()) + 1);
        totalCounter.addAndGet(((TestEvent) event).toAdd);
        LOG.info("Offering event {} on thread {}",((TestEvent)event).toAdd,Thread.currentThread());
        try {
            processedNotification.offer(notification, 20, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Single<AbstractNotificationProcessor.ProcessingResultInfo> manageEvent(Logger LOG, IEvent event, Notification notification){
        manageInternalEvent(LOG, event,notification);
        return AbstractNotificationProcessor.ProcessingResultInfo.buildSingle(notification,false, AbstractNotificationProcessor.ProcessingResult.PROCESSED);
    }

    public Single<AbstractNotificationProcessor.ProcessingResult> manageEventWithFakeNotif(Logger LOG, IEvent event,Notification notification){
        manageInternalEvent(LOG,event,notification);
        return Single.just(AbstractNotificationProcessor.ProcessingResult.PROCESSED);
    }
}
