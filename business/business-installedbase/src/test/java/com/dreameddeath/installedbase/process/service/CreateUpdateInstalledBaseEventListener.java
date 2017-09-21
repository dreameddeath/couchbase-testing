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

package com.dreameddeath.installedbase.process.service;


import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.notification.common.IEvent;
import com.dreameddeath.core.notification.listener.impl.AbstractLocalListener;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.installedbase.model.EntityConstants;
import com.dreameddeath.installedbase.model.notifications.v1.CreateUpdateInstalledBaseEvent;
import io.reactivex.Single;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Christophe Jeunesse on 10/11/2016.
 */
public class CreateUpdateInstalledBaseEventListener extends AbstractLocalListener {
    public static final AtomicInteger counter = new AtomicInteger(0);

    public CreateUpdateInstalledBaseEventListener(IEventBus bus) {
        bus.addListener(this);
    }

    @Override
    public String getDomain(){ return EntityConstants.INSTALLED_BASE_DOMAIN;}

    @Override
    public String getName() {
        return CreateUpdateInstalledBaseEventListener.class.getSimpleName();
    }

    @Override
    public String getType() {
        return CreateUpdateInstalledBaseEventListener.class.getSimpleName();
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean isApplicable(Class<? extends IEvent> event) {
        return CreateUpdateInstalledBaseEvent.class.isAssignableFrom(event);
    }

    @Override
    protected <T extends IEvent> Single<ProcessingResultInfo> doProcess(T event, Notification notification, ICouchbaseSession session) {
        counter.incrementAndGet();
        return ProcessingResultInfo.buildSingle(notification,false,ProcessingResult.PROCESSED);
    }
}
