package com.dreameddeath.core.couchbase.exception;

/**
 * Created by Christophe Jeunesse on 16/06/2016.
 */
public class StorageUnkownException extends StorageException {
    public StorageUnkownException(){}
    public StorageUnkownException(Throwable e){
        super(e);
    }
    public StorageUnkownException(String message, Throwable e){
        super(message,e);
    }
    public StorageUnkownException(String message){
        super(message);
    }
}
