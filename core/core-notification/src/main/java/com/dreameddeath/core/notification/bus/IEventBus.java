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

package com.dreameddeath.core.notification.bus;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.notification.listener.IEventListener;
import com.dreameddeath.core.notification.model.v1.Event;
import io.reactivex.Single;

/**
 * Created by Christophe Jeunesse on 29/05/2016.
 */
public interface IEventBus {
    <T extends Event> Single<EventFireResult<T>> asyncFireEvent(T event, ICouchbaseSession session);
    <T extends Event> Single<EventFireResult<T>> asyncFireEvent(Single<T> event, ICouchbaseSession session);
    <T extends Event> EventFireResult<T> fireEvent(T event,ICouchbaseSession session);

    void start();
    void stop();
    void addListener(IEventListener listener);
    void removeListener(IEventListener listener);
    void addLifeCycleListener(IEventBusLifeCycleListener listener);
}
