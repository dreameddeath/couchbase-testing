package com.dreameddeath.core.storage;

import com.couchbase.client.CouchbaseClient;
import com.dreameddeath.core.exception.storage.*;
import com.dreameddeath.core.model.common.BaseCouchbaseDocument;

import net.spy.memcached.CASResponse;
import net.spy.memcached.CASValue;
import net.spy.memcached.transcoders.Transcoder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
*  Class used to perform storage 
*/
public class CouchbaseClientWrapper{
    private CouchbaseClient _couchbaseClient;
    
    public CouchbaseClientWrapper(CouchbaseClient couchbaseClient){
        _couchbaseClient = couchbaseClient;
    }
    
    public CouchbaseClient getClient(){
        return _couchbaseClient;
    }
    
    
    public <T extends BaseCouchbaseDocument> T get(String key, Transcoder<T> tc) throws StorageException{
        try {
            CASValue<T> result = asyncGet(key, tc).get();
            if(result==null){
                throw new DocumentNotFoundException(key,"Cannot find document using key");
            }
            else {
                return result.getValue();
            }
        } catch (InterruptedException e) {
            throw new DocumentAccessException(key,"Interrupted waiting for value", e);
        } catch (ExecutionException e) {
            throw new DocumentAccessException(key,"Error during fetch execution",e);
        }
    }
    
    
    public <T extends BaseCouchbaseDocument> OperationFutureWrapper<CASValue<T>,T> asyncGet(String key, Transcoder<T> tc) throws StorageException{
        return new OperationFutureWrapper<CASValue<T>,T>(_couchbaseClient.asyncGets(key,tc));
    }
    
    public <T extends BaseCouchbaseDocument> T add(T doc,Transcoder<T> tc,int exp) throws StorageException{
        try{
            Boolean result = asyncAdd(doc,tc,exp).get();
            if(result==false){
                throw new DuplicateDocumentKeyException(doc);
            }
        }
        catch (InterruptedException e) {
            throw new DocumentStorageException(doc,"Interrupted waiting for value", e);
        } catch (ExecutionException e) {
            throw new DocumentStorageException(doc,"Error during fetch execution",e);
        }

        return  doc;
    }
    public <T extends BaseCouchbaseDocument> T add(T doc,Transcoder<T> tc) throws StorageException{ return add(doc,tc,0); }
    public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> asyncAdd(T doc,Transcoder<T> tc,int exp) throws StorageException{ return new OperationFutureWrapper(_couchbaseClient.add(doc.getDocumentKey(),exp,doc,tc),doc); }
    public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> asyncAdd(T doc,Transcoder<T> tc) throws StorageException{ return asyncAdd(doc, tc, 0); }


    public <T extends BaseCouchbaseDocument> T set(T doc,Transcoder<T> tc,int exp)throws StorageException{
        try{
            Boolean result = asyncSet(doc,tc,exp).get();
            if(result==false){
                throw new DocumentStorageException(doc,"Cannot apply set method to the document");
            }
        }
        catch (InterruptedException e) {
            throw new DocumentStorageException(doc,"Interrupted waiting for value", e);
        }
        catch (ExecutionException e) {
            throw new DocumentStorageException(doc,"Error during fetch execution",e);
        }
        return  doc;
    }
    public <T extends BaseCouchbaseDocument> T set(T doc,Transcoder<T> tc)throws StorageException{ return set(doc,tc,0); }
    public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> asyncSet(T doc,Transcoder<T> tc,int exp)throws StorageException{ return new OperationFutureWrapper(_couchbaseClient.set(doc.getDocumentKey(),exp,doc,tc),doc); }
    public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> asyncSet(T doc,Transcoder<T> tc)throws StorageException{ return asyncSet(doc, tc, 0); }

    public <T extends BaseCouchbaseDocument> T replace(T doc,Transcoder<T> tc,int exp)throws StorageException{
        try{
        Boolean result = asyncReplace(doc,tc,exp).get();
            if(result==false){
                throw new DocumentNotFoundException(doc,"Cannot apply replace method");
            }
        }
        catch (InterruptedException e) {
            throw new DocumentStorageException(doc,"Interrupted waiting for value", e);
        }
        catch (ExecutionException e) {
            throw new DocumentStorageException(doc,"Error during fetch execution",e);
        }
        return  doc;
    }
    public <T extends BaseCouchbaseDocument> T replace(T doc,Transcoder<T> tc)throws StorageException{ return replace(doc,tc,0); }
    public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> asyncReplace(T doc,Transcoder<T> tc,int exp)throws StorageException{ return new OperationFutureWrapper(_couchbaseClient.replace(doc.getDocumentKey(),exp,doc,tc),doc); }
    public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> asyncReplace(T doc,Transcoder<T> tc)throws StorageException{ return asyncReplace(doc,tc,0); }


