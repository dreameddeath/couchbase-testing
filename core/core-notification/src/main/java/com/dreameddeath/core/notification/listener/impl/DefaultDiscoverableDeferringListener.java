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

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.notification.common.IEvent;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;
import io.reactivex.Single;

/**
 * Created by Christophe Jeunesse on 10/08/2016.
 */
public class DefaultDiscoverableDeferringListener extends AbstractDiscoverableListener {
    public DefaultDiscoverableDeferringListener(ListenerDescription description) {
        super(description);
    }

    @Override
    protected <T extends IEvent> Single<ProcessingResultInfo> doProcess(T event, Notification notification, ICouchbaseSession session) {
        return ProcessingResultInfo.buildSingle(notification,false,ProcessingResult.DEFERRED);
    }
}
