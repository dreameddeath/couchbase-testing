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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Christophe Jeunesse on 23/11/2014.
 */
public class JobContext<T extends AbstractJob> {
    private final ICouchbaseSession session;
    private final IJobExecutorService<T> executorService;
    private final IJobProcessingService<T> processingService;
    private final ExecutorClientFactory clientFactory;
    //private final IExecutorServiceFactory executorFactory;
    //private final IProcessingServiceFactory processingFactory;
    private final T job;
    private final MetricRegistry metricRegistry;
    private boolean isJobSaved;
    private final List<TaskContext<T,?>> tasks=new ArrayList<>();
    private int loadedTaskCounter=0;

    private JobContext(Builder<T> jobCtxtBuilder){
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
    public T getJob() {
        return job;
    }
    public ProcessState getJobState(){
        return job.getStateInfo();
    }
    public List<TaskContext<T,?>> getTaskContexts() {
        return Collections.unmodifiableList(tasks);
    }

    public <TTASK extends AbstractTask> TaskContext<T,TTASK> getTaskContext(int pos,Class<TTASK> taskClass) {
        return (TaskContext<T,TTASK>)tasks.get(pos);
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
        //Save new tasks before continuing
        for(TaskContext<T,?> taskContext:tasks){
            if(taskContext.getTask().getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.NEW)){
                taskContext.save();
            }
        }
        //Save the job itself
        session.save(job);
        updateIsJobSaved();
    }

    public void resyncTasksContext() throws DaoException,StorageException,InterruptedException{
        int nbTaskContext =(int)session.getCounter(String.format(TaskDao.TASK_CNT_FMT,job.getUid()));
        if(nbTaskContext>loadedTaskCounter){
            final CountDownLatch nbToLoad=new CountDownLatch(nbTaskContext-loadedTaskCounter);
            final AtomicInteger nbFailure=new AtomicInteger(0);
            final String uid = job.getUid().toString();
            final int previousSize=tasks.size();
            for(int taskId=loadedTaskCounter+1;taskId<=nbTaskContext;++taskId){
                session.asyncGetFromKeyParams(AbstractTask.class,uid,taskId)
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
            }
            nbToLoad.await(1, TimeUnit.MINUTES);
            loadedTaskCounter=nbTaskContext;
            for(int pos=previousSize;pos<tasks.size();++pos){
                tasks.get(pos).updatePreRequisistes();
            }
        }
    }

    public TaskContext<T,?> getNextExecutableTask(boolean resync) throws DaoException,StorageException,InterruptedException{
        List<TaskContext<T,?>> contexts=getPendingTasks(resync);
        for(TaskContext<T,?> context:contexts){
            if(context.getTaskState().isDone()){
               continue;
            }
            boolean allPreRequisitesValid = true;
            for(TaskContext<T,?> prereqCtxt:context.getPreRequisites()){
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


    public IJobExecutorService<T> getExecutorService() {
        return executorService;
    }

    public IJobProcessingService<T> getProcessingService() {
        return processingService;
    }

    public TaskContext<T,?> getNextExecutableTask() throws DaoException,StorageException,InterruptedException{
        return getNextExecutableTask(false);
    }

    public List<TaskContext<T,?>> getPendingTasks(boolean forceResync) throws DaoException,StorageException,InterruptedException{
        if(forceResync || tasks.size()==0){
            resyncTasksContext();
        }
        List<TaskContext<T,?>> result=new ArrayList<>();
        for(TaskContext<T,?> currCtxt:tasks){
            if(!currCtxt.getTaskState().isDone()){
                result.add(currCtxt);
            }
        }
        return result;
    }

    public static <T extends AbstractJob> JobContext<T> newContext(Builder<T> builder){
        return new JobContext<>(builder);
    }


    public <TTASK extends AbstractTask> TaskContext<T,TTASK> addTask(TTASK task){
        return TaskContext.newContext(this,task);
    }

    public void addTask(TaskContext<T,?> ctxt){
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
    }
}
