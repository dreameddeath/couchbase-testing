package com.dreameddeath.common.process;

import com.dreameddeath.common.model.process.AbstractTask;

/**
 * Created by ceaj8230 on 21/05/2014.
 */
public interface TaskProcessingService<T extends AbstractTask> {
    public void execute(T task);
    public ProcessingServiceFactory getFactory();
}
