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

package com.dreameddeath.infrastructure.plugin.process;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.JobProcessingResult;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.context.UpdateJobTaskProcessingResult;
import com.dreameddeath.core.process.service.impl.processor.DocumentCreateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.processor.StandardJobProcessingService;
import io.reactivex.Single;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Christophe Jeunesse on 03/01/2016.
 */
@JobProcessingForClass(TestDocCreateJob.class)
public class TestDocCreateJobProcess extends StandardJobProcessingService<TestDocCreateJob>{
    @Override
    public Single<JobProcessingResult<TestDocCreateJob>> init(JobContext<TestDocCreateJob> context){
        context.addTask(new TestDocCreateJob.TestDocCreateTask());
        return JobProcessingResult.build(context,false);
    }

    @TaskProcessingForClass(TestDocCreateJob.TestDocCreateTask.class)
    public static class TestDocCreateTaskProcess extends DocumentCreateTaskProcessingService<TestDocCreateJob,TestDocProcess,TestDocCreateJob.TestDocCreateTask> {
        private ExternalDocCreateJobService service;

        @Autowired
        public void setService(ExternalDocCreateJobService service) {
            this.service = service;
        }



        @Override
        protected Single<ContextAndDocument> buildDocument(TaskContext<TestDocCreateJob, TestDocCreateJob.TestDocCreateTask> ctxt){
            return buildContextAndDocumentObservable(ctxt,service.createDoc(ctxt.getParentInternalJob().name));
        }

        @Override
        public Single<UpdateJobTaskProcessingResult<TestDocCreateJob, TestDocCreateJob.TestDocCreateTask>> updatejob(TestDocCreateJob job, TestDocCreateJob.TestDocCreateTask task, ICouchbaseSession session) {
            job.key = task.getDocKey();
            return new UpdateJobTaskProcessingResult<>(job,task,true).toSingle();
        }
    }
}
