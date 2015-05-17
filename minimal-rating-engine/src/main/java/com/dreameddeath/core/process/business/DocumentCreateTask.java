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

package com.dreameddeath.core.process.business;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.dao.DaoException;
import com.dreameddeath.core.dao.exception.dao.ValidationException;
import com.dreameddeath.core.exception.process.TaskExecutionException;
import com.dreameddeath.core.model.business.CouchbaseDocument;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.process.common.AbstractTask;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public abstract class DocumentCreateTask<T extends CouchbaseDocument> extends AbstractTask {
    @DocumentProperty("docKey")
    private Property<String> _docKey=new ImmutableProperty<String>(DocumentCreateTask.this);

    public String getDocKey(){return _docKey.get(); }
    public void setDocKey(String docKey){_docKey.set(docKey); }
    public T getDocument() throws DaoException,StorageException{
        return (T)this.getParentJob().getMeta().getSession().get(_docKey.get());
    }


    @Override
    public boolean process() throws TaskExecutionException{
        try {
            //Recovery mode
            if(getDocKey()!=null){
                if(getDocument()!=null){
                    return false;
                }
            }

            T doc = buildDocument();
            //Prebuild key
            setDocKey(doc.getMeta().getSession().buildKey(doc).getMeta().getKey());
            //Attach it to the document
            getParentJob().getMeta().getSession().save(getParentJob());
            //Save Document afterwards
            doc.getMeta().getSession().save(doc);
        }
        catch(ValidationException e){
            throw new TaskExecutionException(this,State.PROCESSED,"Validation error", e);
        }
        catch(DaoException e){
            throw new TaskExecutionException(this,State.PROCESSED,"Dao error", e);
        }
        catch(StorageException e){
            throw new TaskExecutionException(this,State.PROCESSED,"Dao error", e);
        }
        return false; //No need to save (retry allowed)
    }

    protected abstract T buildDocument() throws DaoException,StorageException;

}
