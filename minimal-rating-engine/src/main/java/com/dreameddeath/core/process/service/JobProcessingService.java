package com.dreameddeath.core.process.service;

import com.dreameddeath.core.exception.process.JobExecutionException;
import com.dreameddeath.core.process.common.AbstractJob;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public interface JobProcessingService<T extends AbstractJob> {
    public void execute(T job) throws JobExecutionException;
    public ProcessingServiceFactory getFactory();
}
