package com.dreameddeath.core.session;

import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.dao.ReadOnlyException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.common.RawCouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;

/**
 * Created by ceaj8230 on 20/11/2014.
 */
public interface ICouchbaseSession {
    long getCounter(String key) throws DaoException,StorageException;
    long incrCounter(String key, long byVal) throws ReadOnlyException, DaoException,StorageException;
    long decrCounter(String key, long byVal) throws ReadOnlyException, DaoException,StorageException;

    RawCouchbaseDocument get(String key) throws DaoException,StorageException;
    <T extends RawCouchbaseDocument> T get(String key, Class<T> targetClass) throws DaoException,StorageException;
    <T extends RawCouchbaseDocument> T getFromUID(String uid, Class<T> targetClass) throws DaoException,StorageException;
    <T extends RawCouchbaseDocument> String getKeyFromUID(String uid, Class<T> targetClass) throws DaoException;

    <T extends RawCouchbaseDocument> T newEntity(Class<T> clazz);
    <T extends RawCouchbaseDocument> T create(T obj) throws DaoException,StorageException;
    <T extends RawCouchbaseDocument> T buildKey(T obj) throws DaoException,StorageException;
    <T extends CouchbaseDocument> T save(T obj) throws DaoException,StorageException;

    <T extends RawCouchbaseDocument> T update(T obj)throws DaoException,StorageException;
    <T extends RawCouchbaseDocument> T delete(T obj)throws DaoException,StorageException;

    public void validate(RawCouchbaseDocument doc);

    CouchbaseUniqueKey getUniqueKey(String internalKey)throws DaoException,StorageException;
    void addOrUpdateUniqueKey(CouchbaseDocument doc, Object value, String nameSpace)throws DaoException,StorageException;
    void removeUniqueKey(String internalKey) throws DaoException,StorageException;


}
