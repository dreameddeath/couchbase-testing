package com.dreameddeath.core.process;

import com.dreameddeath.core.exception.process.TaskExecutionException;
import com.dreameddeath.core.model.process.AbstractTask;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public interface TaskProcessingService<T extends AbstractTask> {
    public void execute(T task) throws TaskExecutionException;
    public ProcessingServiceFactory getFactory();
}
