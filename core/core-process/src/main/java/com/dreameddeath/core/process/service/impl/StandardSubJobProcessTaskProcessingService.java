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
import com.dreameddeath.core.process.model.base.AbstractJob;
import com.dreameddeath.core.process.model.base.ProcessState;
import com.dreameddeath.core.process.model.tasks.SubJobProcessTask;
import com.dreameddeath.core.process.service.ITaskProcessingService;
import com.dreameddeath.core.process.service.context.TaskContext;

/**
 * Created by Christophe Jeunesse on 25/11/2014.
 */
public abstract class StandardSubJobProcessTaskProcessingService<TPARENTJOB extends AbstractJob,TJOB extends AbstractJob,TTASK extends SubJobProcessTask<TJOB>> implements ITaskProcessingService<TPARENTJOB,TTASK> {
    @Override
    public boolean init(TaskContext<TPARENTJOB,TTASK> ctxt) throws TaskExecutionException {
        TTASK task = ctxt.getTask();
        try {
            if(task.getSubJobId()!=null){
                if(task.getJob(ctxt.getSession())!=null) return false;
            }

            TJOB job=buildSubJob(ctxt);
            //Retrieve UID
            task.setSubJobId(job.getUid());
            //Save task to allow retries without creation duplicates
            ctxt.save();
            //Save job (should be a creation)
            ctxt.getSession().save(job);
        }
        catch(ValidationException e){
            throw new TaskExecutionException(task, ProcessState.State.INITIALIZED,"Validation of job or parent job failed",e);
        }
        catch(DaoException e){
            throw new TaskExecutionException(task, ProcessState.State.INITIALIZED,"Dao error",e);
        }
        catch(StorageException e){
            throw new TaskExecutionException(task, ProcessState.State.INITIALIZED,"Storage error",e);
        }

        return false;
    }

    @Override
    public boolean preprocess(TaskContext ctxt) throws TaskExecutionException {
        return false;
    }

    @Override
    public boolean process(TaskContext ctxt) throws TaskExecutionException {
        throw new TaskExecutionException(ctxt.getTask(), ProcessState.State.PROCESSED,"Cannot process at this level this type of task : must be handled at TaskProcessService level");
    }

    @Override
    public boolean postprocess(TaskContext ctxt) throws TaskExecutionException {
        return false;
    }

    @Override
    public boolean finish(TaskContext ctxt) throws TaskExecutionException {
        return false;
    }

    @Override
    public boolean updatejob(TaskContext<TPARENTJOB, TTASK> ctxt) throws TaskExecutionException {
        return false;
    }

    @Override
    public boolean cleanup(TaskContext ctxt) throws TaskExecutionException {
        return false;
    }

    protected abstract TJOB buildSubJob(TaskContext<TPARENTJOB,TTASK> ctxt) throws DaoException,StorageException;
}
