package com.dreameddeath.common.storage;

import com.dreameddeath.common.model.CouchbaseDocument;
import com.dreameddeath.common.model.CouchbaseDocumentLink;
import net.spy.memcached.internal.GetFuture;

public class GetFutureWrapper<T extends CouchbaseDocument> extends FutureWrapper<T,T>{
    private String _key;
    protected GetFuture<T> _futureOp;
    
    public String getKey(){
        return _key;
    }
    
    public GetFutureWrapper(GetFuture<T> futureOp,String key){
        _futureOp = futureOp;
        _key =key;
    }
    
    public GetFutureWrapper(GetFuture<T> futureOp,String key,CouchbaseDocumentLink<T> link){
        this(futureOp,key);
        setLink(link);
    }
    public GetFuture<T> getFuture(){
        return _futureOp;
    }
    
    
}
