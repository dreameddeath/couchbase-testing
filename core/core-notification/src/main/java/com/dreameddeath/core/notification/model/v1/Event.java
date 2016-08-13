package com.dreameddeath.core.notification.model.v1;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.entity.model.IVersionedEntity;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.NumericProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardLongProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 24/05/2016.
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.CUSTOM, include= JsonTypeInfo.As.PROPERTY, property="@t",visible = true)
@JsonTypeIdResolver(CouchbaseDocumentTypeIdResolver.class)
@DocumentEntity
public abstract class Event extends CouchbaseDocument implements IVersionedEntity {
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
     *  id : event unique Id
     */
    @DocumentProperty("id")
    private Property<UUID> id = new ImmutableProperty<>(Event.this,UUID.randomUUID());
    /**
     *  type : type of event
     */
    @DocumentProperty("type")
    private Property<EventType> type = new ImmutableProperty<>(Event.this);
    /**
     *  correlationId : correlation id of event to allow group by
     */
    @DocumentProperty("correlationId")
    private Property<String> correlationId = new ImmutableProperty<>(Event.this);
    /**
     *  rank : Ordering rank for the given correlation id. Used to perform correlation checks
     */
    @DocumentProperty("rank")
    private Property<String> rank = new ImmutableProperty<>(Event.this);
    /**
     *  listeners : List of listeners to post to
     */
    @DocumentProperty("listeners")
    private ListProperty<String> listeners = new ArrayListProperty<>(Event.this);
    /**
     *  submissionAttempt : number of attempts of submission
     */
    @DocumentProperty("submissionAttempt")
    private NumericProperty<Long> submissionAttempt = new StandardLongProperty(Event.this,0);
    /**
     *  status : The status of the event
     */
    @DocumentProperty("status")
    private Property<Status> status = new StandardProperty<>(Event.this,Status.CREATED);

    /**
     * Getter of id
     * @return the value of id
     */
    public UUID getId() { return id.get(); }
    /**
     * Setter of id
     * @param val the new value for id
     */
    public void setId(UUID val) { id.set(val); }
    /**
     * Getter of type
     * @return the value of type
     */
    public EventType getType() { return type.get(); }
    /**
     * Setter of type
     * @param val the new value for type
     */
    public void setType(EventType val) { type.set(val); }
    /**
     * Getter of correlationId
     * @return the value of correlationId
     */
    public String getCorrelationId() { return correlationId.get(); }
    /**
     * Setter of correlationId
     * @param val the new value for correlationId
     */
    public void setCorrelationId(String val) { correlationId.set(val); }
    /**
     * Getter of rank
     * @return the value of rank
     */
    public String getRank() { return rank.get(); }
    /**
     * Setter of rank
     * @param val the new value for rank
     */
    public void setRank(String val) { rank.set(val); }
    /**
     * Getter of listeners
     * @return the whole (immutable) list of listeners
     */
    public List<String> getListeners() { return listeners.get(); }
    /**
     * Setter of listeners
     * @param newListeners the new collection of listeners
     */
    public void setListeners(Collection<String> newListeners) { listeners.set(newListeners); }
    /**
     * Add a new entry to the property listeners
     * @param newListeners the new entry to be added
     * @return true if the entry has been added
     */
    public boolean addListeners(String newListeners){ return listeners.add(newListeners); }
    /**
     * Add a new entry to the property listeners at the specified position
     * @param index the new entry to be added
     * @param newListeners the new entry to be added
     * @return true if the entry has been added
     */
    public boolean addListeners(int index,String newListeners){ return listeners.add(newListeners); }
    /**
     * Remove an entry to the property listeners
     * @param oldListeners the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeListeners(String oldListeners){ return listeners.remove(oldListeners); }
    /**
     * Remove an entry to the property listeners at the specified position
     * @param index the position of element to be removed
     * @return the entry removed if any
     */
    public String removeListeners(int index){ return listeners.remove(index); }

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
    public Status getStatus() { return status.get(); }
    /**
     * Setter of status
     * @param val the new value of status
     */
    public void setStatus(Status val) { status.set(val); }

    public enum Status{
        CREATED,
        NOTIFICATIONS_LIST_NAME_GENERATED,
        NOTIFICATIONS_IN_DB
    }

}
