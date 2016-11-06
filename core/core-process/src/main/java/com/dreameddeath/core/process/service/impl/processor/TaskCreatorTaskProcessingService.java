/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.process.service.impl.processor;

import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.tasks.SubTaskCreatorTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Christophe Jeunesse on 29/01/2016.
 */
public abstract class TaskCreatorTaskProcessingService<TJOB extends AbstractJob,TTASK extends SubTaskCreatorTask> extends StandardTaskProcessingService<TJOB,TTASK> {
    private static final Logger LOG = LoggerFactory.getLogger(TaskCreatorTaskProcessingService.class);
/*
    @Override
    final public Observable<TaskProcessingResult<TJOB,TTASK>> process(TaskContext<TJOB, TTASK> ctxt){
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
            tasks = ctxt.assignIds(tasks);
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
    public final Observable<TaskProcessingResult<TJOB,TTASK>>  postprocess(TaskContext<TJOB, TTASK> context) {
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

    protected abstract Collection<AbstractTask> buildAdditionnalTasks(TaskContext<TJOB, TTASK> ctxt) throws TaskExecutionException;*/
}
