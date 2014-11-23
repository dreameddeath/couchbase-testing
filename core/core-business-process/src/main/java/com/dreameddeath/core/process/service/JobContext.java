package com.dreameddeath.core.process.service;

import com.dreameddeath.core.session.ICouchbaseSession;

/**
 * Created by ceaj8230 on 23/11/2014.
 */
public class JobContext {
    private ICouchbaseSession _session;
    private ExecutorServiceFactory _executorFactory;
    private ProcessingServiceFactory _processingFactory;

    public ICouchbaseSession getSession(){return _session;}
    public void setSession(ICouchbaseSession session){ _session=session;}

    public void setExecutorFactory(ExecutorServiceFactory factory){_executorFactory = factory;}
    public ExecutorServiceFactory getExecutorFactory(){return _executorFactory;}

    public void setProcessingFactory(ProcessingServiceFactory factory){_processingFactory = factory;}
    public ProcessingServiceFactory getProcessingFactory(){return _processingFactory;}

    public static JobContext newContext(ICouchbaseSession session, ExecutorServiceFactory execFactory,ProcessingServiceFactory processFactory){
        JobContext res = new JobContext();
        res.setSession(session);
        res.setExecutorFactory(execFactory);
        res.setProcessingFactory(processFactory);
        return res;
    }

    public static JobContext newContext(JobContext ctxt){
        JobContext res = new JobContext();
        res.setSession(ctxt.getSession());
        res.setExecutorFactory(ctxt.getExecutorFactory());
        res.setProcessingFactory(ctxt.getProcessingFactory());
        return res;
    }

}
