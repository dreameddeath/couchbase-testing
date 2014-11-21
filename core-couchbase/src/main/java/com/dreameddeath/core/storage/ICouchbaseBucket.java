package com.dreameddeath.core.storage;

import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.common.RawCouchbaseDocument;
import rx.Observable;

/**
 * Created by ceaj8230 on 21/11/2014.
 */
public interface ICouchbaseBucket {
    public ICouchbaseBucket addTranscoder(ICouchbaseTranscoder transcoder);

    public <T extends RawCouchbaseDocument> T get(final String key, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    public <T extends RawCouchbaseDocument> Observable<T> asyncGet(final String id, final ICouchbaseTranscoder<T> transcoder);

    public <T extends RawCouchbaseDocument> T add(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    public <T extends RawCouchbaseDocument> Observable<T> asyncAdd(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;

    public <T extends RawCouchbaseDocument> T set(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    public <T extends RawCouchbaseDocument> Observable<T> asyncSet(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;

    public <T extends RawCouchbaseDocument> T replace(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    public <T extends RawCouchbaseDocument> Observable<T> asyncReplace(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;

    public <T extends RawCouchbaseDocument> T delete(T bucketDoc,ICouchbaseTranscoder<T> transcoder) throws StorageException;
    public <T extends RawCouchbaseDocument> Observable<T> asyncDelete(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;

    public <T extends RawCouchbaseDocument> T append(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    public <T extends RawCouchbaseDocument> Observable<T> asyncAppend(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;

    public <T extends RawCouchbaseDocument> T prepend(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    public <T extends RawCouchbaseDocument> Observable<T> asyncPrepend(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;


    public Long counter(String key, Long by, Long defaultValue, Integer expiration) throws StorageException;
    public Observable<Long> asyncCounter(String key, Long by, Long defaultValue, Integer expiration)throws StorageException;
    public Long counter(String key, Long by, Long defaultValue) throws StorageException;
    public Observable<Long> asyncCounter(String key, Long by, Long defaultValue)throws StorageException;
    public Long counter(String key, Long by) throws StorageException;
    public Observable<Long> asyncCounter(String key, Long by)throws StorageException;
}
