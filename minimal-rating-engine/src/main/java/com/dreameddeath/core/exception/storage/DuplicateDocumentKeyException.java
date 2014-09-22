package com.dreameddeath.core.exception.storage;

import com.dreameddeath.core.model.common.BaseCouchbaseDocument;

/**
 * Created by CEAJ8230 on 21/09/2014.
 */
public class DuplicateDocumentKeyException extends DocumentStorageException {
    public DuplicateDocumentKeyException(BaseCouchbaseDocument doc,String message){super(doc,message);}
    public DuplicateDocumentKeyException(BaseCouchbaseDocument doc,String message,Throwable e){super(doc,message,e);}
    public DuplicateDocumentKeyException(BaseCouchbaseDocument doc,Throwable e){super(doc,e);}
    public DuplicateDocumentKeyException(BaseCouchbaseDocument doc){super(doc);}
}
