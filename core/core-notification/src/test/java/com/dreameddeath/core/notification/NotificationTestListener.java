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
import com.dreameddeath.core.notification.annotation.Listener;
import com.dreameddeath.core.notification.listener.impl.AbstractLocalListener;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.google.common.base.Preconditions;
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
 * Created by Christophe Jeunesse on 17/08/2016.
 */
@Listener(forTypes = {"testListenerDiscovery"})
public class NotificationTestListener extends AbstractLocalListener {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationTestListener.class);
    private static final AtomicInteger nbEventProcessed = new AtomicInteger(0);
    private static final AtomicInteger totalCounter = new AtomicInteger(0);
    private static final BlockingQueue<Notification> processedNotification = new ArrayBlockingQueue<>(100);
    private static final Map<String, Integer> threadCounter = new ConcurrentHashMap<>();
    private final String name;

    public static void clear(){
        nbEventProcessed.set(0);
        totalCounter.set(0);
        processedNotification.clear();
        threadCounter.clear();
    }

    public NotificationTestListener(String name) {
        this.name = name;
    }

    public NotificationTestListener(String type,String name,Map<String,String> params){
        this(name);
        Preconditions.checkArgument(type.equals(getType()));
    }

    @Override
    public String getType() {
        return "testListenerDiscovery";
    }

    public static AtomicInteger getNbEventProcessed() {
        return nbEventProcessed;
    }

    public Notification poll() throws InterruptedException {
        return processedNotification.poll(2, TimeUnit.SECONDS);
    }

    public int getTotalCounter() {
        return totalCounter.get();
    }

    public Map<String, Integer> getThreadCounter() {
        return Collections.unmodifiableMap(threadCounter);
    }

    @Override
    protected <T extends Event> Observable<ProcessingResult> doProcess(T event, Notification notification, ICouchbaseSession session) {
        //LOG.error("Received event {} on thread {}",((EventTest)event).toAdd,Thread.currentThread());
        try {
            Thread.sleep(new Double(Math.random() * 100).longValue());
        } catch (InterruptedException e) {

        }
        nbEventProcessed.incrementAndGet();
        if (!threadCounter.containsKey(Thread.currentThread().getName())) {
            threadCounter.put(Thread.currentThread().getName(), 0);
        }
        threadCounter.put(Thread.currentThread().getName(), threadCounter.get(Thread.currentThread().getName()) + 1);
        totalCounter.addAndGet(((EventTest) event).toAdd);
        try {
            //Thread.sleep(100);
            processedNotification.offer(notification, 20, TimeUnit.SECONDS);
            return Observable.just(ProcessingResult.PROCESSED);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public <T extends Event> boolean isApplicable(T event) {
        return event instanceof EventTest;
    }
}