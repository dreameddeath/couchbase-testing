package com.dreameddeath.common.model.process;

import com.dreameddeath.common.annotation.DocumentProperty;
import com.dreameddeath.common.model.property.ImmutableProperty;
import com.dreameddeath.common.model.property.Property;
import com.dreameddeath.common.model.property.StandardProperty;
import com.dreameddeath.common.process.JobProcessingService;

import java.util.UUID;

/**
 * Created by ceaj8230 on 01/08/2014.
 */
public abstract class SubJobProcessTask<T extends AbstractJob> extends AbstractTask {
    @DocumentProperty("jobId")
    private Property<UUID> _jobId =new StandardProperty<UUID>(SubJobProcessTask.this);

    public UUID getJobId(){ return _jobId.get(); }
    public void setJobId(UUID jobId){_jobId.set(jobId);}
    public T getJob(){return (T)this.getParentJob().getSession().getFromUID(_jobId.get().toString(),AbstractJob.class);}

    @Override
    public final boolean init(){
        if(_jobId.get()!=null){
            if(getJob()!=null) return false;
        }
        T job=buildSubJob();
        //Retrieve UID
        _jobId.set(job.getUid());
        //Save task to allow retries without creation duplicates
        this.getParentJob().save();
        //Save job (should be a creation)
        job.save();
        return false;
    }

    protected abstract T buildSubJob();

    @Override
    public final boolean process(){
        T job = getJob();
        if(!job.isDone()){
            JobProcessingService<T> processingService = (JobProcessingService<T>)getParentJob().getProcessingService().getFactory().getJobServiceForClass(job.getClass());
            processingService.execute(job);
        }
        return true;
    }



}
