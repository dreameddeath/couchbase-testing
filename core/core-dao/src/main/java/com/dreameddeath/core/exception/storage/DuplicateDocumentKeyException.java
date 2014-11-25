package com.dreameddeath.core.exception.storage;

import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by CEAJ8230 on 21/09/2014.
 */
public class DuplicateDocumentKeyException extends DocumentStorageException {
    public DuplicateDocumentKeyException(CouchbaseDocument doc,String message){super(doc,message);}
    public DuplicateDocumentKeyException(CouchbaseDocument doc,String message,Throwable e){super(doc,message,e);}
    public DuplicateDocumentKeyException(CouchbaseDocument doc,Throwable e){super(doc,e);}
    public DuplicateDocumentKeyException(CouchbaseDocument doc){super(doc);}
}
