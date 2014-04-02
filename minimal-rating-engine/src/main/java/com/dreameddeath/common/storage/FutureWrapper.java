package com.dreameddeath.common.storage;

import java.util.concurrent.Future;
import net.spy.memcached.CASValue;
import net.spy.memcached.internal.OperationFuture;

public abstract class FutureWrapper<T,TDOC extends CouchbaseDocument>{
    private CouchbaseDocumentLink<TDOC> _link;
    private TDOC _linkedDoc;
    
    public abstract String getKey();
    public abstract Future<T> getFuture();

    public void setLink(CouchbaseDocumentLink<TDOC> link){
        _link = link;
    }
    
    public void setDoc(TDOC doc){
        _linkedDoc = doc;
    }
    
    protected final T enrich(T input){
        if(input instanceof CouchbaseDocument){
            ((CouchbaseDocument)input).setKey(getKey());
            if(_link!=null){
                _link.setLinkedObject((TDOC)input);
            }
        }
        else if(input instanceof CASValue){
            Object doc = ((CASValue)input).getValue();
            if(doc instanceof CouchbaseDocument){
                ((CouchbaseDocument)doc).setKey(getKey());
                ((CouchbaseDocument)doc).setCas(((CASValue)input).getCas());
                if(_link!=null){
                    _link.setLinkedObject(((TDOC)input));
                }
            }
        }
        
        if((_linkedDoc !=null) && (getFuture() instanceof OperationFuture)){
            _linkedDoc.setCas(((OperationFuture)getFuture()).getCas());
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
