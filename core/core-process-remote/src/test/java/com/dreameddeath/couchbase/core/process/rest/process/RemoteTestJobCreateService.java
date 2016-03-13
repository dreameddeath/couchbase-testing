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

package com.dreameddeath.couchbase.core.process.rest.process;

import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.impl.StandardJobProcessingService;
import com.dreameddeath.couchbase.core.process.remote.RemoteJobTaskProcessing;
import com.dreameddeath.couchbase.core.process.remote.annotation.RemoteServiceInfo;
import com.dreameddeath.couchbase.core.process.remote.model.RemoteCreateJob;
import com.dreameddeath.couchbase.core.process.remote.model.rest.RemoteJobResultWrapper;
import com.dreameddeath.couchbase.core.process.remote.model.rest.TestDocJobCreateRequest;
import com.dreameddeath.couchbase.core.process.remote.model.rest.TestDocJobCreateResult;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Created by Christophe Jeunesse on 25/02/2016.
 */
@JobProcessingForClass(RemoteCreateJob.class)
public class RemoteTestJobCreateService extends StandardJobProcessingService<RemoteCreateJob> {
    @Override
    public boolean init(JobContext<RemoteCreateJob> context) throws JobExecutionException {
        context.addTask(new RemoteCreateJob.RemoteTestJobCreateTask());
        return false;
    }

    public static class Result extends RemoteJobResultWrapper<TestDocJobCreateResult>{
        @JsonCreator
        public Result(TestDocJobCreateResult result) {
            super(result);
        }
    }

    @TaskProcessingForClass(RemoteCreateJob.RemoteTestJobCreateTask.class) @RemoteServiceInfo(domain = "test",name = "testdocjobcreate", version = "1.0.0")
    public static class TestJobCreateTaskService extends RemoteJobTaskProcessing<TestDocJobCreateRequest,TestDocJobCreateResult,RemoteCreateJob,RemoteCreateJob.RemoteTestJobCreateTask> {
        @Override
        protected TestDocJobCreateRequest getRequest(TaskContext<RemoteCreateJob, RemoteCreateJob.RemoteTestJobCreateTask> ctxt) {
            TestDocJobCreateRequest request = new TestDocJobCreateRequest();
            request.tempUid = ctxt.getParentJob().tempUid;
            request.initIntValue =ctxt.getParentJob().initIntValue;
            request.name= ctxt.getParentJob().name;
            return request;
        }

        @Override
        protected void updateTaskWithResponse(RemoteCreateJob.RemoteTestJobCreateTask task, TestDocJobCreateResult resp) {
            task.key = resp.createdKey;
        }

        @Override
        protected Class<Result> getResponseClass() {
            return Result.class;
        }
    }
}
