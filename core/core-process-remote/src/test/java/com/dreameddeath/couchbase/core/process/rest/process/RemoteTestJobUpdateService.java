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

package com.dreameddeath.couchbase.core.process.rest.process;

/**
 * Updated by Christophe Jeunesse on 27/02/2016.
 */


import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.JobProcessingResult;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.impl.processor.StandardJobProcessingService;
import com.dreameddeath.couchbase.core.process.remote.RemoteJobTaskProcessing;
import com.dreameddeath.couchbase.core.process.remote.model.RemoteUpdateJob;
import com.dreameddeath.couchbase.core.process.remote.model.rest.RemoteJobResultWrapper;
import com.dreameddeath.couchbase.core.process.remote.model.rest.TestDocJobUpdateRequest;
import com.dreameddeath.couchbase.core.process.remote.model.rest.TestDocJobUpdateResult;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.reactivex.Single;


/**
 * Updated by Christophe Jeunesse on 25/02/2016.
 */
@JobProcessingForClass(RemoteUpdateJob.class)
public class RemoteTestJobUpdateService extends StandardJobProcessingService<RemoteUpdateJob> {
    @Override
    public Single<JobProcessingResult<RemoteUpdateJob>> init(JobContext<RemoteUpdateJob> context){
        context.addTask(new RemoteUpdateJob.RemoteTestJobUpdateTask());
        return JobProcessingResult.build(context,false);
    }

    public static class Result extends RemoteJobResultWrapper<TestDocJobUpdateResult> {
        @JsonCreator
        public Result(TestDocJobUpdateResult result) {
            super(result);
        }
    }

    @TaskProcessingForClass(RemoteUpdateJob.RemoteTestJobUpdateTask.class)
    public static class TestJobUpdateTaskService extends RemoteJobTaskProcessing<TestDocJobUpdateRequest,TestDocJobUpdateResult,RemoteUpdateJob,RemoteUpdateJob.RemoteTestJobUpdateTask> {
        @Override
        protected Single<TestDocJobUpdateRequest> getRequest(TaskContext<RemoteUpdateJob, RemoteUpdateJob.RemoteTestJobUpdateTask> ctxt) {
            TestDocJobUpdateRequest request = new TestDocJobUpdateRequest();
            request.incrIntValue=ctxt.getParentInternalJob().incrIntValue;
            request.key = ctxt.getParentInternalJob().key;
            return Single.just(request);
        }

        @Override
        protected Class<Result> getResponseClass() {
            return Result.class;
        }
    }
}
