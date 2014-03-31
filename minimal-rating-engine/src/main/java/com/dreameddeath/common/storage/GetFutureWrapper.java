package com.dreameddeath.common.storage;

import net.spy.memcached.internal.GetFuture;
//import java.util.concurrent.Future;
/**
*  Class used to perform storage 
*/
public class GetFutureWrapper<T> extends FutureWrapper<T>{
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
