package com.dreameddeath.core.exception.storage;

import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class DuplicateUniqueKeyStorageException extends DocumentStorageException {
    public DuplicateUniqueKeyStorageException(CouchbaseDocument doc){ super(doc);}
    public DuplicateUniqueKeyStorageException(CouchbaseDocument doc, String message){ super(doc,message);}
    public DuplicateUniqueKeyStorageException(CouchbaseDocument doc, String message, Throwable e){ super(doc,message,e);}
    public DuplicateUniqueKeyStorageException(CouchbaseDocument doc, Throwable e){ super(doc,e);}
}
