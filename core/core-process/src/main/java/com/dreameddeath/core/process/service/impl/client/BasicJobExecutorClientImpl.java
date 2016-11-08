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

package com.dreameddeath.core.process.service.impl.client;

import com.codahale.metrics.MetricRegistry;
import com.dreameddeath.core.dao.exception.DuplicateUniqueKeyDaoException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.dao.exception.validation.ValidationObservableException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.session.ICouchbaseSessionFactory;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.process.exception.DuplicateJobExecutionException;
import com.dreameddeath.core.process.exception.ExecutorServiceNotFoundException;
import com.dreameddeath.core.process.exception.JobObservableExecutionException;
import com.dreameddeath.core.process.exception.ProcessingServiceNotFoundException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.ProcessState;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.dreameddeath.core.process.service.IJobExecutorService;
import com.dreameddeath.core.process.service.IJobProcessingService;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.factory.IExecutorServiceFactory;
import com.dreameddeath.core.process.service.factory.IProcessingServiceFactory;
import com.dreameddeath.core.process.service.factory.impl.ExecutorClientFactory;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.core.validation.exception.ValidationCompositeFailure;
import rx.Observable;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 31/12/2015.
 */
public class BasicJobExecutorClientImpl<T extends AbstractJob> implements IJobExecutorClient<T> {
    private final UUID instanceUUID=UUID.randomUUID();
    private final Class<T> jobClass;
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
    public Observable<JobContext<T>> executeJob(T job, IUser user) {
        final JobContext<T> ctxt = JobContext.newContext(new JobContext.Builder<>(job)
                .withSession(sessionFactory.newSession(ICouchbaseSession.SessionType.READ_WRITE, user))
                .withClientFactory(parentClientFactory)
                .withJobExecutorService(executorService)
                .withJobProcessingService(processingService)
                .withEventBus(eventBus)
        );
        Observable<JobContext<T>> sourceCtxtObs;
        if (StringUtils.isNotEmpty(job.getRequestUid())) {
            ctxt.getStateInfo().setState(ProcessState.State.NEW);
            sourceCtxtObs = ctxt.asyncSave()
                    .onErrorResumeNext(throwable -> {
                        if(throwable instanceof JobObservableExecutionException && throwable.getCause()!=null){
                            throwable=((JobObservableExecutionException)throwable).getCause();
                            if(throwable !=null && throwable.getCause()!=null){
                                throwable = throwable.getCause();
                            }
                        }
                        if(throwable instanceof ValidationObservableException && throwable.getCause()!=null){
                            throwable = throwable.getCause();
                        }
                        if (throwable instanceof ValidationException) {
                            ValidationException e = (ValidationException) throwable;
                            if ((e.getFailure() != null) && (e.getFailure() instanceof ValidationCompositeFailure)) {
                                DuplicateUniqueKeyDaoException duplicate = (((ValidationCompositeFailure) e.getFailure()).findException(DuplicateUniqueKeyDaoException.class));
                                if (duplicate != null) {
                                    return Observable.error(new JobObservableExecutionException(new DuplicateJobExecutionException(ctxt, "DuplicateJob creation", duplicate.getCause())));
                                }
                            }
                            return Observable.error(new JobObservableExecutionException(ctxt, "Cannot perform initial save due to validation error", e));
                        }
                        else {
                            return Observable.error(new JobObservableExecutionException(ctxt, "Cannot perform initial save (unexpected error)", throwable));
                        }
                    });
        }
        else{
            sourceCtxtObs=Observable.just(ctxt);
        }

        return sourceCtxtObs.flatMap(JobContext::execute);
    }

    @Override
    public Observable<JobContext<T>> submitJob(T job, IUser user) {
        job.getStateInfo().setState(ProcessState.State.ASYNC_NEW);
        JobContext<T> ctxt = JobContext.newContext(new JobContext.Builder<>(job)
                .withSession(sessionFactory.newSession(ICouchbaseSession.SessionType.READ_WRITE,user))
                .withClientFactory(parentClientFactory)
                .withJobExecutorService(executorService)
                .withJobProcessingService(processingService)
                .withEventBus(eventBus)
        );
        return ctxt.asyncSave();
    }

    @Override
    public Observable<JobContext<T>> resumeJob(T job, IUser user){
        JobContext<T> ctxt = JobContext.newContext(new JobContext.Builder<>(job)
                .withSession(sessionFactory.newSession(ICouchbaseSession.SessionType.READ_WRITE,user))
                .withClientFactory(parentClientFactory)
                .withJobExecutorService(executorService)
                .withJobProcessingService(processingService)
                .withEventBus(eventBus)
        );

        return ctxt.execute();
    }

    @Override
    public Observable<JobContext<T>> cancelJob(T job, IUser user){
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
}
