package com.dreameddeath.core.exception.storage;

import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class CasUpdateException extends DocumentStorageException {
    public CasUpdateException(CouchbaseDocument doc){ super(doc);}
    public CasUpdateException(CouchbaseDocument doc,String message){ super(doc,message);}
    public CasUpdateException(CouchbaseDocument doc,String message,Throwable e){ super(doc,message,e);}
    public CasUpdateException(CouchbaseDocument doc,Throwable e){ super(doc,e);}
}
