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

package com.dreameddeath.core.process.service.impl;

import com.codahale.metrics.MetricRegistry;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.session.ICouchbaseSessionFactory;
import com.dreameddeath.core.process.exception.ExecutorServiceNotFoundException;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.exception.ProcessingServiceNotFoundException;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.model.AbstractJob;
import com.dreameddeath.core.process.model.AbstractTask;
import com.dreameddeath.core.process.service.*;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.factory.IExecutorServiceFactory;
import com.dreameddeath.core.process.service.factory.IProcessingServiceFactory;
import com.dreameddeath.core.process.service.factory.impl.ExecutorClientFactory;
import com.dreameddeath.core.user.IUser;

/**
 * Created by Christophe Jeunesse on 03/01/2016.
 */
public class BasicTaskExecutorClient<TJOB extends AbstractJob,TTASK extends AbstractTask> implements ITaskExecutorClient<TJOB,TTASK> {
    private final ExecutorClientFactory clientFactory;
    private final Class<TTASK> taskClass;
    private final ICouchbaseSessionFactory sessionFactory;
    private final IExecutorServiceFactory executorServiceFactory;
    private final IProcessingServiceFactory processingServiceFactory;
    private final IJobExecutorService<TJOB> jobExecutorService;
    private final IJobProcessingService<TJOB> jobProcessingService;


    private final ITaskExecutorService<TJOB,TTASK> executorService;
    private final ITaskProcessingService<TJOB,TTASK> processingService;
    private final MetricRegistry metricRegistry;

    public BasicTaskExecutorClient(Class<TJOB> jobClass,Class<TTASK> taskClass,ExecutorClientFactory clientFactory,ICouchbaseSessionFactory sessionFactory, IExecutorServiceFactory executorServiceFactory, IProcessingServiceFactory processingServiceFactory, MetricRegistry registry){
        this.clientFactory = clientFactory;
        this.taskClass = taskClass;
        this.sessionFactory =sessionFactory;
        this.executorServiceFactory =executorServiceFactory;
        this.processingServiceFactory = processingServiceFactory;
        try {
            this.executorService = executorServiceFactory.getTaskExecutorServiceForClass(taskClass);
            this.processingService = processingServiceFactory.getTaskProcessingServiceForClass(taskClass);
        }
        catch(ExecutorServiceNotFoundException |ProcessingServiceNotFoundException e){
            throw new RuntimeException(e);
        }

        this.jobExecutorService = new DummyJobExecutor();
        this.jobProcessingService=new DummyJobProcessing();

        this.metricRegistry = registry;
    }

    @Override
    public TTASK executeTask(JobContext<TJOB> parentContext, TTASK task) throws TaskExecutionException {
        TaskContext<TJOB,TTASK> taskContext = buildTaskContext(parentContext,task);
        taskContext.execute();
        return task;
    }

    @Override
    public TTASK executeTask(TJOB job, TTASK task, IUser user) throws TaskExecutionException {
        JobContext<TJOB> ctxt = JobContext.newContext(new JobContext.Builder<>(job)
                .withSession(sessionFactory.newSession(ICouchbaseSession.SessionType.READ_WRITE,user))
                .withClientFactory(clientFactory)
                .withJobExecutorService(jobExecutorService)
                .withJobProcessingService(jobProcessingService)
        );
        return executeTask(ctxt,task);
    }

    @Override
    public TaskContext<TJOB, TTASK> buildTaskContext(JobContext<TJOB> jobContext, TTASK task) {
        return  TaskContext.builder(jobContext,task)
                .withExecutorService(executorService)
                .withProcessingService(processingService)
                .build();
    }

    public class DummyJobExecutor implements IJobExecutorService<TJOB>{
        @Override
        public void execute(JobContext<TJOB> context) throws JobExecutionException {
            throw new JobExecutionException(context,"Shouldn't occurs from task Client <"+taskClass.getName()+">");
        }
    }

    public class DummyJobProcessing implements IJobProcessingService<TJOB> {
        @Override
        public boolean init(JobContext<TJOB> context) throws JobExecutionException {
            throw new JobExecutionException(context,"Shouldn't occurs from task Client <"+taskClass.getName()+">");
        }

            @Override
            public boolean preprocess(JobContext<TJOB> context) throws JobExecutionException {
            throw new JobExecutionException(context,"Shouldn't occurs from task Client <"+taskClass.getName()+">");
        }

            @Override
            public boolean postprocess(JobContext<TJOB> context) throws JobExecutionException {
            throw new JobExecutionException(context,"Shouldn't occurs from task Client <"+taskClass.getName()+">");
        }

            @Override
            public boolean cleanup(JobContext<TJOB> context) throws JobExecutionException {
            throw new JobExecutionException(context,"Shouldn't occurs from task Client <"+taskClass.getName()+">");
        }
    }
}
