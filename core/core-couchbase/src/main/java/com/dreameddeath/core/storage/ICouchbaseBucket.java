package com.dreameddeath.core.storage;

import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.storage.impl.ReadParams;
import com.dreameddeath.core.storage.impl.WriteParams;
import rx.Observable;

/**
 * Created by ceaj8230 on 21/11/2014.
 */
public interface ICouchbaseBucket {
    public ICouchbaseBucket addTranscoder(ICouchbaseTranscoder transcoder);

    public <T extends CouchbaseDocument> T get(final String key, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    public <T extends CouchbaseDocument> T get(final String key, final ICouchbaseTranscoder<T> transcoder,ReadParams params) throws StorageException;
    public <T extends CouchbaseDocument> Observable<T> asyncGet(final String id, final ICouchbaseTranscoder<T> transcoder);
    public <T extends CouchbaseDocument> Observable<T> asyncGet(final String id, final ICouchbaseTranscoder<T> transcoder,ReadParams params);

    public <T extends CouchbaseDocument> T add(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    public <T extends CouchbaseDocument> T add(final T doc, final ICouchbaseTranscoder<T> transcoder,WriteParams params) throws StorageException;
    public <T extends CouchbaseDocument> Observable<T> asyncAdd(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    public <T extends CouchbaseDocument> Observable<T> asyncAdd(final T doc, final ICouchbaseTranscoder<T> transcoder,WriteParams params) throws StorageException;


    public <T extends CouchbaseDocument> T set(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    public <T extends CouchbaseDocument> T set(final T doc, final ICouchbaseTranscoder<T> transcoder,WriteParams params) throws StorageException;
    public <T extends CouchbaseDocument> Observable<T> asyncSet(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    public <T extends CouchbaseDocument> Observable<T> asyncSet(final T doc, final ICouchbaseTranscoder<T> transcoder,WriteParams params) throws StorageException;

    public <T extends CouchbaseDocument> T replace(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    public <T extends CouchbaseDocument> T replace(final T doc, final ICouchbaseTranscoder<T> transcoder,WriteParams params) throws StorageException;
    public <T extends CouchbaseDocument> Observable<T> asyncReplace(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    public <T extends CouchbaseDocument> Observable<T> asyncReplace(final T doc, final ICouchbaseTranscoder<T> transcoder,WriteParams params) throws StorageException;

    public <T extends CouchbaseDocument> T delete(T bucketDoc,ICouchbaseTranscoder<T> transcoder) throws StorageException;
    public <T extends CouchbaseDocument> T delete(T bucketDoc,ICouchbaseTranscoder<T> transcoder,WriteParams params) throws StorageException;
    public <T extends CouchbaseDocument> Observable<T> asyncDelete(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    public <T extends CouchbaseDocument> Observable<T> asyncDelete(final T doc, final ICouchbaseTranscoder<T> transcoder,WriteParams params) throws StorageException;

    public <T extends CouchbaseDocument> T append(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    public <T extends CouchbaseDocument> T append(final T doc, final ICouchbaseTranscoder<T> transcoder,WriteParams params) throws StorageException;
    public <T extends CouchbaseDocument> Observable<T> asyncAppend(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    public <T extends CouchbaseDocument> Observable<T> asyncAppend(final T doc, final ICouchbaseTranscoder<T> transcoder,WriteParams params) throws StorageException;

    public <T extends CouchbaseDocument> T prepend(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    public <T extends CouchbaseDocument> T prepend(final T doc, final ICouchbaseTranscoder<T> transcoder,WriteParams params) throws StorageException;
    public <T extends CouchbaseDocument> Observable<T> asyncPrepend(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    public <T extends CouchbaseDocument> Observable<T> asyncPrepend(final T doc, final ICouchbaseTranscoder<T> transcoder,WriteParams params) throws StorageException;

    public Long counter(String key, Long by, Long defaultValue, Integer expiration) throws StorageException;
    public Long counter(String key, Long by, Long defaultValue, Integer expiration,WriteParams params) throws StorageException;

    public Observable<Long> asyncCounter(String key, Long by, Long defaultValue, Integer expiration)throws StorageException;
    public Observable<Long> asyncCounter(String key, Long by, Long defaultValue, Integer expiration,WriteParams params)throws StorageException;
    public Long counter(String key, Long by, Long defaultValue) throws StorageException;
    public Long counter(String key, Long by, Long defaultValue,WriteParams params) throws StorageException;
    public Observable<Long> asyncCounter(String key, Long by, Long defaultValue)throws StorageException;
    public Observable<Long> asyncCounter(String key, Long by, Long defaultValue,WriteParams params)throws StorageException;
    public Long counter(String key, Long by) throws StorageException;
    public Long counter(String key, Long by,WriteParams params) throws StorageException;
    public Observable<Long> asyncCounter(String key, Long by)throws StorageException;
    public Observable<Long> asyncCounter(String key, Long by,WriteParams params)throws StorageException;

}
