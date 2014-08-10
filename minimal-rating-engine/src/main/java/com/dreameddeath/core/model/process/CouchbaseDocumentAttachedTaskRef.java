package com.dreameddeath.core.model.process;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.ImmutableProperty;
import com.dreameddeath.core.model.property.Property;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.MINIMAL_CLASS, include= JsonTypeInfo.As.PROPERTY, property="@c")
public class CouchbaseDocumentAttachedTaskRef extends CouchbaseDocumentElement{
    @DocumentProperty("jobKey")
    private Property<String> _jobKey = new ImmutableProperty<String>(CouchbaseDocumentAttachedTaskRef.this);
    @DocumentProperty("taskId")
    private Property<String> _taskId = new ImmutableProperty<String>(CouchbaseDocumentAttachedTaskRef.this);

    public String getJobKey(){ return _jobKey.get();}
    public void setJobKey(String key){ _jobKey.set(key);}

    public String getTaskId(){ return _taskId.get();}
    public void setTaskId(String id){ _taskId.set(id);}


    public boolean isForTask(AbstractTask task){
        return task.getParentJob().getKey().equals(_jobKey.get()) && task.getUid().equals(_taskId.get());
    }

    public static enum State{
        INITIALIZED,
        PROCESSED;
    }

}
