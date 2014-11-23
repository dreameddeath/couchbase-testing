package com.dreameddeath.core.process.service;

import com.dreameddeath.core.exception.process.JobExecutionException;
import com.dreameddeath.core.model.process.AbstractJob;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public interface IJobExecutorService<T extends AbstractJob> {
    public void execute(JobContext context,T job) throws JobExecutionException;
}
