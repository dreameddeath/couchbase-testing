package com.dreameddeath.core.dao.document;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by CEAJ8230 on 17/05/2015.
 */
public interface IDaoForDocumentWithUID<T extends CouchbaseDocument> {
    String getKeyFromUID(String uid);
    T getFromUID(String uid) throws DaoException,StorageException;
}
