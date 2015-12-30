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

package com.dreameddeath.core.process.model;

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
    private transient Property<Integer> id = new ImmutableProperty<>(AbstractTask.this);
    /**
     *  dependencies : List of task Id being a pre-requisite
     */
    @DocumentProperty("dependencies")
    private ListProperty<Integer> dependencies = new ArrayListProperty<>(AbstractTask.this);
    /**
     *  stateInfo : Gives job current state info
     */
    @DocumentProperty("stateInfo")
    private Property<ProcessState> stateInfo = new ImmutableProperty<>(AbstractTask.this,new ProcessState());


    // jobUid accessors
    public UUID getJobUid() { return jobUid.get(); }
    public void setJobUid(UUID val) { jobUid.set(val); }
    // id accessors
    public Integer getId() { return id.get(); }
    public void setId(Integer val) { id.set(val); }

    // Dependency Accessors
    public List<Integer> getDependencies() { return dependencies.get(); }
    public void setDependencies(Collection<Integer> taskKeys) { dependencies.set(taskKeys); }
    public boolean addDependency(Integer taskKey){ return dependencies.add(taskKey); }
    public boolean removeDependency(Integer taskKey){ return dependencies.remove(taskKey); }

    // stateInfo accessors
    public ProcessState getStateInfo() { return stateInfo.get(); }
    public void setStateInfo(ProcessState val) { stateInfo.set(val); }

}