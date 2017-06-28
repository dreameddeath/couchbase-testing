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

/**
 * Created by Christophe Jeunesse on 30/05/2016.
 */
public abstract class AbstractLocalListener  extends AbstractNotificationProcessor implements IEventListener{
    @Override
    public <T extends IEvent> Single<SubmissionResult> submit(final Notification sourceNotif, final T event) {
        return process(sourceNotif,event);
    }

    @Override
    public Single<SubmissionResult> submit(String domain,String notifKey) {
        return process(domain,notifKey);
    }
}
