package com.dreameddeath.core.dao.archive;

import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.session.ICouchbaseSession;
import com.dreameddeath.core.storage.BucketDocument;
import com.dreameddeath.core.storage.ICouchbaseTranscoder;
import com.dreameddeath.core.transcoder.ITranscoder;

/**
 * Created by CEAJ8230 on 17/09/2014.
 */
public class CouchbaseArchiveDao<T extends CouchbaseDocument> extends CouchbaseDocumentDao<T> {
    public static String BASE_PATTERN_FMT ="arch/%s";
    public static String BASE_PATTERN ="arch/";
    private CouchbaseDocumentDao<T> _refDao;

    public void setRefDao(CouchbaseDocumentDao<T> refDao){_refDao = refDao;}
    public CouchbaseDocumentDao<T> getRefDao(){return _refDao;}

    @Override
    public ICouchbaseTranscoder<T> getTranscoder(){
        return _refDao.getTranscoder();
    }

    @Override
    public Class<? extends BucketDocument<T>> getBucketDocumentClass() {
        return _refDao.getBucketDocumentClass();
    }

    @Override
    public T buildKey(ICouchbaseSession session, T newObject) throws DaoException, StorageException {
        String key = String.format(BASE_PATTERN_FMT,newObject.getBaseMeta().getKey());
        newObject.getBaseMeta().setKey(key);
        return newObject;
    }
}
