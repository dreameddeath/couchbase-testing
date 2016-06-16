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

package com.dreameddeath.core.process.model.v1.base;

import com.dreameddeath.core.dao.model.IHasUniqueKeysRef;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.entity.model.IVersionedEntity;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.SetProperty;
import com.dreameddeath.core.model.property.impl.HashSetProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.TreeSetProperty;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentTypeIdResolver;
import com.dreameddeath.core.validation.annotation.Unique;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.CUSTOM, include= JsonTypeInfo.As.PROPERTY, property="@t",visible = true)
@JsonTypeIdResolver(CouchbaseDocumentTypeIdResolver.class)
@DocumentEntity
public abstract class AbstractJob extends CouchbaseDocument implements IVersionedEntity,IHasUniqueKeysRef {
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

    public AbstractJob(){
        super(null);
        super.setBaseMeta(AbstractJob.this.new MetaInfo());
    }

    @DocumentProperty("uid")
    private transient Property<UUID> uid=new ImmutableProperty<>(AbstractJob.this,UUID.randomUUID());
    /**
     *  requestUid : The external job request uid
     */
    @DocumentProperty("requestUid") @Unique(nameSpace = "jobRequestlId")
    private Property<String> requestUid = new ImmutableProperty<>(AbstractJob.this);
    /**
     *  stateInfo : Gives job current state info
     */
    @DocumentProperty("stateInfo")
    private Property<ProcessState> stateInfo = new ImmutableProperty<>(AbstractJob.this,ProcessState.class);
    /**
     *  tasks : List of tasks attached to the document
     */
    @DocumentProperty("tasks")
    private SetProperty<String> tasks = new TreeSetProperty<>(AbstractJob.this);
    /**
     *  docUniqKeys : List of uniqueness Keys attached to this document
     */
    @DocumentProperty("docUniqKeys")
    private SetProperty<String> docUniqKeys = new HashSetProperty<>(AbstractJob.this);
    private Set<String> inDbUniqKeys = new HashSet<>();

    // uid accessors
    public UUID getUid() { return uid.get(); }
    public void setUid(UUID uid) { this.uid.set(uid); }
    // stateInfo accessors
    public ProcessState getStateInfo() { return stateInfo.get(); }
    public void setStateInfo(ProcessState val) { stateInfo.set(val); }

    /**
     * Getter of tasks
     * @return the content
     */
    public Set<String> getTasks() { return tasks.get(); }
    /**
     * Setter of tasks
     * @param vals the new collection of values
     */
    public void setTasks(Collection<String> vals) { tasks.set(vals); }
    /**
     * Add a new entry to the property tasks
     * @param val the new entry to be added
     */
    public boolean addTask(String val){ return tasks.add(val); }
    /**
     * Remove an entry to the property tasks
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeTask(String val){ return tasks.remove(val); }
    /**
     * Getter of requestUid
     * @return the value of requestUid
     */
    public String getRequestUid() { return requestUid.get(); }
    /**
     * Setter of requestUid
     * @param val the new value of requestUid
     */
    public void setRequestUid(String val) { requestUid.set(val); }

    // DocUniqKeys Accessors
    public final Set<String> getDocUniqKeys() { return docUniqKeys.get(); }
    public final void setDocUniqKeys(Set<String> vals) { docUniqKeys.set(vals); }
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


    public MetaInfo getMeta(){
        return (MetaInfo) getBaseMeta();
    }

    public class MetaInfo extends BaseMetaInfo {

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
    }
}
