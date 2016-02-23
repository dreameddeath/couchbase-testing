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

import com.codahale.metrics.MetricRegistry;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.process.dao.TaskDao;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.model.AbstractJob;
import com.dreameddeath.core.process.model.AbstractTask;
import com.dreameddeath.core.process.model.ProcessState;
import com.dreameddeath.core.process.service.IJobExecutorService;
import com.dreameddeath.core.process.service.IJobProcessingService;
import com.dreameddeath.core.process.service.factory.impl.ExecutorClientFactory;
import com.dreameddeath.core.user.IUser;
import rx.Subscriber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Christophe Jeunesse on 23/11/2014.
 */
public class JobContext<TJOB extends AbstractJob> {
    private final ICouchbaseSession session;
    private final IJobExecutorService<TJOB> executorService;
    private final IJobProcessingService<TJOB> processingService;
    private final ExecutorClientFactory clientFactory;
    //private final IExecutorServiceFactory executorFactory;
    //private final IProcessingServiceFactory processingFactory;
    private final TJOB job;
    private final MetricRegistry metricRegistry;
    private boolean isJobSaved;
    private final List<TaskContext<TJOB,?>> tasks=new ArrayList<>();
    private int loadedTaskCounter=0;

    private JobContext(Builder<TJOB> jobCtxtBuilder){
        this.session = jobCtxtBuilder.session;
        this.clientFactory = jobCtxtBuilder.clientFactory;
        this.job = jobCtxtBuilder.job;
        this.executorService = jobCtxtBuilder.jobExecutorService;
        this.processingService = jobCtxtBuilder.jobProcessingService;
        this.metricRegistry=jobCtxtBuilder.metricRegistry;
        updateIsJobSaved();
    }

    public ICouchbaseSession getSession(){return session;}
    public IUser getUser(){ return session.getUser();}
    public ExecutorClientFactory getClientFactory(){return clientFactory;}
    public TJOB getJob() {
        return job;
    }
    public ProcessState getJobState(){
        return job.getStateInfo();
    }
    public List<TaskContext<TJOB,?>> getTaskContexts() {
        return Collections.unmodifiableList(tasks);
    }

    public <TTASK extends AbstractTask> TaskContext<TJOB,TTASK> getTaskContext(int pos, Class<TTASK> taskClass) {
        return (TaskContext<TJOB,TTASK>)tasks.get(pos);
    }

    public <TTASK extends AbstractTask> TTASK getTask(int pos,Class<TTASK> taskClass) {
        return (TTASK)tasks.get(pos).getTask();
    }


    public boolean isJobSaved() {
        return isJobSaved;
    }

