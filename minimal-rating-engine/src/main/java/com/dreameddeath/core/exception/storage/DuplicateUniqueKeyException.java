package com.dreameddeath.core.exception.storage;

import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class DuplicateUniqueKeyException extends DocumentStorageException {
    public DuplicateUniqueKeyException(CouchbaseDocument doc){ super(doc);}
    public DuplicateUniqueKeyException(CouchbaseDocument doc, String message){ super(doc,message);}
    public DuplicateUniqueKeyException(CouchbaseDocument doc, String message, Throwable e){ super(doc,message,e);}
    public DuplicateUniqueKeyException(CouchbaseDocument doc, Throwable e){ super(doc,e);}
}
