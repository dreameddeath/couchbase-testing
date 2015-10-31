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

import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.couchbase.impl.ReadParams;
import com.dreameddeath.core.couchbase.impl.WriteParams;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.InconsistentStateException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDao;
import com.dreameddeath.core.dao.view.CouchbaseViewDao;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocument.DocumentFlag;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 12/10/2014.
 */
@DaoForClass(CouchbaseDocument.class)
public abstract class CouchbaseDocumentDao<T extends CouchbaseDocument>{
    private final UUID uuid = UUID.randomUUID();
    private ICouchbaseBucket client;
    private List<CouchbaseViewDao> daoViews=null;
    private boolean isReadOnly=false;


    public abstract Class<? extends BucketDocument<T>> getBucketDocumentClass();
    public abstract T buildKey(ICouchbaseSession session,T newObject) throws DaoException,StorageException;
    public abstract Class<T> getBaseClass();

    public List<CouchbaseCounterDao.Builder> getCountersBuilder(){return Collections.emptyList();}
    public List<CouchbaseUniqueKeyDao.Builder> getUniqueKeysBuilder(){return Collections.emptyList();}
    protected List<CouchbaseViewDao> generateViewDaos(){ return Collections.emptyList();}


    synchronized public List<CouchbaseViewDao> getViewDaos(){
        if(daoViews==null){
            daoViews = generateViewDaos();
        }
        return Collections.unmodifiableList(daoViews);
    }

    public CouchbaseViewDao getViewDao(String name){
        for(CouchbaseViewDao viewDao:getViewDaos()){
            if(viewDao.getViewName().equals(name)){
                return viewDao;
            }
        }
        return null;
    }

    public CouchbaseDocumentDao<T> setClient(ICouchbaseBucket client){
        this.client = client;
        return this;
    }

    public ICouchbaseBucket getClient(){ return client; }

    public UUID getUuid() {
        return uuid;
    }

    //May be overriden to improve (bulk key attribution)
    protected void buildKeys(ICouchbaseSession session,Collection<T> newObjects) throws DaoException,StorageException{
        for(T newObject:newObjects){
            if(newObject.getBaseMeta().getKey()==null){
                buildKey(session,newObject);
            }
        }
    }

    public void checkUpdatableState(T obj) throws InconsistentStateException {
        if(obj.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.NEW)){
            throw new InconsistentStateException(obj,"The document is new and then cannot be updated");
        }
        if(obj.getBaseMeta().getKey()==null){
            throw new InconsistentStateException(obj,"The document is doesn't have a key and then cannot be deleted");
        }
        if(obj.getBaseMeta().hasFlag(DocumentFlag.Deleted) ||
                obj.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.DELETED)){
            throw new InconsistentStateException(obj,"The document is in deletion and then cannot be modified");
        }
        if(isReadOnly){
            throw new InconsistentStateException(obj,"Cannot update document in readonly mode");
        }
    }

    public T checkCreatableState(T obj) throws InconsistentStateException{
        if(!obj.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.NEW)){
            throw new InconsistentStateException(obj,"The document is not new and then cannot be create");
        }
        if(isReadOnly){
            throw new InconsistentStateException(obj,"Cannot update document in readonly mode");
        }
        return obj;
    }

    public T checkDeletableState(T obj) throws InconsistentStateException{
        if(obj.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.NEW)){
            throw new InconsistentStateException(obj,"The document is new and then cannot be deleted");
        }
        if(obj.getBaseMeta().getKey()==null){
            throw new InconsistentStateException(obj,"The document is doesn't have a key and then cannot be deleted");
        }
        if(obj.getBaseMeta().hasFlag(DocumentFlag.Deleted) ||
                obj.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.DELETED)){
            throw new InconsistentStateException(obj,"The document is already in deletion and then cannot be deleted");
        }
        if(isReadOnly){
            throw new InconsistentStateException(obj,"Cannot update document in readonly mode");
        }
        return obj;
    }


    public T create(ICouchbaseSession session,T obj,boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
        checkCreatableState(obj);
        //Precreate the key perform the validation with all target data
        if(obj.getBaseMeta().getKey()==null){
            buildKey(session,obj);
        }
        session.validate(obj);

        if(!isCalcOnly) {
            String keyPrefix = session.getKeyPrefix();
            if(keyPrefix!=null){
                getClient().add(obj, WriteParams.create().with(keyPrefix));
            }
            else{
                getClient().add(obj);
            }
        }

        obj.getBaseMeta().setStateSync();
        return obj;
    }


    public T get(ICouchbaseSession session,String key) throws DaoException,StorageException{
        T result;
        if(session.getKeyPrefix()!=null) {
            result = getClient().get(key, getBaseClass(), ReadParams.create().with(session.getKeyPrefix()));
        }
        else{
            result = getClient().get(key, getBaseClass());
        }
        if(result.getBaseMeta().hasFlag(DocumentFlag.Deleted)){
            result.getBaseMeta().setStateDeleted();
        }
        else {
            result.getBaseMeta().setStateSync();
        }
        return result;
    }

    public T update(ICouchbaseSession session,T obj,boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
        checkUpdatableState(obj);
        session.validate(obj);
        if(!isCalcOnly) {
            getClient().replace(obj);
        }
        obj.getBaseMeta().setStateSync();
        return obj;
    }

    //Should only be used through DeletionJob
    public T delete(ICouchbaseSession session,T doc,boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
        checkDeletableState(doc);
        doc.getBaseMeta().addFlag(DocumentFlag.Deleted);
        if(!isCalcOnly) {
            getClient().delete(doc);
        }
        doc.getBaseMeta().setStateDeleted();
        return doc;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void isReadOnly(boolean isReadOnly) {
        this.isReadOnly=isReadOnly;
    }


}
