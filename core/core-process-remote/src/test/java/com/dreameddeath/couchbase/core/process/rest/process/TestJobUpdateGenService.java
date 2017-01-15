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
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.JobProcessingResult;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.impl.processor.DocumentUpdateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.processor.StandardJobProcessingService;
import com.dreameddeath.couchbase.core.process.remote.model.TestDoc;
import com.dreameddeath.couchbase.core.process.remote.model.TestDocJobUpdateGen;
import io.reactivex.Single;

/**
 * Created by Christophe Jeunesse on 04/01/2016.
 */
@JobProcessingForClass(TestDocJobUpdateGen.class)
public class TestJobUpdateGenService extends StandardJobProcessingService<TestDocJobUpdateGen> {
    @Override
    public Single<JobProcessingResult<TestDocJobUpdateGen>> init(JobContext<TestDocJobUpdateGen> context){
        TestDocJobUpdateGen.TestJobUpdateTaskGen task =new TestDocJobUpdateGen.TestJobUpdateTaskGen();
        task.setDocKey(context.getInternalJob().docKey);
        context.addTask(task);
        return JobProcessingResult.build(context,false);
    }


    @TaskProcessingForClass(TestDocJobUpdateGen.TestJobUpdateTaskGen.class)
    public static class TestJobUpdateTaskService extends DocumentUpdateTaskProcessingService<TestDocJobUpdateGen,TestDoc,TestDocJobUpdateGen.TestJobUpdateTaskGen>{
        @Override
        protected Single<ContextAndDocument> cleanTaskBeforeRetryProcessing(ContextAndDocument ctxtAndDoc) {
            ctxtAndDoc.getCtxt().getInternalTask().isFirstCall=false;
            return Single.just(ctxtAndDoc);
        }

        @Override
        protected Single<ProcessingDocumentResult> processDocument(ContextAndDocument ctxtAndDoc) {
            if(ctxtAndDoc.getCtxt().getInternalTask().isFirstCall){
                ctxtAndDoc.getCtxt().getInternalTask().setUpdatedWithDoc(true);
                return ctxtAndDoc.getCtxt().getStandardSessionContext()
                        .asyncSave()
                        .map(TaskContext::getTemporaryReadOnlySessionContext)
                        .flatMap(ctxt->Single.error(new TaskExecutionException(ctxt,"First call test")));
            }
            else {
                ctxtAndDoc.getDoc().intValue -= ctxtAndDoc.getCtxt().getParentInternalJob().decrIntValue;
            }
            return new ProcessingDocumentResult(ctxtAndDoc,true).toSingle();
        }
    }
}
