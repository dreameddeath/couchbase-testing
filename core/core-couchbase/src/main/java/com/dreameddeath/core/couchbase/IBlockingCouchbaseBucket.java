package com.dreameddeath.core.couchbase;

import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.couchbase.impl.ReadParams;
import com.dreameddeath.core.couchbase.impl.WriteParams;
import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by Christophe Jeunesse on 17/06/2016.
 */
public interface IBlockingCouchbaseBucket {
    <T extends CouchbaseDocument> T get(final String key, Class<T> entity) throws StorageException;
    <T extends CouchbaseDocument> T get(final String key,Class<T> entity, ReadParams params) throws StorageException;

    <T extends CouchbaseDocument> T add(final T doc) throws StorageException;
    <T extends CouchbaseDocument> T add(final T doc, WriteParams params) throws StorageException;

    <T extends CouchbaseDocument> T set(final T doc) throws StorageException;
    <T extends CouchbaseDocument> T set(final T doc, WriteParams params) throws StorageException;

    <T extends CouchbaseDocument> T replace(final T doc) throws StorageException;
    <T extends CouchbaseDocument> T replace(final T doc, WriteParams params) throws StorageException;

    <T extends CouchbaseDocument> T delete(T bucketDoc) throws StorageException;
    <T extends CouchbaseDocument> T delete(T bucketDoc, WriteParams params) throws StorageException;

    <T extends CouchbaseDocument> T append(final T doc) throws StorageException;
    <T extends CouchbaseDocument> T append(final T doc, WriteParams params) throws StorageException;

    <T extends CouchbaseDocument> T prepend(final T doc) throws StorageException;
    <T extends CouchbaseDocument> T prepend(final T doc, WriteParams params) throws StorageException;

    Long counter(String key, Long by, Long defaultValue, Integer expiration) throws StorageException;
    Long counter(String key, Long by, Long defaultValue, Integer expiration, WriteParams params) throws StorageException;
    Long counter(String key, Long by, Long defaultValue) throws StorageException;
    Long counter(String key, Long by, Long defaultValue, WriteParams params) throws StorageException;
    Long counter(String key, Long by) throws StorageException;
    Long counter(String key, Long by, WriteParams params) throws StorageException;

    ViewResult query(ViewQuery query);

}
