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

package com.dreameddeath.core.process.service.context;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import com.dreameddeath.core.process.model.v1.base.ProcessState;
import com.dreameddeath.core.process.service.ITaskExecutorService;
import com.dreameddeath.core.process.service.ITaskProcessingService;
import com.dreameddeath.core.user.IUser;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 23/11/2014.
 */
public class TaskContext<TJOB extends AbstractJob,T extends AbstractTask> {
    private final JobContext<TJOB> jobContext;
    private final ITaskExecutorService<TJOB,T> taskExecutorService;
    private final ITaskProcessingService<TJOB,T> taskProcessingService;
    private final T task;
    private boolean isTaskSaved=false;
    private List<TaskContext<TJOB,?>> preRequisites=new ArrayList<>();

    private TaskContext(Builder<TJOB,T> taskCtxtBuilder){
        this.jobContext = taskCtxtBuilder.jobContext;
        this.task = taskCtxtBuilder.task;
        this.jobContext.addTask(this);
        this.taskExecutorService = taskCtxtBuilder.executorService;
        this.taskProcessingService = taskCtxtBuilder.processingService;
        updateIsTaskSaved();
    }

    public JobContext<TJOB> getJobContext(){return jobContext;}
    public ICouchbaseSession getSession(){return jobContext.getSession();}
    public IUser getUser() {return jobContext.getUser(); }
    public T getTask() {
        return task;
    }
    public ProcessState getTaskState() {
        return task.getStateInfo();
    }
    public TJOB getParentJob(){ return jobContext.getJob();}

    public void save() throws ValidationException,DaoException,StorageException{
        for(TaskContext<TJOB,?> preReq:preRequisites){
            if(preReq.getTask().getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.NEW)){
                preReq.save();
                Preconditions.checkNotNull(preReq.getTask().getId());
                task.addDependency(preReq.getTask().getId());
            }
        }
        if(task.getId()==null){
            jobContext.assignIds(Collections.singletonList(task));
        }
        getSession().save(task);
    }

    public Collection<AbstractTask> assignIds(Collection<AbstractTask> tasks) throws DaoException,StorageException{
        return jobContext.assignIds(tasks);
    }

    public boolean isTaskSaved() {
        return isTaskSaved;
    }

    public void updateIsTaskSaved(){
        this.isTaskSaved = task.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.SYNC);
    }

    public ITaskExecutorService<TJOB, T> getExecutorService() {
        return taskExecutorService;
    }

    public ITaskProcessingService<TJOB, T> getProcessingService() {
        return taskProcessingService;
    }

    public void execute() throws TaskExecutionException{
        this.task.getStateInfo().setLastRunError(null);
        taskExecutorService.execute(this);
    }


    public void updatePreRequisistes(){
        for(String id : task.getDependencies()){
            TaskContext<TJOB,?> foundTask=null;
            for(TaskContext<TJOB,?>currTask: jobContext.getTaskContexts()){
                if(currTask.getTask().getId().equals(id)){
                    foundTask=currTask;
                    break;
                }
            }
            if(foundTask==null){
                throw new RuntimeException("Cannot find pre-requisite task for id "+id);
            }
            preRequisites.add(foundTask);
        }
    }

    public List<TaskContext<TJOB,?>> getPreRequisites() {
        return preRequisites;
    }

    public <TTASK extends AbstractTask> TaskContext<TJOB,TTASK> chainWith(TTASK task){
        TaskContext<TJOB,TTASK> ctxt = TaskContext.newContext(jobContext,task);
        ctxt.preRequisites.add(this);
        return ctxt;
    }

    public <TTASK extends AbstractTask> TaskContext<TJOB,TTASK> getDependentTaskContext(Class<TTASK> taskClass){
        for(TaskContext<TJOB,?> dependantTask:preRequisites){
            if(taskClass.isAssignableFrom(dependantTask.getTask().getClass())){
                return (TaskContext<TJOB,TTASK>)dependantTask;
            }
        }
        for(TaskContext<TJOB,?> dependantTask:preRequisites){
            TaskContext<TJOB,TTASK> fromParent = dependantTask.getDependentTaskContext(taskClass);
            if(fromParent!=null){
                return fromParent;
            }
        }
        return null;
    }

    public <TTASK extends AbstractTask> TTASK getDependentTask(Class<TTASK> taskClass){
        TaskContext<TJOB,TTASK> taskCtxt = getDependentTaskContext(taskClass);
        if(taskClass!=null){
            return taskCtxt.getTask();
        }
        return null;
    }

    public static <TJOB extends AbstractJob,TTASK extends AbstractTask> TaskContext<TJOB,TTASK> newContext(JobContext<TJOB> ctxt,TTASK task){
        return ctxt.getClientFactory().buildTaskClient((Class<TJOB>)ctxt.getJob().getClass(),(Class<TTASK>)task.getClass()).buildTaskContext(ctxt,task);
    }

    public static <TJOB extends AbstractJob,TTASK extends AbstractTask> Builder<TJOB,TTASK> builder(JobContext<TJOB> jobCtxt,TTASK task){
        return new Builder<>(jobCtxt,task);
    }

    public static class Builder<TJOB extends AbstractJob,T extends AbstractTask>{
        private final JobContext<TJOB> jobContext;
        private final T task;
        private ITaskExecutorService<TJOB,T> executorService=null;
        private ITaskProcessingService<TJOB,T> processingService=null;

        public Builder(JobContext<TJOB> jobCtxt,T task){
            this.jobContext = jobCtxt;
            this.task=task;
        }

        public Builder<TJOB,T> withExecutorService(ITaskExecutorService<TJOB, T> executorService) {
            this.executorService = executorService;
            return this;
        }

        public Builder<TJOB,T> withProcessingService(ITaskProcessingService<TJOB, T> processingService) {
            this.processingService = processingService;
            return this;
        }

        public TaskContext<TJOB,T> build(){
            return new TaskContext<>(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskContext<?,?> that = (TaskContext<?,?>) o;
        return task.equals(that.task);
    }

    @Override
    public int hashCode() {
        return task.hashCode();
    }
}
