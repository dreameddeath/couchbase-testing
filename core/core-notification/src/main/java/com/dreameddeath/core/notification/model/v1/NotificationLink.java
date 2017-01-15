/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.notification.model.v1;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.google.common.base.Preconditions;
import io.reactivex.Single;

/**
 * Created by ceaj8230 on 09/01/2017.
 */
public class NotificationLink extends CouchbaseDocumentElement {
    /**
     * key : The notification key
     */
    @DocumentProperty("key")
    private ImmutableProperty<String> key = new ImmutableProperty<>(NotificationLink.this);

    /**
     * Getter for property key
     * @return The current value
     */
    public String getKey(){
        return key.get();
    }

    /**
     * Setter for property key
     * @param newValue  the new value for the property
     */
    public void setKey(String newValue){
        key.set(newValue);
    }


    public <T extends Notification> Single<T> getNotification(ICouchbaseSession session){
        return session.asyncGet(key.get().toString(),(Class<T>)Notification.class);
    }

    public <T extends Notification> T getBlockingNotification(ICouchbaseSession session) throws StorageException,DaoException {
        return session.toBlocking().blockingGet(key.get().toString(),(Class<T>)Notification.class);
    }


    public NotificationLink(){

    }

    public NotificationLink(Notification notification){
        Preconditions.checkNotNull(notification.getBaseMeta().getKey(),"The notification %s should have a key");
        key.set(notification.getBaseMeta().getKey());
    }

    public NotificationLink(NotificationLink source){
        key.set(source.key.get());
    }

}
