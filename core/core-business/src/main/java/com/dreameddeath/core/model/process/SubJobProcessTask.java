package com.dreameddeath.core.model.process;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.session.ICouchbaseSession;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
public abstract class SubJobProcessTask<T extends AbstractJob> extends AbstractTask {
    @DocumentProperty("jobId")
    private Property<UUID> _jobId =new StandardProperty<UUID>(SubJobProcessTask.this);

    public UUID getJobId(){ return _jobId.get(); }
    public void setJobId(UUID jobId){_jobId.set(jobId);}

    public T getJob(ICouchbaseSession session) throws DaoException,StorageException{return (T)session.getFromUID(getJobId().toString(),AbstractJob.class);}
}
