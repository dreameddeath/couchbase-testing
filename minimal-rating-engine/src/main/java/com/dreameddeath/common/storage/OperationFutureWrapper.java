package com.dreameddeath.common.storage;

import com.dreameddeath.common.model.document.CouchbaseDocument;
import com.dreameddeath.common.model.document.CouchbaseDocumentLink;
import net.spy.memcached.internal.OperationFuture;

public class OperationFutureWrapper<T,TDOC extends CouchbaseDocument> extends FutureWrapper<T,TDOC>{
    protected OperationFuture<T> _futureOp;
    
    public OperationFutureWrapper(OperationFuture<T> futureOp){
        _futureOp = futureOp;
    }
    
    public OperationFutureWrapper(OperationFuture<T> futureOp,CouchbaseDocumentLink<TDOC> link){
        this(futureOp);
        setLink(link);
    }
    
    public OperationFutureWrapper(OperationFuture<T> futureOp,TDOC doc){
        this(futureOp);
        setDoc(doc);
    }
    
    
    public OperationFuture<T> getFuture(){
        return _futureOp;
    }
    
    public String getKey(){
        return _futureOp.getKey();
    }
    
}
