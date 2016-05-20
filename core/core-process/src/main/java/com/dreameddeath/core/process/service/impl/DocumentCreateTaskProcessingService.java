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
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.ProcessState;
import com.dreameddeath.core.process.model.v1.tasks.DocumentCreateTask;
import com.dreameddeath.core.process.service.context.TaskContext;

/**
 * Created by Christophe Jeunesse on 23/11/2014.
 */
public abstract class DocumentCreateTaskProcessingService<TJOB extends AbstractJob,TDOC extends CouchbaseDocument,T extends DocumentCreateTask<TDOC>> extends StandardTaskProcessingService<TJOB,T> {
    @Override
    public final boolean process(TaskContext<TJOB,T> ctxt) throws TaskExecutionException {
        T task = ctxt.getTask();
        try {
            //Recovery mode
            if(task.getDocKey()!=null){
                if(ctxt.getSession().get(task.getDocKey())!=null){
                    return false;
                }
            }
            TDOC doc;
            try {
                ctxt.getTask().getBaseMeta().freeze();
                ctxt.getSession().setTemporaryReadOnlyMode(true);
                doc=buildDocument(ctxt);
            }
            finally {
                ctxt.getSession().setTemporaryReadOnlyMode(false);
                ctxt.getTask().getBaseMeta().unfreeze();
            }

            //Prebuild key
            task.setDocKey(ctxt.getSession().buildKey(doc).getBaseMeta().getKey());
            //Attach it to the document
            ctxt.save();
            //Save Document afterwards
            ctxt.getSession().save(doc);
        }
        catch(ValidationException e){
            throw new TaskExecutionException(task, ProcessState.State.PROCESSED,"Validation error", e);
        }
        catch(DaoException e){
            throw new TaskExecutionException(task, ProcessState.State.PROCESSED,"Dao error", e);
        }
        catch(StorageException e){
            throw new TaskExecutionException(task, ProcessState.State.PROCESSED,"Storage error", e);
        }
        finally {
            ctxt.getSession().setTemporaryReadOnlyMode(false);
            ctxt.getTask().getBaseMeta().unfreeze();
        }
        return false; //No need to save (retry allowed)
    }

    protected abstract TDOC buildDocument(TaskContext<TJOB,T> ctxt) throws DaoException,StorageException;

}
