package com.dreameddeath.common.process;

import com.dreameddeath.common.model.process.AbstractJob;

/**
 * Created by ceaj8230 on 21/05/2014.
 */
public interface JobProcessingService<T extends AbstractJob> {
    public void execute(T job);
    public ProcessingServiceFactory getFactory();
}
