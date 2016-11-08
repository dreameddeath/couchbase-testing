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

package com.dreameddeath.core.process.model.v1.base;

import com.dreameddeath.core.dao.model.IHasUniqueKeysRef;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.entity.model.IVersionedEntity;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.SetProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.HashSetProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.TreeSetProperty;
import com.dreameddeath.core.notification.model.v1.EventLink;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentTypeIdResolver;
import com.dreameddeath.core.validation.annotation.NotNull;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import java.util.*;

@JsonTypeInfo(use= JsonTypeInfo.Id.CUSTOM, include= JsonTypeInfo.As.PROPERTY, property="@t",visible = true)
@JsonTypeIdResolver(CouchbaseDocumentTypeIdResolver.class)
@DocumentEntity
public abstract class AbstractTask extends CouchbaseDocument implements IVersionedEntity {
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

    public AbstractTask() {
        super(null);
        super.setBaseMeta(AbstractTask.this.new MetaInfo());
    }

    /**
     *  jobUid : The parent job unique id
     */
    @DocumentProperty("jobUid") @NotNull
    private transient Property<UUID> jobUid = new ImmutableProperty<>(AbstractTask.this);
    /**
     *  id : the task id
     */
    @DocumentProperty("id")
    private transient Property<String> id = new ImmutableProperty<>(AbstractTask.this);
    /**
     *  dependencies : List of task Id being a pre-requisite
     */
    @DocumentProperty("dependencies")
    private SetProperty<String> dependencies = new TreeSetProperty<>(AbstractTask.this);
    /**
     *  stateInfo : Gives job current state info
     */
    @DocumentProperty("stateInfo")
    private Property<ProcessState> stateInfo = new ImmutableProperty<>(AbstractTask.this,ProcessState.class);
    /**
     *  parentTaskId : The root task which has created the task
     */
    @DocumentProperty("parentTaskId")
    private transient Property<String> parentTaskId = new ImmutableProperty<>(AbstractTask.this);
    /**
     *  notifications : List of attached notifications
     */
    @DocumentProperty("notifications")
    private ListProperty<EventLink> notifications = new ArrayListProperty<>(AbstractTask.this);
    /**
     *  docUniqKeys : List of uniqueness Keys attached to this document
     */
    @DocumentProperty("docUniqKeys")
    private SetProperty<String> docUniqKeys = new HashSetProperty<>(AbstractTask.this);

    // jobUid accessors
    public UUID getJobUid() { return jobUid.get(); }
    public void setJobUid(UUID val) { jobUid.set(val); }
    // id accessors
    public String getId() { return id.get(); }
    public void setId(String val) { id.set(val); }

    // Dependency Accessors
    public Set<String> getDependencies() { return dependencies.get(); }
    public void setDependencies(Collection<String> taskKeys) { dependencies.set(taskKeys); }
    public synchronized boolean addDependency(String taskKey){ return dependencies.add(taskKey); }
    public synchronized boolean removeDependency(String taskKey){ return dependencies.remove(taskKey); }

    // stateInfo accessors
    public ProcessState getStateInfo() { return stateInfo.get(); }
    public void setStateInfo(ProcessState val) { stateInfo.set(val); }

    // DocUniqKeys Accessors
    public final Set<String> getDocUniqKeys() { return docUniqKeys.get(); }
    public final void setDocUniqKeys(Set<String> vals) { docUniqKeys.set(vals); }


    /**
     * Getter of parentTaskId
     * @return the content
     */
    public String getParentTaskId() { return parentTaskId.get(); }
    /**
     * Setter of parentTaskId
     * @param val the new content
     */
    public void setParentTaskId(String val) { parentTaskId.set(val); }
    /**
     * Getter of notifications
     * @return the whole (immutable) list of notifications
     */
    public List<EventLink> getNotifications() { return notifications.get(); }
    /**
     * Setter of notifications
     * @param newNotifications the new collection of notifications
     */
    public void setNotifications(Collection<EventLink> newNotifications) { notifications.set(newNotifications); }
    /**
     * Add a new entry to the property notifications
     * @param newNotifications the new entry to be added
     * @return true if the entry has been added
     */
    public boolean addNotifications(EventLink newNotifications){ return notifications.add(newNotifications); }
    /**
     * Add a new entry to the property notifications at the specified position
     * @param index the new entry to be added
     * @param newNotifications the new entry to be added
     * @return true if the entry has been added
     */
    public boolean addNotifications(int index,EventLink newNotifications){ return notifications.add(newNotifications); }
    /**
     * Remove an entry to the property notifications
     * @param oldNotifications the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeNotifications(EventLink oldNotifications){ return notifications.remove(oldNotifications); }
    /**
     * Remove an entry to the property notifications at the specified position
     * @param index the position of element to be removed
     * @return the entry removed if any
     */
    public EventLink removeNotifications(int index){ return notifications.remove(index); }

    public MetaInfo getMeta(){
        return (MetaInfo) getBaseMeta();
    }

    public class MetaInfo extends BaseMetaInfo implements IHasUniqueKeysRef {
        private Set<String> inDbUniqKeys = new HashSet<>();

        @Override
        public void setStateDeleted(){
            //syncKeyWithDb(); voluntary to key in db values up to date
            super.setStateDeleted();
        }

        @Override
        public void setStateSync(){
            syncKeyWithDb();
            super.setStateSync();
        }
        @Override
        public final boolean addDocUniqKeys(String key){ return docUniqKeys.add(key); }

        protected void syncKeyWithDb(){
            inDbUniqKeys.clear();
            inDbUniqKeys.addAll(docUniqKeys.get());
            docUniqKeys.clear();
        }

        public Set<String> getToBeDeletedUniqueKeys(){
            Set<String> toRemoveKeyList=new HashSet<>(inDbUniqKeys);
            toRemoveKeyList.addAll(docUniqKeys.get());
            return toRemoveKeyList;
        }

        @Override
        public Set<String> getRemovedUniqueKeys(){
            Set<String> removed=new HashSet<>(inDbUniqKeys);
            removed.removeAll(docUniqKeys.get());
            return removed;
        }

        @Override
        public boolean isInDbKey(String key) {
            return inDbUniqKeys.contains(key);
        }
    }
}