package com.dreameddeath.core.model.process;

import java.util.*;

import com.dreameddeath.core.event.TaskProcessEvent;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.*;
import com.dreameddeath.core.process.JobProcessingService;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
@JsonTypeInfo(use=Id.MINIMAL_CLASS,include=As.PROPERTY, property="@c")
public abstract class AbstractJob<TREQ,TRES> extends CouchbaseDocument {
    @DocumentProperty("uid")
    private Property<UUID> _uid=new ImmutableProperty<UUID>(AbstractJob.this,UUID.randomUUID());
    @DocumentProperty(value = "state",getter = "getJobState",setter = "setJobState")
    private Property<State> _state=new StandardProperty<State>(AbstractJob.this,State.NEW);
    @DocumentProperty("tasks")
    private ListProperty<AbstractTask> _taskList = new ArrayListProperty<AbstractTask>(AbstractJob.this);
    @DocumentProperty("lastRunError")
    private Property<String> _errorName=new StandardProperty<String>(AbstractJob.this);
    /**
     *  request : The request content for this job
     */
    @DocumentProperty("request")
    private Property<TREQ> _request = new StandardProperty<TREQ>(AbstractJob.this);
    /**
     *  result : The result content for this job
     */
    @DocumentProperty("result")
    private Property<TRES> _result = new StandardProperty<TRES>(AbstractJob.this);


    //Save current processing Service
    private JobProcessingService _processingService=null;

    public AbstractJob(TREQ request){_request.set(request);}
    public AbstractJob(){}


    public String getLastRunError(){return _errorName.get();}
    public void setLastRunError(String errorName){_errorName.set(errorName);}

    public State getJobState() { return _state.get(); }
    public void setJobState(State state) { _state.set(state); }
    public boolean isInitialized(){ return _state.get().compareTo(State.INITIALIZED)>=0; }
    public boolean isPrepared(){ return _state.get().compareTo(State.PREPROCESSED)>=0; }
    public boolean isProcessed(){ return _state.get().compareTo(State.PROCESSED)>=0; }
    public boolean isFinalized(){ return _state.get().compareTo(State.POSTPROCESSED)>=0; }
    public boolean isDone(){ return _state.get().compareTo(State.DONE)>=0; }
    // uid accessors
    public UUID getUid() { return _uid.get(); }
    public void setUid(UUID uid) { _uid.set(uid); }
    // result accessors
    public TRES getResult() { return _result.get(); }
    public void setResult(TRES val) { _result.set(val); }

    // request accessors
    public TREQ getRequest() { return _request.get(); }
    public void setRequest(TREQ val) { _request.set(val); }

    public List<AbstractTask> getTasks() { return _taskList.get();}
    public void setTasks(Collection<AbstractTask> tasks) {
        _taskList.clear();
        for(AbstractTask task : tasks){
            addTask(task);
        }
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
        return String.format("%010d", _taskList.size());
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
        UNKNOWN,
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
