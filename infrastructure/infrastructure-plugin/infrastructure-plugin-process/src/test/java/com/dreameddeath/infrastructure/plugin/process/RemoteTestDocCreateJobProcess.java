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

package com.dreameddeath.infrastructure.plugin.process;

import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.JobProcessingResult;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.impl.processor.StandardJobProcessingService;
import com.dreameddeath.couchbase.core.process.remote.RemoteJobTaskProcessing;
import com.dreameddeath.couchbase.core.process.remote.annotation.RemoteServiceInfo;
import com.dreameddeath.couchbase.core.process.remote.model.rest.RemoteJobResultWrapper;
import com.dreameddeath.infrastructure.plugin.process.published.process.TestDocCreateJobRequest;
import com.dreameddeath.infrastructure.plugin.process.published.process.TestDocCreateJobResponse;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.reactivex.Single;

/**
 * Created by Christophe Jeunesse on 13/03/2016.
 */
@JobProcessingForClass(RemoteTestDocCreateJob.class)
public class RemoteTestDocCreateJobProcess extends StandardJobProcessingService<RemoteTestDocCreateJob>{
    @Override
    public Single<JobProcessingResult<RemoteTestDocCreateJob>> init(JobContext<RemoteTestDocCreateJob> context){
        context.addTask(new RemoteTestDocCreateJob.RemoteTestDocCreateTask());
        return JobProcessingResult.build(context,false);
    }

    public static class Response extends RemoteJobResultWrapper<TestDocCreateJobResponse>{
        @JsonCreator
        public Response(TestDocCreateJobResponse result) {
            super(result);
        }
    }

    @TaskProcessingForClass(RemoteTestDocCreateJob.RemoteTestDocCreateTask.class)
    @RemoteServiceInfo(domain="test",name="testdoccreatejob",version = "1.0.0")
    public static class RemoteTestDocCreateJobTaskProcess extends RemoteJobTaskProcessing<TestDocCreateJobRequest,TestDocCreateJobResponse,RemoteTestDocCreateJob,RemoteTestDocCreateJob.RemoteTestDocCreateTask>{
        @Override
        protected Class<Response> getResponseClass() {
            return Response.class;
        }

        @Override
        protected Single<TestDocCreateJobRequest> getRequest(TaskContext<RemoteTestDocCreateJob, RemoteTestDocCreateJob.RemoteTestDocCreateTask> ctxt) {
            TestDocCreateJobRequest request = new TestDocCreateJobRequest();
            request.setName(ctxt.getParentInternalJob().remoteName);
            return Single.just(request);
        }

        @Override
        protected void updateTaskWithResponse(RemoteTestDocCreateJob.RemoteTestDocCreateTask task, TestDocCreateJobResponse resp) {
            task.key =resp.getKey();
        }
    }
}
