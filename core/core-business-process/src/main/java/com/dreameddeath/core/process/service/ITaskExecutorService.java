package com.dreameddeath.core.process.service;

import com.dreameddeath.core.exception.process.TaskExecutionException;
import com.dreameddeath.core.model.process.AbstractTask;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public interface ITaskExecutorService<T extends AbstractTask> {
    public void execute(TaskContext ctxt,T task) throws TaskExecutionException;
}
