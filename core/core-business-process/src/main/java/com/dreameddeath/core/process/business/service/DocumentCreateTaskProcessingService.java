/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.process.business.service;

import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.process.TaskExecutionException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.exception.validation.ValidationException;
import com.dreameddeath.core.model.business.BusinessCouchbaseDocument;
import com.dreameddeath.core.model.process.AbstractTask;
import com.dreameddeath.core.process.business.model.DocumentCreateTask;
import com.dreameddeath.core.process.service.ITaskProcessingService;
import com.dreameddeath.core.process.service.TaskContext;

/**
 * Created by ceaj8230 on 23/11/2014.
 */
public abstract class DocumentCreateTaskProcessingService<TDOC extends BusinessCouchbaseDocument,T extends DocumentCreateTask<TDOC>> implements ITaskProcessingService<T> {
    @Override
    public boolean init(TaskContext ctxt, T task) throws TaskExecutionException {return false;}

    @Override
    public boolean preprocess(TaskContext ctxt, T task) throws TaskExecutionException {return false;}

    @Override
    public boolean process(TaskContext ctxt, T task) throws TaskExecutionException {
        try {
            //Recovery mode
            if(task.getDocKey()!=null){
                if(ctxt.getSession().get(task.getDocKey())!=null){
                    return false;
                }
            }

            TDOC doc = buildDocument(ctxt,task);
            //Prebuild key
            task.setDocKey(ctxt.getSession().buildKey(doc).getBaseMeta().getKey());
            //Attach it to the document
            ctxt.getSession().save(task.getParentJob());
            //Save Document afterwards
            ctxt.getSession().save(doc);
        }
        catch(ValidationException e){
            throw new TaskExecutionException(task, AbstractTask.State.PROCESSED,"Validation error", e);
        }
        catch(DaoException e){
            throw new TaskExecutionException(task, AbstractTask.State.PROCESSED,"Dao error", e);
        }
        catch(StorageException e){
            throw new TaskExecutionException(task, AbstractTask.State.PROCESSED,"Dao error", e);
        }
        return false; //No need to save (retry allowed)
    }

    protected abstract TDOC buildDocument(TaskContext ctxt,T task) throws DaoException,StorageException;

    @Override
    public boolean postprocess(TaskContext ctxt, T task) throws TaskExecutionException {return false;}

    @Override
    public boolean finish(TaskContext ctxt, T task) throws TaskExecutionException {
        return false;
    }

    @Override
    public boolean cleanup(TaskContext ctxt, T task) throws TaskExecutionException {
        return false;
    }
}
