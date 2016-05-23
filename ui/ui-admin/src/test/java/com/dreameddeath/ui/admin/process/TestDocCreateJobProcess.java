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

package com.dreameddeath.ui.admin.process;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.impl.processor.DocumentCreateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.processor.StandardJobProcessingService;

/**
 * Created by Christophe Jeunesse on 03/01/2016.
 */
@JobProcessingForClass(TestDocCreateJob.class)
public class TestDocCreateJobProcess extends StandardJobProcessingService<TestDocCreateJob>{
    @Override
    public boolean init(JobContext<TestDocCreateJob> context) throws JobExecutionException {
        context.addTask(new TestDocCreateJob.TestDocCreateTask());
        return false;
    }

    @TaskProcessingForClass(TestDocCreateJob.TestDocCreateTask.class)
    public static class TestDocCreateTaskProcess extends DocumentCreateTaskProcessingService<TestDocCreateJob,TestDocProcess,TestDocCreateJob.TestDocCreateTask> {
        @Override
        protected TestDocProcess buildDocument(TaskContext<TestDocCreateJob, TestDocCreateJob.TestDocCreateTask> ctxt) throws DaoException, StorageException {
            TestDocProcess doc = new TestDocProcess();
            doc.name = ctxt.getParentJob().name;
            return doc;
        }

        @Override
        public boolean updatejob(TaskContext<TestDocCreateJob, TestDocCreateJob.TestDocCreateTask> context) throws TaskExecutionException {
            context.getParentJob().key = context.getTask().getDocKey();
            return super.updatejob(context);
        }
    }
}
