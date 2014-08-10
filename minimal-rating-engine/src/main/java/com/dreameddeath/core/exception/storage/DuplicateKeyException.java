package com.dreameddeath.core.exception.storage;

import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class DuplicateKeyException extends DocumentStorageException {
    public DuplicateKeyException(CouchbaseDocument doc){ super(doc);}
    public DuplicateKeyException(CouchbaseDocument doc,String message){ super(doc,message);}
    public DuplicateKeyException(CouchbaseDocument doc,String message,Throwable e){ super(doc,message,e);}
    public DuplicateKeyException(CouchbaseDocument doc,Throwable e){ super(doc,e);}
}
