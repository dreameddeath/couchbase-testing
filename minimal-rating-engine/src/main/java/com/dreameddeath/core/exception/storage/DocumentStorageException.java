package com.dreameddeath.core.exception.storage;

import com.dreameddeath.core.model.document.CouchbaseDocument;


/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class DocumentStorageException extends StorageException {
    CouchbaseDocument _doc;

    public DocumentStorageException(CouchbaseDocument doc,String message) {
        super(message);
        _doc = doc;
    }

    public DocumentStorageException(CouchbaseDocument doc,String message,Throwable e) {
        super(message,e);
        _doc = doc;
    }

    public DocumentStorageException(CouchbaseDocument doc,Throwable e) {
        super(e);
        _doc = doc;
    }

    public DocumentStorageException(CouchbaseDocument doc) {
        _doc = doc;
    }
}
