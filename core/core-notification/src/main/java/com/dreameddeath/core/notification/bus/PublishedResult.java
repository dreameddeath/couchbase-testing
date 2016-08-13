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

package com.dreameddeath.core.notification.bus;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.notification.model.v1.Notification;

/**
 * Created by Christophe Jeunesse on 29/05/2016.
 */
public class PublishedResult {
    private final Notification notification;
    private final String listnenerName;
    private final Throwable throwable;

    public PublishedResult(Notification notification) {
        this.notification=notification;
        this.listnenerName = notification.getListenerName();
        this.throwable=null;
    }

    public PublishedResult(String inputListnerName, Throwable throwable) {
        this.notification=null;
        this.listnenerName = inputListnerName;
        this.throwable = throwable;
    }

    public boolean isSuccess() {
        return notification!=null;
    }

    public boolean hasFailure(){
        return throwable!=null;
    }

    public Notification getNotification() {
        return notification;
    }

    public String getListnenerName() {
        return listnenerName;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public boolean isNotificationInDb(){
        return this.getNotification().getBaseMeta().getState()!= CouchbaseDocument.DocumentState.NEW;
    }
}
