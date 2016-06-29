package com.dreameddeath.core.notification.model.v1;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
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

/**
 * Created by Christophe Jeunesse on 25/05/2016.
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.CUSTOM, include= JsonTypeInfo.As.PROPERTY, property="@t",visible = true)
@JsonTypeIdResolver(CouchbaseDocumentTypeIdResolver.class)
@DocumentEntity
public class Notification extends CouchbaseDocument implements IVersionedEntity{
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


    /**
     *  eventId : the event id being notified
     */
    @DocumentProperty("eventId") @Unique(nameSpace = "core/notification/id",additionnalFields = {"listenerName"})
    private Property<String> eventId = new ImmutableProperty<>(Notification.this);
    /**
     *  listenerName : name of the listener
     */
    @DocumentProperty("listenerName")
    private Property<String> listenerName = new ImmutableProperty<>(Notification.this);
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
    private NumericProperty<Long> nbAttempts = new StandardLongProperty(Notification.this);



    /**
     * Getter of eventId
     * @return the value of eventId
     */
    public String getEventId() { return eventId.get(); }
    /**
     * Setter of eventId
     * @param val the new value for eventId
     */
    public void setEventId(String val) { eventId.set(val); }
    /**
     * Getter of listenerName
     * @return the value of listenerName
     */
    public String getListenerName() { return listenerName.get(); }
    /**
     * Setter of listenerName
     * @param val the new value for listenerName
     */
    public void setListenerName(String val) { listenerName.set(val); }
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




    public enum Status{
        INITIALIZED,
        SUBMITTED,
        FAILED,
        CANCELLED
    }
}
