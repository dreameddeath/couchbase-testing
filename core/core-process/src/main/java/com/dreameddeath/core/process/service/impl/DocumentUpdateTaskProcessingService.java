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
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.CouchbaseDocumentAttachedTaskRef;
import com.dreameddeath.core.process.model.v1.base.IDocumentWithLinkedTasks;
import com.dreameddeath.core.process.model.v1.tasks.DocumentUpdateTask;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.google.common.base.Preconditions;

/**
 * Created by Christophe Jeunesse on 23/11/2014.
 */
public abstract class DocumentUpdateTaskProcessingService<TJOB extends AbstractJob,TDOC extends CouchbaseDocument & IDocumentWithLinkedTasks,T extends DocumentUpdateTask<TDOC>> extends StandardTaskProcessingService<TJOB,T> {

    @Override
    public boolean process(TaskContext<TJOB,T> ctxt) throws TaskExecutionException {
        DocumentUpdateTask<TDOC> task = ctxt.getTask();
        Preconditions.checkNotNull(task.getDocKey(),"The document to update hasn't key for task %s of type %s",task.getId(),task.getClass().getName());
        try {
            @SuppressWarnings("unchecked")
            TDOC doc = (TDOC)ctxt.getSession().get(task.getDocKey());

            //Check if the task is already done on the documment
            CouchbaseDocumentAttachedTaskRef reference = doc.getAttachedTaskRef(task);
            if (reference == null) {
                if(ctxt.getTask().getBaseMeta().getState()== CouchbaseDocument.DocumentState.NEW){
                    try{
                        ctxt.save();
                    } catch (ValidationException e) {
                        throw new TaskExecutionException(ctxt, "Updated Document Validation exception", e);
                    }
                }
                if(ctxt.getTask().getUpdatedWithDoc()){
                    cleanTaskBeforeRetryProcessing(ctxt,doc);
                    ctxt.getTask().setUpdatedWithDoc(false);
                    try {
                        ctxt.save();
                    }
                    catch(ValidationException e){
                        throw new TaskExecutionException(ctxt, "Cleaned up Task Validation exception", e);
                    }
                }
                boolean taskUpdated;
                try {
                    ctxt.getSession().setTemporaryReadOnlyMode(true);
                    taskUpdated=processDocument(ctxt, doc);
                }
                finally {
                    ctxt.getSession().setTemporaryReadOnlyMode(false);
                }

                if(taskUpdated){
                    try {
                        ctxt.getTask().setUpdatedWithDoc(true);
                        ctxt.save();
                    }
                    catch(ValidationException e){
                        throw new TaskExecutionException(ctxt, "Updated Task Validation exception", e);
                    }
                }

                //Tell that the document has been updated
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
        return false; //No need to save, retry allowed
    }

    /**
     * Allow a clean up of task result before retry the processing of the document
     * @param ctxt
     * @param doc
     */
    protected void cleanTaskBeforeRetryProcessing(TaskContext<TJOB, T> ctxt, TDOC doc) {
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

    /**
     * Implement the document update processing
     * @param ctxt
     * @param doc
     * @return a value telling to save or not the task
     * @throws DaoException
     * @throws StorageException
     * @throws TaskExecutionException
     */
    protected abstract boolean processDocument(TaskContext<TJOB,T> ctxt,TDOC doc) throws DaoException,StorageException,TaskExecutionException;
}
