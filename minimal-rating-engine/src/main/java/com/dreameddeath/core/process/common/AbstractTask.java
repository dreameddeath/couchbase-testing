package com.dreameddeath.core.process.common;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.annotation.NotNull;
import com.dreameddeath.core.exception.process.DuplicateTaskException;
import com.dreameddeath.core.exception.process.TaskExecutionException;
import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.business.CouchbaseDocument;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;

@JsonTypeInfo(use=Id.MINIMAL_CLASS, include=As.PROPERTY, property="@c")
public abstract class AbstractTask extends BaseCouchbaseDocumentElement {
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
    public AbstractJob getParentJob(){ return getFirstParentOfClass(AbstractJob.class);}
    public <T extends AbstractJob> T getParentJob(Class<T> clazz){ return (T)getParentJob();}
    public <T> T getJobRequest(Class<T> reqClass){ return (T) getParentJob().getRequest();}
    public <T> T getJobResult(Class<T> resClass){ return (T) getParentJob().getResult();}


    public enum State{
        UNKNOWN,
        NEW,
        INITIALIZED, //Init done
        PREPROCESSED,
        PROCESSED, //Processing Done
        POSTPROCESSED, //Job Update Processing done
        DONE//Cleaning done
    }


    public boolean init() throws TaskExecutionException{return false;}
    public boolean preprocess() throws TaskExecutionException{return false;}
    public boolean process() throws TaskExecutionException {return false;}
    public boolean postprocess() throws TaskExecutionException{return false;}
    public boolean finish() throws TaskExecutionException{return false;}
    public boolean cleanup() throws TaskExecutionException{return false;}


    public <T extends CouchbaseDocument> T newEntity(Class<T> clazz){
        return (T)getParentJob().newEntity(clazz);
    }

    public <T extends AbstractTask> T chainWith(T task) throws DuplicateTaskException{
        this.getParentJob().addTask(task);
        task.addDependency(this.getUid());
        return task;
    }
}