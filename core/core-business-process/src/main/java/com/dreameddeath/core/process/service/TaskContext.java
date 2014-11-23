package com.dreameddeath.core.process.service;

import com.dreameddeath.core.session.ICouchbaseSession;

/**
 * Created by ceaj8230 on 23/11/2014.
 */
public class TaskContext {
    private JobContext _jobContext;
    public JobContext getJobContext(){return _jobContext;}
    public void setJobContext(JobContext context){_jobContext=context;}

    public ICouchbaseSession getSession(){return _jobContext.getSession();}

    public ExecutorServiceFactory getExecutorFactory(){return _jobContext.getExecutorFactory();}
    public ProcessingServiceFactory getProcessingFactory(){return _jobContext.getProcessingFactory();}

    public static TaskContext newContext(JobContext ctxt){
        TaskContext res = new TaskContext();
        res.setJobContext(ctxt);
        return res;
    }

}
