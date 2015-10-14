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

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.entity.model.IVersionedEntity;
import com.dreameddeath.core.model.property.HasParent;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.process.exception.DuplicateTaskException;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentTypeIdResolver;
import com.dreameddeath.core.validation.annotation.NotNull;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;

@JsonTypeInfo(use= JsonTypeInfo.Id.CUSTOM, include= JsonTypeInfo.As.PROPERTY, property="@t",visible = true)
@JsonTypeIdResolver(CouchbaseDocumentTypeIdResolver.class)
public abstract class AbstractTask extends CouchbaseDocumentElement implements IVersionedEntity {
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

    @DocumentProperty("uid") @NotNull
    private Property<String> uid=new ImmutableProperty<String>(AbstractTask.this);
    @DocumentProperty("label")
    private Property<String> label=new StandardProperty<String>(AbstractTask.this);
    @DocumentProperty(value = "state") @NotNull
    private Property<State> state=new StandardProperty<State>(AbstractTask.this, State.NEW);
    @DocumentProperty("effectiveDate")
    private Property<DateTime> effectiveDate=new StandardProperty<DateTime>(AbstractTask.this);
    @DocumentProperty("lastRunError")
    private Property<String> errorName=new StandardProperty<String>(AbstractTask.this);
    /**
     *  dependency : List of task Id being a pre-requisite
     */
    @DocumentProperty("dependency")
    private ListProperty<String> dependency = new ArrayListProperty<String>(AbstractTask.this);

    // uid accessors
    public String getUid() { return uid.get(); }
    public void setUid(String uid) { this.uid.set(uid); }
    // label accessors
    public String getLabel(){ return label.get(); }
    public void setLabel(String label){ this.label.set(label); }
    // lastRunError Accessors
    public String getLastRunError(){return errorName.get();}
    public void setLastRunError(String errorName){this.errorName.set(errorName);}
    // Dependency Accessors
    public List<String> getDependency() { return dependency.get(); }
    public void setDependency(Collection<String> taskKeys) { dependency.set(taskKeys); }
    public boolean addDependency(String taskKey){ return dependency.add(taskKey); }
    public boolean removeDependency(String taskKey){ return dependency.remove(taskKey); }
    public <T extends AbstractTask> T getDependentTask(Class<T> clazz){
        for(String key:dependency){
            AbstractTask task=getParentJob().getTask(key);
            if((task!=null) && (clazz.isAssignableFrom(task.getClass()))){
                return (T) task;
            }
        }
        //Manage Recursive lookup
        for(String key:dependency){
            AbstractTask task=getParentJob().getTask(key);
            if(task!=null){
                T result = task.getDependentTask(clazz);
                if(result!=null){
                    return result;
                }
            }
        }
        return null;
    }

    public State getState() { return state.get(); }
    public void setState(State state) { this.state.set(state); }
    public boolean isInitialized(){ return state.get().compareTo(State.INITIALIZED)>=0; }
    public boolean isPrepared(){ return state.get().compareTo(State.PREPROCESSED)>=0; }
    public boolean isProcessed(){ return state.get().compareTo(State.PROCESSED)>=0; }
    public boolean isFinalized(){ return state.get().compareTo(State.POSTPROCESSED)>=0; }
    public boolean isDone(){ return state.get().compareTo(State.DONE)>=0; }




    // Parent Job Accessors and helpers
    public AbstractJob getParentJob(){ return HasParent.Helper.getFirstParentOfClass(this, AbstractJob.class);}
    public <T extends AbstractJob> T getParentJob(Class<T> clazz){ return (T)getParentJob();}
    public <T> T getJobRequest(Class<T> reqClass){ return (T) getParentJob().getRequest();}
    public <T> T getJobResult(Class<T> resClass){ return (T) getParentJob().getResult();}

    public DateTime getEffectiveDate() {
        return effectiveDate.get();
    }

    public void setEffectiveDate(DateTime effectiveDate) {
        this.effectiveDate.set(effectiveDate);
    }


    public enum State{
        UNKNOWN,
        NEW,
        INITIALIZED, //Init done
        PREPROCESSED,
        PROCESSED, //Processing Done
        POSTPROCESSED, //Job Update Processing done
        DONE//Cleaning done
    }

    public <T extends AbstractTask> T chainWith(T task) throws DuplicateTaskException {
        this.getParentJob().addTask(task);
        task.addDependency(this.getUid());
        return task;
    }
}