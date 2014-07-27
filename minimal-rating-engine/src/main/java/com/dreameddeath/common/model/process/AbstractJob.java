package com.dreameddeath.common.model.process;

import java.util.*;

import com.dreameddeath.common.event.TaskProcessEvent;
import com.dreameddeath.common.annotation.DocumentProperty;
import com.dreameddeath.common.model.document.CouchbaseDocument;
import com.dreameddeath.common.model.document.CouchbaseDocumentArrayList;
import com.dreameddeath.common.model.property.ImmutableProperty;
import com.dreameddeath.common.model.property.Property;
import com.dreameddeath.common.model.property.StandardProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by ceaj8230 on 21/05/2014.
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.MINIMAL_CLASS, include= JsonTypeInfo.As.PROPERTY, property="@c")
public abstract class AbstractJob extends CouchbaseDocument {
    @DocumentProperty("uid")
    private Property<UUID> _uid=new ImmutableProperty<UUID>(AbstractJob.this,UUID.randomUUID());
    @DocumentProperty("state")
    private Property<State> _state=new StandardProperty<State>(AbstractJob.this,State.NEW);
    @DocumentProperty("tasks")
    private List<AbstractTask> _taskList = new CouchbaseDocumentArrayList<AbstractTask>(AbstractJob.this);


    public State getJobState() { return _state.get(); }
    public void setJobState(State state) { _state.set(state); }
    public boolean isInitialized(){ return _state.get().compareTo(State.INITIALIZED)>=0; }
    public boolean isPrepared(){ return _state.get().compareTo(State.PREPROCESSED)>=0; }
    public boolean isProcessed(){ return _state.get().compareTo(State.PROCESSED)>=0; }
    public boolean isFinalized(){ return _state.get().compareTo(State.POSTPROCESSED)>=0; }
    public boolean isDone(){ return _state.get().compareTo(State.DONE)>=0; }

    public UUID getUid() { return _uid.get(); }
    public void setUid(UUID uid) { _uid.set(uid); }

    public List<AbstractTask> getTasks() { return Collections.unmodifiableList(_taskList);}
    public void setTasks(Collection<AbstractTask> tasks) {
        _taskList.clear();
        for(AbstractTask task : tasks){
            task.setParentElement(this);
        }
        _taskList.addAll(tasks);
    }

    public AbstractTask getTask(String id) {
        for(AbstractTask task :_taskList){
            if(id.equals(task.getUid())){
               return task;
            }
        }
        return null;
    }
    public <T extends AbstractTask> T getTask(String id, Class<T>clazz) {
        return (T)getTask(id);
    }

    public void addTask(AbstractTask task){
        if(task.getUid()==null){
            task.setUid(buildTaskId(task));
        }
        if(getTask(task.getUid())!=null){
            ///TODO throw an error
        }
        task.setParentElement(this);
        _taskList.add(task);
    }


    public String buildTaskId(AbstractTask task){
        return String.format("%10d", _taskList.size());
    }

    public List<AbstractTask> getPendingTasks() {
        List<AbstractTask> pendingTasks=new ArrayList<AbstractTask>();
        for(AbstractTask task:_taskList){
            if(! task.isDone()){
                pendingTasks.add(task);
            }
        }
        return pendingTasks;
    }

    public AbstractTask getNextPendingTask(){
        for(AbstractTask task:_taskList){
            if(! task.isDone()){
                return task;
            }
        }
        return null;
    }
    public static enum State{
        NEW, //Init done
        INITIALIZED, //Init done
        PREPROCESSED,    //Preparation done
        PROCESSED, //Processing Done
        POSTPROCESSED, //Job Update Processing done
        DONE;//Cleaning done
    }


    public void init(){}
    public void preprocess(){}
    public abstract void when(TaskProcessEvent evt);
    public void postprocess(){}
    public void cleanup(){}

}
