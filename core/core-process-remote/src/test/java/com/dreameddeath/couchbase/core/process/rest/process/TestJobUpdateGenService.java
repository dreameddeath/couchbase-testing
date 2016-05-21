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
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.impl.DocumentUpdateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.StandardJobProcessingService;
import com.dreameddeath.couchbase.core.process.remote.model.TestDoc;
import com.dreameddeath.couchbase.core.process.remote.model.TestDocJobUpdateGen;

/**
 * Created by Christophe Jeunesse on 04/01/2016.
 */
@JobProcessingForClass(TestDocJobUpdateGen.class)
public class TestJobUpdateGenService extends StandardJobProcessingService<TestDocJobUpdateGen> {
    @Override
    public boolean init(JobContext<TestDocJobUpdateGen> context) throws JobExecutionException {
        TestDocJobUpdateGen.TestJobUpdateTaskGen task =new TestDocJobUpdateGen.TestJobUpdateTaskGen();
        task.setDocKey(context.getJob().docKey);
        context.addTask(task);
        return false;
    }


    @TaskProcessingForClass(TestDocJobUpdateGen.TestJobUpdateTaskGen.class)
    public static class TestJobUpdateTaskService extends DocumentUpdateTaskProcessingService<TestDocJobUpdateGen,TestDoc,TestDocJobUpdateGen.TestJobUpdateTaskGen>{
        @Override
        protected void cleanTaskBeforeRetryProcessing(TaskContext<TestDocJobUpdateGen, TestDocJobUpdateGen.TestJobUpdateTaskGen> ctxt, TestDoc doc) {
            ctxt.getTask().isFirstCall=false;
        }

        @Override
        protected boolean processDocument(TaskContext<TestDocJobUpdateGen, TestDocJobUpdateGen.TestJobUpdateTaskGen> ctxt, TestDoc doc) throws TaskExecutionException {
            if(ctxt.getTask().isFirstCall){
                ctxt.getTask().setUpdatedWithDoc(true);
                try {
                    ctxt.getSession().setTemporaryReadOnlyMode(false);
                    ctxt.save();
                }catch(Throwable e){
                    throw new TaskExecutionException(ctxt,"error saved");
                }
                finally {
                    ctxt.getSession().setTemporaryReadOnlyMode(true);
                }
                throw new TaskExecutionException(ctxt,"First call test");
            }
            else {
                doc.intValue -= ctxt.getParentJob().decrIntValue;
            }
            return true;
        }
    }
}
