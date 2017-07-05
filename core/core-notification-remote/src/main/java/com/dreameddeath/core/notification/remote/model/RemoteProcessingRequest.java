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

package com.dreameddeath.core.notification.remote.model;

import com.dreameddeath.core.notification.common.IEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by Christophe Jeunesse on 29/06/2017.
 */

public class RemoteProcessingRequest<T extends IEvent> {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,include=JsonTypeInfo.As.PROPERTY, property="@evtclass")
    @JsonProperty("event")
    private final T event;
    @JsonProperty("notificationKey")
    private final String notificationKey;

    @JsonCreator
    public RemoteProcessingRequest(@JsonProperty("event") T event,@JsonProperty("notificationKey") String notificationKey) {
        this.event = event;
        this.notificationKey = notificationKey;
    }

    @JsonGetter("event")
    public T getEvent() {
        return event;
    }


    @JsonGetter("notificationKey")
    public String getNotificationKey() {
        return notificationKey;
    }
}
