package com.dreameddeath.common.model.process;

import java.util.*;

import com.dreameddeath.common.event.TaskProcessEvent;
import com.dreameddeath.common.annotation.DocumentProperty;
import com.dreameddeath.common.model.document.CouchbaseDocument;
import com.dreameddeath.common.model.document.CouchbaseDocumentArrayList;
import com.dreameddeath.common.model.property.ImmutableProperty;
import com.dreameddeath.common.model.property.Property;
import com.dreameddeath.common.model.property.StandardProperty;
import com.dreameddeath.common.process.JobProcessingService;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
/**
 * Created by ceaj8230 on 21/05/2014.
 */
@JsonTypeInfo(use=Id.MINIMAL_CLASS,include=As.PROPERTY, property="@t")
public abstract class AbstractJob extends CouchbaseDocument {
    @DocumentProperty("uid")
    private Property<UUID> _uid=new ImmutableProperty<UUID>(AbstractJob.this,UUID.randomUUID());
    @DocumentProperty("state")
    private Property<State> _state=new StandardProperty<State>(AbstractJob.this,State.NEW);
    @DocumentProperty("tasks")
    private List<AbstractTask> _taskList = new CouchbaseDocumentArrayList<AbstractTask>(AbstractJob.this);


    //Save current processing Service
    private JobProcessingService _processingService=null;


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
            addTask(task);
        }
        //_taskList.addAll(tasks);
    }

    public AbstractTask getTask(String id) {
        for(AbstractTask task :_taskList){
            if(id.equals(task.getUid())){
               return task;
            }
        }
        return null;
    }

    public AbstractTask getTask(Integer pos) {
        return _taskList.get(pos);
    }

    public <T extends AbstractTask> T  getTask(Integer pos,Class<T> clazz) {
        return (T)_taskList.get(pos);
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
        task.setParentJob(this);
        _taskList.add(task);
    }

    public <T extends CouchbaseDocument> T newEntity(Class<T> clazz){
        return getSession().newEntity(clazz);
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
        NEW, //Just created
        INITIALIZED, //Init done
        PREPROCESSED,    //Preparation done
        PROCESSED, //Processing Done
        POSTPROCESSED, //Job Update Processing done
        DONE;//Cleaning done
    }


    public boolean init(){return false;}
    public boolean preprocess(){return false;}
    public abstract boolean when(TaskProcessEvent evt);
    public boolean postprocess(){return false;}
    public boolean cleanup(){return false;}

    public final void setProcessingService(JobProcessingService service){
        _processingService = service;
    }

    public final JobProcessingService getProcessingService(){
        return _processingService ;
    }
}
