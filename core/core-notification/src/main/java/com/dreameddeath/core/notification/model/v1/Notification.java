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

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.annotation.HasEffectiveDomain;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.entity.model.IVersionedEntity;
import com.dreameddeath.core.model.property.NumericProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardLongProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentTypeIdResolver;
import com.dreameddeath.core.validation.annotation.Unique;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import java.util.UUID;

import static com.dreameddeath.core.notification.dao.NotificationDao.NOTIFICATION_UID_NAMESPACE;

/**
 * Created by Christophe Jeunesse on 25/05/2016.
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.CUSTOM, include= JsonTypeInfo.As.PROPERTY, property="@t",visible = true)
@JsonTypeIdResolver(CouchbaseDocumentTypeIdResolver.class)
@DocumentEntity
public final class Notification extends CouchbaseDocument implements IVersionedEntity, HasEffectiveDomain{
    private EntityModelId fullEntityId;
    @JsonSetter("@t") @Override
    public final void setDocumentFullVersionId(String typeId){
        fullEntityId = EntityModelId.build(typeId);
    }
    @Override
    public final String getDocumentFullVersionId(){
        return fullEntityId!=null?fullEntityId.toString():null;
    }
    @Override
    public final EntityModelId getModelId(){
        return fullEntityId;
    }

    @Override
    public String getEffectiveDomain() {
        return getDomain();
    }

    /**
     *  id : the notification id within the event
     */
    @DocumentProperty("id")
    transient private Property<Long> id = new ImmutableProperty<>(Notification.this);
    /**
     *  eventId : the event id being notified
     */
    @DocumentProperty("eventId") @Unique(nameSpace = NOTIFICATION_UID_NAMESPACE,additionnalFields = {"listenerLink.name"})
    transient private Property<UUID> eventId = new ImmutableProperty<>(Notification.this);
    /**
     * domain : the event parent domain
     */
    @DocumentProperty("domain")
    private ImmutableProperty<String> domain = new ImmutableProperty<>(Notification.this);
    /**
     * listenerLink : The info of the target listener
     */
    @DocumentProperty("listenerLink")
    private Property<EventListenerLink> listenerLink = new ImmutableProperty<>(Notification.this);
    /**
     *  status : status of the notification
     */
    @DocumentProperty("status")
    private Property<Status> status = new StandardProperty<>(Notification.this,Status.INITIALIZED);
    /**
     *  lastError : last error generated
     */
    @DocumentProperty("lastError")
    private Property<String> lastError = new StandardProperty<>(Notification.this);
    /**
     *  nbAttempts : number of processing attempts
     */
    @DocumentProperty("nbAttempts")
    private NumericProperty<Long> nbAttempts = new StandardLongProperty(Notification.this,0);
    /**
     *  nbRemoteAttempts : number of remote processing attempts
     */
    @DocumentProperty("nbRemoteAttempts")
    private NumericProperty<Long> nbRemoteAttempts = new StandardLongProperty(Notification.this,0);


    /**
     * Getter of id
     * @return the value of id
     */
    public Long getId() { return id.get(); }
    /**
     * Setter of id
     * @param val the new value for id
     */
    public void setId(Long val) { id.set(val); }
    /**
     * Getter of eventId
     * @return the value of eventId
     */
    public UUID getEventId() { return eventId.get(); }
    /**
     * Setter of eventId
     * @param val the new value for eventId
     */
    public void setEventId(UUID val) { eventId.set(val); }
    /**
     * Getter for property domain
     * @return The current value
     */
    public String getDomain(){
        return domain.get();
    }

    /**
     * Setter for property domain
     * @param newValue  the new value for the property
     */
    public void setDomain(String newValue){
        domain.set(newValue);
    }
    /**
     * Getter of the attribute listenerLink
     * return the currentValue of listenerLink
     */
    public EventListenerLink getListenerLink(){
        return this.listenerLink.get();
    }

    /**
     * Setter of the attribute listenerLink
     * @param newValue the newValue of listenerLink
     */
    public void setListenerLink(EventListenerLink newValue){
        this.listenerLink.set(newValue);
    }

    /**
     * Getter of status
     * @return the value of status
     */
    public Status getStatus() { return status.get(); }
    /**
     * Setter of status
     * @param val the new value for status
     */
    public void setStatus(Status val) { status.set(val); }
    /**
     * Getter of lastError
     * @return the value of lastError
     */
    public String getLastError() { return lastError.get(); }
    /**
     * Setter of lastError
     * @param val the new value of lastError
     */
    public void setLastError(String val) { lastError.set(val); }
    /**
     * Getter of nbAttempts
     * @return the value of nbAttempts
     */
    public Long getNbAttempts() { return nbAttempts.get(); }
    /**
     * Setter of nbAttempts
     * @param val the new value of nbAttempts
     */
    public void setNbAttempts(Long val) { nbAttempts.set(val); }
    /**
     * Increment of nbAttempts
     * @return the new value of nbAttempts
     */
    public Long incNbAttempts() { return nbAttempts.inc(1L).get(); }


    /**
     * Getter of nbRemoteAttempts
     * @return the value of nbRemoteAttempts
     */
    public Long getNbRemoteAttempts() { return nbRemoteAttempts.get(); }
    /**
     * Setter of nbRemoteAttempts
     * @param val the new value of nbRemoteAttempts
     */
    public void setNbRemoteAttempts(Long val) { nbRemoteAttempts.set(val); }
    /**
     * Increment of nbRemoteAttempts
     * @return the new value of nbRemoteAttempts
     */
    public Long incNbRemoteAttempts() { return nbRemoteAttempts.inc(1L).get(); }



    public enum Status{
        INITIALIZED,
        DEFERRED,
        SUBMITTED,
        PROCESSED,
        FAILED,
        CANCELLED
    }
}
