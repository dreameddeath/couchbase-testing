/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.process.service.context;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.process.exception.TaskObservableExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import com.dreameddeath.core.process.model.v1.base.ProcessState;
import com.dreameddeath.core.process.model.v1.tasks.SubJobProcessTask;
import com.dreameddeath.core.process.service.ITaskExecutorService;
import com.dreameddeath.core.process.service.ITaskProcessingService;
import com.dreameddeath.core.user.IUser;
import com.google.common.base.Preconditions;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 23/11/2014.
 */
public class TaskContext<TJOB extends AbstractJob,T extends AbstractTask> implements Comparable<TaskContext>{
    private final JobContext<TJOB> jobContext;
    private final ICouchbaseSession session;
    private final ITaskExecutorService<TJOB,T> taskExecutorService;
    private final ITaskProcessingService<TJOB,T> taskProcessingService;
    private final T task;
    private final String tempId;
    private final List<String> pendingPreRequisites = new ArrayList<>();

    private TaskContext(Builder<TJOB,T> taskCtxtBuilder){
        this.jobContext = taskCtxtBuilder.jobContext;
        this.session = taskCtxtBuilder.session;
        this.task = taskCtxtBuilder.task;
        this.taskExecutorService = taskCtxtBuilder.executorService;
        this.taskProcessingService = taskCtxtBuilder.processingService;
        this.tempId=taskCtxtBuilder.tempId;
        //Always keep it last
        this.jobContext.addTask((TaskContext<TJOB,AbstractTask>)this);
    }

    public JobContext<TJOB> getJobContext(){return jobContext;}
    public ICouchbaseSession getSession(){return session;}
    public IUser getUser() {return jobContext.getUser(); }
    public Observable<T> getTask() {
        return getSession().asyncGet(task.getBaseMeta().getKey(),(Class<T>)task.getClass());
    }

    public TaskContext<TJOB,T> getTemporaryReadOnlySessionContext(){
        return new Builder<>(this,this.task,this.getSession().getTemporaryReadOnlySession()).build();
    }

    public TaskContext<TJOB,T> getStandardSessionContext(){
        return new Builder<>(this,this.task,this.session.toParentSession()).build();
    }


    public T getInternalTask() {
        return task;
    }

