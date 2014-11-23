package com.dreameddeath.core.exception.storage;

import com.dreameddeath.core.model.common.RawCouchbaseDocument;

/**
 * Created by CEAJ8230 on 21/09/2014.
 */
public class DuplicateDocumentKeyException extends DocumentStorageException {
    public DuplicateDocumentKeyException(RawCouchbaseDocument doc,String message){super(doc,message);}
    public DuplicateDocumentKeyException(RawCouchbaseDocument doc,String message,Throwable e){super(doc,message,e);}
    public DuplicateDocumentKeyException(RawCouchbaseDocument doc,Throwable e){super(doc,e);}
    public DuplicateDocumentKeyException(RawCouchbaseDocument doc){super(doc);}
}
