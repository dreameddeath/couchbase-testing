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

package com.dreameddeath.core.notification.listener;

import com.dreameddeath.core.notification.model.v1.EventListenerLink;
import com.dreameddeath.core.notification.model.v1.Notification;

/**
 * Created by Christophe Jeunesse on 29/05/2016.
 */
public class SubmissionResult {
    private Notification notification;
    private Throwable error;

    public SubmissionResult(Notification notif,boolean isProcessed){
        this(notif,null);
    }

    public SubmissionResult(Notification notification,Throwable e){
        this.notification = notification;
        this.error = e;
    }

    public EventListenerLink getListenerLink() {
        return notification.getListenerLink();
    }

    public boolean isSuccess() {
        return error==null && notification.getStatus()== Notification.Status.PROCESSED;
    }

    public boolean isDeferred() {
        return error==null && notification.getStatus()== Notification.Status.DEFERRED;
    }

    public String getNotificationKey(){
        return notification.getBaseMeta().getKey();
    }


    public boolean isFailure(){
        return error!=null;
    }

    public Throwable getError(){
        return error;
    }
}
