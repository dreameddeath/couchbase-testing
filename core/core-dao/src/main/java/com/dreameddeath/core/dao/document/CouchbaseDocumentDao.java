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
import com.dreameddeath.core.couchbase.ICouchbaseTranscoder;
import com.dreameddeath.core.couchbase.exception.StorageException;
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

/**
 * Created by Christophe Jeunesse on 12/10/2014.
 */
@DaoForClass(CouchbaseDocument.class)
public abstract class CouchbaseDocumentDao<T extends CouchbaseDocument>{
    private ICouchbaseBucket _client;
    private ICouchbaseTranscoder<T> _transcoder;
    private List<CouchbaseViewDao> _daoViews=null;
    public abstract Class<? extends BucketDocument<T>> getBucketDocumentClass();
    public abstract T buildKey(ICouchbaseSession session,T newObject) throws DaoException,StorageException;

    public List<CouchbaseCounterDao.Builder> getCountersBuilder(){return Collections.emptyList();}
    public List<CouchbaseUniqueKeyDao.Builder> getUniqueKeysBuilder(){return Collections.emptyList();}
    protected List<CouchbaseViewDao> generateViewDaos(){ return Collections.emptyList();}

    synchronized public List<CouchbaseViewDao> getViewDaos(){
        if(_daoViews==null){
            _daoViews = generateViewDaos();
        }
        return Collections.unmodifiableList(_daoViews);
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
        _client = client;
        if(getTranscoder()!=null){
            _client.addTranscoder(getTranscoder());
        }
        return this;
    }
    public ICouchbaseBucket getClient(){ return _client; }

    public ICouchbaseTranscoder<T> getTranscoder(){return _transcoder;}

    public CouchbaseDocumentDao<T> setTranscoder(ICouchbaseTranscoder<T> transcoder){
        _transcoder=transcoder;
        if(getClient()!=null){
            getClient().addTranscoder(_transcoder);
        }
        return this;
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
    }

    public T checkCreatableState(T obj) throws InconsistentStateException{
        if(!obj.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.NEW)){
            throw new InconsistentStateException(obj,"The document is not new and then cannot be create");
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
        return obj;
    }


    public T create(ICouchbaseSession session,T obj,boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
        checkCreatableState(obj);
        //Precreate the key perform the validation with all target data
        if(obj.getBaseMeta().getKey()==null){buildKey(session,obj); }
        session.validate(obj);

        if(!isCalcOnly) {
            getClient().add(obj,getTranscoder());
            //getClient().add(getTranscoder().newDocument(obj));
        }

        obj.getBaseMeta().setStateSync();
        return obj;
    }


    public T get(String key) throws DaoException,StorageException{
        T result=getClient().get(key, getTranscoder());
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
            getClient().replace(obj,getTranscoder());
        }
        obj.getBaseMeta().setStateSync();
        return obj;
    }

    //Should only be used through DeletionJob
    public T delete(ICouchbaseSession session,T doc,boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
        checkDeletableState(doc);
        doc.getBaseMeta().addFlag(DocumentFlag.Deleted);
        if(!isCalcOnly) {
            getClient().delete(doc,getTranscoder());
        }
        doc.getBaseMeta().setStateDeleted();
        return doc;
    }

}
