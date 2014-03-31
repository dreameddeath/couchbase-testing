package com.dreameddeath.common.storage;

import net.spy.memcached.internal.OperationFuture;

public class OperationFutureWrapper<T> extends FutureWrapper<T>{
    protected OperationFuture<T> _futureOp;
    public OperationFutureWrapper(OperationFuture<T> futureOp){
        _futureOp = futureOp;
    }
    
    public OperationFuture<T> getFuture(){
        return _futureOp;
    }
    
    public String getKey(){
        return _futureOp.getKey();
    }
    
}
