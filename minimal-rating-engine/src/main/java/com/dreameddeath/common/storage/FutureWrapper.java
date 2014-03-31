package com.dreameddeath.common.storage;

import java.util.concurrent.Future;
import net.spy.memcached.CASValue;


public abstract class FutureWrapper<T>{
    public abstract String getKey();
    public abstract Future<T> getFuture();

    protected final T enrich(T input){
        if(input instanceof CouchbaseDocument){
            ((CouchbaseDocument)input).setKey(getKey());
        }
        else if(input instanceof CASValue){
            Object doc = ((CASValue)input).getValue();
            if(doc instanceof CouchbaseDocument){
                ((CouchbaseDocument)doc).setKey(getKey());
                ((CouchbaseDocument)doc).setCas(((CASValue)input).getCas());
            }
        }
        return input;
    }
    
    public final T get()
      throws java.lang.InterruptedException,
             java.util.concurrent.ExecutionException{
        return enrich(getFuture().get());
    }
    
    public final T get(long userTimeout, java.util.concurrent.TimeUnit unit)
      throws java.lang.InterruptedException,
             java.util.concurrent.ExecutionException,
             java.util.concurrent.TimeoutException{
        return enrich(getFuture().get(userTimeout,unit));
    }
}
