/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.process.service.impl.client;

import com.codahale.metrics.MetricRegistry;
import com.dreameddeath.core.dao.exception.DuplicateUniqueKeyDaoException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.session.ICouchbaseSessionFactory;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.process.exception.DuplicateJobExecutionException;
import com.dreameddeath.core.process.exception.ExecutorServiceNotFoundException;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.exception.ProcessingServiceNotFoundException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.ProcessState;
import com.dreameddeath.core.process.service.IJobBlockingExecutorClient;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.dreameddeath.core.process.service.IJobExecutorService;
import com.dreameddeath.core.process.service.IJobProcessingService;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.factory.IExecutorServiceFactory;
import com.dreameddeath.core.process.service.factory.IProcessingServiceFactory;
import com.dreameddeath.core.process.service.factory.impl.ExecutorClientFactory;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.core.validation.utils.ValidationExceptionUtils;
import com.google.common.base.Preconditions;
import io.reactivex.Single;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 31/12/2015.
 */
public class BasicJobExecutorClientImpl<T extends AbstractJob> implements IJobExecutorClient<T> {
    private final UUID instanceUUID=UUID.randomUUID();
    private final Class<T> jobClass;
    private final String domain;
    private final ExecutorClientFactory parentClientFactory;
    private final ICouchbaseSessionFactory sessionFactory;
    private final IExecutorServiceFactory executorServiceFactory;
    private final IProcessingServiceFactory processingServiceFactory;
    private final IJobExecutorService<T> executorService;
    private final IJobProcessingService<T> processingService;
    private final IEventBus eventBus;
    private final MetricRegistry metricRegistry;