    public ProcessState getTaskState() {
        return task.getStateInfo();
    }
    public Observable<TJOB> getParentJob(){ return jobContext.getJob();}
    public TJOB getParentInternalJob(){ return jobContext.getInternalJob();}
    public boolean isNew(){ return task.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.NEW);}
    public String getId(){ return task.getId();}
    public String getParentTaskId(){ return task.getParentTaskId();}
    public void setId(String id){
        task.setId(id);
    }

    public Observable<TaskContext<TJOB, T>> asyncSave() {
        Preconditions.checkNotNull(task.getId(),"The task should have an id before saving");
        return jobContext
                .getTaskContexts()
                .filter(ctxt->ctxt.tempId!=null && pendingPreRequisites.contains(ctxt.tempId))
                .map(ctxt->{
                    if(ctxt.getId()==null){
                        throw new TaskObservableExecutionException(this,"The pre-requisite task <"+ctxt.getTaskClass()+"/"+ctxt.tempId+"/"+ctxt.getInternalTask().toString()+"> doesn't have ids prior to save");
                    }
                    synchronized (task) {
                        task.addDependency(ctxt.getId());
                    }
                    return ctxt;
                })
                .toList()
                .flatMap(listDependencyCtxt-> getSession().asyncSave(this.task))
                .map(savedTask-> new Builder<>(this,savedTask).build());
    }

    public boolean isTaskSaved() {
        return task.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.SYNC);
    }

    public ITaskExecutorService<TJOB, T> getExecutorService() {
        return taskExecutorService;
    }

    public ITaskProcessingService<TJOB, T> getProcessingService() {
        return taskProcessingService;
    }

    public Observable<TaskContext<TJOB,T>> execute(){
        this.task.getStateInfo().setLastRunError(null);
        return taskExecutorService.execute(this);
    }

    public Observable<TaskContext<TJOB,AbstractTask>> getPreRequisites() {
        return jobContext.getTaskContexts()
                .filter(taskCtxt->
                        taskCtxt.tempId!=null && pendingPreRequisites.contains(taskCtxt.tempId)||
                        taskCtxt.getId()!=null && task.getDependencies().contains(taskCtxt.getId())
                );
    }

    public <TTASK extends AbstractTask> TaskContext<TJOB,TTASK> chainWith(TTASK targetTask){
        TaskContext<TJOB,TTASK> ctxt = TaskContext.newContext(jobContext,targetTask);
        if(task.getId()==null) {
            ctxt.pendingPreRequisites.add(this.tempId);
        }
        else{
            targetTask.addDependency(task.getId());
        }
        return ctxt;
    }

    public <TTASK extends AbstractTask> Observable<TaskContext<TJOB,TTASK>> getDependentTaskContext(final Class<TTASK> taskClass){
        return getPreRequisites()
                .flatMap(taskCtxt->{
                    Observable<TaskContext<TJOB,TTASK>> resultObs=taskCtxt.getDependentTaskContext(taskClass);
                    if(taskClass.isAssignableFrom(taskCtxt.getTaskClass())){
                        resultObs=Observable.merge(resultObs,(Observable)Observable.just(taskCtxt));
                    }
                    return resultObs;
                });
    }

    public <TTASK extends AbstractTask> Observable<TTASK> getDependentTask(Class<TTASK> taskClass){
        return getDependentTaskContext(taskClass).map(taskCtxt->taskCtxt.task);
    }

    //Package level to avoid
    static <TJOB extends AbstractJob,TTASK extends AbstractTask> TaskContext<TJOB,TTASK> newContext(JobContext<TJOB> ctxt,TTASK task){
        return ctxt.getClientFactory().buildTaskClient(ctxt.getJobClass(),(Class<TTASK>)task.getClass()).buildTaskContext(ctxt,task);
    }

    public static <TJOB extends AbstractJob,TTASK extends AbstractTask> Builder<TJOB,TTASK> builder(JobContext<TJOB> jobCtxt,TTASK task){
        return new Builder<>(jobCtxt,task);
    }

    public Class<T> getTaskClass() {
        return (Class<T>)task.getClass();
    }

    public boolean isSubJobTask(){
        return task instanceof SubJobProcessTask;
    }

    public Observable<? extends AbstractJob> getSubJob(){
        Preconditions.checkArgument(isSubJobTask());
        return ((SubJobProcessTask<? extends AbstractJob>)task).getJob(getSession());
    }

    public void setState(ProcessState.State state) {
        this.task.getStateInfo().setState(state);
    }

    public UUID getJobUid() {
        return task.getJobUid();
    }

    public void setJobUid(UUID jobUid){
        task.setJobUid(jobUid);
    }

    public IEventBus getEventBus() {
        return jobContext.getEventBus();
    }

    public static class Builder<TJOB extends AbstractJob,T extends AbstractTask>{
        private final JobContext<TJOB> jobContext;
        private final T task;
        private final String tempId;
        public final ICouchbaseSession session;
        private ITaskExecutorService<TJOB,T> executorService=null;
        private ITaskProcessingService<TJOB,T> processingService=null;

        public Builder(JobContext<TJOB> jobCtxt,TaskContext<TJOB,T> taskContext,T task,ICouchbaseSession session) {
            this.jobContext = jobCtxt;
            this.session =session;
            this.tempId=taskContext.tempId;
            this.task=task;
            this.executorService=taskContext.taskExecutorService;
            this.processingService=taskContext.taskProcessingService;
        }

        public Builder(TaskContext<TJOB,T> taskContext,T task,ICouchbaseSession session) {
            this(taskContext.jobContext,taskContext,task,session);
        }

        public Builder(JobContext<TJOB> parentJob,TaskContext<TJOB,T> taskContext){
            this(parentJob,taskContext,taskContext.getInternalTask(),taskContext.getSession());
        }

        public Builder(TaskContext<TJOB,T> taskContext,T task){
            this(taskContext,task,taskContext.getSession());
        }

        public Builder(JobContext<TJOB> jobCtxt,T task){
            this.jobContext = jobCtxt;
            this.session = jobCtxt.getSession();
            this.task=task;
            if(task.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.NEW)){
                tempId= UUID.randomUUID().toString();
            }
            else{
                tempId=null;
            }
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
    public String toString(){
        return super.toString()+
                "{ tid:"+(task.getId()!=null?task.getId():"unknown")+","+
                "  type:"+task.getClass().getName()+","+
                "  jid:"+jobContext.getJobId()+","+
                "  jtype:"+jobContext.getInternalJob().getClass().getName()+"}";

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskContext<?,?> that = (TaskContext<?,?>) o;
        if(tempId!=null){
            return tempId.equals(that.tempId);
        }
        else{
            return task.equals(that.task);
        }
    }

    @Override
    public int hashCode() {
        if(tempId!=null){
            return tempId.hashCode();
        }
        return task.hashCode();
    }

    @Override
    public int compareTo(TaskContext other) {
        if(this.equals(other)){
            return 0;
        }
        if(tempId!=null){
            if(other.tempId!=null){
                return tempId.compareTo(other.tempId);
            }
            return 1;
        }
        if(other.tempId!=null){
            return -1;
        }
        else if(task.getId()!=null) {
            if (other.task.getId() != null) {
                return task.getId().compareTo(other.task.getId());
            }
            return 1;
        }
        else if(other.getId()!=null){
            return -1;
        }
        else{
            return new Integer(task.hashCode()).compareTo(other.hashCode());
        }
    }
}
