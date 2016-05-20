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

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import com.dreameddeath.core.process.model.v1.tasks.SubTaskCreatorTask;
import com.dreameddeath.core.process.service.context.TaskContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Christophe Jeunesse on 29/01/2016.
 */
public abstract class TaskCreatorTaskProcessingService<TJOB extends AbstractJob,TTASK extends SubTaskCreatorTask> extends StandardTaskProcessingService<TJOB,TTASK> {
    private static final Logger LOG = LoggerFactory.getLogger(TaskCreatorTaskProcessingService.class);
    @Override
    final public boolean process(TaskContext<TJOB, TTASK> ctxt) throws TaskExecutionException {
        Collection<AbstractTask> tasks;
        try {
            ctxt.getTask().getBaseMeta().freeze();
            ctxt.getSession().setTemporaryReadOnlyMode(true);
            tasks = buildAdditionnalTasks(ctxt);
        }
        finally {
            ctxt.getSession().setTemporaryReadOnlyMode(false);
            ctxt.getTask().getBaseMeta().unfreeze();
        }
        try {
            tasks = ctxt.getJobContext().assignIds(tasks);
            final AtomicInteger nbErrors=new AtomicInteger();
            tasks.forEach(subTask-> {
                    ctxt.getTask().addSubTasks(subTask.getId());
                    subTask.addDependency(ctxt.getTask().getId());
                    TaskContext subTaskContext = ctxt.getJobContext().addTask(subTask);
                    try {
                        subTaskContext.save();
                    }
                    catch(DaoException|StorageException|ValidationException e){
                        LOG.error("Failling to save subTask of task "+ctxt.getTask().getId(),e);
                        nbErrors.incrementAndGet();
                    }
                }
            );
            if(nbErrors.get()>0){
                throw new TaskExecutionException(ctxt,"Save at least one of sub tasks");
            }
        }
        catch(DaoException|StorageException e){
            throw new TaskExecutionException(ctxt,"Failling to assign ids to sub tasks",e);
        }
        return true;
    }

    @Override
    public final boolean postprocess(TaskContext<TJOB, TTASK> context) throws TaskExecutionException {
        for(String taskId:context.getTask().getSubTasks()) {
            context.getJobContext().getJob().addTask(taskId);
        }
        try {
            context.getJobContext().save();
        }
        catch(DaoException|StorageException|ValidationException e){
            throw new TaskExecutionException(context,"Failling to add tasks to job tasks",e);
        }
        return true;
    }

    protected abstract Collection<AbstractTask> buildAdditionnalTasks(TaskContext<TJOB, TTASK> ctxt) throws TaskExecutionException;
}
