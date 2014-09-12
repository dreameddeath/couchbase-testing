package com.dreameddeath.core.storage;

import com.dreameddeath.core.model.common.BaseCouchbaseDocument;
import net.spy.memcached.internal.GetFuture;

public class GetFutureWrapper<T extends BaseCouchbaseDocument> extends FutureWrapper<T,T>{
    private String _key;
    protected GetFuture<T> _futureOp;
    
    public String getKey(){
        return _key;
    }
    
    public GetFutureWrapper(GetFuture<T> futureOp,String key){
        _futureOp = futureOp;
        _key =key;
    }

    public GetFuture<T> getFuture(){
        return _futureOp;
    }
    
    
}
