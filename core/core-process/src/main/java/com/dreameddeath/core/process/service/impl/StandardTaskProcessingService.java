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

package com.dreameddeath.core.process.service.impl;

import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.model.base.AbstractJob;
import com.dreameddeath.core.process.model.base.AbstractTask;
import com.dreameddeath.core.process.service.ITaskProcessingService;
import com.dreameddeath.core.process.service.context.TaskContext;

/**
 * Created by Christophe Jeunesse on 29/12/2015.
 */
public abstract class StandardTaskProcessingService<TJOB extends AbstractJob,T extends AbstractTask> implements ITaskProcessingService<TJOB,T> {
    @Override
    public boolean init(TaskContext<TJOB,T> context) throws TaskExecutionException {
        return false;
    }

    @Override
    public boolean preprocess(TaskContext<TJOB,T> context) throws TaskExecutionException {
        return false;
    }

    @Override
    public boolean postprocess(TaskContext<TJOB,T> context) throws TaskExecutionException {
        return false;
    }

    @Override
    public boolean finish(TaskContext<TJOB,T> context) throws TaskExecutionException {
        return false;
    }

    @Override
    public boolean updatejob(TaskContext<TJOB,T> context) throws TaskExecutionException {
        return false;
    }


    @Override
    public boolean cleanup(TaskContext<TJOB,T> context) throws TaskExecutionException {
        return false;
    }
}
