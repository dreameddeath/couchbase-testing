/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.couchbase.core.process.remote;

import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.service.IJobBlockingExecutorClient;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.dreameddeath.core.process.service.IJobExecutorService;
import com.dreameddeath.core.process.service.IJobProcessingService;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.JobNotificationBuildResult;
import com.dreameddeath.core.process.service.context.JobProcessingResult;
import com.dreameddeath.core.user.IUser;
import io.reactivex.Single;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 10/01/2016.
 */
public class RemoteJobExecutorClient<T extends AbstractJob> implements IJobExecutorClient<T>{
    private final UUID instanceUUID=UUID.randomUUID();
    private final Class<T> jobClass;
    private final String domain;
    private final IJobProcessingService<T> dummyProcessing;
    private final RemoteJobExecutorService<T> remoteJobExecutorService;

    public RemoteJobExecutorClient(Class<T> jobClass, RemoteJobExecutorService<T> executorService) {
        this.jobClass = jobClass;
        this.domain= EntityDef.build(jobClass).getModelId().getDomain();

        this.remoteJobExecutorService = executorService;
        this.dummyProcessing = new DummyRemoteJobProcessingService<>();
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public Single<JobContext<T>> executeJob(T job, IUser user){
        JobContext<T> context = JobContext.newContext(
                new JobContext.Builder<>(job)
                .withJobExecutorService(remoteJobExecutorService));
        return context.execute();
    }

    @Override
    public Single<JobContext<T>> submitJob(T job, IUser user){
        return null;//TODO
    }

    @Override
    public Single<JobContext<T>> resumeJob(T job, IUser user){
        JobContext<T> ctxt = JobContext.newContext(new JobContext.Builder<>(job)
                .withJobExecutorService(remoteJobExecutorService)
        );

        if(!ctxt.getJobState().isDone()){
            return ctxt.execute();
        }
        else{
            return Single.error(new JobExecutionException(ctxt,"Cannot resume from done state"));
        }
    }

    @Override
    public Single<JobContext<T>> cancelJob(T job, IUser user) {
        return null;//TODO
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
        return remoteJobExecutorService;
    }

    @Override
    public IJobProcessingService<T> getProcessingService() {
        return dummyProcessing;
    }


    public static class DummyRemoteJobProcessingService<T extends AbstractJob> implements IJobProcessingService<T>{
        @Override
        public Single<JobProcessingResult<T>> init(JobContext<T> context){
            return JobProcessingResult.build(context,false);
        }

        @Override
        public Single<JobProcessingResult<T>> preprocess(JobContext<T> context){
            return JobProcessingResult.build(context,false);
        }

        @Override
        public Single<JobProcessingResult<T>> postprocess(JobContext<T> context){
            return JobProcessingResult.build(context,false);
        }

        @Override
        public Single<JobNotificationBuildResult<T>> buildNotifications(JobContext<T> context){
            return JobNotificationBuildResult.build(context);
        }

        @Override
        public Single<JobProcessingResult<T>> cleanup(JobContext<T> context){
            return JobProcessingResult.build(context,false);
        }
    }

    @Override
    public IJobBlockingExecutorClient<T> toBlocking() {
        return new IJobBlockingExecutorClient<T>() {
            @Override
            public JobContext<T> executeJob(T job, IUser user) throws JobExecutionException {
                return mapError(RemoteJobExecutorClient.this.executeJob(job,user));
            }

            @Override
            public JobContext<T> submitJob(T job, IUser user) throws JobExecutionException {
                return mapError(RemoteJobExecutorClient.this.submitJob(job,user));
            }

            @Override
            public JobContext<T> resumeJob(T job, IUser user) throws JobExecutionException {
                return mapError(RemoteJobExecutorClient.this.resumeJob(job,user));
            }

            @Override
            public JobContext<T> cancelJob(T job, IUser user) throws JobExecutionException {
                return mapError(RemoteJobExecutorClient.this.cancelJob(job,user));
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
