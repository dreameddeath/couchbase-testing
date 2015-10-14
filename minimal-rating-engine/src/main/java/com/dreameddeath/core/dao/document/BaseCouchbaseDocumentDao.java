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

package com.dreameddeath.core.dao.document;

import com.dreameddeath.core.couchbase.CouchbaseBucketWrapper;
import com.dreameddeath.core.couchbase.CouchbaseConstants;
import com.dreameddeath.core.couchbase.GenericTranscoder;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.exception.dao.DaoException;
import com.dreameddeath.core.dao.exception.dao.InconsistentStateException;
import com.dreameddeath.core.model.document.BaseCouchbaseDocument;

import java.util.Collection;

/**
 * Created by Christophe Jeunesse on 12/10/2014.
 */
public abstract class BaseCouchbaseDocumentDao<T extends BaseCouchbaseDocument>{
    private CouchbaseBucketWrapper client;
    private BaseCouchbaseDocumentDaoFactory factory;
    public CouchbaseBucketWrapper getClient(){ return client; }
    public BaseCouchbaseDocumentDaoFactory getDaoFactory(){ return factory; }

    public abstract GenericTranscoder<T> getTranscoder();
    //public abstract BucketDocument<T> buildBucketDocument(T doc);
    public abstract void buildKey(T newObject) throws DaoException,StorageException;

    //May be overriden to improve (bulk key attribution)
    protected void buildKeys(Collection<T> newObjects) throws DaoException,StorageException{
        for(T newObject:newObjects){
            if(newObject.getBaseMeta().getKey()==null){
                buildKey(newObject);
            }
        }
    }

    protected void registerCounterDao(CouchbaseCounterDao.Builder counterDaoBuilder){
        if(counterDaoBuilder.getClient()==null){
            counterDaoBuilder.withClient(getClient());
        }
        factory.registerCounter(new CouchbaseCounterDao(counterDaoBuilder));
    }


    public BaseCouchbaseDocumentDao(CouchbaseBucketWrapper client,BaseCouchbaseDocumentDaoFactory factory){
        this.client = client;
        this.factory = factory;
        client.addTranscoder(getTranscoder());
    }

    public void checkUpdatableState(T obj) throws InconsistentStateException {
        if(obj.getBaseMeta().getState().equals(BaseCouchbaseDocument.DocumentState.NEW)){
            throw new InconsistentStateException(obj,"The document is new and then cannot be updated");
        }
        if(obj.getBaseMeta().getKey()==null){
            throw new InconsistentStateException(obj,"The document is doesn't have a key and then cannot be deleted");
        }
        if(obj.getBaseMeta().hasFlag(CouchbaseConstants.DocumentFlag.Deleted) ||
                obj.getBaseMeta().getState().equals(BaseCouchbaseDocument.DocumentState.DELETED)){
            throw new InconsistentStateException(obj,"The document is in deletion and then cannot be modified");
        }
    }

    public T checkCreatableState(T obj) throws InconsistentStateException{
        if(!obj.getBaseMeta().getState().equals(BaseCouchbaseDocument.DocumentState.NEW)){
            throw new InconsistentStateException(obj,"The document is not new and then cannot be create");
        }
        return obj;
    }

    public T checkDeletableState(T obj) throws InconsistentStateException{
        if(obj.getBaseMeta().getState().equals(BaseCouchbaseDocument.DocumentState.NEW)){
            throw new InconsistentStateException(obj,"The document is new and then cannot be deleted");
        }
        if(obj.getBaseMeta().getKey()==null){
            throw new InconsistentStateException(obj,"The document is doesn't have a key and then cannot be deleted");
        }
        if(obj.getBaseMeta().hasFlag(CouchbaseConstants.DocumentFlag.Deleted) ||
                obj.getBaseMeta().getState().equals(BaseCouchbaseDocument.DocumentState.DELETED)){
            throw new InconsistentStateException(obj,"The document is already in deletion and then cannot be deleted");
        }
        return obj;
    }


    public T create(T obj,boolean isCalcOnly) throws DaoException,StorageException{
        checkCreatableState(obj);

        if(obj.getBaseMeta().getKey()==null){buildKey(obj); }

        if(!isCalcOnly) {
            getClient().add(getTranscoder().newDocument(obj));
        }

        obj.getBaseMeta().setStateSync();
        return obj;
    }


    public T get(String key) throws DaoException,StorageException{
        T result=getClient().get(key, getTranscoder().documentType());
        if(result.getBaseMeta().hasFlag(CouchbaseConstants.DocumentFlag.Deleted)){
            result.getBaseMeta().setStateDeleted();
        }
        else {
            result.getBaseMeta().setStateSync();
        }
        return result;
    }

    public T update(T obj,boolean isCalcOnly) throws DaoException,StorageException{
        checkUpdatableState(obj);
        if(!isCalcOnly) {
            getClient().replace(getTranscoder().newDocument(obj));
        }
        obj.getBaseMeta().setStateSync();
        return obj;
    }

    //Should only be used through DeletionJob
    public T delete(T doc,boolean isCalcOnly) throws DaoException,StorageException{
        checkDeletableState(doc);
        doc.getBaseMeta().addFlag(CouchbaseConstants.DocumentFlag.Deleted);
        if(!isCalcOnly) {
            getClient().delete(getTranscoder().newDocument(doc));
        }
        doc.getBaseMeta().setStateDeleted();
        return doc;
    }

}
