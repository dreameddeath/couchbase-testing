package com.dreameddeath.core.exception.process;

import com.dreameddeath.core.process.common.AbstractTask;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class TaskExecutionException extends Exception {
    AbstractTask.State _state;
    AbstractTask _task;

    public TaskExecutionException(AbstractTask task, AbstractTask.State state, String message) {
        super(message);
        _task = task;
        _state = state;
    }

    public TaskExecutionException(AbstractTask task, AbstractTask.State state, String message, Throwable e) {
        super(message, e);
        _task = task;
        _state = state;
    }

    public TaskExecutionException(AbstractTask task, AbstractTask.State state, Throwable e) {
        super(e);
        _task = task;
        _state = state;
    }

    public AbstractTask getTask(){ return _task;}
    public AbstractTask.State getState(){ return _state;}

}
