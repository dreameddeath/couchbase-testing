package com.dreameddeath.core.dao.document;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by CEAJ8230 on 17/05/2015.
 */
public abstract class CouchbaseDocumentDaoWithUID<T extends CouchbaseDocument> extends CouchbaseDocumentWithKeyPatternDao<T> implements IDaoForDocumentWithUID<T> {
    public abstract String getKeyFromUID(String uid);

    public T getFromUID(String uid) throws DaoException,StorageException {
        T result= get(getKeyFromUID(uid));
        result.getBaseMeta().setStateSync();
        return result;
    }
}
