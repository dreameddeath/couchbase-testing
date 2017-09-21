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

package com.dreameddeath.core.notification.listener.impl.crossdomain;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.notification.bus.EventFireResult;
import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.notification.common.IEvent;
import com.dreameddeath.core.notification.listener.HasListenerDescription;
import com.dreameddeath.core.notification.listener.IEventListener;
import com.dreameddeath.core.notification.model.v1.CrossDomainBridge;
import io.reactivex.Single;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Christophe Jeunesse on 10/09/2017.
 */
public class LocalCrossDomainListener<T extends IEventListener & HasListenerDescription> extends AbstractCrossDomainListener<T> {
    private IEventBus eventBus;

    public LocalCrossDomainListener(String sourceDomain, T parentListener) {
        super(sourceDomain, parentListener);
    }

    @Autowired
    public void setEventBus(IEventBus eventBus) {
        this.eventBus = eventBus;
    }

    protected <T extends IEvent> Single<EventFireResult<T, CrossDomainBridge>> doProcessCrossDomainEvent(T event, ICouchbaseSession session) {
        return eventBus.fireCrossDomainEvent(getDomain(),event, session);
    }

}
