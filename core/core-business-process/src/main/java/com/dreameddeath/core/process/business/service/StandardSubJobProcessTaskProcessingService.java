package com.dreameddeath.core.process.business.service;

import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.process.TaskExecutionException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.exception.validation.ValidationException;
import com.dreameddeath.core.model.process.AbstractJob;
import com.dreameddeath.core.model.process.SubJobProcessTask;
import com.dreameddeath.core.process.service.ITaskProcessingService;
import com.dreameddeath.core.process.service.TaskContext;

/**
 * Created by CEAJ8230 on 25/11/2014.
 */
public abstract class StandardSubJobProcessTaskProcessingService<TJOB extends AbstractJob,TTASK extends SubJobProcessTask<TJOB>> implements ITaskProcessingService<TTASK> {
    @Override
    public boolean init(TaskContext ctxt, TTASK task) throws TaskExecutionException {
        try {
            if(task.getJobId()!=null){
                if(task.getJob(ctxt.getSession())!=null) return false;
            }

            TJOB job=buildSubJob(ctxt,task);
            //Retrieve UID
            task.setJobId(job.getUid());
            //Save task to allow retries without creation duplicates
            ctxt.getSession().save(task.getParentJob());
            //Save job (should be a creation)
            ctxt.getSession().save(job);
        }
        catch(ValidationException e){
            throw new TaskExecutionException(task,task.getState(),"Validation of job or parent job failed",e);
        }
        catch(DaoException e){
            throw new TaskExecutionException(task,task.getState(),"Dao error",e);
        }
        catch(StorageException e){
            throw new TaskExecutionException(task,task.getState(),"Storage error",e);
        }

        return false;
    }

    @Override
    public boolean preprocess(TaskContext ctxt, TTASK task) throws TaskExecutionException {
        return false;
    }

    @Override
    public boolean process(TaskContext ctxt, TTASK task) throws TaskExecutionException {
        throw new TaskExecutionException(task,task.getState(),"Cannot process at this level this type of task : must be handled at TaskProcessService level");
    }

    @Override
    public boolean postprocess(TaskContext ctxt, TTASK task) throws TaskExecutionException {
        return false;
    }

    @Override
    public boolean finish(TaskContext ctxt, TTASK task) throws TaskExecutionException {
        return false;
    }

    @Override
    public boolean cleanup(TaskContext ctxt, TTASK task) throws TaskExecutionException {
        return false;
    }

    protected abstract TJOB buildSubJob(TaskContext ctxt, TTASK task) throws DaoException,StorageException;

}
