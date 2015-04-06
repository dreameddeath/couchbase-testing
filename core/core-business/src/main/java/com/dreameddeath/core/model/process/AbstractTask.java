/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.model.process;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.annotation.NotNull;
import com.dreameddeath.core.exception.model.DuplicateTaskException;
import com.dreameddeath.core.model.IVersionedDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.HasParent;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;

@JsonTypeInfo(use= JsonTypeInfo.Id.CUSTOM, include= JsonTypeInfo.As.PROPERTY, property="@t",visible = true)
@JsonTypeIdResolver(CouchbaseDocumentTypeIdResolver.class)
public abstract class AbstractTask extends CouchbaseDocumentElement implements IVersionedDocument{
    private String _classTypeId;
    @JsonSetter("@t")
    public void setDocumentFullVersionId(String typeId){_classTypeId=typeId;}
    public String getDocumentFullVersionId(){return _classTypeId;}

    @DocumentProperty("uid") @NotNull
    private Property<String> _uid=new ImmutableProperty<String>(AbstractTask.this);
    @DocumentProperty("label")
    private Property<String> _label=new StandardProperty<String>(AbstractTask.this);
    @DocumentProperty(value = "state") @NotNull
    private Property<State> _state=new StandardProperty<State>(AbstractTask.this, State.NEW);
    @DocumentProperty("effectiveDate")
    private Property<DateTime> _effectiveDate=new StandardProperty<DateTime>(AbstractTask.this);
    @DocumentProperty("lastRunError")
    private Property<String> _errorName=new StandardProperty<String>(AbstractTask.this);
    /**
     *  dependency : List of task Id being a pre-requisite
     */
    @DocumentProperty("dependency")
    private ListProperty<String> _dependency = new ArrayListProperty<String>(AbstractTask.this);

    // uid accessors
    public String getUid() { return _uid.get(); }
    public void setUid(String uid) { _uid.set(uid); }
    // label accessors
    public String getLabel(){ return _label.get(); }
    public void setLabel(String label){ _label.set(label); }
    // lastRunError Accessors
    public String getLastRunError(){return _errorName.get();}
    public void setLastRunError(String errorName){_errorName.set(errorName);}
    // Dependency Accessors
    public List<String> getDependency() { return _dependency.get(); }
    public void setDependency(Collection<String> taskKeys) { _dependency.set(taskKeys); }
    public boolean addDependency(String taskKey){ return _dependency.add(taskKey); }
    public boolean removeDependency(String taskKey){ return _dependency.remove(taskKey); }
    public <T extends AbstractTask> T getDependentTask(Class<T> clazz){
        for(String key:_dependency){
            AbstractTask task=getParentJob().getTask(key);
            if((task!=null) && (clazz.isAssignableFrom(task.getClass()))){
                return (T) task;
            }
        }
        //Manage Recursive lookup
        for(String key:_dependency){
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

    public State getState() { return _state.get(); }
    public void setState(State state) { _state.set(state); }
    public boolean isInitialized(){ return _state.get().compareTo(State.INITIALIZED)>=0; }
    public boolean isPrepared(){ return _state.get().compareTo(State.PREPROCESSED)>=0; }
    public boolean isProcessed(){ return _state.get().compareTo(State.PROCESSED)>=0; }
    public boolean isFinalized(){ return _state.get().compareTo(State.POSTPROCESSED)>=0; }
    public boolean isDone(){ return _state.get().compareTo(State.DONE)>=0; }




    // Parent Job Accessors and helpers
    public AbstractJob getParentJob(){ return HasParent.Helper.getFirstParentOfClass(this, AbstractJob.class);}
    public <T extends AbstractJob> T getParentJob(Class<T> clazz){ return (T)getParentJob();}
    public <T> T getJobRequest(Class<T> reqClass){ return (T) getParentJob().getRequest();}
    public <T> T getJobResult(Class<T> resClass){ return (T) getParentJob().getResult();}

    public DateTime getEffectiveDate() {
        return _effectiveDate.get();
    }

    public void setEffectiveDate(DateTime effectiveDate) {
        _effectiveDate.set(effectiveDate);
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