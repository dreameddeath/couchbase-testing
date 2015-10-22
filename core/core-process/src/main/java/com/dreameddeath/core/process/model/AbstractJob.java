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
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.entity.model.IVersionedEntity;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.process.exception.DuplicateTaskException;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentTypeIdResolver;
import com.dreameddeath.core.validation.annotation.NotNull;
import com.dreameddeath.core.validation.annotation.Validate;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import java.util.*;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.CUSTOM, include= JsonTypeInfo.As.PROPERTY, property="@t",visible = true)
@JsonTypeIdResolver(CouchbaseDocumentTypeIdResolver.class)
@DocumentDef(domain="core")
public abstract class AbstractJob<TREQ extends CouchbaseDocumentElement,TRES extends CouchbaseDocumentElement> extends CouchbaseDocument implements IVersionedEntity {
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

    @DocumentProperty("uid")
    private Property<UUID> uid=new ImmutableProperty<>(AbstractJob.this,UUID.randomUUID());
    @DocumentProperty(value = "state",getter = "getJobState",setter = "setJobState")
    private Property<State> state=new StandardProperty<>(AbstractJob.this, State.NEW);
    @DocumentProperty("tasks") @Validate
    private ListProperty<AbstractTask> taskList = new ArrayListProperty<>(AbstractJob.this);
    @DocumentProperty("lastRunError")
    private Property<String> errorName=new StandardProperty<>(AbstractJob.this);
    /**
     *  request : The request content for this job
     */
    @DocumentProperty("request") @NotNull @Validate
    private Property<TREQ> request = new StandardProperty<>(AbstractJob.this);
    /**
     *  result : The result content for this job
     */
    @DocumentProperty("result")
    private Property<TRES> result = new StandardProperty<>(AbstractJob.this);

    public AbstractJob(TREQ request,TRES result){
        this.request.set(request);
        this.result.set(result);
    }

    public AbstractJob(TREQ request){
        this.request.set(request);
        result.set(newResult());
    }
    public AbstractJob(){
        request.set(newRequest());
        result.set(newResult());
    }

    public abstract TREQ newRequest();
    public abstract TRES newResult();

    public String getLastRunError(){return errorName.get();}
    public void setLastRunError(String errorName){this.errorName.set(errorName);}

    public State getJobState() { return state.get(); }
    public void setJobState(State state) { this.state.set(state); }
    public boolean isInitialized(){ return state.get().compareTo(State.INITIALIZED)>=0; }
    public boolean isPrepared(){ return state.get().compareTo(State.PREPROCESSED)>=0; }
    public boolean isProcessed(){ return state.get().compareTo(State.PROCESSED)>=0; }
    public boolean isFinalized(){ return state.get().compareTo(State.POSTPROCESSED)>=0; }
    public boolean isDone(){ return state.get().compareTo(State.DONE)>=0; }
    // uid accessors
    public UUID getUid() { return uid.get(); }
    public void setUid(UUID uid) { this.uid.set(uid); }
    // result accessors
    public TRES getResult() { return result.get(); }
    public void setResult(TRES val) { result.set(val); }

    // request accessors
    public TREQ getRequest() { return request.get(); }
    public void setRequest(TREQ val) { request.set(val); }

    public List<AbstractTask> getTasks() { return taskList.get();}
    //should not be used direclty (only used for Jackson)
    public void setTasks(Collection<AbstractTask> tasks) {
        taskList.set(tasks);
    }

    public AbstractTask getTask(String id) {
        for(AbstractTask task :taskList){
            if(id.equals(task.getUid())){ return task; }
        }
        return null;
    }

    public AbstractTask getTask(Integer pos) {
        return taskList.get(pos);
    }
    public <T extends AbstractTask> T getTask(Integer pos,Class<T> clazz) {return (T)getTask(pos);}
    public <T extends AbstractTask> T getTask(String id, Class<T>clazz) {
        return (T)getTask(id);
    }


    public <T extends AbstractTask> T addTask(T task,Collection<String> prerequisiteTasks) throws DuplicateTaskException {
        if(task.getUid()==null){
            task.setUid(buildTaskId(task));
        }
        if(getTask(task.getUid())!=null){
            if(task!=getTask(task.getUid())) {
                throw new DuplicateTaskException(task,this);
            }
            else{
                for(String preReq:prerequisiteTasks){
                    task.addDependency(preReq);
                }
            }
        }
        else {
            task.setDependency(prerequisiteTasks);
            taskList.add(task);
        }
        return task;
    }

    public <T extends AbstractTask> T addTask(T task) throws DuplicateTaskException{
        return addTask(task,(Collection<String>)Collections.EMPTY_LIST);
    }

    public <T extends AbstractTask> T addTask(T task,String preRequisiteTaskUid) throws DuplicateTaskException{
        List<String> preRequisiteTaskList= new ArrayList<String>(1);
        preRequisiteTaskList.add(preRequisiteTaskUid);
        return addTask(task,preRequisiteTaskList);
    }

    public <T extends AbstractTask> T  addTask(T task,AbstractTask preRequisiteTask) throws DuplicateTaskException{
        return addTask(task,preRequisiteTask.getUid());
    }

    public String buildTaskId(AbstractTask task){
        return String.format("%010d", taskList.size());
    }

    public List<AbstractTask> getPendingTasks() {
        List<AbstractTask> pendingTasks=new ArrayList<AbstractTask>();
        for(AbstractTask task:taskList){
            if(! task.isDone()){
                pendingTasks.add(task);
            }
        }
        return pendingTasks;
    }

    public AbstractTask getNextExecutableTask(){
        for(AbstractTask task:taskList){
            if(! task.isDone()){
                List<String> preRequisiteList = new ArrayList<String>();
                for(String uid:task.getDependency()){
                    if(!getTask(uid).isDone()){
                        preRequisiteList.add(uid);
                    }
                }
                if(preRequisiteList.size()==0){
                    //TODO maybe log warning/debug message
                    return task;
                }
            }
        }
        return null;
    }

    public enum State{
        UNKNOWN,
        NEW, //Just created
        INITIALIZED, //Init done
        PREPROCESSED,    //Preparation done
        PROCESSED, //Processing Done
        POSTPROCESSED, //Job Update Processing done
        DONE//Cleaning done
    }
}
