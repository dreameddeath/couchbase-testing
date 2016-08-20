/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.notification.bus.impl;

import com.dreameddeath.core.notification.metrics.NotificationMetrics;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.Notification;

/**
 * Created by Christophe Jeunesse on 23/06/2016.
 */
public class InternalEvent {
    private Notification notification;
    private Event event;
    private NotificationMetrics.Context notificationMetricsContext;

    public void setProcessingElement(Event event, Notification notification, NotificationMetrics.Context metricContext) {
        this.notification = notification;
        this.event = event;
        this.notificationMetricsContext = metricContext;
    }

    public void cleanup(){
        this.notification = null;
        this.notificationMetricsContext=null;
    }


    public <T extends Event> T getEvent() {
        return (T)event;
    }

    public Notification getNotification() {
        return notification;
    }

    public NotificationMetrics.Context getNotificationMetricsContext() {
        return notificationMetricsContext;
    }
}
