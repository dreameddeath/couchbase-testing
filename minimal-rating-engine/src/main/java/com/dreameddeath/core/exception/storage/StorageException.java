package com.dreameddeath.core.exception.storage;

/**
 * Created by ceaj8230 on 03/09/2014.
 */
public class StorageException extends Exception {
    public StorageException(){}
    public StorageException(Throwable e){
        super(e);
    }
    public StorageException(String message, Throwable e){
        super(message,e);
    }
    public StorageException(String message){
        super(message);
    }
}
