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

package com.dreameddeath.core.notification.model.v1;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import rx.Observable;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 07/11/2016.
 */
public class EventLink extends CouchbaseDocumentElement {
    /**
     *  uid : the event uid
     */
    @DocumentProperty("uid")
    private Property<UUID> uid = new ImmutableProperty<>(EventLink.this);
    /**
     *  eventType : Type
     */
    @DocumentProperty("eventType")
    private Property<String> eventType = new ImmutableProperty<>(EventLink.this);

    /**
     * Getter of eventType
     * @return the value of eventType
     */
    public String getEventType() { return eventType.get(); }
    /**
     * Setter of eventType
     * @param val the new value for eventType
     */
    public void setEventType(String val) { eventType.set(val); }


    /**
     * Getter of uid
     * @return the value of uid
     */
    public UUID getUid() { return uid.get(); }
    /**
     * Setter of uid
     * @param val the new value for uid
     */
    public void setUid(UUID val) { uid.set(val); }

    public <T extends Event> Observable<T> getEvent(ICouchbaseSession session){
        return session.asyncGetFromUID(uid.get().toString(),(Class<T>)Event.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventLink eventLink = (EventLink) o;

        return uid != null ? uid.equals(eventLink.uid) : eventLink.uid == null;

    }

    @Override
    public int hashCode() {
        return uid != null ? uid.hashCode() : 0;
    }

    public EventLink(Event event){
        DocumentEntity annot=event.getClass().getAnnotation(DocumentEntity.class);
        if(annot!=null){
            setEventType(EntityModelId.build(annot, event.getClass()).toString());
        }
        setUid(event.getId());
    }

    public EventLink(EventLink link){
        setEventType(link.eventType.get());
        setUid(link.uid.get());
    }

    public EventLink(){
    }
}
