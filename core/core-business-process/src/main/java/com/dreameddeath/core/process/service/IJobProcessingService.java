package com.dreameddeath.core.process.service;

import com.dreameddeath.core.exception.process.JobExecutionException;
import com.dreameddeath.core.model.process.AbstractJob;

/**
 * Created by ceaj8230 on 23/11/2014.
 */
public interface IJobProcessingService<T extends AbstractJob> {
    public boolean init(JobContext context,T job) throws JobExecutionException;
    public boolean preprocess(JobContext context,T job)throws JobExecutionException;
    public boolean postprocess(JobContext context,T job)throws JobExecutionException;
    public boolean cleanup(JobContext context,T job)throws JobExecutionException;
}
