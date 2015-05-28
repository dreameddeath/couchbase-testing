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

package com.dreameddeath.core.dao.business;

import com.dreameddeath.core.couchbase.CouchbaseBucketWrapper;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.document.BaseCouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.document.BaseCouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.dao.exception.dao.DaoException;
import com.dreameddeath.core.dao.exception.dao.ValidationException;
import com.dreameddeath.core.dao.validation.Validator;
import com.dreameddeath.core.model.business.CouchbaseDocument;
import com.dreameddeath.core.model.business.CouchbaseDocumentLink;

import java.util.Set;

public abstract class CouchbaseDocumentDao<T extends CouchbaseDocument> extends BaseCouchbaseDocumentWithKeyPatternDao<T> {

    protected  void updateRevision(T obj){
        obj.incDocRevision();
        obj.updateDocLastModDate();
    }

    public CouchbaseDocumentDao(CouchbaseBucketWrapper client,BaseCouchbaseDocumentDaoFactory factory){
        super(client,factory);
    }


    public T validate(T obj) throws ValidationException{
        Validator<T> validator = getDaoFactory().getValidatorFactory().getValidator(obj);
        if(validator!=null){
            validator.validate(obj,null);
        }
        return obj;
    }


    public T create(T obj,boolean isCalcOnly) throws DaoException,StorageException{
        validate(obj);
        return super.create(obj,isCalcOnly);
    }

    /*public void cleanRemovedUniqueKey(T obj) throws DaoException,StorageException{
        for(String key :obj.getRemovedUniqueKeys()){
            obj.getBaseMeta().getSession().removeUniqueKey(key);
        }
    }*/

    public T update(T obj,boolean isCalcOnly) throws DaoException,StorageException{
        validate(obj);
        updateRevision(obj);
        Set<String> keysToRemove=obj.getRemovedUniqueKeys();
        super.update(obj, isCalcOnly);
        for(String key :keysToRemove){
            obj.getBaseMeta().getSession().removeUniqueKey(key);
        }
        //cleanRemovedUniqueKey(obj);
        return obj;
    }

    public T getLinkObj(CouchbaseDocumentLink<T> link) throws StorageException,DaoException{
        if(link.getLinkedObjectFromCache()!=null) { return link.getLinkedObjectFromCache(); }
        T result = get(link.getKey());
        link.setLinkedObject(result);
        return result;
    }

    //Should only be used through DeletionJob
    public T delete(T doc,boolean isCalcOnly) throws DaoException,StorageException{
        Set<String> keysToRemove=doc.getRemovedUniqueKeys();
        super.delete(doc,isCalcOnly);
        for(String key :keysToRemove){
            doc.getBaseMeta().getSession().removeUniqueKey(key);
        }
        return doc;
    }


    public enum AccessRight{
        READ,
        CREATE,
        UPDATE,
        DELETE
    }
}