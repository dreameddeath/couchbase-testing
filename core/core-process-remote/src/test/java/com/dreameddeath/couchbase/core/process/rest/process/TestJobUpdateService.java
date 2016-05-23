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
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.impl.processor.DocumentUpdateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.processor.StandardJobProcessingService;
import com.dreameddeath.couchbase.core.process.remote.model.TestDoc;
import com.dreameddeath.couchbase.core.process.remote.model.TestDocJobUpdate;

/**
 * Created by Christophe Jeunesse on 04/01/2016.
 */
@JobProcessingForClass(TestDocJobUpdate.class)
public class TestJobUpdateService extends StandardJobProcessingService<TestDocJobUpdate> {
    @Override
    public boolean init(JobContext<TestDocJobUpdate> context) throws JobExecutionException {
        TestDocJobUpdate.TestJobUpdateTask task =new TestDocJobUpdate.TestJobUpdateTask();
        task.setDocKey(context.getJob().docKey);
        context.addTask(task);
        return false;
    }


    @TaskProcessingForClass(TestDocJobUpdate.TestJobUpdateTask.class)
    public static class TestJobUpdateTaskService extends DocumentUpdateTaskProcessingService<TestDocJobUpdate,TestDoc,TestDocJobUpdate.TestJobUpdateTask>{
        @Override
        protected boolean processDocument(TaskContext<TestDocJobUpdate, TestDocJobUpdate.TestJobUpdateTask> ctxt, TestDoc doc) throws DaoException, StorageException {
            doc.intValue+=ctxt.getParentJob().incrIntValue;
            return false;
        }
    }
}
