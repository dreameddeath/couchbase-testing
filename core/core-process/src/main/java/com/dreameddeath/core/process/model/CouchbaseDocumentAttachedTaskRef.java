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

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.validation.annotation.NotNull;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;


/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.MINIMAL_CLASS, include= JsonTypeInfo.As.PROPERTY, property="@c")
public class CouchbaseDocumentAttachedTaskRef extends CouchbaseDocumentElement {
    @DocumentProperty("jobUid") @NotNull
    private Property<UUID> jobUid = new ImmutableProperty<>(CouchbaseDocumentAttachedTaskRef.this);
    /**
     *  jobClass : class of the job
     */
    @DocumentProperty("jobClass") @NotNull
    private Property<String> jobClassName = new ImmutableProperty<>(CouchbaseDocumentAttachedTaskRef.this);

    @DocumentProperty("taskId") @NotNull
    private Property<String> taskId = new ImmutableProperty<>(CouchbaseDocumentAttachedTaskRef.this);
    /**
     *  taskClass : the class of the task
     */
    @DocumentProperty("taskClass") @NotNull
    private Property<String> taskClassName = new ImmutableProperty<>(CouchbaseDocumentAttachedTaskRef.this);

    // jobKey accessors
    public UUID getJobUid(){ return jobUid.get();}
    public void setJobUid(UUID key){ jobUid.set(key);}
    // jobClass accessors
    public String getJobClass() { return jobClassName.get(); }
    public void setJobClass(String val) { jobClassName.set(val); }
    // task id accessors
    public String getTaskId(){ return taskId.get();}
    public void setTaskId(String id){ taskId.set(id);}
    // taskClass accessors
    public String getTaskClass() { return taskClassName.get(); }
    public void setTaskClass(String val) { taskClassName.set(val); }

    public boolean isForTask(AbstractTask task){
        return task.getJobUid().equals(jobUid.get()) && task.getId().equals(taskId.get());
    }

    public boolean isOfJobType(Class<? extends AbstractJob> jobClass){
        try {
            return jobClass.isAssignableFrom(Class.forName(jobClassName.get()));
        }
        catch(ClassNotFoundException e){
            throw new RuntimeException("The job class <"+jobClassName.get()+"> is not found",e);
        }
    }

    public boolean isOfTaskType(Class<? extends AbstractTask> taskClass){
        try {
            return taskClass.isAssignableFrom(Class.forName(taskClassName.get()));
        }
        catch(ClassNotFoundException e){
            throw new RuntimeException("The task class <"+taskClassName.get()+"> is not found",e);
        }
    }

    public boolean isOfType(Class<? extends AbstractJob> jobClass,Class<? extends AbstractTask> taskClass){
        return isOfJobType(jobClass) && isOfTaskType(taskClass);
    }

    public enum State{
        INITIALIZED,
        PROCESSED
    }
}
