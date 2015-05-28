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

package com.dreameddeath.core.process.business;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.annotation.NotNull;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.dao.DaoException;
import com.dreameddeath.core.dao.exception.dao.ValidationException;
import com.dreameddeath.core.exception.process.TaskExecutionException;
import com.dreameddeath.core.model.business.CouchbaseDocument;
import com.dreameddeath.core.model.process.CouchbaseDocumentAttachedTaskRef;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.process.common.AbstractTask;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public abstract class DocumentUpdateTask<T extends CouchbaseDocument> extends AbstractTask {
    @DocumentProperty("docKey") @NotNull
    private Property<String> _docKey=new ImmutableProperty<String>(DocumentUpdateTask.this);

    public String getDocKey(){return _docKey.get(); }
    public DocumentUpdateTask<T> setDocKey(String docKey){_docKey.set(docKey); return this;}
    public T getDocument() throws DaoException,StorageException{
        return (T)this.getParentJob().getMeta().getSession().get(_docKey.get());
    }

    @Override
    public final boolean process() throws TaskExecutionException{
        try {
            CouchbaseDocumentAttachedTaskRef reference = getDocument().getAttachedTaskRef(this);
            if (reference == null) {
                processDocument();
                CouchbaseDocumentAttachedTaskRef attachedTaskRef = new CouchbaseDocumentAttachedTaskRef();
                attachedTaskRef.setJobKey(getParentJob().getMeta().getKey());
                attachedTaskRef.setJobClass(getParentJob().getClass().getName());
                attachedTaskRef.setTaskId(this.getUid());
                attachedTaskRef.setTaskClass(this.getClass().getName());
                getDocument().addAttachedTaskRef(attachedTaskRef);
                try {
                    getDocument().getMeta().getSession().save(getDocument());
                } catch (ValidationException e) {
                    throw new TaskExecutionException(this, this.getState(), "Updated Document Validation exception", e);
                }
                return true;
            }
        }
        catch(DaoException e){
            throw new TaskExecutionException(this, this.getState(), "Dao exception", e);
        }
        catch(StorageException e){
            throw new TaskExecutionException(this, this.getState(), "Storage exception", e);
        }
        return false;
    }

    @Override
    public final boolean cleanup() throws TaskExecutionException{
        try {
            getDocument().cleanupAttachedTaskRef(this);
            getDocument().getMeta().getSession().save(getDocument());
        }
        catch(ValidationException e){
            throw new TaskExecutionException(this,this.getState(),"Cleaned updated document Validation exception",e);
        }
        catch(DaoException e){
            throw new TaskExecutionException(this,this.getState(),"Error in dao",e);
        }
        catch(StorageException e){
            throw new TaskExecutionException(this,this.getState(),"Error in storage",e);
        }

        return false;
    }

    protected abstract void processDocument() throws DaoException,StorageException;
}
