package com.dreameddeath.core.exception.process;

import com.dreameddeath.core.model.process.AbstractJob;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class JobExecutionException extends Exception {
    AbstractJob.State _state;
    AbstractJob _job;

    public JobExecutionException(AbstractJob job, AbstractJob.State state, String message) {
        super(message);
        _job = job;
        _state = state;
    }

    public JobExecutionException(AbstractJob job, AbstractJob.State state, String message, Throwable e) {
        super(message, e);
        _job = job;
        _state = state;
    }

    public JobExecutionException(AbstractJob job, AbstractJob.State state, Throwable e) {
        super(e);
        _job = job;
        _state = state;
    }

    public AbstractJob getJob(){ return _job;}
    public AbstractJob.State getState(){ return _state;}

}
