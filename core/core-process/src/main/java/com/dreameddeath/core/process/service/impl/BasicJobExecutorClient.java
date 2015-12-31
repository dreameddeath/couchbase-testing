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

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.process.exception.ExecutorServiceNotFoundException;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.exception.ProcessingServiceNotFoundException;
import com.dreameddeath.core.process.model.AbstractJob;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.dreameddeath.core.process.service.IJobExecutorService;
import com.dreameddeath.core.process.service.IJobProcessingService;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.factory.ExecutorServiceFactory;
import com.dreameddeath.core.process.service.factory.ProcessingServiceFactory;

/**
 * Created by Christophe Jeunesse on 31/12/2015.
 */
public class BasicJobExecutorClient<T extends AbstractJob> implements IJobExecutorClient<T> {
    private final ICouchbaseSession session;
    private final ExecutorServiceFactory executorServiceFactory;
    private final ProcessingServiceFactory processingServiceFactory;
    private final IJobExecutorService<T> executorService;
    private final IJobProcessingService<T> processingService;

    public BasicJobExecutorClient(Class<T> jobClass,ICouchbaseSession session,ExecutorServiceFactory executorServiceFactory,ProcessingServiceFactory processingServiceFactory){
        this.session =session;
        this.executorServiceFactory =executorServiceFactory;
        this.processingServiceFactory = processingServiceFactory;
        try {
            this.executorService = executorServiceFactory.getJobExecutorServiceForClass(jobClass);
            this.processingService = processingServiceFactory.getJobProcessingServiceForClass(jobClass);
        }
        catch(ExecutorServiceNotFoundException|ProcessingServiceNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public T executeJob(T job) throws JobExecutionException {
        JobContext<T> ctxt = JobContext.newContext(new JobContext.Builder<>(job)
                .withSession(session)
                .withExecutorFactory(executorServiceFactory)
                .withProcessingFactory(processingServiceFactory)
                .withJobExecutorService(executorService)
                .withJobProcessingService(processingService)
        );
        ctxt.execute();
        return job;
    }
}
