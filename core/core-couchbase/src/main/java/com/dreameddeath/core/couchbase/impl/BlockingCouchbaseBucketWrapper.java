package com.dreameddeath.core.couchbase.impl;

import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.dreameddeath.core.couchbase.IBlockingCouchbaseBucket;
import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.exception.DocumentNotFoundException;
import com.dreameddeath.core.couchbase.exception.DocumentStorageException;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.couchbase.exception.StorageObservableException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 17/06/2016.
 */
public class BlockingCouchbaseBucketWrapper implements IBlockingCouchbaseBucket {
    private final ICouchbaseBucket asyncWrapper;

    public BlockingCouchbaseBucketWrapper(ICouchbaseBucket asyncWrapper){
        this.asyncWrapper=asyncWrapper;
    }

    @Override
    public <T extends CouchbaseDocument> T get(String key, Class<T> entity) throws StorageException {
        try{
            return asyncWrapper.asyncGet(key,entity).toBlocking().first();
        }
        catch(StorageObservableException e){
            throw e.getCause();
        }
    }

    @Override
    public <T extends CouchbaseDocument> T get(String key, Class<T> entity,ReadParams params) throws StorageException {
        try{
            return asyncWrapper.asyncGet(key,entity,params).toBlocking().single();
        }
        catch(StorageObservableException e){
            throw e.getCause();
        }
    }

    @Override
    public <T extends CouchbaseDocument> T add(final T doc) throws StorageException{
        return syncDocumentObserverManage(doc, asyncWrapper.asyncAdd(doc));
    }

    @Override
    public <T extends CouchbaseDocument> T add(T doc , WriteParams params) throws StorageException {
        return syncDocumentObserverManage(doc, asyncWrapper.asyncAdd(doc, params));
    }


    @Override
    public <T extends CouchbaseDocument> T set(final T doc) throws StorageException{
        return syncDocumentObserverManage(doc, asyncWrapper.asyncSet(doc));
    }

    @Override
    public <T extends CouchbaseDocument> T set(T doc, WriteParams params) throws StorageException {
        return syncDocumentObserverManage(doc, asyncWrapper.asyncSet(doc, params));
    }


    @Override
    public <T extends CouchbaseDocument> T replace(final T doc) throws StorageException{
        return syncDocumentObserverManage(doc,asyncWrapper.asyncReplace(doc));
    }

    @Override
    public <T extends CouchbaseDocument> T replace(T doc, WriteParams params) throws StorageException {
        return syncDocumentObserverManage(doc, asyncWrapper.asyncReplace(doc, params));
    }

    @Override
    public <T extends CouchbaseDocument> T delete(final T doc) throws StorageException{
        try {
            T result = syncDocumentObserverManage(doc,asyncWrapper.asyncDelete(doc));
            if(result==null){ throw new DocumentNotFoundException(doc,"Cannot apply delete method");}
            else{ return result; }
        }
        catch(DocumentNotFoundException e){ throw e; }
        catch(Throwable e){  throw new DocumentStorageException(doc,"Error during delete execution",e); }
    }

    @Override
    public <T extends CouchbaseDocument> T delete(T doc, WriteParams params) throws StorageException {
        try {
            T result = syncDocumentObserverManage(doc,asyncWrapper.asyncDelete(doc,params));
            if(result==null){ throw new DocumentNotFoundException(doc,"Cannot apply delete method");}
            else{ return result; }
        }
        catch(StorageException e){
            throw e;
        }
        catch(Throwable e){  throw new DocumentStorageException(doc,"Error during delete execution",e); }
    }

    @Override
    public <T extends CouchbaseDocument> T append(final T doc) throws StorageException{
        return syncDocumentObserverManage(doc,asyncWrapper.asyncAppend(doc));
    }

    @Override
    public <T extends CouchbaseDocument> T append(T doc, WriteParams params) throws StorageException {
        return syncDocumentObserverManage(doc, asyncWrapper.asyncAppend(doc, params));
    }

    @Override
    public <T extends CouchbaseDocument> T prepend(final T doc) throws StorageException{
        return  syncDocumentObserverManage(doc,asyncWrapper.asyncPrepend(doc));
    }

    @Override
    public <T extends CouchbaseDocument> T prepend(T doc, WriteParams params) throws StorageException {
        return  syncDocumentObserverManage(doc, asyncWrapper.asyncPrepend(doc, params));
    }

    @Override
    public Long counter(String key, Long by, Long defaultValue, Integer expiry) throws StorageException{
        return syncCounterObserverManage(key,asyncWrapper.asyncCounter(key, by, defaultValue, expiry));
    }

    @Override
    public Long counter(String key, Long by, Long defaultValue, Integer expiration, WriteParams params) throws StorageException {
        return syncCounterObserverManage(key, asyncWrapper.asyncCounter(key, by, defaultValue, 0, params));
    }

    @Override
    public Long counter(String key, Long by, Long defaultValue) throws StorageException{
        return counter(key,by,defaultValue,0);
    }
    @Override
    public Long counter(String key, Long by, Long defaultValue,WriteParams params) throws StorageException{
        return counter(key, by, defaultValue, 0, params);
    }

    @Override
    public Long counter(String key, Long by) throws StorageException{
        return counter(key, by, by);
    }

    @Override
    public Long counter(String key, Long by,WriteParams params) throws StorageException{
        return counter(key, by, by, params);
    }

    @Override
    public ViewResult query(ViewQuery query){
        //TODO improve a lot
        return ((CouchbaseBucketWrapper)asyncWrapper).getBucket().query(query);
    }


    protected <T extends CouchbaseDocument> T syncDocumentObserverManage(final T doc, Observable<T> obj) throws StorageException{
        try{
            return obj.toBlocking().single();
        }
        catch(StorageObservableException e) {
            throw e.getCause();
        }
    }

    protected Long syncCounterObserverManage(final String key, Observable<Long> obj) throws StorageException{
        try{
            return obj.toBlocking().single();
        }
        catch(StorageObservableException e) {
            throw e.getCause();
        }
    }

}