    public void updateIsJobSaved(){
        this.isJobSaved = job.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.SYNC);
    }

    public void execute() throws JobExecutionException{
        this.job.getStateInfo().setLastRunError(null);
        executorService.execute(this);
    }

    public void save() throws ValidationException,DaoException,StorageException{
        List<AbstractTask> tasksWithoutIds=new ArrayList<>(tasks.size());
        List<TaskContext<TJOB,?>> tasksToSave=new ArrayList<>(tasks.size());
        //Update tasks ids before continuing
        for(TaskContext<TJOB,?> taskContext:tasks){
            if(taskContext.getTask().getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.NEW)){
                if(taskContext.getTask().getId()==null){
                    tasksWithoutIds.add(taskContext.getTask());
                }
                tasksToSave.add(taskContext);
            }
        }
        assignIds(tasksWithoutIds);
        for(TaskContext<TJOB,?> taskContext:tasksToSave){
            taskContext.save();
        }
        //Save the job itself
        session.save(job);
        updateIsJobSaved();
    }


    public Collection<AbstractTask> assignIds(Collection<AbstractTask> tasks) throws DaoException,StorageException{
        Long cntNewValue = session.incrCounter(String.format(TaskDao.TASK_CNT_FMT, job.getUid()), tasks.size());
        for(AbstractTask task:tasks){
            if(task.getId()==null){
                long id = cntNewValue--;
                if(task.getParentTaskId()!=null){
                    task.setId(task.getParentTaskId()+"-"+id);
                }
                else{
                    task.setId(Long.toString(id));
                }
            }
            job.addTask(task.getId());
        }
        return tasks;
    }


    public void resyncTasksContext() throws DaoException,StorageException,InterruptedException{
        /**
         * TODO : force reload of the job itself (not required if optimistic locking), then load all task not yet loaded
         */
        //int nbTaskContext =tasks.size();  //(int)session.getCounter(String.format(TaskDao.TASK_CNT_FMT,job.getUid()));
        if(tasks.size()<job.getTasks().size()) {
            List<String> missingTaskIds = new ArrayList<>(job.getTasks().size()-tasks.size());
            for(String taskId:job.getTasks()){
                TaskContext<TJOB,?> foundTaskContext = null;
                for(TaskContext<TJOB,?> taskContext:tasks){
                    if(taskContext.getTask().getId().equals(taskId)){
                        foundTaskContext = taskContext;
                        break;
                    }
                }
                if(foundTaskContext==null){
                    missingTaskIds.add(taskId);
                }
            }
            final CountDownLatch nbToLoad = new CountDownLatch(missingTaskIds.size());
            final AtomicInteger nbFailure=new AtomicInteger(0);
            final String uid = job.getUid().toString();
            for(String missingTaskId:missingTaskIds) {
                session.asyncGetFromKeyParams(AbstractTask.class,uid,missingTaskId)
                        .map(task->TaskContext.newContext(JobContext.this,task))
                        .subscribe(new Subscriber<TaskContext>() {
                            @Override
                            public void onCompleted() {
                                nbToLoad.countDown();
                            }

                            @Override
                            public void onError(Throwable e) {
                                nbToLoad.countDown();
                                nbFailure.incrementAndGet();
                            }

                            @Override
                            public void onNext(TaskContext taskContext) {
                                tasks.add(taskContext);
                            }
                        });
                nbToLoad.await(1, TimeUnit.MINUTES);
            }
            for(TaskContext<TJOB,?> taskContext:tasks){
                taskContext.updatePreRequisistes();
            }
        }
    }

    public TaskContext<TJOB,?> getNextExecutableTask(boolean resync) throws DaoException,StorageException,InterruptedException{
        List<TaskContext<TJOB,?>> contexts=getPendingTasks(resync);
        for(TaskContext<TJOB,?> context:contexts){
            if(context.getTaskState().isDone()){
               continue;
            }
            boolean allPreRequisitesValid = true;
            for(TaskContext<TJOB,?> prereqCtxt:context.getPreRequisites()){
                if(!prereqCtxt.getTaskState().isDone()){
                    allPreRequisitesValid=false;
                    break;
                }
            }
            if(allPreRequisitesValid){
                return context;
            }
        }
        return null;
    }


    public IJobExecutorService<TJOB> getExecutorService() {
        return executorService;
    }

    public IJobProcessingService<TJOB> getProcessingService() {
        return processingService;
    }

    public TaskContext<TJOB,?> getNextExecutableTask() throws DaoException,StorageException,InterruptedException{
        return getNextExecutableTask(false);
    }

    public List<TaskContext<TJOB,?>> getPendingTasks(boolean forceResync) throws DaoException,StorageException,InterruptedException{
        if(forceResync || tasks.size()==0){
            resyncTasksContext();
        }
        List<TaskContext<TJOB,?>> result=new ArrayList<>();
        for(TaskContext<TJOB,?> currCtxt:tasks){
            if(!currCtxt.getTaskState().isDone()){
                result.add(currCtxt);
            }
        }
        return result;
    }

    public static <T extends AbstractJob> JobContext<T> newContext(Builder<T> builder){
        return new JobContext<>(builder);
    }


    public <TTASK extends AbstractTask> TaskContext<TJOB,TTASK> addTask(TTASK task){
        return TaskContext.newContext(this,task);//Note the addTask(taskCtxt is called by the TaskContext constructor)
    }

    //Package level to be called by TaskContext
    void addTask(TaskContext<TJOB,?> ctxt){
        tasks.add(ctxt);
        if(ctxt.getTask().getJobUid()==null){
            ctxt.getTask().setJobUid(job.getUid());
        }
    }

    public static class Builder<T extends AbstractJob>{
        private final T job;
        private ExecutorClientFactory clientFactory;
        private ICouchbaseSession session;
        private IJobExecutorService<T> jobExecutorService=null;
        private IJobProcessingService<T> jobProcessingService=null;
        private MetricRegistry metricRegistry=null;

        public Builder(T job){
            this.job= job;
        }

        public Builder<T> withSession(ICouchbaseSession session) {
            this.session = session;
            return this;
        }

        public Builder<T> withClientFactory(ExecutorClientFactory clientFactory) {
            this.clientFactory = clientFactory;
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

        public Builder<T> fromJobContext(JobContext<T> jobContext){
            this.jobExecutorService = jobContext.executorService;
            this.jobProcessingService = jobContext.processingService;
            this.metricRegistry =jobContext.metricRegistry;
            this.clientFactory = jobContext.clientFactory;
            this.session = jobContext.session;
            return this;
        }
    }
}
