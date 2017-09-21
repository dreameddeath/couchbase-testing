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

package com.dreameddeath.core.notification.listener.impl;

import com.dreameddeath.core.notification.common.IEvent;
import com.dreameddeath.core.notification.listener.IEventListener;
import com.dreameddeath.core.notification.listener.SubmissionResult;
import com.dreameddeath.core.notification.model.v1.Notification;
import io.reactivex.Single;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 30/05/2016.
 */
public abstract class AbstractLocalListener  extends AbstractNotificationProcessor implements IEventListener{
    private Map<EventKey,Boolean> applicableEvents = new ConcurrentHashMap<>();

    @Override
    public <T extends IEvent> Single<SubmissionResult> submit(final Notification sourceNotif, final T event) {
        return processIfNeeded(sourceNotif,event);
    }

    @Override
    public <T extends IEvent> Single<SubmissionResult> submit(String domain, String notifId, T event) {
        return processIfNeeded(domain,notifId,event);
    }

    @Override
    public Single<SubmissionResult> submit(String domain,String notifKey) {
        return processIfNeeded(domain,notifKey);
    }

    @Override
    public final boolean isApplicable(String effectiveDomain, Class<? extends IEvent> eventClazz) {
        return applicableEvents.computeIfAbsent(new EventKey(effectiveDomain,eventClazz),
                key->getDomain().equals(effectiveDomain) && isApplicable(key.eventClazz)
                );
    }


    protected abstract boolean isApplicable(Class<? extends IEvent> clazz);

    private static class EventKey {
        private final String domain;
        private final Class<? extends IEvent> eventClazz;

        public EventKey(String domain, Class<? extends IEvent> eventClazz) {
            this.domain = domain;
            this.eventClazz = eventClazz;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EventKey eventKey = (EventKey) o;

            if (!domain.equals(eventKey.domain)) return false;
            return eventClazz.equals(eventKey.eventClazz);
        }

        @Override
        public int hashCode() {
            int result = domain.hashCode();
            result = 31 * result + eventClazz.hashCode();
            return result;
        }
    }
}
