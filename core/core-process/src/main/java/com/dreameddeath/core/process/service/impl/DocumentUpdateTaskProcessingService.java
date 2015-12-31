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

package com.dreameddeath.core.process.service.impl;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.process.exception.DuplicateAttachedTaskException;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.model.AbstractJob;
import com.dreameddeath.core.process.model.CouchbaseDocumentAttachedTaskRef;
import com.dreameddeath.core.process.model.DocumentUpdateTask;
import com.dreameddeath.core.process.model.IDocumentWithLinkedTasks;
import com.dreameddeath.core.process.service.context.TaskContext;

/**
 * Created by Christophe Jeunesse on 23/11/2014.
 */
public abstract class DocumentUpdateTaskProcessingService<TJOB extends AbstractJob,TDOC extends CouchbaseDocument & IDocumentWithLinkedTasks,T extends DocumentUpdateTask<TDOC>> extends StandardTaskProcessingService<TJOB,T> {

    @Override
    public boolean process(TaskContext<TJOB,T> ctxt) throws TaskExecutionException {
        DocumentUpdateTask<TDOC> task = ctxt.getTask();
        try {
            TDOC doc = (TDOC)ctxt.getSession().get(task.getDocKey());

            CouchbaseDocumentAttachedTaskRef reference = doc.getAttachedTaskRef(task);
            if (reference == null) {
                if(ctxt.getTask().getBaseMeta().getState()== CouchbaseDocument.DocumentState.NEW){
                    try{
                        ctxt.save();
                    } catch (ValidationException e) {
                        throw new TaskExecutionException(ctxt, "Updated Document Validation exception", e);
                    }
                }
                processDocument(ctxt,doc);
                CouchbaseDocumentAttachedTaskRef attachedTaskRef = new CouchbaseDocumentAttachedTaskRef();
                attachedTaskRef.setJobUid(ctxt.getParentJob().getUid());
                attachedTaskRef.setJobClass(ctxt.getParentJob().getClass().getName());
                attachedTaskRef.setTaskId(ctxt.getTask().getId());
                attachedTaskRef.setTaskClass(task.getClass().getName());
                doc.addAttachedTaskRef(attachedTaskRef);
                try {
                    ctxt.getSession().save(doc);
                } catch (ValidationException e) {
                    throw new TaskExecutionException(ctxt, "Updated Document Validation exception", e);
                }
                return true;
            }
        }
        catch (DuplicateAttachedTaskException e){
            throw new TaskExecutionException(ctxt, "Duplicate task exception", e);
        }
        catch(DaoException e){
            throw new TaskExecutionException(ctxt, "Dao exception", e);
        }
        catch(StorageException e){
            throw new TaskExecutionException(ctxt, "Storage exception", e);
        }
        return false;
    }

    @Override
    public boolean cleanup(TaskContext<TJOB,T> ctxt) throws TaskExecutionException {
        T task=ctxt.getTask();
        try {
            TDOC doc = (TDOC)ctxt.getSession().get(ctxt.getTask().getDocKey());
            doc.cleanupAttachedTaskRef(task);
            ctxt.getSession().save(doc);
        }
        catch(ValidationException e){
            throw new TaskExecutionException(ctxt,"Cleaned updated document Validation exception",e);
        }
        catch(DaoException e){
            throw new TaskExecutionException(ctxt,"Error in dao",e);
        }
        catch(StorageException e){
            throw new TaskExecutionException(ctxt,"Error in storage",e);
        }
        return false;
    }


    protected abstract void processDocument(TaskContext<TJOB,T> ctxt,TDOC doc) throws DaoException,StorageException;
}