    public BasicJobExecutorClientImpl(Class<T> jobClass, ExecutorClientFactory clientFactory, ICouchbaseSessionFactory sessionFactory, IExecutorServiceFactory executorServiceFactory, IProcessingServiceFactory processingServiceFactory,IEventBus bus, MetricRegistry registry){
        this.jobClass = jobClass;
        this.domain=EntityDef.build(jobClass).getModelId().getDomain();
        this.parentClientFactory = clientFactory;
        this.sessionFactory =sessionFactory;
        this.executorServiceFactory =executorServiceFactory;
        this.processingServiceFactory = processingServiceFactory;
        this.eventBus=bus;
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
    public String getDomain() {
        return domain;
    }

    @Override
    public Single<JobContext<T>> executeJob(T job, IUser user) {
        return executeJob(job,sessionFactory.newSession(ICouchbaseSession.SessionType.READ_WRITE,domain, user));
    }

    @Override
    public Single<JobContext<T>> executeJob(T job, ICouchbaseSession session) {
        Preconditions.checkArgument(session.getDomain().equals(domain),"The domain of session %s mismatch with the domain of the client %s",session.getDomain(),domain);
        final JobContext<T> ctxt = JobContext.newContext(new JobContext.Builder<>(job)
                .withSession(session)
                .withClientFactory(parentClientFactory)
                .withJobExecutorService(executorService)
                .withJobProcessingService(processingService)
                .withEventBus(eventBus)
        );
        Single<JobContext<T>> sourceCtxtObs;
        if (StringUtils.isNotEmpty(job.getRequestUid())) {
            ctxt.getStateInfo().setState(ProcessState.State.NEW);
            sourceCtxtObs = ctxt.asyncSave()
                    .onErrorResumeNext(throwable -> {
                        if (throwable instanceof ValidationException) {
                            Optional<DuplicateUniqueKeyDaoException> duplicateException = ValidationExceptionUtils.findUniqueKeyException((ValidationException)throwable);
                            if (duplicateException.isPresent()) {
                                return Single.error(new DuplicateJobExecutionException(ctxt, "DuplicateJob creation", duplicateException.get().getCause()));
                            }
                            return Single.error(new JobExecutionException(ctxt, "Cannot perform initial save due to validation error", throwable));
                        }
                        else {
                            return Single.error(new JobExecutionException(ctxt, "Cannot perform initial save (unexpected error)", throwable));
                        }
                    });
        }
        else{
            sourceCtxtObs=Single.just(ctxt);
        }

        return sourceCtxtObs.flatMap(JobContext::execute);
    }

    @Override
    public Single<JobContext<T>> submitJob(T job, IUser user) {
        return submitJob(job,sessionFactory.newSession(ICouchbaseSession.SessionType.READ_WRITE,domain,user));
    }

    @Override
    public Single<JobContext<T>> submitJob(T job, ICouchbaseSession session) {
        Preconditions.checkArgument(session.getDomain().equals(domain),"The domain of session %s mismatch with the domain of the client %s",session.getDomain(),domain);
        job.getStateInfo().setState(ProcessState.State.ASYNC_NEW);
        JobContext<T> ctxt = JobContext.newContext(new JobContext.Builder<>(job)
                .withSession(session)
                .withClientFactory(parentClientFactory)
                .withJobExecutorService(executorService)
                .withJobProcessingService(processingService)
                .withEventBus(eventBus)
        );
        return ctxt.asyncSave();
    }

    @Override
    public Single<JobContext<T>> resumeJob(T job, IUser user) {
        return resumeJob(job,sessionFactory.newSession(ICouchbaseSession.SessionType.READ_WRITE,domain,user));
    }

    @Override
    public Single<JobContext<T>> resumeJob(T job, ICouchbaseSession session){
        Preconditions.checkArgument(session.getDomain().equals(domain),"The domain of session %s mismatch with the domain of the client %s",session.getDomain(),domain);
        JobContext<T> ctxt = JobContext.newContext(new JobContext.Builder<>(job)
                .withSession(session)
                .withClientFactory(parentClientFactory)
                .withJobExecutorService(executorService)
                .withJobProcessingService(processingService)
                .withEventBus(eventBus)
        );

        return ctxt.execute();
    }

    @Override
    public Single<JobContext<T>> cancelJob(T job, IUser user){
        return cancelJob(job,sessionFactory.newSession(ICouchbaseSession.SessionType.READ_WRITE,domain,user));
    }

    @Override
    public Single<JobContext<T>> cancelJob(T job, ICouchbaseSession session){
        Preconditions.checkArgument(session.getDomain().equals(domain),"The domain of session %s mismatch with the domain of the client %s",session.getDomain(),domain);
        //TODO
        return null;
    }


    @Override
    public UUID getInstanceUUID() {
        return instanceUUID;
    }

    @Override
    public Class<T> getJobClass() {
        return jobClass;
    }

    @Override
    public IJobExecutorService<T> getExecutorService() {
        return executorService;
    }

    @Override
    public IJobProcessingService<T> getProcessingService() {
        return processingService;
    }

    @Override
    public IJobBlockingExecutorClient<T> toBlocking() {
        return new IJobBlockingExecutorClient<T>() {
            @Override
            public JobContext<T> executeJob(T job, IUser user) throws JobExecutionException {
                return mapError(BasicJobExecutorClientImpl.this.executeJob(job,user));
            }

            @Override
            public JobContext<T> executeJob(T job, ICouchbaseSession session) throws JobExecutionException {
                return mapError(BasicJobExecutorClientImpl.this.executeJob(job,session));
            }

            @Override
            public JobContext<T> submitJob(T job, IUser user) throws JobExecutionException {
                return mapError(BasicJobExecutorClientImpl.this.submitJob(job,user));
            }

            @Override
            public JobContext<T> submitJob(T job, ICouchbaseSession session) throws JobExecutionException {
                return mapError(BasicJobExecutorClientImpl.this.submitJob(job,session));
            }


            @Override
            public JobContext<T> resumeJob(T job, IUser user) throws JobExecutionException {
                return mapError(BasicJobExecutorClientImpl.this.resumeJob(job,user));
            }

            @Override
            public JobContext<T> resumeJob(T job, ICouchbaseSession session) throws JobExecutionException {
                return mapError(BasicJobExecutorClientImpl.this.resumeJob(job,session));
            }

            @Override
            public JobContext<T> cancelJob(T job, IUser user) throws JobExecutionException {
                return mapError(BasicJobExecutorClientImpl.this.cancelJob(job,user));
            }

            @Override
            public JobContext<T> cancelJob(T job, ICouchbaseSession session) throws JobExecutionException {
                return mapError(BasicJobExecutorClientImpl.this.resumeJob(job,session));
            }

        };
    }

    private JobContext<T> mapError(Single<JobContext<T>> single) throws JobExecutionException{
        try{
            return single.blockingGet();
        }
        catch(RuntimeException e){
            Throwable eCause = e.getCause();
            if(eCause!=null){
                if(eCause instanceof JobExecutionException){
                    throw (JobExecutionException)eCause;
                }
            }
            throw e;
        }
    }
}
