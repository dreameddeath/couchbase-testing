package com.dreameddeath.core.exception.model;

import com.dreameddeath.core.model.process.AbstractJob;
import com.dreameddeath.core.model.process.AbstractTask;

/**
 * Created by CEAJ8230 on 08/10/2014.
 */
public class DuplicateTaskException extends Exception {
    private AbstractTask _task;
    private AbstractJob _job;


    public DuplicateTaskException(AbstractTask task, AbstractJob job){
        this(task,job,"The task <"+task.getUid()+"> is already existing in job <"+job.getBaseMeta().getKey()+">");
    }

    public DuplicateTaskException(AbstractTask task, AbstractJob job, String message){
        super(message);
        _job = job;
        _task = task;
    }
}
