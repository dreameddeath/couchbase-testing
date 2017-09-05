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
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.MapProperty;
import com.dreameddeath.core.model.property.NumericProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.*;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 30/08/2017.
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.CUSTOM, include= JsonTypeInfo.As.PROPERTY, property="@t",visible = true)
@JsonTypeIdResolver(CouchbaseDocumentTypeIdResolver.class)
@DocumentEntity
public class CrossDomainBridge extends CouchbaseDocument  implements IVersionedEntity, HasEffectiveDomain,INotificationsHolder {
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
        return getTargetDomain();
    }

    /**
     *  eventId : the event id being notified
     */
    @DocumentProperty("eventId")
    transient private Property<UUID> eventId = new ImmutableProperty<>(CrossDomainBridge.this);
    /**
     * domain : the event parent domain
     */
    @DocumentProperty("event-domain")
    private ImmutableProperty<String> domain = new ImmutableProperty<>(CrossDomainBridge.this);
    /**
     * targetDomain : the target domain of the bridge
     */
    @DocumentProperty("target-domain")
    private Property<String> targetDomain = new ImmutableProperty<>(CrossDomainBridge.this);
    /**
     *  listeners : List of listeners to post to
     */
    @DocumentProperty("listeners")
    private final ListProperty<EventListenerLink> listeners = new ArrayListProperty<>(CrossDomainBridge.this);
    /**
     *  submissionAttempt : number of attempts of submission
     */
    @DocumentProperty("submissionAttempt")
    private final NumericProperty<Long> submissionAttempt = new StandardLongProperty(CrossDomainBridge.this,0);
    /**
     *  status : The status of the event
     */
    @DocumentProperty("status")
    private final Property<Event.Status> status = new StandardProperty<>(CrossDomainBridge.this, Event.Status.CREATED);
    /**
     * notifications : the notifications attached to the event
     */
    @DocumentProperty("notifications")
    private final MapProperty<String,NotificationLink> notifications = new HashMapProperty<>(CrossDomainBridge.this);


    /**
     * Getter of {@link #eventId}
     * @return the value of eventId
     */
    public UUID getEventId() { return eventId.get(); }
    /**
     * Setter of {@link #eventId}
     * @param val the new value for eventId
     */
    public void setEventId(UUID val) { eventId.set(val); }
    /**
     * Getter for property {@link #domain}
     * @return The current value
     */
    public String getDomain(){
        return domain.get();
    }
    /**
     * Setter for property {@link #domain}
     * @param newValue  the new value for the property
     */
    public void setDomain(String newValue){
        domain.set(newValue);
    }
    /**
     * Getter of the attribute {@link #targetDomain}
     * return the currentValue of {@link #targetDomain}
     */
    public String getTargetDomain(){
        return this.targetDomain.get();
    }

    /**
     * Setter of the attribute {@link #targetDomain}
     * @param newValue the newValue of {@link #targetDomain}
     */
    public void setTargetDomain(String newValue){
        this.targetDomain.set(newValue);
    }

    /**
     * Getter of {@link #listeners}
     * @return the whole (immutable) list of {@link #listeners}
     */
    public List<EventListenerLink> getListeners() { return listeners.get(); }
    /**
     * Setter of {@link #listeners}
     * @param newListeners the new collection of {@link #listeners}
     */
    public void setListeners(Collection<EventListenerLink> newListeners) { listeners.set(newListeners); }
    /**
     * Add a new entry to the property listeners
     * @param newListener the new entry to be added
     * @param domain the domain of the entry
     * @return true if the entry has been added
     */
    public boolean addListener(String newListener, String domain){
        EventListenerLink link = new EventListenerLink();
        link.setName(newListener);
        link.setDomain(domain);
        return listeners.add(link);
    }
    /**
     * Getter of submissionAttempt
     * @return the value of submissionAttempt
     */
    public Long getSubmissionAttempt() { return submissionAttempt.get(); }
    /**
     * Setter of submissionAttempt
     * @param val the new value of submissionAttempt
     */
    public void setSubmissionAttempt(Long val) { submissionAttempt.set(val); }
    /**
     * Setter of submissionAttempt
     * @return the new value of submissionAttempt
     */
    public Long incrSubmissionAttempt() { return submissionAttempt.inc(1L).get(); }
    /**
     * Getter of status
     * @return the value of status
     */
    public Event.Status getStatus() { return status.get(); }
    /**
     * Setter of status
     * @param val the new value of status
     */
    public void setStatus(Event.Status val) { status.set(val); }

    /**
     * put a new Notification link for given listener name
     * @param listenerName the listener name corresponding to the link
     * @param link the notification link to map to
     */
    public void putNotification(String listenerName,NotificationLink link){
        notifications.put(listenerName,link);
    }

    /**
     * set the whole notification links
     * @param notifications the notifications to replace with
     */
    public void setNotifications(Map<String,NotificationLink> notifications){
        this.notifications.set(notifications);
    }

    /**
     * get the current notification list
     * @return the notifications to replace with
     */
    public Map<String,NotificationLink> getNotifications(){
        return notifications.get();
    }


}
