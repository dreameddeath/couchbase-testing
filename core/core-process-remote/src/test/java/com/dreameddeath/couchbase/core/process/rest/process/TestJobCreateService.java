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

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.JobProcessingResult;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.context.UpdateJobTaskProcessingResult;
import com.dreameddeath.core.process.service.impl.processor.DocumentCreateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.processor.StandardJobProcessingService;
import com.dreameddeath.couchbase.core.process.remote.model.TestDoc;
import com.dreameddeath.couchbase.core.process.remote.model.TestDocJobCreate;
import io.reactivex.Single;

/**
 * Created by Christophe Jeunesse on 04/01/2016.
 */
@JobProcessingForClass(TestDocJobCreate.class)
public class TestJobCreateService extends StandardJobProcessingService<TestDocJobCreate> {
    @Override
    public Single<JobProcessingResult<TestDocJobCreate>> init(JobContext<TestDocJobCreate> context){
        context.addTask(new TestDocJobCreate.TestJobCreateTask());
        return JobProcessingResult.build(context,false);
    }

    @TaskProcessingForClass(TestDocJobCreate.TestJobCreateTask.class)
    public static class TestJobCreateTaskService extends DocumentCreateTaskProcessingService<TestDocJobCreate,TestDoc,TestDocJobCreate.TestJobCreateTask>{
        @Override
        protected Single<ContextAndDocument> buildDocument(TaskContext<TestDocJobCreate, TestDocJobCreate.TestJobCreateTask> ctxt){
            TestDoc newDoc = new TestDoc();
            newDoc.name = ctxt.getParentInternalJob().name;
            newDoc.intValue = ctxt.getParentInternalJob().initIntValue;
            if(newDoc.intValue==null) { newDoc.intValue=0;}
            return buildContextAndDocumentObservable(ctxt,newDoc);
        }

        @Override
        public Single<UpdateJobTaskProcessingResult<TestDocJobCreate, TestDocJobCreate.TestJobCreateTask>> updatejob(TestDocJobCreate job, TestDocJobCreate.TestJobCreateTask task, ICouchbaseSession session) {
            job.createdKey = task.getDocKey();
            return new UpdateJobTaskProcessingResult<>(job,task,true).toSingle();
        }
    }
}
