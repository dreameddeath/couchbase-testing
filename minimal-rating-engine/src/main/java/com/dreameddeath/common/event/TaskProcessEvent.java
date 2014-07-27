package com.dreameddeath.common.event;

import com.dreameddeath.common.model.process.AbstractJob;
import com.dreameddeath.common.model.process.AbstractTask;

/**
 * Created by ceaj8230 on 27/07/2014.
 */
public class TaskProcessEvent {
    private AbstractTask _task;

    public TaskProcessEvent(AbstractTask task){
        _task = task;
    }

    public AbstractTask getTask(){
        return _task;
    }

    public AbstractJob getJob(){
        return _task.getParentJob();
    }

}
