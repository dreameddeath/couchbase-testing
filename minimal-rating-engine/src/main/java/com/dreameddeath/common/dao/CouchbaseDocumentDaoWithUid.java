package com.dreameddeath.common.dao;

import com.dreameddeath.common.model.document.CouchbaseDocument;
import com.dreameddeath.common.storage.CouchbaseClientWrapper;

/**
 * Created by ceaj8230 on 27/07/2014.
 */
public abstract class CouchbaseDocumentDaoWithUID<T extends CouchbaseDocument> extends CouchbaseDocumentDao<T> {
    public CouchbaseDocumentDaoWithUID(CouchbaseClientWrapper client,CouchbaseDocumentDaoFactory factory){
        super(client,factory);
    }

    public abstract String getKeyFromUID(String uid);

    public T getFromUID(String uid){
        T result=getClientWrapper().gets(getKeyFromUID(uid), getTranscoder());
        result.setStateSync();
        return result;
    }
}
