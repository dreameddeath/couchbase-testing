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

package com.dreameddeath.couchbase.core.process.rest;

import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.model.AbstractJob;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.couchbase.core.process.RemoteExecutorService;

/**
 * Created by Christophe Jeunesse on 10/01/2016.
 */
public class RemoteJobExecutorClient<T extends AbstractJob> implements IJobExecutorClient<T>{
    private final RemoteExecutorService<T> remoteExecutorService;

    public RemoteJobExecutorClient(RemoteExecutorService<T> executorService) {
        this.remoteExecutorService = executorService;
    }

    @Override
    public JobContext<T> executeJob(T job, IUser user) throws JobExecutionException {
        JobContext<T> context = JobContext.newContext(
                new JobContext.Builder<>(job)
                .withJobExecutorService(remoteExecutorService));
        context.execute();
        return context;
    }

    @Override
    public JobContext<T> submitJob(T job, IUser user) throws JobExecutionException {
        return null;
    }

    @Override
    public JobContext<T> resumeJob(T job, IUser user) throws JobExecutionException {
        JobContext<T> ctxt = JobContext.newContext(new JobContext.Builder<>(job)
                .withJobExecutorService(remoteExecutorService)
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
        return null;
    }
}
