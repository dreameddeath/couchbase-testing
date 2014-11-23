package com.dreameddeath.core.exception.process;

import com.dreameddeath.core.model.process.AbstractJob;
import com.dreameddeath.core.model.process.AbstractTask;

/**
 * Created by CEAJ8230 on 08/10/2014.
 */
public class DuplicateTaskException extends JobExecutionException {
    AbstractTask _task;

    public DuplicateTaskException(AbstractTask task, AbstractJob job){
        super(job,job.getJobState(),"The task <"+task.getUid()+"> is already existing in job <"+job.getBaseMeta().getKey()+">");
        _task = task;
    }

    public DuplicateTaskException(AbstractTask task, AbstractJob job,String message){
        super(job,job.getJobState(),message);
        _job = job;
        _task = task;
    }
}
