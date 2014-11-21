package com.dreameddeath.core.exception.storage;

import com.dreameddeath.core.model.common.RawCouchbaseDocument;


/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class DocumentStorageException extends StorageException {
    RawCouchbaseDocument _doc;

    public DocumentStorageException(RawCouchbaseDocument doc,String message) {
        super(message);
        _doc = doc;
    }

    public DocumentStorageException(RawCouchbaseDocument doc,String message,Throwable e) {
        super(message,e);
        _doc = doc;
    }

    public DocumentStorageException(RawCouchbaseDocument doc,Throwable e) {
        super(e);
        _doc = doc;
    }

    public DocumentStorageException(RawCouchbaseDocument doc) {
        _doc = doc;
    }

    @Override
    public String getMessage(){
        return super.getMessage() + "\n The doc was " + _doc;
    }
}
