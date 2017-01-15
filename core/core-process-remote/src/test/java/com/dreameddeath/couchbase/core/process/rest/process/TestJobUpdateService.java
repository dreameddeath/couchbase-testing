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

import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.JobProcessingResult;
import com.dreameddeath.core.process.service.impl.processor.DocumentUpdateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.processor.StandardJobProcessingService;
import com.dreameddeath.couchbase.core.process.remote.model.TestDoc;
import com.dreameddeath.couchbase.core.process.remote.model.TestDocJobUpdate;
import io.reactivex.Single;

/**
 * Created by Christophe Jeunesse on 04/01/2016.
 */
@JobProcessingForClass(TestDocJobUpdate.class)
public class TestJobUpdateService extends StandardJobProcessingService<TestDocJobUpdate> {
    @Override
    public Single<JobProcessingResult<TestDocJobUpdate>> init(JobContext<TestDocJobUpdate> context){
        TestDocJobUpdate.TestJobUpdateTask task =new TestDocJobUpdate.TestJobUpdateTask();
        task.setDocKey(context.getInternalJob().docKey);
        context.addTask(task);
        return JobProcessingResult.build(context,false);
    }


    @TaskProcessingForClass(TestDocJobUpdate.TestJobUpdateTask.class)
    public static class TestJobUpdateTaskService extends DocumentUpdateTaskProcessingService<TestDocJobUpdate,TestDoc,TestDocJobUpdate.TestJobUpdateTask>{
        @Override
        protected Single<ProcessingDocumentResult> processDocument(ContextAndDocument ctxtAndDoc) {
            ctxtAndDoc.getDoc().intValue+=ctxtAndDoc.getCtxt().getParentInternalJob().incrIntValue;
            return new ProcessingDocumentResult(ctxtAndDoc,false).toSingle();
        }
    }
}
