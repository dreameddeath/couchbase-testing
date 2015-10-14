/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.process.common;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.dao.DaoException;
import com.dreameddeath.core.dao.exception.dao.ValidationException;
import com.dreameddeath.core.exception.process.TaskExecutionException;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
public abstract class SubJobProcessTask<T extends AbstractJob> extends AbstractTask {
    @DocumentProperty("jobId")
    private Property<UUID> jobId =new StandardProperty<UUID>(SubJobProcessTask.this);

    public UUID getJobId(){ return jobId.get(); }
    public void setJobId(UUID jobId){jobId.set(jobId);}
    public T getJob() throws DaoException,StorageException {return (T)this.getParentJob().getMeta().getSession().getFromUID(jobId.get().toString(),AbstractJob.class);}

    @Override
    public final boolean init() throws TaskExecutionException{
        try {
            if(jobId.get()!=null){
                if(getJob()!=null) return false;
            }

            T job=buildSubJob();
            //Retrieve UID
            jobId.set(job.getUid());
            //Save task to allow retries without creation duplicates
            getParentJob().getMeta().getSession().save(getParentJob());
            //Save job (should be a creation)
            job.getMeta().getSession().save(job);
        }
        catch(ValidationException e){
            throw new TaskExecutionException(this,this.getState(),"Validation of job or parent job failed",e);
        }
        catch(DaoException e){
            throw new TaskExecutionException(this,this.getState(),"Dao error",e);
        }
        catch(StorageException e){
            throw new TaskExecutionException(this,this.getState(),"Storage error",e);
        }

        return false;
    }

    protected abstract T buildSubJob() throws DaoException,StorageException;

    @Override
    public final boolean process() throws TaskExecutionException{
        throw new TaskExecutionException(this,this.getState(),"Cannot process at this level this type of task : must be handled at TaskProcessService level");
    }

}
