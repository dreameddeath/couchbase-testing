package com.dreameddeath.core.event;

import com.dreameddeath.core.model.process.AbstractJob;
import com.dreameddeath.core.model.process.AbstractTask;

/**
 * Created by Christophe Jeunesse on 27/07/2014.
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