    public <T extends BaseCouchbaseDocument> OperationFutureWrapper<CASResponse,T> asyncCas(T doc,Transcoder<T> tc,int exp) throws StorageException{ return new OperationFutureWrapper(_couchbaseClient.asyncCAS(doc.getDocumentKey(),doc.getDocumentCas(),exp,doc,tc),doc); }
    public <T extends BaseCouchbaseDocument> OperationFutureWrapper<CASResponse,T> asyncCas(T doc,Transcoder<T> tc) throws StorageException{ return asyncCas(doc,tc,0); }
    public <T extends BaseCouchbaseDocument> CASResponse cas(T doc,Transcoder<T> tc,int exp)throws StorageException{
        try {
            CASResponse result = asyncCas(doc,tc,exp).get();
            if(!result.equals(CASResponse.OK)){
                throw new CasUpdateException(doc,result,"Cas Update Failed");
            }
            return result;

        }
        catch (InterruptedException e) {
            throw new DocumentStorageException(doc,"Interrupted waiting for value", e);
        }
        catch (ExecutionException e) {
            throw new DocumentStorageException(doc,"Error during fetch execution",e);
        }
    }

    public <T extends BaseCouchbaseDocument> CASResponse cas(T doc,Transcoder<T> tc)throws StorageException{ return cas(doc,tc,0); }

    //Todo Check Cas value
    public <T extends BaseCouchbaseDocument> Future<Boolean> asyncAppendCas(T doc,Transcoder<T> tc)throws StorageException{ return _couchbaseClient.append(doc.getDocumentCas(),doc.getDocumentKey(),doc,tc); }
    public <T extends BaseCouchbaseDocument> Future<Boolean> asyncAppend(T doc,Transcoder<T> tc)throws StorageException{ return _couchbaseClient.append(doc.getDocumentKey(),doc,tc); }
    
    //Todo Check Cas value
    public <T extends BaseCouchbaseDocument> Future<Boolean> asyncPrependCas(T doc,Transcoder<T> tc)throws StorageException{ return _couchbaseClient.prepend(doc.getDocumentCas(),doc.getDocumentKey(),doc,tc); }
    public <T extends BaseCouchbaseDocument> Future<Boolean> asyncPrepend(T doc,Transcoder<T> tc)throws StorageException{ return _couchbaseClient.prepend(doc.getDocumentKey(),doc,tc); }


    //Todo Check Cas value
    public <T extends BaseCouchbaseDocument> T deleteCas(T doc)throws StorageException{
        try {
            Boolean result = asyncDeleteCas(doc).get();
            if(!result){
                throw new DocumentStorageException(doc,"Failing to delete");
            }
            return doc;
        }
        catch (InterruptedException e) {
            throw new DocumentStorageException(doc,"Interrupted waiting for deletion", e);
        }
        catch (ExecutionException e) {
            throw new DocumentStorageException(doc,"Error during deletion execution",e);
        }
    }
    public <T extends BaseCouchbaseDocument> T delete(T doc)throws StorageException{ try {
        Boolean result = asyncDelete(doc).get();
        if(!result){
            throw new DocumentStorageException(doc,"Failing to delete");
        }
        return doc;
    }
    catch (InterruptedException e) {
        throw new DocumentStorageException(doc,"Interrupted waiting for deletion", e);
    }
    catch (ExecutionException e) {
        throw new DocumentStorageException(doc,"Error during deletion execution",e);
    }}

    public <T extends BaseCouchbaseDocument> Future<Boolean> asyncDeleteCas(T doc)throws StorageException{ return _couchbaseClient.delete(doc.getDocumentKey(),doc.getDocumentCas()); }
    public <T extends BaseCouchbaseDocument> Future<Boolean> asyncDelete(T doc)throws StorageException{ return _couchbaseClient.delete(doc.getDocumentKey()); }


    public boolean shutdown(long timeout,java.util.concurrent.TimeUnit unit){
        return _couchbaseClient.shutdown(timeout,unit);
    }
    
    public void shutdown(){
        _couchbaseClient.shutdown();
    }
}
