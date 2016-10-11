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

package com.dreameddeath.core.notification.listener;

import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.Notification;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 29/05/2016.
 */
public interface IEventListener {
    String getName();
    String getType();
    String getVersion();

    <T extends Event> Observable<SubmissionResult> submit(Notification notif, T event);

    Observable<SubmissionResult> submit(String notifId);

    <T extends Event> boolean isApplicable(T event);
}
