package com.dreameddeath.core.dao.business;

import com.dreameddeath.core.dao.document.BaseCouchbaseDocumentDaoFactory;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.business.CouchbaseDocument;
import com.dreameddeath.core.storage.CouchbaseBucketWrapper;

/**
 * Created by Christophe Jeunesse on 27/07/2014.
 */
public abstract class CouchbaseDocumentDaoWithUID<T extends CouchbaseDocument> extends CouchbaseDocumentDao<T> {
    public CouchbaseDocumentDaoWithUID(CouchbaseBucketWrapper client,BaseCouchbaseDocumentDaoFactory factory){
        super(client,factory);
    }

    public abstract String getKeyFromUID(String uid);

    public T getFromUID(String uid) throws DaoException,StorageException{
        T result= get(getKeyFromUID(uid));
        result.getBaseMeta().setStateSync();
        return result;
    }
}
