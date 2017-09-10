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

package com.dreameddeath.core.process.services;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.notification.common.IEvent;
import com.dreameddeath.core.notification.listener.impl.AbstractLocalListener;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.core.process.model.TestDocEvent;
import io.reactivex.Single;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Christophe Jeunesse on 09/11/2016.
 */

public class TestDocNotificationListener extends AbstractLocalListener {
    public static final String DOMAIN="test";
    public static final String NAME="testDocEvent";
    public static final Map<EventNotifInfo,AtomicInteger> mapCounter=new ConcurrentHashMap<>();

    @Override
    public String getDomain() {
        return DOMAIN;
    }


    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getType() {
        return NAME;
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public <T extends IEvent> boolean isApplicable(String domain,T event) {
        return domain.equals(getDomain()) && event instanceof TestDocEvent;
    }

    @Override
    protected <T extends IEvent> Single<ProcessingResultInfo> doProcess(T event, Notification notification, ICouchbaseSession session){
        if(event instanceof TestDocEvent) {
            synchronized (mapCounter) {
                int nbCalls = mapCounter.
                        computeIfAbsent(new EventNotifInfo(((TestDocEvent) event).sourceTask, event.getId().toString()),
                                (key) -> new AtomicInteger(0)
                        ).incrementAndGet();
                return ProcessingResultInfo.buildSingle(notification,false,ProcessingResult.PROCESSED);
            }
        }
        return ProcessingResultInfo.buildSingle(notification,false,ProcessingResult.PROCESSED);
    }

    public static class EventNotifInfo{
        private final String source;
        private final String id;

        public EventNotifInfo(String source, String id) {
            this.source = source;
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EventNotifInfo that = (EventNotifInfo) o;

            if (!source.equals(that.source)) return false;
            return id.equals(that.id);
        }

        @Override
        public int hashCode() {
            int result = source.hashCode();
            result = 31 * result + id.hashCode();
            return result;
        }
    }
}
