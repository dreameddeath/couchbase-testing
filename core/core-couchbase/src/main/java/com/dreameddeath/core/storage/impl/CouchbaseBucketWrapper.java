package com.dreameddeath.core.storage.impl;


import com.couchbase.client.java.*;
import com.couchbase.client.java.document.Document;
import com.couchbase.client.java.document.JsonLongDocument;
import com.couchbase.client.java.error.CASMismatchException;
import com.couchbase.client.java.error.DocumentAlreadyExistsException;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.couchbase.client.java.transcoder.Transcoder;
import com.couchbase.client.java.view.*;
import com.dreameddeath.core.exception.storage.*;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.storage.BucketDocument;
import com.dreameddeath.core.storage.ICouchbaseBucket;
import com.dreameddeath.core.storage.ICouchbaseTranscoder;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

//import java.util.concurrent.Future;

/**
*  Class used to perform storage 
*/
public class CouchbaseBucketWrapper implements ICouchbaseBucket {

    private Bucket _bucket;
    private int _replicatCount;
    private Random _replicateRandom = new Random();
    private PersistTo _persistToAllValue;
    private ReplicateTo _replicateToAllValue;
    private final Cluster _cluster;
    private final String _bucketName;
    private final String _bucketPassword;
    protected final String _keyPrefix;
    private List<Transcoder<? extends Document, ?>> _transcoders = new ArrayList<Transcoder<? extends Document, ?>>();



    public CouchbaseBucketWrapper(CouchbaseCluster cluster, String bucketName, String bucketPassword){
        _cluster = cluster;
        _bucketName = bucketName;
        _bucketPassword = bucketPassword;
        _keyPrefix=null;
    }

    public CouchbaseBucketWrapper(CouchbaseCluster cluster, String bucketName, String bucketPassword,String keyPrefix){
        _cluster = cluster;
        _bucketName = bucketName;
        _bucketPassword = bucketPassword;
        _keyPrefix=keyPrefix;
    }

    public Bucket getInternalBucket(){
        return _bucket;
    }
    @Override
    public void start(long timeout,TimeUnit unit){
        _bucket = _cluster.openBucket(_bucketName,_bucketPassword,_transcoders,timeout,unit);
        initReplicatInfo();
    }

    @Override
    public void start(){
        _bucket = _cluster.openBucket(_bucketName,_bucketPassword,_transcoders);
        initReplicatInfo();
    }

    @Override
    public boolean shutdown(long timeout,TimeUnit unit){
        return _bucket.close(timeout,unit);
    }
    @Override
    public void shutdown(){
        _bucket.close();
    }

    public void initReplicatInfo(){
        _replicatCount = _bucket.bucketManager().info().replicaCount();
        switch (_replicatCount){
            case 0:
                _persistToAllValue = PersistTo.MASTER;
                _replicateToAllValue = ReplicateTo.NONE;
                break;
            case 1:
                _persistToAllValue = PersistTo.ONE;
                _replicateToAllValue = ReplicateTo.ONE;
                break;
            case 2:
                _persistToAllValue = PersistTo.TWO;
                _replicateToAllValue = ReplicateTo.TWO;
                break;
            case 3:
                _persistToAllValue = PersistTo.THREE;
                _replicateToAllValue = ReplicateTo.THREE;
                break;
        }
    }

    public ReplicaMode getReplicat(){
        switch(_replicateRandom.nextInt(_replicatCount)){
            case 0:return ReplicaMode.FIRST;
            case 1:return ReplicaMode.SECOND;
            case 2:return ReplicaMode.THIRD;
        }
        return ReplicaMode.ALL;
    }



    @Override
    public ICouchbaseBucket addTranscoder(ICouchbaseTranscoder transcoder){
        if(_bucket!=null){
            throw new IllegalStateException("Cannot add transcoder "+transcoder.getClass().getName()+" after client initialization");
        }
        _transcoders.add(transcoder);
        if(_keyPrefix!=null){transcoder.setKeyPrefix(_keyPrefix);}
        return this;
    }

    public Bucket getBucket(){
        return _bucket;
    }

    @Override
    public <T extends CouchbaseDocument> T get(String key,final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        try {
            T result=asyncGet(key,transcoder).toBlocking().single();
            if(result==null){ throw new DocumentNotFoundException(key,"Cannot find document using key <"+key+">");}
            else{ return result; }
        }
        catch(DocumentNotFoundException e){ throw e; }
        catch(Throwable e){ throw new DocumentAccessException(key,"Error during document access attempt of <"+key+">",e); }

    }

