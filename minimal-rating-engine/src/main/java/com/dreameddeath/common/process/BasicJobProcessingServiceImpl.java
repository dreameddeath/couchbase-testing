package com.dreameddeath.common.process;

import com.dreameddeath.common.model.process.AbstractJob;
import com.dreameddeath.common.model.process.AbstractJob.State;
import com.dreameddeath.common.model.process.AbstractTask;

/**
 * Created by ceaj8230 on 21/05/2014.
 */
public class BasicJobProcessingServiceImpl implements JobProcessingService<AbstractJob> {
    private ProcessingServiceFactory _factory;

    public BasicJobProcessingServiceImpl(ProcessingServiceFactory factory){
        _factory = factory;
    }

    public ProcessingServiceFactory getFactory(){
        return _factory;
    }

    public void execute(AbstractJob job) {
        job.setProcessingService(this);
        if(!job.isInitialized()){
            if(job.init()) {
                job.setJobState(State.INITIALIZED);
                job.save();
            }
        }

        if(!job.isPrepared()){
            if(job.preprocess()) {
                job.setJobState(State.PREPROCESSED);
                job.save();
            }
        }

        if(!job.isProcessed()){
            try{
                AbstractTask task=null;
                while((task=job.getNextPendingTask())!=null){
                    getFactory().getTaskServiceForClass(AbstractTask.class).execute(task);
                }
                job.setJobState(State.PROCESSED);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            job.save();
        }

        if(!job.isFinalized()){
            if(job.postprocess()) {
                job.setJobState(State.POSTPROCESSED);
                job.save();
            }
        }

        if(!job.isDone()){
            job.cleanup();
            job.setJobState(State.DONE);
            job.save();
        }
        job.setProcessingService(null);
    }

}
