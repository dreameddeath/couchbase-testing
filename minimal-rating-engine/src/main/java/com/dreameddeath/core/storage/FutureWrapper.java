package com.dreameddeath.core.storage;

import com.dreameddeath.core.model.common.BaseCouchbaseDocument;
import net.spy.memcached.CASValue;
import net.spy.memcached.internal.OperationFuture;

import java.util.concurrent.Future;


public abstract class FutureWrapper<T,TDOC extends BaseCouchbaseDocument>{
    //private CouchbaseDocumentLink<TDOC> _link;
    private TDOC _linkedDoc;
    
    public abstract String getKey();
    public abstract Future<T> getFuture();

    /*public void setLink(CouchbaseDocumentLink<TDOC> link){
        _link = link;
    }*/
    
    protected void setDoc(TDOC doc){
        _linkedDoc = doc;
    }
    public TDOC getDoc(){
        return _linkedDoc;
    }
    
    protected final T enrich(T input){
        if(input instanceof BaseCouchbaseDocument){
            BaseCouchbaseDocument inputDoc = (BaseCouchbaseDocument)input;
            inputDoc.setDocumentKey(getKey());
            if(getFuture() instanceof OperationFuture){
                inputDoc.setDocumentCas(((OperationFuture) getFuture()).getCas());
            }
            /*if(_link!=null){
                _link.setLinkedObject((TDOC)input);
            }*/
        }
        else if(input instanceof CASValue){
            Object doc = ((CASValue)input).getValue();
            if(doc instanceof BaseCouchbaseDocument){
                BaseCouchbaseDocument inputDoc = (BaseCouchbaseDocument)doc;
                inputDoc.setDocumentKey(getKey());
                inputDoc.setDocumentCas(((CASValue) input).getCas());
                /*if(_link!=null){
                    _link.setLinkedObject(((TDOC)input));
                }*/
            }
        }
        
        if((_linkedDoc !=null) && (getFuture() instanceof OperationFuture)){
            _linkedDoc.setDocumentCas(((OperationFuture) getFuture()).getCas());
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
