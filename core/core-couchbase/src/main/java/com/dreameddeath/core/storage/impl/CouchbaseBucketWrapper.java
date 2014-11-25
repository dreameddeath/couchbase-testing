package com.dreameddeath.core.storage.impl;


import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.Document;
import com.couchbase.client.java.document.JsonLongDocument;
import com.couchbase.client.java.error.DocumentAlreadyExistsException;
import com.couchbase.client.java.transcoder.Transcoder;
import com.dreameddeath.core.exception.storage.*;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.storage.BucketDocument;
import com.dreameddeath.core.storage.ICouchbaseBucket;
import com.dreameddeath.core.storage.ICouchbaseTranscoder;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

//import java.util.concurrent.Future;

/**
*  Class used to perform storage 
*/
public class CouchbaseBucketWrapper implements ICouchbaseBucket {
    private Bucket _bucket;
    private final Cluster _cluster;
    private final String _bucketName;
    private final String _bucketPassword;
    private List<Transcoder<? extends Document, ?>> _transcoders = new ArrayList<Transcoder<? extends Document, ?>>();

    public CouchbaseBucketWrapper(CouchbaseCluster cluster, String bucketName, String bucketPassword){
        _cluster = cluster;
        _bucketName = bucketName;
        _bucketPassword = bucketPassword;
    }

    public void start(long timeout,TimeUnit unit){_bucket = _cluster.openBucket(_bucketName,_bucketPassword,_transcoders,timeout,unit);}
    public void start(){
        _bucket = _cluster.openBucket(_bucketName,_bucketPassword,_transcoders);
    }

    public boolean shutdown(long timeout,TimeUnit unit){
        return _bucket.close(timeout,unit);
    }
    public void shutdown(){
        _bucket.close();
    }

    @Override
    public ICouchbaseBucket addTranscoder(ICouchbaseTranscoder transcoder){
        if(_bucket!=null){
            throw new IllegalStateException("Cannot add transcoder "+transcoder.getClass().getName()+" after client initialization");
        }
        _transcoders.add(transcoder);
        return this;
    }

    public Bucket getBucket(){
        return _bucket;
    }

