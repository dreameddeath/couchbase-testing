package com.dreameddeath.core.dao.document;

import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.dao.InconsistentStateException;
import com.dreameddeath.core.exception.dao.ValidationException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.document.BaseCouchbaseDocument;
import com.dreameddeath.core.storage.CouchbaseBucketWrapper;
import com.dreameddeath.core.storage.CouchbaseConstants;
import com.dreameddeath.core.storage.GenericTranscoder;

import java.util.Collection;

/**
 * Created by ceaj8230 on 12/10/2014.
 */
public abstract class BaseCouchbaseDocumentDao<T extends BaseCouchbaseDocument>{
    private CouchbaseBucketWrapper _client;
    private BaseCouchbaseDocumentDaoFactory _factory;
    public CouchbaseBucketWrapper getClient(){ return _client; }
    public BaseCouchbaseDocumentDaoFactory getDaoFactory(){ return _factory; }

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
        _factory.registerCounter(new CouchbaseCounterDao(counterDaoBuilder));
    }


    public BaseCouchbaseDocumentDao(CouchbaseBucketWrapper client,BaseCouchbaseDocumentDaoFactory factory){
        _client = client;
        _factory = factory;
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


    public T create(T obj,boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
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

    public T update(T obj,boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
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
