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

package com.dreameddeath.core.process.service.impl.processor;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.tasks.ChildDocumentCreateTask;
import com.dreameddeath.core.process.service.context.TaskContext;

/**
 * Created by Christophe Jeunesse on 23/02/2016.
 */
public abstract class ChildDocumentCreateTaskProcessingService<TJOB extends AbstractJob,TDOC extends CouchbaseDocument,TPARENT extends CouchbaseDocument,T extends ChildDocumentCreateTask<TDOC,TPARENT>> extends DocumentCreateTaskProcessingService<TJOB,TDOC,T> {
    @Override
    final public boolean postprocess(TaskContext<TJOB,T> context) throws TaskExecutionException {
        try {
            TPARENT parent = context.getTask().blockingGetParentDocument(context.getSession());
            TDOC child = context.getTask().blockingGetDocument(context.getSession());
            if (needParentUpdate(parent, child)) {
                updateParent(parent,child);
                try {
                    context.getSession().toBlocking().blockingSave(parent);
                }
                catch(DaoException|StorageException|ValidationException e){
                    throw new TaskExecutionException(context,"Cannot save parent",e);
                }
            }
        }
        catch(DaoException|StorageException e){
            throw new TaskExecutionException(context,"Cannot read child and/or parent",e);
        }
        return true;
    }


    protected abstract boolean needParentUpdate(TPARENT parent,TDOC child);
    protected abstract void updateParent(TPARENT parent,TDOC child);
}