    @Override
    public <T extends CouchbaseDocument> T get(final String key,final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        try {
            T result=asyncGet(key,transcoder).toBlocking().singleOrDefault(null);
            if(result==null){ throw new DocumentNotFoundException(key,"Cannot find document using key <"+key+">");}
            else{ return result; }
        }
        catch(DocumentNotFoundException e){ throw e; }
        catch(Throwable e){ throw new DocumentAccessException(key,"Error during document access attempt of <"+key+">",e); }

    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncGet(final String id,final ICouchbaseTranscoder<T> transcoder){
        return _bucket.async().get(id,transcoder.documentType()).map(new Func1<BucketDocument<T>, T>() {
            @Override
            public T call(BucketDocument<T> tBucketDocument) {
                return tBucketDocument.content();
            }
        });
    }

    @Override
    public <T extends CouchbaseDocument> T add(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        try {
            T result=asyncAdd(doc,transcoder).toBlocking().singleOrDefault(null);
            if(result==null){ throw new DuplicateDocumentKeyException(doc);}
            else{ return result; }
        }
        catch(DuplicateDocumentKeyException e){ throw e; }
        catch(DocumentAlreadyExistsException e){ throw new DuplicateDocumentKeyException(doc,e); }
        catch(Throwable e){ throw new DocumentStorageException(doc,"Error during fetch execution",e); }
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncAdd(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        final BucketDocument<T> bucketDoc = transcoder.newDocument(doc);
        return _bucket.async().insert(bucketDoc).map(new DocumentResync<T>(bucketDoc));
    }

    @Override
    public <T extends CouchbaseDocument> T set(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        try {
            T result=asyncSet(doc,transcoder).toBlocking().singleOrDefault(null);
            if(result==null){ throw new DocumentStorageException(doc,"Cannot apply set method to the document");}
            else{ return result; }
        }
        catch(DuplicateDocumentKeyException e){ throw e; }
        catch(Throwable e){ throw new DocumentStorageException(doc,"Error during fetch execution",e); }
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncSet(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        final BucketDocument<T> bucketDoc = transcoder.newDocument(doc);
        return _bucket.async().upsert(bucketDoc).map(new DocumentResync<T>(bucketDoc));
    }

    @Override
    public <T extends CouchbaseDocument> T replace(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        try {
            T result=asyncReplace(doc,transcoder).toBlocking().singleOrDefault(null);
            if(result==null){ throw new DocumentNotFoundException(doc,"Cannot apply replace method");}
            else{ return result; }
        }
        catch(DocumentNotFoundException e){ throw e; }
        catch(Throwable e){ throw new DocumentStorageException(doc,"Error during fetch execution",e); }
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncReplace(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        final BucketDocument<T> bucketDoc = transcoder.newDocument(doc);
        return _bucket.async().upsert(bucketDoc).map(new DocumentResync<T>(bucketDoc));
    }

    @Override
    public <T extends CouchbaseDocument> T delete(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        try {
            T result=asyncDelete(doc,transcoder).toBlocking().singleOrDefault(null);
            if(result==null){ throw new DocumentNotFoundException(doc,"Cannot apply replace method");}
            else{ return result; }
        }
        catch(DocumentNotFoundException e){ throw e; }
        catch(Throwable e){  throw new DocumentStorageException(doc,"Error during fetch execution",e); }
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncDelete(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        final BucketDocument<T> bucketDoc = transcoder.newDocument(doc);
        return _bucket.async().remove(bucketDoc).map(new DocumentResync<T>(bucketDoc));
    }


    @Override
    public <T extends CouchbaseDocument> T append(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        try {
            T result=asyncAppend(doc,transcoder).toBlocking().singleOrDefault(null);
            if(result==null){ throw new DocumentNotFoundException(doc,"Cannot apply replace method");}
            else{ return result; }
        }
        catch(DocumentNotFoundException e){ throw e; }
        catch(Throwable e){  throw new DocumentStorageException(doc,"Error during fetch execution",e); }
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncAppend(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        final BucketDocument<T> bucketDoc = transcoder.newDocument(doc);
        return _bucket.async().append(bucketDoc).map(new DocumentResync<T>(bucketDoc));
    }

    @Override
    public <T extends CouchbaseDocument> T prepend(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        try {
            T result=asyncAppend(doc,transcoder).toBlocking().singleOrDefault(null);
            if(result==null){ throw new DocumentNotFoundException(doc,"Cannot apply replace method");}
            else{ return result; }
        }
        catch(DocumentNotFoundException e){ throw e; }
        catch(Throwable e){  throw new DocumentStorageException(doc,"Error during fetch execution",e); }
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncPrepend(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        final BucketDocument<T> bucketDoc = transcoder.newDocument(doc);
        return _bucket.async().prepend(bucketDoc).map(new DocumentResync<T>(bucketDoc));
    }


    @Override
    public Long counter(String key, Long by, Long defaultValue, Integer expiry) throws StorageException{
        return asyncCounter(key,by,defaultValue,expiry).toBlocking().singleOrDefault(null);
    }

    @Override
    public Observable<Long> asyncCounter(String key, Long by, Long defaultValue, Integer expiry)throws StorageException{
        return _bucket.async().counter(key,by,defaultValue,expiry).map(new Func1<JsonLongDocument, Long>() {
            @Override
            public Long call(JsonLongDocument jsonLongDocument) {
                return jsonLongDocument.content();
            }
        });
    }

    @Override
    public Long counter(String key, Long by, Long defaultValue) throws StorageException{
        return asyncCounter(key,by,defaultValue).toBlocking().singleOrDefault(null);
    }
    @Override
    public Observable<Long> asyncCounter(String key, Long by, Long defaultValue)throws StorageException{
        return _bucket.async().counter(key,by,defaultValue).map(new Func1<JsonLongDocument, Long>() {
            @Override
            public Long call(JsonLongDocument jsonLongDocument) {
                return jsonLongDocument.content();
            }
        });
    }

    @Override
    public Long counter(String key, Long by) throws StorageException{
        return asyncCounter(key,by).toBlocking().singleOrDefault(null);
    }

    @Override
    public Observable<Long> asyncCounter(String key, Long by)throws StorageException{
        return _bucket.async().counter(key,by).map(new Func1<JsonLongDocument, Long>() {
            @Override
            public Long call(JsonLongDocument jsonLongDocument) {
                return jsonLongDocument.content();
            }
        });
    }

    public class DocumentResync<T extends CouchbaseDocument> implements Func1<BucketDocument<T>, T>{
        private final BucketDocument<T> _bucketDoc;
        public DocumentResync(final BucketDocument<T> doc){
            _bucketDoc = doc;
        }
        @Override
        public T call(BucketDocument<T> tBucketDocument) {
            _bucketDoc.syncMeta(tBucketDocument);
            return _bucketDoc.content();
        }
    }
}
