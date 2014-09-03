package com.dreameddeath.core.model.process;

import java.util.*;

import com.dreameddeath.core.annotation.NotNull;
import com.dreameddeath.core.annotation.Validate;
import com.dreameddeath.core.event.TaskProcessEvent;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.exception.process.JobExecutionException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.*;
import com.dreameddeath.core.process.JobProcessingService;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
@JsonTypeInfo(use=Id.MINIMAL_CLASS,include=As.PROPERTY, property="@c")
public abstract class AbstractJob<TREQ extends CouchbaseDocumentElement,TRES extends CouchbaseDocumentElement> extends CouchbaseDocument {
    @DocumentProperty("uid")
    private Property<UUID> _uid=new ImmutableProperty<UUID>(AbstractJob.this,UUID.randomUUID());
    @DocumentProperty(value = "state",getter = "getJobState",setter = "setJobState")
    private Property<State> _state=new StandardProperty<State>(AbstractJob.this,State.NEW);
    @DocumentProperty("tasks") @Validate
    private ListProperty<AbstractTask> _taskList = new ArrayListProperty<AbstractTask>(AbstractJob.this);
    @DocumentProperty("lastRunError")
    private Property<String> _errorName=new StandardProperty<String>(AbstractJob.this);
    /**
     *  request : The request content for this job
     */
    @DocumentProperty("request") @NotNull @Validate
    private Property<TREQ> _request = new StandardProperty<TREQ>(AbstractJob.this);
    /**
     *  result : The result content for this job
     */
    @DocumentProperty("result")
    private Property<TRES> _result = new StandardProperty<TRES>(AbstractJob.this);


    //Save current processing Service
    private JobProcessingService _processingService=null;

    public AbstractJob(TREQ request,TRES result){
        _request.set(request);
        _result.set(result);
    }

    public AbstractJob(TREQ request){
        _request.set(request);
        _result.set(newResult());
    }
    public AbstractJob(){
        _request.set(newRequest());
        _result.set(newResult());
    }

    public abstract TREQ newRequest();
    public abstract TRES newResult();

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
            if(id.equals(task.getUid())){ return task; }
        }
        return null;
    }

    public AbstractTask getTask(Integer pos) {
        return _taskList.get(pos);
    }
    public <T extends AbstractTask> T getTask(Integer pos,Class<T> clazz) {return (T)getTask(pos);}
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


    public boolean init() throws JobExecutionException{return false;}
    public boolean preprocess()throws JobExecutionException{return false;}
    public abstract boolean when(TaskProcessEvent evt) throws JobExecutionException;
    public boolean postprocess()throws JobExecutionException{return false;}
    public boolean cleanup()throws JobExecutionException{return false;}

    public final void setProcessingService(JobProcessingService service){
        _processingService = service;
    }

    public final JobProcessingService getProcessingService(){
        return _processingService ;
    }
}
