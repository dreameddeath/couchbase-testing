package com.dreameddeath.common.storage;

import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;

import com.couchbase.client.CouchbaseClient;
import net.spy.memcached.CASValue;
import net.spy.memcached.transcoders.Transcoder;
import net.spy.memcached.OperationTimeoutException;

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
    
    public <T extends CouchbaseDocument> T get(String key, Transcoder<T> tc){
        try {
            return asyncGet(key,tc).get();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted waiting for value", e);
        } catch (ExecutionException e) {
            if(e.getCause() instanceof CancellationException) {
                throw (CancellationException) e.getCause();
            } else {
                throw new RuntimeException("Exception waiting for value", e);
            }
        }
    }
    

    public <T extends CouchbaseDocument> T gets(String key, Transcoder<T> tc){
        try {
            return asyncGets(key,tc).get().getValue();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted waiting for value", e);
        } catch (ExecutionException e) {
            if(e.getCause() instanceof CancellationException) {
                throw (CancellationException) e.getCause();
            } else {
                throw new RuntimeException("Exception waiting for value", e);
            }
        }
    }
    
    
    public <T extends CouchbaseDocument> GetFutureWrapper<T> asyncGet(String key, Transcoder<T> tc){
        return new GetFutureWrapper<T>(_couchbaseClient.asyncGet(key,tc),key);
    }

    public <T extends CouchbaseDocument> OperationFutureWrapper<CASValue<T>> asyncGets(String key, Transcoder<T> tc){
        return new OperationFutureWrapper<CASValue<T>>(_couchbaseClient.asyncGets(key,tc));
    }

}
