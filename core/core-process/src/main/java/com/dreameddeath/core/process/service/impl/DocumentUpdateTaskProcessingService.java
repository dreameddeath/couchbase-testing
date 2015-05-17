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

package com.dreameddeath.core.process.service.impl;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.process.exception.DuplicateAttachedTaskException;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.model.CouchbaseDocumentAttachedTaskRef;
import com.dreameddeath.core.process.model.DocumentUpdateTask;
import com.dreameddeath.core.process.model.IDocumentWithLinkedTasks;
import com.dreameddeath.core.process.service.ITaskProcessingService;
import com.dreameddeath.core.process.service.TaskContext;

/**
 * Created by ceaj8230 on 23/11/2014.
 */
public abstract class DocumentUpdateTaskProcessingService<TDOC extends CouchbaseDocument & IDocumentWithLinkedTasks,T extends DocumentUpdateTask<TDOC>> implements ITaskProcessingService<T> {
    @Override
    public boolean init(TaskContext ctxt, T task) throws TaskExecutionException {
        return false;
    }

    @Override
    public boolean preprocess(TaskContext ctxt, T task) throws TaskExecutionException {
        return false;
    }

    @Override
    public boolean process(TaskContext ctxt, T task) throws TaskExecutionException {
        try {
            TDOC doc = (TDOC)ctxt.getSession().get(task.getDocKey());

            CouchbaseDocumentAttachedTaskRef reference = doc.getAttachedTaskRef(task);
            if (reference == null) {
                processDocument(ctxt,task);
                CouchbaseDocumentAttachedTaskRef attachedTaskRef = new CouchbaseDocumentAttachedTaskRef();
                attachedTaskRef.setJobKey(task.getParentJob().getBaseMeta().getKey());
                attachedTaskRef.setJobClass(task.getParentJob().getClass().getName());
                attachedTaskRef.setTaskId(task.getUid());
                attachedTaskRef.setTaskClass(task.getClass().getName());
                doc.addAttachedTaskRef(attachedTaskRef);
                try {
                    ctxt.getSession().save(doc);
                } catch (ValidationException e) {
                    throw new TaskExecutionException(task, task.getState(), "Updated Document Validation exception", e);
                }
                return true;
            }
        }
        catch (DuplicateAttachedTaskException e){
            throw new TaskExecutionException(task, task.getState(), "Duplicate task exception", e);
        }
        catch(DaoException e){
            throw new TaskExecutionException(task, task.getState(), "Dao exception", e);
        }
        catch(StorageException e){
            throw new TaskExecutionException(task, task.getState(), "Storage exception", e);
        }
        return false;
    }

    @Override
    public boolean postprocess(TaskContext ctxt, T task) throws TaskExecutionException {
        return false;
    }

    @Override
    public boolean finish(TaskContext ctxt, T task) throws TaskExecutionException {
        return false;
    }

    @Override
    public boolean cleanup(TaskContext ctxt, T task) throws TaskExecutionException {
        try {
            TDOC doc = (TDOC)ctxt.getSession().get(task.getDocKey());
            doc.cleanupAttachedTaskRef(task);
            ctxt.getSession().save(doc);
        }
        catch(ValidationException e){
            throw new TaskExecutionException(task,task.getState(),"Cleaned updated document Validation exception",e);
        }
        catch(DaoException e){
            throw new TaskExecutionException(task,task.getState(),"Error in dao",e);
        }
        catch(StorageException e){
            throw new TaskExecutionException(task,task.getState(),"Error in storage",e);
        }
        return false;
    }


    protected abstract void processDocument(TaskContext ctxt,T task) throws DaoException,StorageException;
}
