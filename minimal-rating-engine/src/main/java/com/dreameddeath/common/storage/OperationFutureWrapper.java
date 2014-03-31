package com.dreameddeath.common.storage;

import net.spy.memcached.internal.OperationFuture;
import net.spy.memcached.CASValue;
import java.util.concurrent.Future;
/**
*  Class used to perform storage 
*/
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
