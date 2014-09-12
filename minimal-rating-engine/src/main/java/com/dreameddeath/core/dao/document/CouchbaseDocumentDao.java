package com.dreameddeath.core.dao.document;

import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.validation.Validator;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.dao.ValidationException;
import com.dreameddeath.core.exception.storage.BulkUpdateException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.common.BaseCouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentLink;
import com.dreameddeath.core.storage.CouchbaseClientWrapper;
import com.dreameddeath.core.storage.OperationFutureWrapper;
import net.spy.memcached.CASResponse;
import net.spy.memcached.CASValue;
import net.spy.memcached.transcoders.Transcoder;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public abstract class CouchbaseDocumentDao<T extends CouchbaseDocument>{
    private CouchbaseDocumentDaoFactory _parentFactory;
    private CouchbaseClientWrapper _client;
    public abstract Transcoder<T> getTranscoder();
    public abstract void buildKey(T newObject) throws DaoException,StorageException;
    public abstract String getKeyPattern();

    private CouchbaseClientWrapper getClientWrapper(){
        return _client;
    }


    public CouchbaseDocumentDaoFactory getDaoFactory(){
        return _parentFactory;
    }
    
    //May be overriden to improve (bulk key attribution)
    protected void buildKeys(Collection<T> newObjects) throws DaoException,StorageException{
        for(T newObject:newObjects){
            if(newObject.getDocumentKey()==null){
                buildKey(newObject);
            }
        }
    }

    protected  void updateRevision(T obj){
        obj.incDocRevision();
        obj.updateDocLastModDate();
    }

    protected void updateRevision(Collection<T> objs){
        for(T obj:objs){
            updateRevision(obj);
        }
    }
    
    //Maybe overriden to improve (bulk key attribution)
    public void buildKeysForLinks(Collection<? extends CouchbaseDocumentLink<? extends T>> links) throws DaoException,StorageException{
        for(CouchbaseDocumentLink<? extends T> link:links){
            if(link.getKey()!=null){
                continue;
            }
            buildKey(link.getLinkedObject());
        }
    }
    
    
    public CouchbaseDocumentDao(CouchbaseClientWrapper client,CouchbaseDocumentDaoFactory factory){
        _client = client;
        _parentFactory = factory;
    }

    protected void registerCounterDao(CouchbaseCounterDao.Builder counterDaoBuilder){
        if(counterDaoBuilder.getClient()==null){
            counterDaoBuilder.withClient(getClientWrapper());
        }
        getDaoFactory().registerCounter(new CouchbaseCounterDao(counterDaoBuilder));
    }

    public void validate(T obj) throws ValidationException{
        Validator<T> validator = getDaoFactory().getValidatorFactory().getValidator(obj);
        if(validator!=null){
            validator.validate(obj,null);
        }
    }

    public T create(T obj,boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
        validate(obj);
        if(!obj.getDocState().equals(BaseCouchbaseDocument.DocumentState.NEW)){
            /**TODO throw an error*/
        }
        if(obj.getDocumentKey()==null){
            buildKey(obj);
        }
        
        try {
            if(!isCalcOnly) {
                _client.add(obj, getTranscoder()).get();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted waiting for cas update", e);
        } catch (ExecutionException e) {
            if(e.getCause() instanceof CancellationException) {
                throw (CancellationException) e.getCause();
            } else {
                throw new RuntimeException("Exception waiting for cas update", e);
            }
        }
        obj.setDocStateSync();
        return obj;
    }
    
    public Collection<T> createBulk(Collection<T> objs,boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
        List<OperationFutureWrapper<Boolean,T>> futures = new ArrayList<OperationFutureWrapper<Boolean,T>>(objs.size());
        for(T obj : objs){
            if(!obj.getDocState().equals(CouchbaseDocument.DocumentState.NEW)){
                /**TODO throw an error*/
            }
            validate(obj);
        }
        
        buildKeys(objs);
        updateRevision(objs);
        for (T obj : objs) {
            if(!isCalcOnly) {
                futures.add(_client.add(obj, getTranscoder()));
            }
            else{
                obj.setDocStateSync();
            }
        }
        List<RuntimeException> exceptions = new ArrayList<RuntimeException>();
        for(OperationFutureWrapper<Boolean,T> future:futures){
            try{
                Boolean result = future.get();
                if(!result){
                    future.getDoc().setDocumentKey(null);
                    ///TODO better error management for errors
                }
                else{
                    future.getDoc().setDocStateSync();
                }
            }
            catch (InterruptedException e) {
                exceptions.add(new RuntimeException("Interrupted waiting for value", e));
            }
            catch (ExecutionException e) {
                if(e.getCause() instanceof CancellationException) {
                    exceptions.add((CancellationException) e.getCause());
                } else {
                    exceptions.add(new RuntimeException("Exception waiting for value", e));
                }
            }
        }
        return objs;
    }

    public T get(String key) throws DaoException,StorageException{
        T result=_client.gets(key,getTranscoder());
        result.setDocStateSync();
        return result;
    }
    
    public List<T> getBulk(Set<String> keys) throws StorageException,DaoException{
        List<OperationFutureWrapper<CASValue<T>,T>> futures = new ArrayList<OperationFutureWrapper<CASValue<T>,T>>(keys.size());
        List<T> results = new ArrayList<T>(keys.size());
        for(String key : keys){
            futures.add(_client.asyncGets(key,getTranscoder()));
        }
        List<RuntimeException> exceptions = new ArrayList<RuntimeException>();
        for(OperationFutureWrapper<CASValue<T>,T> future : futures){
            try{
                T result = future.get().getValue();
                result.setDocStateSync();
                results.add(result);
            }
            catch (InterruptedException e) {
                exceptions.add(new RuntimeException("Interrupted waiting for value", e));
            }
            catch (ExecutionException e) {
                if(e.getCause() instanceof CancellationException) {
                    exceptions.add((CancellationException) e.getCause());
                } else {
                    exceptions.add(new RuntimeException("Exception waiting for value", e));
                }
            }
        }
        return results;
    }

    public void cleanRemovedUniqueKey(T obj) throws DaoException,StorageException{
        for(String key :obj.getRemovedUniqueKeys()){
            obj.getSession().removeUniqueKey(key);
        }
    }

    public T update(T obj,boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
        validate(obj);
        if(obj.getDocumentKey()==null){/**TODO throw an error*/}
        updateRevision(obj);
        if(!isCalcOnly) {
            CASResponse response = _client.cas(obj, getTranscoder());
            if(!response.equals(CASResponse.OK)){
                throw new StorageException("The update of the object <"+obj.getDocumentKey()+"> failed with erreor <"+response+">");
            }
        }
        cleanRemovedUniqueKey(obj);
        obj.setDocStateSync();

        return obj;
    }
    
    public Collection<T> updateBulk(Collection<T> objs,boolean isCalcOnly) throws DaoException,StorageException{
        List<OperationFutureWrapper<CASResponse,T>> futures = new ArrayList<OperationFutureWrapper<CASResponse,T>>(objs.size());

        for(T obj:objs){
            validate(obj);
        }
        buildKeys(objs);
        updateRevision(objs);
        for(T obj:objs){
            if(!isCalcOnly) {
                futures.add(_client.asyncCas(obj, getTranscoder()));
            }
            else{
                obj.setDocStateSync();
            }
        }
        
        List<BulkUpdateException> exceptions = new ArrayList<BulkUpdateException>();
        for(OperationFutureWrapper<CASResponse,T> future:futures){
            try{
                CASResponse result = future.get();
                if(result.equals(CASResponse.OK)){
                    cleanRemovedUniqueKey(future.getDoc());
                    future.getDoc().setDocStateSync();
                }
                else{
                   exceptions.add(new BulkUpdateException("The update of the object <"+future.getDoc().getDocumentKey()+"> failed with erreor <"+result+">"));
                }
            }
            catch (InterruptedException e) {
                exceptions.add(new BulkUpdateException("Interrupted waiting for value", e));
            }
            catch (ExecutionException e) {
                if(e.getCause() instanceof CancellationException) {
                    exceptions.add(new BulkUpdateException("The update was called with erreor",e.getCause()));
                } else {
                    exceptions.add(new BulkUpdateException("Exception waiting for value", e));
                }
            }
        }
        if(exceptions.size()>0){
            throw new BulkUpdateException("Error during bulk update",exceptions);
        }
        return objs;
    }
    
    public Collection<T> getLinkObjBulk(Collection<CouchbaseDocumentLink<T>> links) throws DaoException,StorageException{
        Collection<T> results = new ArrayList<T>(links.size());
        
        Map<String,List<CouchbaseDocumentLink<T>>> linkedDocs = new HashMap<String,List<CouchbaseDocumentLink<T>>>(links.size());
        //Retrive Unique Key
        for(CouchbaseDocumentLink<T> link : links){
            if(link.getLinkedObjectFromCache()!=null){ results.add(link.getLinkedObjectFromCache()); continue; }
            if(linkedDocs.containsKey(link.getKey())){
                linkedDocs.get(link.getKey()).add(link);
            }
            else{
                List<CouchbaseDocumentLink<T>> linksPerKeyList = new ArrayList<CouchbaseDocumentLink<T>>();
                linksPerKeyList.add(link);
                linkedDocs.put(link.getKey(),linksPerKeyList);
            }
        }
        Collection<T> objs = getBulk(linkedDocs.keySet());
        results.addAll(objs);
        for(T obj:objs){
            for(CouchbaseDocumentLink<T> link: linkedDocs.get(obj.getDocumentKey())){
                link.setLinkedObject(obj);
            }
        }
        return results;
    }
    
    public T getLinkObj(CouchbaseDocumentLink<T> link) throws StorageException,DaoException{
        if(link.getLinkedObjectFromCache()!=null) { return link.getLinkedObjectFromCache(); }
        T result = get(link.getKey());
        link.setLinkedObject(result);
        
        return result;
    }

    public enum AccessRight{
        READ,
        CREATE,
        UPDATE,
        DELETE
    }
}