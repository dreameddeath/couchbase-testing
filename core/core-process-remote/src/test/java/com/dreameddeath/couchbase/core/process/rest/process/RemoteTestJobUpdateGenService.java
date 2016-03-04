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

/**
 * Updated by Christophe Jeunesse on 27/02/2016.
 */


import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.impl.StandardJobProcessingService;
import com.dreameddeath.couchbase.core.process.remote.RemoteJobTaskProcessing;
import com.dreameddeath.couchbase.core.process.remote.annotation.RemoteServiceInfo;
import com.dreameddeath.couchbase.core.process.remote.model.RemoteUpdateGenJob;
import com.dreameddeath.couchbase.core.process.remote.model.published.TestDocJobUpdateGenRequest;
import com.dreameddeath.couchbase.core.process.remote.model.published.TestDocJobUpdateGenResponse;
import com.dreameddeath.couchbase.core.process.remote.model.rest.RemoteJobResultWrapper;
import com.fasterxml.jackson.annotation.JsonCreator;


/**
 * Updated by Christophe Jeunesse on 25/02/2016.
 */
@JobProcessingForClass(RemoteUpdateGenJob.class)
public class RemoteTestJobUpdateGenService extends StandardJobProcessingService<RemoteUpdateGenJob> {
    @Override
    public boolean init(JobContext<RemoteUpdateGenJob> context) throws JobExecutionException {
        context.addTask(new RemoteUpdateGenJob.RemoteTestJobUpdateGenTask());
        return false;
    }

    public static class Result extends RemoteJobResultWrapper<TestDocJobUpdateGenResponse> {
        @JsonCreator
        public Result(TestDocJobUpdateGenResponse result) {
            super(result);
        }
    }

    @TaskProcessingForClass(RemoteUpdateGenJob.RemoteTestJobUpdateGenTask.class) @RemoteServiceInfo(name = "testdocjobupdategen", version = "1.0.0")
    public static class TestJobUpdateTaskService extends RemoteJobTaskProcessing<TestDocJobUpdateGenRequest,TestDocJobUpdateGenResponse,RemoteUpdateGenJob,RemoteUpdateGenJob.RemoteTestJobUpdateGenTask> {
        @Override
        protected TestDocJobUpdateGenRequest getRequest(TaskContext<RemoteUpdateGenJob, RemoteUpdateGenJob.RemoteTestJobUpdateGenTask> ctxt) {
            TestDocJobUpdateGenRequest request = new TestDocJobUpdateGenRequest();
            request.setDecrIntValue(ctxt.getParentJob().descrIntValue);
            request.setDocKey(ctxt.getParentJob().key);
            return request;
        }

        @Override
        protected Class<Result> getResponseClass() {
            return Result.class;
        }
    }
}
