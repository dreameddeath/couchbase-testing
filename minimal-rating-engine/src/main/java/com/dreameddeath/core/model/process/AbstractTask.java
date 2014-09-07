package com.dreameddeath.core.model.process;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.annotation.NotNull;
import com.dreameddeath.core.exception.process.TaskExecutionException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.process.TaskProcessingService;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import org.joda.time.DateTime;

@JsonTypeInfo(use=Id.MINIMAL_CLASS, include=As.PROPERTY, property="@c")
public abstract class AbstractTask extends CouchbaseDocumentElement{
    private AbstractJob _parentJob;
    @DocumentProperty("uid") @NotNull
    private Property<String> _uid=new ImmutableProperty<String>(AbstractTask.this);
    @DocumentProperty("label")
    private Property<String> _label=new StandardProperty<String>(AbstractTask.this);
    @DocumentProperty(value = "state") @NotNull
    private Property<State> _state=new StandardProperty<State>(AbstractTask.this,State.NEW);
    @DocumentProperty("effectiveDate")
    private Property<DateTime> _effectiveDate=new StandardProperty<DateTime>(AbstractTask.this);
    @DocumentProperty("lastRunError")
    private Property<String> _errorName=new StandardProperty<String>(AbstractTask.this);


    //TaskProcessingService
    private TaskProcessingService _processingService=null;

    public void setParentJob(AbstractJob job){ _parentJob = job;}
    public AbstractJob getParentJob(){ return _parentJob;}
    public <T extends AbstractJob> T getParentJob(Class<T> clazz){ return (T)_parentJob;}
    public <T> T getJobRequest(Class<T> reqClass){ return (T) getParentJob().getRequest();}
    public <T> T getJobResult(Class<T> resClass){ return (T) getParentJob().getResult();}

    public String getLastRunError(){return _errorName.get();}
    public void setLastRunError(String errorName){_errorName.set(errorName);}

    public State getState() { return _state.get(); }
    public void setState(State state) { _state.set(state); }
    public boolean isInitialized(){ return _state.get().compareTo(State.INITIALIZED)>=0; }
    public boolean isPrepared(){ return _state.get().compareTo(State.PREPROCESSED)>=0; }
    public boolean isProcessed(){ return _state.get().compareTo(State.PROCESSED)>=0; }
    public boolean isFinalized(){ return _state.get().compareTo(State.POSTPROCESSED)>=0; }
    public boolean isDone(){ return _state.get().compareTo(State.DONE)>=0; }

    public String getUid() { return _uid.get(); }
    public void setUid(String uid) { _uid.set(uid); }

    public String getLabel(){ return _label.get(); }
    public void setLabel(String label){ _label.set(label); }
    
    public enum State{
        UNKNOWN,
        NEW,
        INITIALIZED, //Init done
        PREPROCESSED,
        PROCESSED, //Processing Done
        POSTPROCESSED, //Job Update Processing done
        DONE;//Cleaning done
    }


    public boolean init() throws TaskExecutionException{return false;}
    public boolean preprocess() throws TaskExecutionException{return false;}
    public boolean process() throws TaskExecutionException {return false;}
    public boolean postprocess() throws TaskExecutionException{return false;}
    public boolean finish() throws TaskExecutionException{return false;}
    public boolean cleanup() throws TaskExecutionException{return false;}

    public final void setProcessingService(TaskProcessingService service){
        _processingService = service;
    }
    public final TaskProcessingService getProcessingService(){
        return _processingService;
    }

    public <T extends CouchbaseDocument> T newEntity(Class<T> clazz){
        return getParentJob().getSession().newEntity(clazz);
    }
}