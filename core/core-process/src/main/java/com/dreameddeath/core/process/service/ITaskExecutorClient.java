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

package com.dreameddeath.core.process.service;

import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.model.base.AbstractJob;
import com.dreameddeath.core.process.model.base.AbstractTask;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.user.IUser;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 03/01/2016.
 */
public interface ITaskExecutorClient<TJOB extends AbstractJob,TTASK extends AbstractTask> {
    UUID getInstanceUUID();
    Class<TTASK> getTaskClass();
    ITaskExecutorService<TJOB,TTASK> getExecutorService();
    ITaskProcessingService<TJOB,TTASK> getProcessingService();


    TTASK executeTask(JobContext<TJOB> parentContext,TTASK task) throws TaskExecutionException;
    TTASK executeTask(TJOB parentJob,TTASK task, IUser user) throws TaskExecutionException;
    TaskContext<TJOB,TTASK> buildTaskContext(JobContext<TJOB> jobContext,TTASK task);
}
