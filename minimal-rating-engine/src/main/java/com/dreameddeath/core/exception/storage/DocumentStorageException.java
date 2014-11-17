package com.dreameddeath.core.exception.storage;

import com.dreameddeath.core.model.common.BaseCouchbaseDocument;


/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class DocumentStorageException extends StorageException {
    BaseCouchbaseDocument _doc;

    public DocumentStorageException(BaseCouchbaseDocument doc,String message) {
        super(message);
        _doc = doc;
    }

    public DocumentStorageException(BaseCouchbaseDocument doc,String message,Throwable e) {
        super(message,e);
        _doc = doc;
    }

    public DocumentStorageException(BaseCouchbaseDocument doc,Throwable e) {
        super(e);
        _doc = doc;
    }

    public DocumentStorageException(BaseCouchbaseDocument doc) {
        _doc = doc;
    }

    @Override
    public String getMessage(){
        StringBuilder builder = new StringBuilder(super.getMessage());
        builder.append("\n The doc was "+_doc);
        return builder.toString();
    }
}