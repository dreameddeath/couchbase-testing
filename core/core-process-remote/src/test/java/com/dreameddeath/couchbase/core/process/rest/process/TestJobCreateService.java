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

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.impl.DocumentCreateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.StandardJobProcessingService;
import com.dreameddeath.couchbase.core.process.remote.model.TestDoc;
import com.dreameddeath.couchbase.core.process.remote.model.TestDocJobCreate;

/**
 * Created by Christophe Jeunesse on 04/01/2016.
 */
@JobProcessingForClass(TestDocJobCreate.class)
public class TestJobCreateService extends StandardJobProcessingService<TestDocJobCreate> {
    @Override
    public boolean init(JobContext<TestDocJobCreate> context) throws JobExecutionException {
        context.addTask(new TestDocJobCreate.TestJobCreateTask());
        return false;
    }

    @TaskProcessingForClass(TestDocJobCreate.TestJobCreateTask.class)
    public static class TestJobCreateTaskService extends DocumentCreateTaskProcessingService<TestDocJobCreate,TestDoc,TestDocJobCreate.TestJobCreateTask>{
        @Override
        protected TestDoc buildDocument(TaskContext<TestDocJobCreate, TestDocJobCreate.TestJobCreateTask> ctxt) throws DaoException, StorageException {
            TestDoc newDoc = new TestDoc();
            newDoc.name = ctxt.getParentJob().name;
            newDoc.intValue = ctxt.getParentJob().initIntValue;
            if(newDoc.intValue==null) { newDoc.intValue=0;}
            return newDoc;
        }

        @Override
        public boolean updatejob(TaskContext<TestDocJobCreate, TestDocJobCreate.TestJobCreateTask> context) throws TaskExecutionException {
            context.getParentJob().createdKey = context.getTask().getDocKey();
            return true;
        }
    }
}