    @Override
    public <T extends CouchbaseDocument> T get(String key, ICouchbaseTranscoder<T> transcoder, ReadParams params) throws StorageException {
        try {
            T result=asyncGet(key,transcoder,params).toBlocking().single();
            if(result==null){ throw new DocumentNotFoundException(key,"Cannot find document using key <"+key+">");}
            else{ return result; }
        }
        catch(DocumentNotFoundException e){ throw e; }
        catch(Throwable e){ throw new DocumentAccessException(key,"Error during document access attempt of <"+key+">",e); }
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncGet(String id,final ICouchbaseTranscoder<T> transcoder){
        id = ICouchbaseBucket.Utils.buildKey(_keyPrefix,id);
        return _bucket.async().get(id,transcoder.documentType()).map(new Func1<BucketDocument<T>, T>() {
            @Override
            public T call(BucketDocument<T> tBucketDocument) {
                return tBucketDocument.content();
            }
        });
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncGet(String id, ICouchbaseTranscoder<T> transcoder, ReadParams params) {
        id = ICouchbaseBucket.Utils.buildKey(_keyPrefix,id);
        Observable<BucketDocument<T>> result;
        switch (params.getReadMode()){
            case FROM_REPLICATE:result=_bucket.async().getFromReplica(id, getReplicat(),transcoder.documentType());break;
            case FROM_MASTER_THEN_REPLICATE:
            {
                final Observable<BucketDocument<T>> fromReplicatResult = _bucket.async().getFromReplica(id, getReplicat(), transcoder.documentType());
                result=_bucket.async().get(id, transcoder.documentType()).onErrorResumeNext(fromReplicatResult);
            }
            break;
            default:
                result=_bucket.async().get(id,transcoder.documentType());
        }
        if(params.getTimeOutUnit()!=null){
            result = result.timeout(params.getTimeOut(),params.getTimeOutUnit());
        }

        return result.map(new Func1<BucketDocument<T>, T>() {
            @Override
            public T call(BucketDocument<T> tBucketDocument) {
                return tBucketDocument.content();
            }
        });
    }

    protected PersistTo getPersistToFromParam(WriteParams params){
        switch(params.getWritePersistMode()) {
            case MASTER:return PersistTo.MASTER;
            case MASTER_AND_ALL_SLAVES:return _persistToAllValue;
            default:return PersistTo.NONE;
        }
    }

    protected ReplicateTo getReplicateToFromParam(WriteParams params){
        switch (params.getWriteReplicateMode()) {
            case ALL_SLAVES:return _replicateToAllValue;
            default:return ReplicateTo.NONE;
        }
    }

    protected <T extends CouchbaseDocument> Observable<T> asyncWritePostProcess(Observable<BucketDocument<T>> obs,final BucketDocument<T> bucketDoc,ICouchbaseTranscoder<T> transcoder,WriteParams params){
        if(params.getTimeOutUnit()!=null){
            obs = obs.timeout(params.getTimeOut(),params.getTimeOutUnit());
        }
        return obs.map(new DocumentResync<T>(bucketDoc));
    }

    protected <T extends CouchbaseDocument> T syncObserverManage(final T doc,Observable<T> obj) throws StorageException{
        try{
            return obj.toBlocking().single();
        }
        catch(RuntimeException e) {
            Throwable rootException =e.getCause();
            if(rootException instanceof InterruptedException){
                throw new DocumentStorageTimeOutException(doc,"Interruption occurs",rootException);
            }
            else if(rootException instanceof CASMismatchException){
                throw new DocumentConcurrentUpdateException(doc,"Concurrent access append",rootException);
            }
            else if(rootException instanceof  DocumentAlreadyExistsException){
                throw new DuplicateDocumentKeyException(doc,rootException);
            }
            else if(rootException instanceof DocumentDoesNotExistException ){
                throw new DocumentNotFoundException(doc,rootException);
            }
            throw new DocumentStorageException(doc,"Error during storage attempt execution",e);
        }
    }


    @Override
    public <T extends CouchbaseDocument> T add(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        return syncObserverManage(doc, asyncAdd(doc, transcoder));
    }

    @Override
    public <T extends CouchbaseDocument> T add(T doc, ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException {
        return syncObserverManage(doc, asyncAdd(doc, transcoder, params));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncAdd(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        final BucketDocument<T> bucketDoc = transcoder.newDocument(doc);
        return _bucket.async().insert(bucketDoc).map(new DocumentResync<T>(bucketDoc));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncAdd(final T doc, ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException {
        final BucketDocument<T> bucketDoc = transcoder.newDocument(doc);
        return asyncWritePostProcess(
                _bucket.async().insert(bucketDoc, getPersistToFromParam(params), getReplicateToFromParam(params)),
                bucketDoc,
                transcoder,
                params);
    }

    @Override
    public <T extends CouchbaseDocument> T set(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        return syncObserverManage(doc, asyncSet(doc, transcoder));
    }

    @Override
    public <T extends CouchbaseDocument> T set(T doc, ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException {
        return syncObserverManage(doc, asyncSet(doc, transcoder, params));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncSet(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        final BucketDocument<T> bucketDoc = transcoder.newDocument(doc);
        return _bucket.async().upsert(bucketDoc).map(new DocumentResync<T>(bucketDoc));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncSet(T doc, ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException {
        final BucketDocument<T> bucketDoc = transcoder.newDocument(doc);
        return asyncWritePostProcess(
                _bucket.async().upsert(bucketDoc, getPersistToFromParam(params), getReplicateToFromParam(params)),
                bucketDoc,
                transcoder,
                params);
    }

    @Override
    public <T extends CouchbaseDocument> T replace(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        return syncObserverManage(doc,asyncReplace(doc,transcoder));
    }

    @Override
    public <T extends CouchbaseDocument> T replace(T doc, ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException {
        return syncObserverManage(doc,asyncReplace(doc,transcoder,params));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncReplace(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        final BucketDocument<T> bucketDoc = transcoder.newDocument(doc);
        return _bucket.async().replace(bucketDoc).map(new DocumentResync<T>(bucketDoc));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncReplace(T doc, ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException {
        final BucketDocument<T> bucketDoc = transcoder.newDocument(doc);
        return asyncWritePostProcess(
                _bucket.async().replace(bucketDoc, getPersistToFromParam(params), getReplicateToFromParam(params)),
                bucketDoc,
                transcoder,
                params);
    }

    @Override
    public <T extends CouchbaseDocument> T delete(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        try {
            T result = asyncDelete(doc,transcoder).toBlocking().single();
            if(result==null){ throw new DocumentNotFoundException(doc,"Cannot apply replace method");}
            else{ return result; }
        }
        catch(DocumentNotFoundException e){ throw e; }
        catch(Throwable e){  throw new DocumentStorageException(doc,"Error during fetch execution",e); }
    }

    @Override
    public <T extends CouchbaseDocument> T delete(T bucketDoc, ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncDelete(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        final BucketDocument<T> bucketDoc = transcoder.newDocument(doc);
        return _bucket.async().remove(bucketDoc).map(new DocumentResync<T>(bucketDoc));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncDelete(T doc, ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException {
        final BucketDocument<T> bucketDoc = transcoder.newDocument(doc);
        return asyncWritePostProcess(
                _bucket.async().remove(bucketDoc, getPersistToFromParam(params), getReplicateToFromParam(params)),
                bucketDoc,
                transcoder,
                params);
    }


    @Override
    public <T extends CouchbaseDocument> T append(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        return syncObserverManage(doc,asyncAppend(doc,transcoder));
    }

    @Override
    public <T extends CouchbaseDocument> T append(T doc, ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException {
        return syncObserverManage(doc,asyncAppend(doc,transcoder,params));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncAppend(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        final BucketDocument<T> bucketDoc = transcoder.newDocument(doc);
        return _bucket.async().append(bucketDoc).map(new DocumentResync<T>(bucketDoc));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncAppend(T doc, ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException {
        final BucketDocument<T> bucketDoc = transcoder.newDocument(doc);
        return asyncWritePostProcess(_bucket.async().append(bucketDoc),
                bucketDoc,
                transcoder,
                params);//.map(new DocumentResync<T>(bucketDoc));
    }

    @Override
    public <T extends CouchbaseDocument> T prepend(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        return  syncObserverManage(doc,asyncPrepend(doc,transcoder));
    }

    @Override
    public <T extends CouchbaseDocument> T prepend(T doc, ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException {
        return  syncObserverManage(doc,asyncPrepend(doc,transcoder,params));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncPrepend(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException{
        final BucketDocument<T> bucketDoc = transcoder.newDocument(doc);
        return _bucket.async().prepend(bucketDoc).map(new DocumentResync<T>(bucketDoc));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncPrepend(T doc, ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException {
        final BucketDocument<T> bucketDoc = transcoder.newDocument(doc);
        return  asyncWritePostProcess(
                _bucket.async().prepend(bucketDoc),
                bucketDoc,
                transcoder,
                params);
    }

    protected Long syncObserverManage(final String key,Observable<Long> obj) throws StorageException{
        try{
            return obj.toBlocking().single();
        }
        catch(RuntimeException e) {
            Throwable rootException =e.getCause();
            if(rootException instanceof InterruptedException){
                throw new DocumentStorageTimeOutException(key,"Interruption occurs",rootException);
            }
            throw new DocumentAccessException(key,"Error during storage attempt execution",e);
        }
    }


    @Override
    public Long counter(String key, Long by, Long defaultValue, Integer expiry) throws StorageException{
        return syncObserverManage(key,asyncCounter(key, by, defaultValue, expiry));
    }

    @Override
    public Long counter(String key, Long by, Long defaultValue, Integer expiration, WriteParams params) throws StorageException {
        return syncObserverManage(key,asyncCounter(key, by, defaultValue, 0,params));
    }

    @Override
    public Observable<Long> asyncCounter(String key, Long by, Long defaultValue, Integer expiry)throws StorageException{
        key = ICouchbaseBucket.Utils.buildKey(_keyPrefix,key);
        return _bucket.async().counter(key, by, defaultValue, expiry).map(new Func1<JsonLongDocument, Long>() {
            @Override
            public Long call(JsonLongDocument jsonLongDocument) {
                return jsonLongDocument.content();
            }
        });
    }

    @Override
    public Observable<Long> asyncCounter(String key, Long by, Long defaultValue, Integer expiration, WriteParams params) throws StorageException {
        key = ICouchbaseBucket.Utils.buildKey(_keyPrefix,key);
        Observable<JsonLongDocument> result = _bucket.async().counter(key, by, defaultValue, expiration);

        if(params.getTimeOutUnit()!=null){
            result = result.timeout(params.getTimeOut(),params.getTimeOutUnit());
        }

        return result.map(new Func1<JsonLongDocument, Long>() {
            @Override
            public Long call(JsonLongDocument jsonLongDocument) {
                return jsonLongDocument.content();
            }
        });
    }

    @Override
    public Long counter(String key, Long by, Long defaultValue) throws StorageException{
        return counter(key,by,defaultValue,0);
    }
    @Override
    public Long counter(String key, Long by, Long defaultValue,WriteParams params) throws StorageException{
        return counter(key,by,defaultValue,0,params);
    }
    @Override
    public Observable<Long> asyncCounter(String key, Long by, Long defaultValue)throws StorageException {
        return asyncCounter(key,by,defaultValue,0);
    }
    @Override
    public Observable<Long> asyncCounter(String key, Long by, Long defaultValue,WriteParams params)throws StorageException {
        return asyncCounter(key,by,defaultValue,0,params);
    }

    @Override
    public Long counter(String key, Long by) throws StorageException{
        return counter(key, by, by);
    }

    @Override
    public Long counter(String key, Long by,WriteParams params) throws StorageException{
        return counter(key, by, by,params);
    }

    @Override
    public Observable<Long> asyncCounter(String key, Long by)throws StorageException {
        return asyncCounter(key,by,by);
    }

    @Override
    public Observable<Long> asyncCounter(String key, Long by,WriteParams params)throws StorageException {
        return asyncCounter(key,by,by,params);
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

    @Override
    public Observable<AsyncViewResult> asyncQuery(ViewQuery query){
        return _bucket.async().query(query);
    }

    @Override
    public ViewResult query(ViewQuery query){
        return _bucket.query(query);
    }

    @Override
    public String getPrefix(){
        return _keyPrefix;
    }




    @Override
    public void createOrUpdateView(String designDoc,Map<String,String> viewsMap) throws StorageException{
        designDoc = ICouchbaseBucket.Utils.buildDesignDoc(_keyPrefix,designDoc);

        DesignDocument existingDesignDocument = _bucket.bucketManager().getDesignDocument(designDoc);
        List<View> viewList = new ArrayList<>();
        for(Map.Entry<String,String> view:viewsMap.entrySet()){
            viewList.add(DefaultView.create(view.getKey(),view.getValue()));
        }
        DesignDocument designDocument = DesignDocument.create(designDoc,viewList);

        _bucket.bucketManager().upsertDesignDocument(designDocument);
    }
}
