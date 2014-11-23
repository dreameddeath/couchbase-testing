package com.dreameddeath.core.exception.storage;

import com.dreameddeath.core.model.common.RawCouchbaseDocument;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class DuplicateUniqueKeyStorageException extends DocumentStorageException {
    public DuplicateUniqueKeyStorageException(RawCouchbaseDocument doc){ super(doc);}
    public DuplicateUniqueKeyStorageException(RawCouchbaseDocument doc, String message){ super(doc,message);}
    public DuplicateUniqueKeyStorageException(RawCouchbaseDocument doc, String message, Throwable e){ super(doc,message,e);}
    public DuplicateUniqueKeyStorageException(RawCouchbaseDocument doc, Throwable e){ super(doc,e);}
}
