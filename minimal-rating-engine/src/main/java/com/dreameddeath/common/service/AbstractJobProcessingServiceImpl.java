package com.dreameddeath.common.service;

import com.dreameddeath.common.model.process.AbstractJob;
import com.dreameddeath.common.model.process.AbstractJob.State;
/**
 * Created by ceaj8230 on 21/05/2014.
 */
public abstract class AbstractJobProcessingServiceImpl<T extends AbstractJob> implements JobProcessingService<T> {
    public abstract void init(T job);
    public void preprocess(T job){}
    public void postprocess(T job){}
    public abstract void cleanup(T job);

    public void process(T job){
        //while(job.)
    }


    public void execute(T job) {
        if(!job.isInitialized()){
            init(job);
            job.setJobState(State.INITIALIZED);
            job.save();
        }

        if(!job.isPrepared()){
            preprocess(job);
            job.setJobState(State.PREPROCESSED);
            job.save();
        }

        if(!job.isProcessed()){
            process(job);
            job.setJobState(State.PROCESSED);
            job.save();
        }

        if(!job.isFinalized()){
            postprocess(job);
            job.setJobState(State.POSTPROCESSED);
            job.save();
        }

        if(!job.isDone()){
            cleanup(job);
            job.setJobState(State.DONE);
            job.save();
        }
    }
}
