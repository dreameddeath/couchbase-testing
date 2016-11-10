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

package com.dreameddeath.core.process.services;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.notification.listener.impl.AbstractLocalListener;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.core.process.model.TestDocEvent;
import rx.Observable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Christophe Jeunesse on 09/11/2016.
 */

public class TestDocNotificationListener extends AbstractLocalListener {
    public static final Map<String,AtomicInteger> mapCounter=new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return "testDocEvent";
    }

    @Override
    public String getType() {
        return "testDocEvent";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public <T extends Event> boolean isApplicable(T event) {
        return event instanceof TestDocEvent;
    }

    @Override
    protected <T extends Event> Observable<ProcessingResultInfo> doProcess(T event, Notification notification, ICouchbaseSession session){
        if(event instanceof TestDocEvent) {
            mapCounter.computeIfAbsent(((TestDocEvent) event).sourceTask,(key)->new AtomicInteger(0)).incrementAndGet();
        }
        return ProcessingResultInfo.buildObservable(notification,false,ProcessingResult.PROCESSED);
    }
}