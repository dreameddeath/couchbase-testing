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
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.DuplicateUniqueKeyStorageException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.session.ICouchbaseSessionFactory;
import com.dreameddeath.core.process.annotation.JobWithDuplicateCheck;
import com.dreameddeath.core.process.exception.DuplicateJobExecutionException;
import com.dreameddeath.core.process.exception.ExecutorServiceNotFoundException;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.exception.ProcessingServiceNotFoundException;
import com.dreameddeath.core.process.model.AbstractJob;
import com.dreameddeath.core.process.model.ProcessState;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.dreameddeath.core.process.service.IJobExecutorService;
import com.dreameddeath.core.process.service.IJobProcessingService;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.factory.IExecutorServiceFactory;
import com.dreameddeath.core.process.service.factory.IProcessingServiceFactory;
import com.dreameddeath.core.process.service.factory.impl.ExecutorClientFactory;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.core.validation.exception.ValidationFailedException;

/**
 * Created by Christophe Jeunesse on 31/12/2015.
 */
public class BasicJobExecutorClient<T extends AbstractJob> implements IJobExecutorClient<T> {
    private final ExecutorClientFactory parentClientFactory;
    private final ICouchbaseSessionFactory sessionFactory;
    private final IExecutorServiceFactory executorServiceFactory;
    private final IProcessingServiceFactory processingServiceFactory;
    private final IJobExecutorService<T> executorService;
    private final IJobProcessingService<T> processingService;
    private final MetricRegistry metricRegistry;

    public BasicJobExecutorClient(Class<T> jobClass, ExecutorClientFactory clientFactory,ICouchbaseSessionFactory sessionFactory, IExecutorServiceFactory executorServiceFactory, IProcessingServiceFactory processingServiceFactory, MetricRegistry registry){
        this.parentClientFactory = clientFactory;
        this.sessionFactory =sessionFactory;
        this.executorServiceFactory =executorServiceFactory;
        this.processingServiceFactory = processingServiceFactory;
        try {
            this.executorService = executorServiceFactory.getJobExecutorServiceForClass(jobClass);
            this.processingService = processingServiceFactory.getJobProcessingServiceForClass(jobClass);
        }
        catch(ExecutorServiceNotFoundException|ProcessingServiceNotFoundException e){
            throw new RuntimeException(e);
        }
        this.metricRegistry = registry;
    }

    @Override
    public JobContext<T> executeJob(T job, IUser user) throws JobExecutionException {
        JobContext<T> ctxt = JobContext.newContext(new JobContext.Builder<>(job)
                .withSession(sessionFactory.newSession(ICouchbaseSession.SessionType.READ_WRITE,user))
                .withClientFactory(parentClientFactory)
                .withJobExecutorService(executorService)
                .withJobProcessingService(processingService)
        );
        if(job.getClass().isAnnotationPresent(JobWithDuplicateCheck.class)){
            ctxt.getJob().getStateInfo().setState(ProcessState.State.NEW);
            try {
                ctxt.save();
            }
            catch(ValidationFailedException e){
                DuplicateUniqueKeyStorageException duplicate = e.findException(DuplicateUniqueKeyStorageException.class);
                if(duplicate!=null){
                    throw new DuplicateJobExecutionException(ctxt,"DuplicateJob creation",duplicate);
                }
                else{
                    throw new JobExecutionException(ctxt,"Cannot perform initial save",e);
                }
            }
            catch(DaoException|StorageException|ValidationException e){
                throw new JobExecutionException(ctxt,"Cannot perform initial save",e);
            }
        }
        ctxt.execute();
        return ctxt;
    }

    @Override
    public JobContext<T> submitJob(T job, IUser user) throws JobExecutionException {
        JobContext<T> ctxt = JobContext.newContext(new JobContext.Builder<>(job)
                .withSession(sessionFactory.newSession(ICouchbaseSession.SessionType.READ_WRITE,user))
                .withClientFactory(parentClientFactory)
                .withJobExecutorService(executorService)
                .withJobProcessingService(processingService)
        );
        try {
            ctxt.getJob().getStateInfo().setState(ProcessState.State.ASYNC_NEW);
            ctxt.save();
        }
        catch(DaoException|StorageException|ValidationException e){
            throw new JobExecutionException(ctxt,"Unable to submit in deferred state",e);
        }
        return ctxt;
    }

    @Override
    public JobContext<T> resumeJob(T job, IUser user) throws JobExecutionException {
        JobContext<T> ctxt = JobContext.newContext(new JobContext.Builder<>(job)
                .withSession(sessionFactory.newSession(ICouchbaseSession.SessionType.READ_WRITE,user))
                .withClientFactory(parentClientFactory)
                .withJobExecutorService(executorService)
                .withJobProcessingService(processingService)
        );

        if(!ctxt.getJobState().isDone()){
            ctxt.execute();
        }
        else{
            throw new JobExecutionException(ctxt,"Cannot resume from done state");
        }
        return ctxt;
    }

    @Override
    public JobContext<T> cancelJob(T job, IUser user) throws JobExecutionException {
        //TODO
        return null;
    }
}
