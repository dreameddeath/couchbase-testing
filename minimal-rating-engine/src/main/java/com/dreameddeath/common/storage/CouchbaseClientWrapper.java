package com.dreameddeath.common.storage;

import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;

import java.util.concurrent.Future;


import net.spy.memcached.internal.OperationFuture;
import com.couchbase.client.CouchbaseClient;
import net.spy.memcached.CASResponse;
import net.spy.memcached.PersistTo;
import net.spy.memcached.ReplicateTo;

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

    
    public <T extends CouchbaseDocument> OperationFuture<Boolean> set(T doc,int exp){ return _couchbaseClient.set(doc.getKey(),exp,doc,(Transcoder<T>)doc.getTranscoder()); }
    //public OperationFuture<Boolean> set(CouchbaseDocument doc,int exp,ReplicateTo rep){  return _couchbaseClient.set(doc.getKey(),exp,doc,doc.getTranscoder(),rep);}
    //public OperationFuture<Boolean> set(CouchbaseDocument doc,int exp,PersistTo req){ return _couchbaseClient.set(doc.getKey(),exp,doc,doc.getTranscoder(),req);}
    //public OperationFuture<Boolean> set(CouchbaseDocument doc,int exp,PersistTo req,ReplicateTo rep){return _couchbaseClient.set(doc.getKey(),exp,doc,doc.getTranscoder(),req,rep);}
    
    public <T extends CouchbaseDocument> OperationFuture<Boolean> set(T doc){ return set(doc,0); }    
    //public OperationFuture<Boolean> set(CouchbaseDocument doc,ReplicateTo rep){ return set(doc,0,rep); }
    //public OperationFuture<Boolean> set(CouchbaseDocument doc,PersistTo req){ return set(doc,0,req);}
    //public OperationFuture<Boolean> set(CouchbaseDocument doc,PersistTo req,ReplicateTo rep){ return set(doc,0,req,rep);}
    
    //Todo Check Cas value
    public <T extends CouchbaseDocument> CASResponse cas(T doc,int exp){ return _couchbaseClient.cas(doc.getKey(),doc.getCas(),exp,doc,(Transcoder<T>)doc.getTranscoder()); }
    //public CASResponse cas(CouchbaseDocument doc,int exp,ReplicateTo rep){  return _couchbaseClient.cas(doc.getKey(),doc.getCas(),exp,doc,doc.getTranscoder(),rep);}
    //public CASResponse cas(CouchbaseDocument doc,int exp,PersistTo req){ return _couchbaseClient.cas(doc.getKey(),doc.getCas(),exp,doc,doc.getTranscoder(),req);}
    //public CASResponse cas(CouchbaseDocument doc,int exp,PersistTo req,ReplicateTo rep){return _couchbaseClient.cas(doc.getKey(),doc.getCas(),exp,doc,doc.getTranscoder(),req,rep);}
    
    public <T extends CouchbaseDocument> CASResponse cas(T doc){ return cas(doc,0); }
    //public CASResponse cas(CouchbaseDocument doc,ReplicateTo rep){ return cas(doc,0,rep); }
    //public CASResponse cas(CouchbaseDocument doc,PersistTo req){ return cas(doc,0,req);}
    //public CASResponse cas(CouchbaseDocument doc,PersistTo req,ReplicateTo rep){ return cas(doc,0,req,rep);}
    
    //Todo Check Cas value
    public <T extends CouchbaseDocument> Future<Boolean> appendCas(T doc){ return _couchbaseClient.append(doc.getCas(),doc.getKey(),doc,(Transcoder<T>)doc.getTranscoder()); }
    public <T extends CouchbaseDocument> Future<Boolean> append(T doc){ return _couchbaseClient.append(doc.getKey(),doc,(Transcoder<T>)doc.getTranscoder()); }    
   
   
    public boolean shutdown(long timeout,java.util.concurrent.TimeUnit unit){
        return _couchbaseClient.shutdown(timeout,unit);
    }
    
    public void shutdown(){
        _couchbaseClient.shutdown();
    }
}
