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

package com.dreameddeath.core.process.model.base;

import com.dreameddeath.core.model.annotation.DocumentDef;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.entity.model.IVersionedEntity;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentTypeIdResolver;
import com.dreameddeath.core.validation.annotation.NotNull;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@JsonTypeInfo(use= JsonTypeInfo.Id.CUSTOM, include= JsonTypeInfo.As.PROPERTY, property="@t",visible = true)
@JsonTypeIdResolver(CouchbaseDocumentTypeIdResolver.class)
@DocumentDef(domain = "core")
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
    private ListProperty<String> dependencies = new ArrayListProperty<>(AbstractTask.this);
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

    // jobUid accessors
    public UUID getJobUid() { return jobUid.get(); }
    public void setJobUid(UUID val) { jobUid.set(val); }
    // id accessors
    public String getId() { return id.get(); }
    public void setId(String val) { id.set(val); }

    // Dependency Accessors
    public List<String> getDependencies() { return dependencies.get(); }
    public void setDependencies(Collection<String> taskKeys) { dependencies.set(taskKeys); }
    public boolean addDependency(String taskKey){ return dependencies.add(taskKey); }
    public boolean removeDependency(String taskKey){ return dependencies.remove(taskKey); }

    // stateInfo accessors
    public ProcessState getStateInfo() { return stateInfo.get(); }
    public void setStateInfo(ProcessState val) { stateInfo.set(val); }

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

}