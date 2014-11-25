package com.dreameddeath.core.process.business.service;

import com.dreameddeath.core.exception.process.JobExecutionException;
import com.dreameddeath.core.model.process.AbstractJob;
import com.dreameddeath.core.process.service.IJobProcessingService;
import com.dreameddeath.core.process.service.JobContext;

/**
 * Created by CEAJ8230 on 25/11/2014.
 */
public abstract class StandardJobProcessingService<T extends AbstractJob> implements IJobProcessingService<T> {

    @Override
    public boolean preprocess(JobContext context, T job) throws JobExecutionException {
        return false;
    }

    @Override
    public boolean postprocess(JobContext context, T job) throws JobExecutionException {
        return false;
    }

    @Override
    public boolean cleanup(JobContext context, T job) throws JobExecutionException {
        return false;
    }
}
