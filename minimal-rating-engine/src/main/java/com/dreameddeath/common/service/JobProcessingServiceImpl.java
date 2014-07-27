package com.dreameddeath.common.service;

import com.dreameddeath.common.model.process.AbstractJob;
import com.dreameddeath.common.model.process.AbstractJob.State;
import com.dreameddeath.common.model.process.AbstractTask;

/**
 * Created by ceaj8230 on 21/05/2014.
 */
public class JobProcessingServiceImpl<T extends AbstractJob> implements JobProcessingService<T> {
    TaskProcessingService _taskService=null;
    public JobProcessingServiceImpl(TaskProcessingService taskService){
        _taskService = taskService;
    }


    public void execute(T job) {
        if(!job.isInitialized()){
            job.init();
            job.setJobState(State.INITIALIZED);
            job.save();
        }

        if(!job.isPrepared()){
            job.preprocess();
            job.setJobState(State.PREPROCESSED);
            job.save();
        }

        if(!job.isProcessed()){
            try{
                AbstractTask task=null;
                while((task=job.getNextPendingTask())!=null){
                    _taskService.execute(task);
                }
                job.setJobState(State.PROCESSED);
            }
            catch(Exception e) {

            }
            job.save();
        }

        if(!job.isFinalized()){
            job.postprocess();
            job.setJobState(State.POSTPROCESSED);
            job.save();
        }

        if(!job.isDone()){
            job.cleanup();
            job.setJobState(State.DONE);
            job.save();
        }
    }
}
