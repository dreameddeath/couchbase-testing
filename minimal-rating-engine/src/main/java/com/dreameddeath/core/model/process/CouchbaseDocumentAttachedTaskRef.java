package com.dreameddeath.core.model.process;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.annotation.NotNull;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.ImmutableProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.StandardProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.MINIMAL_CLASS, include= JsonTypeInfo.As.PROPERTY, property="@c")
public class CouchbaseDocumentAttachedTaskRef extends CouchbaseDocumentElement{

    @DocumentProperty("jobKey") @NotNull
    private Property<String> _jobKey = new ImmutableProperty<String>(CouchbaseDocumentAttachedTaskRef.this);
    /**
     *  jobClass : class of the job
     */
    @DocumentProperty("jobClass") @NotNull
    private Property<String> _jobClassName = new ImmutableProperty<String>(CouchbaseDocumentAttachedTaskRef.this);

    @DocumentProperty("taskId") @NotNull
    private Property<String> _taskId = new ImmutableProperty<String>(CouchbaseDocumentAttachedTaskRef.this);
    /**
     *  taskClass : the class of the task
     */
    @DocumentProperty("taskClass") @NotNull
    private Property<String> _taskClassName = new ImmutableProperty<String>(CouchbaseDocumentAttachedTaskRef.this);

    // jobKey accessors
    public String getJobKey(){ return _jobKey.get();}
    public void setJobKey(String key){ _jobKey.set(key);}
    // jobClass accessors
    public String getJobClass() { return _jobClassName.get(); }
    public void setJobClass(String val) { _jobClassName.set(val); }
    // task id accessors
    public String getTaskId(){ return _taskId.get();}
    public void setTaskId(String id){ _taskId.set(id);}
    // taskClass accessors
    public String getTaskClass() { return _taskClassName.get(); }
    public void setTaskClass(String val) { _taskClassName.set(val); }

    public boolean isForTask(AbstractTask task){
        return task.getParentJob().getKey().equals(_jobKey.get()) && task.getUid().equals(_taskId.get());
    }

    public boolean isOfJobType(Class<? extends AbstractJob> jobClass){
        try {
            return jobClass.isAssignableFrom(Class.forName(_jobClassName.get()));
        }
        catch(ClassNotFoundException e){
            throw new RuntimeException("The job class <"+_jobClassName.get()+"> is not found",e);
        }
    }

    public boolean isOfTaskType(Class<? extends AbstractTask> taskClass){
        try {
            return taskClass.isAssignableFrom(Class.forName(_taskClassName.get()));
        }
        catch(ClassNotFoundException e){
            throw new RuntimeException("The task class <"+_taskClassName.get()+"> is not found",e);
        }
    }

    public boolean isOfType(Class<? extends AbstractJob> jobClass,Class<? extends AbstractTask> taskClass){
        return isOfJobType(jobClass) && isOfTaskType(taskClass);
    }

    public static enum State{
        INITIALIZED,
        PROCESSED;
    }
}
