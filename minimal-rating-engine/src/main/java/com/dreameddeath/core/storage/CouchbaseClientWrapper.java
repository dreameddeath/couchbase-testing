package com.dreameddeath.core.storage;

import com.couchbase.client.CouchbaseClient;
import com.dreameddeath.core.model.common.BaseCouchbaseDocument;

import net.spy.memcached.CASResponse;
import net.spy.memcached.CASValue;
import net.spy.memcached.transcoders.Transcoder;

import java.util.concurrent.CancellationException;
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
    
    
    public <T extends BaseCouchbaseDocument> T gets(String key, Transcoder<T> tc){
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
    
    
    public <T extends BaseCouchbaseDocument> OperationFutureWrapper<CASValue<T>,T> asyncGets(String key, Transcoder<T> tc){
        return new OperationFutureWrapper<CASValue<T>,T>(_couchbaseClient.asyncGets(key,tc));
    }
    
    public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> add(T doc,Transcoder<T> tc,int exp){ return new OperationFutureWrapper(_couchbaseClient.add(doc.getDocumentKey(),exp,doc,tc),doc); }
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> add(T doc,Transcoder<T> tc,int exp,ReplicateTo rep){  return _couchbaseClient.add(doc.getDocumentKey(),exp,doc,tc,rep);}
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> add(T doc,Transcoder<T> tc,int exp,PersistTo req){ return _couchbaseClient.add(doc.getDocumentKey(),exp,doc,tc,req);}
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> add(T doc,Transcoder<T> tc,int exp,PersistTo req,ReplicateTo rep){return _couchbaseClient.add(doc.getDocumentKey(),exp,doc,tc,req,rep);}
    
    public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> add(T doc,Transcoder<T> tc){ return add(doc,tc,0); }    
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> add(T doc,Transcoder<T> tc,ReplicateTo rep){ return add(doc,tc,0,rep); }
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> add(T doc,Transcoder<T> tc,PersistTo req){ return add(doc,tc,0,req);}
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> add(T doc,Transcoder<T> tc,PersistTo req,ReplicateTo rep){ return add(doc,tc,0,req,rep);}
    
    public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> set(T doc,Transcoder<T> tc,int exp){ return new OperationFutureWrapper(_couchbaseClient.set(doc.getDocumentKey(),exp,doc,tc),doc); }
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> set(T doc,Transcoder<T> tc,int exp,ReplicateTo rep){  return _couchbaseClient.set(doc.getDocumentKey(),exp,doc,tc,rep);}
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> set(T doc,Transcoder<T> tc,int exp,PersistTo req){ return _couchbaseClient.set(doc.getDocumentKey(),exp,doc,tc,req);}
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> set(T doc,Transcoder<T> tc,int exp,PersistTo req,ReplicateTo rep){return _couchbaseClient.set(doc.getDocumentKey(),exp,doc,tc,req,rep);}
    
    public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> set(T doc,Transcoder<T> tc){ return set(doc,tc,0); }    
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> set(T doc,Transcoder<T> tc,ReplicateTo rep){ return set(doc,0,rep); }
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> set(T doc,Transcoder<T> tc,PersistTo req){ return set(doc,0,req);}
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> set(T doc,Transcoder<T> tc,PersistTo req,ReplicateTo rep){ return set(doc,0,req,rep);}
    
    public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> replace(T doc,Transcoder<T> tc,int exp){ return new OperationFutureWrapper(_couchbaseClient.replace(doc.getDocumentKey(),exp,doc,tc),doc); }
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> replace(T doc,Transcoder<T> tc,int exp,ReplicateTo rep){  return _couchbaseClient.replace(doc.getDocumentKey(),exp,doc,tc,rep);}
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> replace(T doc,Transcoder<T> tc,int exp,PersistTo req){ return _couchbaseClient.replace(doc.getDocumentKey(),exp,doc,tc,req);}
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> replace(T doc,Transcoder<T> tc,int exp,PersistTo req,ReplicateTo rep){return _couchbaseClient.replace(doc.getDocumentKey(),exp,doc,tc,req,rep);}
    
    public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> replace(T doc,Transcoder<T> tc){ return replace(doc,tc,0); }    
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> replace(T doc,Transcoder<T> tc,ReplicateTo rep){ return replace(doc,tc,0,rep); }
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> replace(T doc,Transcoder<T> tc,PersistTo req){ return replace(doc,tc,0,req);}
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<Boolean,T> replace(T doc,Transcoder<T> tc,PersistTo req,ReplicateTo rep){ return replace(doc,tc,0,req,rep);}
    
    
    //Todo Check Cas value
    public <T extends BaseCouchbaseDocument> OperationFutureWrapper<CASResponse,T> asyncCas(T doc,Transcoder<T> tc,int exp){ return new OperationFutureWrapper(_couchbaseClient.asyncCAS(doc.getDocumentKey(),doc.getDocumentCas(),exp,doc,tc),doc); }
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<CASResponse,T> asyncCas(T doc,Transcoder<T> tc,int exp,ReplicateTo rep){  return _couchbaseClient.asyncCAS(doc.getDocumentKey(),doc.getDocumentCas(),exp,doc,tc,rep);}
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<CASResponse,T> asyncCas(T doc,Transcoder<T> tc,int exp,PersistTo req){ return _couchbaseClient.asyncCAS(doc.getDocumentKey(),doc.getDocumentCas(),exp,doc,tc,req);}
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<CASResponse,T> asyncCas(T doc,Transcoder<T> tc,int exp,PersistTo req,ReplicateTo rep){return _couchbaseClient.asyncCAS(doc.getDocumentKey(),doc.getDocumentCas(),exp,doc,tc,req,rep);}
    
    public <T extends BaseCouchbaseDocument> OperationFutureWrapper<CASResponse,T> asyncCas(T doc,Transcoder<T> tc){ return asyncCas(doc,tc,0); }
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<CASResponse,T> asyncCas(T doc,Transcoder<T> tc,ReplicateTo rep){ return cas(doc,tc,0,rep); }
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<CASResponse,T> asyncCas(T doc,Transcoder<T> tc,PersistTo req){ return cas(doc,tc,0,req);}
  //public <T extends BaseCouchbaseDocument> OperationFutureWrapper<CASResponse,T> asyncCas(T doc,Transcoder<T> tc,PersistTo req,ReplicateTo rep){ return cas(doc,tc,0,req,rep);}
    
    //Todo Check Cas value
    public <T extends BaseCouchbaseDocument> CASResponse cas(T doc,Transcoder<T> tc,int exp){ 
        try {
            return asyncCas(doc,tc,exp).get();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted waiting for cas update", e);
        } catch (ExecutionException e) {
            if(e.getCause() instanceof CancellationException) {
                throw (CancellationException) e.getCause();
            } else {
                throw new RuntimeException("Exception waiting for cas update", e);
            }
        }
    }
    //public CASResponse cas(BaseCouchbaseDocument doc,Transcoder<T> tc,int exp,ReplicateTo rep){  return _couchbaseClient.cas(doc.getDocumentKey(),doc.getDocumentCas(),exp,doc,tc,rep);}
    //public CASResponse cas(BaseCouchbaseDocument doc,Transcoder<T> tc,int exp,PersistTo req){ return _couchbaseClient.cas(doc.getDocumentKey(),doc.getDocumentCas(),exp,doc,tc,req);}
    //public CASResponse cas(BaseCouchbaseDocument doc,Transcoder<T> tc,int exp,PersistTo req,ReplicateTo rep){return _couchbaseClient.cas(doc.getDocumentKey(),doc.getDocumentCas(),exp,doc,tc,req,rep);}
    
    public <T extends BaseCouchbaseDocument> CASResponse cas(T doc,Transcoder<T> tc){ return cas(doc,tc,0); }
    //public CASResponse cas(BaseCouchbaseDocument doc,Transcoder<T> tc,ReplicateTo rep){ return cas(doc,tc,0,rep); }
    //public CASResponse cas(BaseCouchbaseDocument doc,Transcoder<T> tc,PersistTo req){ return cas(doc,tc,0,req);}
    //public CASResponse cas(BaseCouchbaseDocument doc,Transcoder<T> tc,PersistTo req,ReplicateTo rep){ return cas(doc,tc,0,req,rep);}
    
    //Todo Check Cas value
    public <T extends BaseCouchbaseDocument> Future<Boolean> appendCas(T doc,Transcoder<T> tc){ return _couchbaseClient.append(doc.getDocumentCas(),doc.getDocumentKey(),doc,tc); }
    public <T extends BaseCouchbaseDocument> Future<Boolean> append(T doc,Transcoder<T> tc){ return _couchbaseClient.append(doc.getDocumentKey(),doc,tc); }
    
    //Todo Check Cas value
    public <T extends BaseCouchbaseDocument> Future<Boolean> prependCas(T doc,Transcoder<T> tc){ return _couchbaseClient.prepend(doc.getDocumentCas(),doc.getDocumentKey(),doc,tc); }
    public <T extends BaseCouchbaseDocument> Future<Boolean> prepend(T doc,Transcoder<T> tc){ return _couchbaseClient.prepend(doc.getDocumentKey(),doc,tc); }


    //Todo Check Cas value
    public <T extends BaseCouchbaseDocument> Future<Boolean> deleteCas(T doc){ return _couchbaseClient.delete(doc.getDocumentKey(),doc.getDocumentCas()); }
    public <T extends BaseCouchbaseDocument> Future<Boolean> delete(T doc){ return _couchbaseClient.delete(doc.getDocumentKey()); }


    public boolean shutdown(long timeout,java.util.concurrent.TimeUnit unit){
        return _couchbaseClient.shutdown(timeout,unit);
    }
    
    public void shutdown(){
        _couchbaseClient.shutdown();
    }
}
