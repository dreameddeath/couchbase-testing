package com.dreameddeath.core.dao.document;

import com.dreameddeath.core.annotation.dao.DaoForClass;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDao;
import com.dreameddeath.core.dao.view.CouchbaseViewDao;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.dao.InconsistentStateException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.exception.validation.ValidationException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocument.DocumentFlag;
import com.dreameddeath.core.session.ICouchbaseSession;
import com.dreameddeath.core.storage.BucketDocument;
import com.dreameddeath.core.storage.ICouchbaseBucket;
import com.dreameddeath.core.storage.ICouchbaseTranscoder;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by ceaj8230 on 12/10/2014.
 */
@DaoForClass(CouchbaseDocument.class)
public abstract class CouchbaseDocumentDao<T extends CouchbaseDocument>{
    private ICouchbaseBucket _client;
    private ICouchbaseTranscoder<T> _transcoder;

    public abstract Class<? extends BucketDocument<T>> getBucketDocumentClass();
    public abstract T buildKey(ICouchbaseSession session,T newObject) throws DaoException,StorageException;

    public List<CouchbaseCounterDao.Builder> getCountersBuilder(){return Collections.emptyList();}
    public List<CouchbaseUniqueKeyDao.Builder> getUniqueKeysBuilder(){return Collections.emptyList();}
    public List<CouchbaseViewDao> getViews(){ return Collections.emptyList();}

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
        session.validate(obj);
        if(obj.getBaseMeta().getKey()==null){buildKey(session,obj); }

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
