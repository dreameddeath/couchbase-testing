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

import com.codahale.metrics.MetricRegistry;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.DaoObservableException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.process.dao.TaskDao;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import com.dreameddeath.core.process.model.v1.base.ProcessState;
import com.dreameddeath.core.process.service.IJobExecutorService;
import com.dreameddeath.core.process.service.IJobProcessingService;
import com.dreameddeath.core.process.service.factory.impl.ExecutorClientFactory;
import com.dreameddeath.core.user.IUser;
import rx.Observable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 23/11/2014.
 */
public class JobContext<TJOB extends AbstractJob> {
    private final ICouchbaseSession session;
    private final IJobExecutorService<TJOB> executorService;
    private final IJobProcessingService<TJOB> processingService;
    private final ExecutorClientFactory clientFactory;
    private final TJOB job;
    private final MetricRegistry metricRegistry;
    private final Set<TaskContext<TJOB,AbstractTask>> taskContextsCache;
    private final Map<String,String> sharedTaskTempIdMap;
    private final IEventBus eventBus;

    private JobContext(Builder<TJOB> jobCtxtBuilder){
        this.session = jobCtxtBuilder.session;
        this.clientFactory = jobCtxtBuilder.clientFactory;
        this.job = jobCtxtBuilder.job;
        this.executorService = jobCtxtBuilder.jobExecutorService;
        this.processingService = jobCtxtBuilder.jobProcessingService;
        this.metricRegistry=jobCtxtBuilder.metricRegistry;
        this.sharedTaskTempIdMap =jobCtxtBuilder.sharedTaskTempIdMap;
        this.taskContextsCache=new TreeSet<>();
        this.eventBus=jobCtxtBuilder.bus;
        jobCtxtBuilder.taskContextsCache.forEach(ctxt->new TaskContext.Builder<>(this,ctxt).build());
    }

    public ICouchbaseSession getSession(){
        return session;
    }

    public IUser getUser(){
        return session.getUser();
    }

    public ExecutorClientFactory getClientFactory(){
        return clientFactory;
    }

    public Observable<TJOB> getJob() {
        return session.asyncGet(job.getBaseMeta().getKey(),(Class<TJOB>)job.getClass())
                .map(freshJob->{freshJob.getBaseMeta().freeze();return freshJob;});
    }

    public Class<TJOB> getJobClass(){
        return (Class<TJOB>)job.getClass();
    }

    public ProcessState getJobState(){
        return job.getStateInfo();
    }

    public <TTASK extends AbstractTask> List<TaskContext<TJOB,TTASK>> getTasks(Class<TTASK> taskClass) {
        List<TaskContext<TJOB,TTASK>> result = new LinkedList<>();
        synchronized (taskContextsCache) {
            for (TaskContext<TJOB, ? extends AbstractTask> taskCtxt : taskContextsCache) {
                if (taskClass.isAssignableFrom(taskCtxt.getTaskClass())) {
                    result.add((TaskContext<TJOB, TTASK>) taskCtxt);
                }
            }
        }
        return result;
    }

