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

package com.dreameddeath.core.notification.model.v1;

import java.util.List;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 30/08/2017.
 */
public interface INotificationsHolder {
    List<EventListenerLink> getListeners();
    String getDomain();
    Long getSubmissionAttempt();
    Long incrSubmissionAttempt();
    boolean addListener(String newListener, String domain);
    Event.Status getStatus();
    void setStatus(Event.Status val);

    void setNotifications(Map<String, NotificationLink> noficationsLinksMap);
}
