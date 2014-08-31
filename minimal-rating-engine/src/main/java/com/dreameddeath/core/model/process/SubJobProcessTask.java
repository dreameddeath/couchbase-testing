package com.dreameddeath.core.model.process;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.exception.dao.ValidationException;
import com.dreameddeath.core.exception.process.TaskExecutionException;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.StandardProperty;
import com.dreameddeath.core.process.JobProcessingService;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
public abstract class SubJobProcessTask<T extends AbstractJob> extends AbstractTask {
    @DocumentProperty("jobId")
    private Property<UUID> _jobId =new StandardProperty<UUID>(SubJobProcessTask.this);

    public UUID getJobId(){ return _jobId.get(); }
    public void setJobId(UUID jobId){_jobId.set(jobId);}
    public T getJob(){return (T)this.getParentJob().getSession().getFromUID(_jobId.get().toString(),AbstractJob.class);}

    @Override
    public final boolean init() throws TaskExecutionException{
        if(_jobId.get()!=null){
            if(getJob()!=null) return false;
        }
        T job=buildSubJob();
        //Retrieve UID
        _jobId.set(job.getUid());
        try {
            //Save task to allow retries without creation duplicates
            this.getParentJob().save();
            //Save job (should be a creation)
            job.save();
        }
        catch(ValidationException e){
            throw new TaskExecutionException(this,this.getState(),"Validation of job or parent job failed",e);
        }
        return false;
    }

    protected abstract T buildSubJob();

    @Override
    public final boolean process() throws TaskExecutionException{
        try {
            T job = getJob();
            if (!job.isDone()) {
                JobProcessingService<T> processingService = (JobProcessingService<T>) getParentJob().getProcessingService().getFactory().getJobServiceForClass(job.getClass());
                processingService.execute(job);
            }
            return true;
        }
        catch(Exception e){
            throw new TaskExecutionException(this,this.getState(),e);
        }
    }



}