    public boolean isJobSaved() {
        return job.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.SYNC);
    }

    public Observable<JobContext<TJOB>> execute(){
        this.job.getStateInfo().setLastRunError(null);
        return executorService.execute(this);
    }

    public Observable<JobContext<TJOB>> asyncSave(){
        return getPendingTasks()
                .filter(taskCtxt->taskCtxt.getId()==null)
                .toList()//merge
                .flatMap(this::assignTaskContextIds)
                .toList()//Merge
                .flatMap(fullList->getPendingTasks())
                .filter(TaskContext::isNew)
                .flatMap(TaskContext::asyncSave)
                .toList()
                .flatMap(listTasks->session.asyncSave(job))
                .map(newJob->new Builder<>(newJob,this).build());
    }

    public <TTASK extends AbstractTask> Observable<TaskContext<TJOB,TTASK>> assignTaskContextId(TaskContext<TJOB,TTASK> taskContext) {
        return (Observable) assignTaskContextIds(Collections.singletonList(taskContext));
    }

    public Observable<TaskContext<TJOB,? extends AbstractTask>> assignTaskContextIds(Collection<? extends TaskContext<TJOB,? extends AbstractTask>> taskContexts){
        try {
            //Get the right number of ids
            Observable<Long> counterIncObs = session.asyncIncrCounter(
                        String.format(TaskDao.TASK_CNT_FMT, job.getUid()),
                        taskContexts.stream().filter(ctxt->ctxt.getId()==null).count()
                    );
            return counterIncObs.map(cntValue -> {
                Long cntCurrValue = cntValue;
                for (TaskContext<TJOB,?> taskContext : taskContexts) {
                    if (taskContext.getId() == null) {
                        long id = cntCurrValue--;
                        if (taskContext.getParentTaskId() != null) {//TODO should be managed at task level
                            taskContext.setId(taskContext.getParentTaskId() + "-" + id);
                        } else {
                            taskContext.setId(Long.toString(id));
                        }
                    }
                    synchronized (job) {
                        job.addTask(taskContext.getId());
                    }
                }
                return taskContexts;
            }).flatMap(Observable::from);
        }
        catch(DaoException e){
            return Observable.error(new DaoObservableException(e));
        }
    }

    public Observable<TaskContext<TJOB,AbstractTask>> getTaskContexts(){
        final List<TaskContext<TJOB,AbstractTask>> currTaskContexts;
        synchronized (taskContextsCache){
            currTaskContexts=new ArrayList<>(taskContextsCache);
        }
        List<String> missingTaskIds = new ArrayList<>(job.getTasks().size());
        List<Observable<TaskContext<TJOB,AbstractTask>>> missingAbstractJobsObservable=new ArrayList<>(missingTaskIds.size());
        Set<String> attachedTasks = job.getTasks();
        //Loop on attached tasks to find out not yet loaded ones (ids)
        for(String taskId:attachedTasks){
            TaskContext<TJOB,AbstractTask> foundTaskContext = null;
            for(TaskContext<TJOB,AbstractTask> taskContext: currTaskContexts){
                if(taskId.equals(taskContext.getId())){
                    foundTaskContext = taskContext;
                    break;
                }
            }
            if(foundTaskContext==null){
                missingTaskIds.add(taskId);
            }
        }
        final String uid = job.getUid().toString();
        //Perform loading of missing ones
        for(String missingTaskId:missingTaskIds) {
            Observable<TaskContext<TJOB,AbstractTask>> taskContextObservable= session.asyncGetFromKeyParams(AbstractTask.class,uid,missingTaskId)
                    .map(task->TaskContext.newContext(JobContext.this,task));
            missingAbstractJobsObservable.add(taskContextObservable);
        }
        //Merge loading in one global list
        return Observable.merge(missingAbstractJobsObservable)
                .reduce(new ArrayList<>(currTaskContexts),
                        (list,taskContext)->{list.add(taskContext);return list;}
                        )
                //Ask child tasks to update their pre-requisites
                /*.map(taskContextArrayList->{
                    taskContextArrayList.forEach(TaskContext::updatePendingPreRequisistes);
                    return taskContextArrayList;
                })*/
                .flatMap(Observable::from);
    }

    public Observable<TaskContext<TJOB,AbstractTask>> getNextExecutableTasks(boolean resync){
        Observable<TaskContext<TJOB,AbstractTask>> pendingTaskContextsObservable;
        if(resync){
            pendingTaskContextsObservable=session.asyncGet(job.getBaseMeta().getKey(),(Class<TJOB>) job.getClass())
                    .map(newJob->new Builder<>(newJob,this).build())
                    .flatMap(JobContext::getPendingTasks);
        }
        else{
            pendingTaskContextsObservable=getPendingTasks();
        }
        return pendingTaskContextsObservable.concatMap(taskCtxt->
                    taskCtxt.getPreRequisites()
                        .filter(preReqCxt->!preReqCxt.getTaskState().isDone())
                        .count()
                        .filter(resCount->resCount==0)
                        .map(resCount->taskCtxt)
                )
                .map(taskCxt->taskCxt);
    }


    public IJobExecutorService<TJOB> getExecutorService() {
        return executorService;
    }

    public IJobProcessingService<TJOB> getProcessingService() {
        return processingService;
    }

    public Observable<TaskContext<TJOB,AbstractTask>> getNextExecutableTasks() {
        return getNextExecutableTasks(false);
    }

    public Observable<TaskContext<TJOB,AbstractTask>> getPendingTasks(){
        return getTaskContexts().filter(taskCtxt->(!taskCtxt.getTaskState().isDone() || taskCtxt.isNew()));
    }

    public Observable<TaskContext<TJOB,AbstractTask>> getExecutedTasks(){
        return getTaskContexts().filter(taskCtxt->taskCtxt.getTaskState().isDone());
    }


    public static <T extends AbstractJob> JobContext<T> newContext(Builder<T> builder){
        return new JobContext<>(builder);
    }


    public <TTASK extends AbstractTask> TaskContext<TJOB,TTASK> addTask(TTASK task){
        return TaskContext.newContext(this,task);//Note the addTask(taskCtxt) - then cache update also - is called by the TaskContext constructor)
    }

    //Package level to be called only by TaskContext
    void addTask(TaskContext<TJOB,AbstractTask> ctxt){
        synchronized (taskContextsCache) {
            taskContextsCache.remove(ctxt);
            taskContextsCache.add(ctxt);
        }

        if(ctxt.getJobUid()==null){
            ctxt.setJobUid(job.getUid());
        }
    }

    public ProcessState getStateInfo() {
        return job.getStateInfo();
    }

    public Map<String,String> getSharedTaskTempIdMap() {
        return Collections.unmodifiableMap(sharedTaskTempIdMap) ;
    }

    public void setState(ProcessState.State state) {
        this.job.getStateInfo().setState(state);
    }

    public UUID getJobId() {
        return job.getUid();
    }

    public TJOB getInternalJob() {
        return job;
    }

    public boolean isNew() {
        return job.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.NEW);
    }

    public IEventBus getEventBus() {
        return eventBus;
    }


    @Override
    public String toString(){
        return super.toString()+
                "{ id:"+job.getUid()+","+
                "  type:"+job.getClass().getName()+"}";
    }

    public static class Builder<T extends AbstractJob>{
        private final T job;
        private final Map<String,String> sharedTaskTempIdMap;
        private final Set<TaskContext<T,AbstractTask>> taskContextsCache;
        private IEventBus bus;
        private ExecutorClientFactory clientFactory;
        private ICouchbaseSession session;
        private IJobExecutorService<T> jobExecutorService=null;
        private IJobProcessingService<T> jobProcessingService=null;
        private MetricRegistry metricRegistry=null;

        public Builder(T job){
            this.job= job;
            this.sharedTaskTempIdMap =new ConcurrentHashMap<>();
            this.taskContextsCache=new TreeSet<>();
        }

        public Builder(T job,JobContext<T> rootJobCtxt){
            this.job= job;
            this.sharedTaskTempIdMap =rootJobCtxt.sharedTaskTempIdMap;
            this.jobExecutorService = rootJobCtxt.executorService;
            this.jobProcessingService = rootJobCtxt.processingService;
            this.metricRegistry =rootJobCtxt.metricRegistry;
            this.clientFactory = rootJobCtxt.clientFactory;
            this.session = rootJobCtxt.session;
            this.taskContextsCache=rootJobCtxt.taskContextsCache;
            this.bus=rootJobCtxt.eventBus;
        }


        public Builder<T> withSession(ICouchbaseSession session) {
            this.session = session;
            return this;
        }

        public Builder<T> withClientFactory(ExecutorClientFactory clientFactory) {
            this.clientFactory = clientFactory;
            return this;
        }

        public Builder<T> withEventBus(IEventBus bus){
            this.bus=bus;
            return this;
        }

        public Builder<T> withJobExecutorService(IJobExecutorService<T> jobExecutorService) {
            this.jobExecutorService = jobExecutorService;
            return this;
        }

        public Builder<T> withJobProcessingService(IJobProcessingService<T> jobProcessingService) {
            this.jobProcessingService = jobProcessingService;
            return this;
        }

        public Builder<T> withMetricRegistry(MetricRegistry metricRegistry) {
            this.metricRegistry = metricRegistry;
            return this;
        }

        public JobContext<T> build() {
            return new JobContext<>(this);
        }
    }
}
