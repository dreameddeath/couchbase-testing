package com.dreameddeath.core.process;

import com.dreameddeath.core.exception.process.JobExecutionException;
import com.dreameddeath.core.model.process.AbstractJob;
import com.dreameddeath.core.model.process.AbstractJob.State;
import com.dreameddeath.core.model.process.AbstractTask;


/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public class BasicJobProcessingServiceImpl implements JobProcessingService<AbstractJob> {
    private ProcessingServiceFactory _factory;

    public BasicJobProcessingServiceImpl(ProcessingServiceFactory factory){
        _factory = factory;
    }

    public ProcessingServiceFactory getFactory(){
        return _factory;
    }


    public void onSave(AbstractJob job,State state){}
    public void onEndProcessing(AbstractJob job,State state){}

    public void execute(AbstractJob job) throws JobExecutionException{
        job.setProcessingService(this);
        job.setLastRunError(null);
        try {
            if (!job.isInitialized()) {
                try {
                    if (job.init()) {
                        job.setJobState(State.INITIALIZED);
                        onSave(job, State.INITIALIZED);
                        job.save();
                    }
                    onEndProcessing(job, State.INITIALIZED);
                } catch (Throwable e) {
                    throw new JobExecutionException(job, State.INITIALIZED, e);
                }
            }

            if (!job.isPrepared()) {
                try {
                    if (job.preprocess()) {
                        job.setJobState(State.PREPROCESSED);
                        onSave(job, State.PREPROCESSED);
                        job.save();
                    }
                    onEndProcessing(job, State.PREPROCESSED);
                } catch (Throwable e) {
                    throw new JobExecutionException(job, State.PREPROCESSED, e);
                }
            }

            if (!job.isProcessed()) {
                AbstractTask task = null;
                try {
                    while ((task = job.getNextPendingTask()) != null) {
                        task.setLastRunError(null);
                        getFactory().getTaskServiceForClass(AbstractTask.class).execute(task);
                    }
                    onSave(job, State.PROCESSED);
                    job.setJobState(State.PROCESSED);
                    job.save();
                    onEndProcessing(job, State.PROCESSED);
                } catch (Throwable e) {
                    throw new JobExecutionException(job, State.PROCESSED, e);
                }
            }

            if (!job.isFinalized()) {
                try {
                    if (job.postprocess()) {
                        job.setJobState(State.POSTPROCESSED);
                        onSave(job, State.POSTPROCESSED);
                        job.save();
                    }
                    onEndProcessing(job, State.POSTPROCESSED);
                } catch (Throwable e) {
                    throw new JobExecutionException(job, State.POSTPROCESSED, e);
                }
            }

            if (!job.isDone()) {
                try {
                    job.cleanup();
                    job.setJobState(State.DONE);
                    onSave(job,State.DONE);
                    job.save();
                    onEndProcessing(job,State.DONE);
                } catch (Throwable e) {
                    throw new JobExecutionException(job, State.DONE, e);
                }
            }
        }
        catch(JobExecutionException e){
            job.setLastRunError("["+e.getClass().getSimpleName()+"] "+e.getMessage());
            throw e;
        }
        catch(Throwable e){
            job.setLastRunError("["+e.getClass().getSimpleName()+"] "+e.getMessage());
            throw new JobExecutionException(job,State.UNKNOWN,e);
        }
        job.setProcessingService(null);
    }

}
