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

package com.dreameddeath.core.notification.remote;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.notification.listener.impl.AbstractDiscoverableListener;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;
import com.dreameddeath.core.service.client.rest.IRestServiceClient;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 05/10/2016.
 */
public class AbstractRemoteProducerListener extends AbstractDiscoverableListener {
    private IRestServiceClient remoteClient;

    public AbstractRemoteProducerListener(ListenerDescription description) {
        super(description);
    }

    @Override
    protected <T extends Event> Observable<ProcessingResult> doProcess(T event, Notification notification, ICouchbaseSession session) {
        return null;
    }
}
