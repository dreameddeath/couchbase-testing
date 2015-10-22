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

package com.dreameddeath.core.business.dao;

import com.dreameddeath.core.business.model.BusinessDocument;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;

import java.util.Set;

public abstract class BusinessCouchbaseDocumentDao<T extends BusinessDocument> extends CouchbaseDocumentWithKeyPatternDao<T> {

    protected  void updateRevision(ICouchbaseSession session,T obj){
        obj.incDocRevision(session);
        obj.updateDocLastModDate(session);
    }

    @Override
    public T create(ICouchbaseSession session,T obj,boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
        return super.create(session,obj,isCalcOnly);
    }

    @Override
    public T update(ICouchbaseSession session,T obj,boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
        updateRevision(session,obj);
        Set<String> keysToRemove=obj.getRemovedUniqueKeys();
        super.update(session,obj, isCalcOnly);
        for(String key :keysToRemove){
            session.removeUniqueKey(key);
        }
        return obj;
    }

    //Should only be used through DeletionJob
    @Override
    public T delete(ICouchbaseSession session,T doc,boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
        Set<String> keysToRemove=doc.getRemovedUniqueKeys();
        super.delete(session,doc,isCalcOnly);
        for(String key :keysToRemove){
            session.removeUniqueKey(key);
        }
        return doc;
    }
}