package com.dreameddeath.core.process.service;

import com.dreameddeath.core.exception.process.TaskExecutionException;
import com.dreameddeath.core.process.common.AbstractTask;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public interface TaskProcessingService<T extends AbstractTask> {
    public void execute(T task) throws TaskExecutionException;
    public ProcessingServiceFactory getFactory();
}
