/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.process.model.v1.tasks;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public abstract class DocumentUpdateTask<T extends CouchbaseDocument> extends AbstractTask {
    @DocumentProperty("docKey")
    private Property<String> docKey=new ImmutableProperty<>(DocumentUpdateTask.this);
    /**
     *  updatedWithDoc : tell if it has been updated during doc update
     */
    @DocumentProperty("updatedWithDoc")
    private Property<Boolean> updatedWithDoc = new StandardProperty<>(DocumentUpdateTask.this,Boolean.FALSE);


    public String getDocKey(){return docKey.get(); }
    public DocumentUpdateTask<T> setDocKey(String docKey){this.docKey.set(docKey); return this;}

    public T blockingGetDocument(ICouchbaseSession session)throws DaoException, StorageException {return session.toBlocking().blockingGet(getDocKey());}
    public Observable<T> getDocument(ICouchbaseSession session)throws DaoException, StorageException {return session.asyncGet(getDocKey());}



    /**
     * Getter of updatedWithDoc
     * @return the value of updatedWithDoc
     */
    public Boolean getUpdatedWithDoc() { return updatedWithDoc.get(); }
    /**
     * Setter of updatedWithDoc
     * @param val the new value of updatedWithDoc
     */
    public void setUpdatedWithDoc(Boolean val) { updatedWithDoc.set(val); }

}
