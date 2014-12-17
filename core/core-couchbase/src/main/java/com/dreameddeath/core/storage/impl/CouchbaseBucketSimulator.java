package com.dreameddeath.core.storage.impl;

import com.couchbase.client.core.lang.Tuple2;
import com.couchbase.client.core.message.ResponseStatus;
import com.couchbase.client.deps.io.netty.buffer.ByteBuf;
import com.couchbase.client.deps.io.netty.buffer.Unpooled;
import com.couchbase.client.java.document.Document;
import com.couchbase.client.java.error.DocumentAlreadyExistsException;
import com.dreameddeath.core.exception.storage.DocumentAccessException;
import com.dreameddeath.core.exception.storage.DocumentNotFoundException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.storage.BucketDocument;
import com.dreameddeath.core.storage.ICouchbaseBucket;
import com.dreameddeath.core.storage.ICouchbaseTranscoder;
import com.dreameddeath.core.storage.impl.simulator.DocumentSimulator;
import rx.Observable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by CEAJ8230 on 24/11/2014.
 */
public class CouchbaseBucketSimulator extends CouchbaseBucketWrapper {

    public CouchbaseBucketSimulator(String bucketName){
        super(null,bucketName,null);
    }

    public CouchbaseBucketSimulator(String bucketName,String prefix){
        super(null,bucketName,null,prefix);
    }

    private Map<String,DocumentSimulator> _dbContent = new ConcurrentHashMap<String,DocumentSimulator>();

    public Map<Class,ICouchbaseTranscoder> _transcoderMap = new HashMap<Class,ICouchbaseTranscoder>();

    @Override
    public void start(long timeout,TimeUnit unit){}
    @Override
    public void start(){}

    @Override
    public boolean shutdown(long timeout,TimeUnit unit){return true; }
    @Override
    public void shutdown(){}




    @Override
    public ICouchbaseBucket addTranscoder(ICouchbaseTranscoder transcoder) {
        super.addTranscoder(transcoder);
        _transcoderMap.put(transcoder.documentType(),transcoder);
        return this;
    }

    public Long updateCacheCounter(String key,Long by,Long defaultValue,Integer expiration) throws StorageException{
        DocumentSimulator foundDoc = _dbContent.get(key);
        if(foundDoc==null){
            if(defaultValue!=null){
                foundDoc = new DocumentSimulator();
                foundDoc.setFlags(0);
                foundDoc.setCas(0);
                foundDoc.setExpiry(expiration);
                foundDoc.setKey(key);
                foundDoc.setData(Unpooled.wrappedBuffer(defaultValue.toString().getBytes()));
                _dbContent.put(foundDoc.getKey(),foundDoc);
                return defaultValue;
            }
            else{
                throw new DocumentNotFoundException(key,"Not found in couchbase simulator");
            }
        }
        try {
            Long result = Long.parseLong(foundDoc.getData().toString());
            result+=by;
            foundDoc.setData(Unpooled.wrappedBuffer(result.toString().getBytes()));
            return result;
        }
        catch(NumberFormatException e){
            throw new DocumentAccessException(key,"Error during document access attempt of <"+key+">",e);
        }
    }

    public Document getFromCache(String key,Class docType) throws StorageException{
        ICouchbaseTranscoder transcoder = _transcoderMap.get(docType);
        DocumentSimulator foundDoc = _dbContent.get(key);
        if(foundDoc==null){
            throw new DocumentNotFoundException(key,"Not found in couchbase simulator");
        }
        if(transcoder==null){
            throw new DocumentAccessException(key,"Error during document access attempt of <"+key+">");
        }

        return transcoder.decode(foundDoc.getKey(),foundDoc.getData(),foundDoc.getCas(),foundDoc.getExpiry(),foundDoc.getFlags(), ResponseStatus.SUCCESS);
    }

    public enum ImpactMode {
        ADD,
        UPDATE,
        REPLACE
    }

    public <T extends CouchbaseDocument> Document<T> addOrReplaceCache(BucketDocument<T> bucketDoc,Class docType,ImpactMode mode,int expiry) throws StorageException{
        //T doc = bucketDoc.getDocument();
        ICouchbaseTranscoder transcoder = _transcoderMap.get(docType);
        DocumentSimulator foundDoc = _dbContent.get(bucketDoc.id());
        if((foundDoc==null) && mode.equals(ImpactMode.REPLACE)){
            throw new DocumentNotFoundException(bucketDoc.id(),"Key <"+bucketDoc.id()+"> already existing in couchbase simulator");
        }
        if((foundDoc!=null) && mode.equals(ImpactMode.ADD)){
            throw new DocumentAlreadyExistsException("Key <"+bucketDoc.id()+"> already existing in couchbase simulator");
        }
        if(foundDoc==null){
            foundDoc = new DocumentSimulator();
            foundDoc.setKey(bucketDoc.id());
            foundDoc.setCas(0L);
            _dbContent.put(foundDoc.getKey(),foundDoc);
        }

        if(transcoder==null){
            throw new DocumentAccessException(bucketDoc.id(),"Error during document access attempt of <"+bucketDoc.id()+">");
        }

        Tuple2<ByteBuf, Integer> encodedResult = transcoder.encode(transcoder.newDocument(bucketDoc.content()));
        foundDoc.setExpiry(expiry);
        foundDoc.setCas(foundDoc.getCas()+1);
        foundDoc.setData(encodedResult.value1());
        foundDoc.setFlags(encodedResult.value2());

        return getFromCache(bucketDoc.id(),docType);
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncGet(String id, ICouchbaseTranscoder<T> transcoder) {
        try{
            if(_keyPrefix!=null){id = _keyPrefix+id;}
            return Observable.just((T)(getFromCache(id,transcoder.documentType()).content()));
        }
        catch(Throwable e){
            return Observable.error(e);
        }
    }


    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncAdd(T doc, ICouchbaseTranscoder<T> transcoder) throws StorageException {
        try{
            final BucketDocument<T> bucketDoc = transcoder.newDocument(doc);
            return (Observable.just((BucketDocument<T>) addOrReplaceCache(bucketDoc,transcoder.documentType(), ImpactMode.ADD,0)).map(new DocumentResync(bucketDoc)));
        }
        catch(Exception e){
            return Observable.error(e);
        }
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncSet(T doc, ICouchbaseTranscoder<T> transcoder) throws StorageException {
        try{
            final BucketDocument<T> bucketDoc = transcoder.newDocument(doc);
            return (Observable<T>)(Observable.just((BucketDocument<T>) addOrReplaceCache(bucketDoc,transcoder.documentType(), ImpactMode.UPDATE,0)).map(new DocumentResync(bucketDoc)));
        }
        catch(Exception e){
            return Observable.error(e);
        }
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncReplace(T doc, ICouchbaseTranscoder<T> transcoder) throws StorageException {
        try{
            final BucketDocument<T> bucketDoc = transcoder.newDocument(doc);
            return (Observable<T>)(Observable.just((BucketDocument<T>)addOrReplaceCache(bucketDoc,transcoder.documentType(), ImpactMode.REPLACE,0)).map(new DocumentResync(bucketDoc)));
        }
        catch(Exception e){
            return Observable.error(e);
        }
    }


    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncDelete(T doc, ICouchbaseTranscoder<T> transcoder) throws StorageException {
        return null;
    }


    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncAppend(T doc, ICouchbaseTranscoder<T> transcoder) throws StorageException {
        return null;
    }


    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncPrepend(T doc, ICouchbaseTranscoder<T> transcoder) throws StorageException {
        return null;
    }


    @Override
    public Observable<Long> asyncCounter(String key, Long by, Long defaultValue, Integer expiration) throws StorageException {
        try{
            if(_keyPrefix!=null){key = _keyPrefix+key;}
            return Observable.just(updateCacheCounter(key,by,defaultValue,expiration));
        }
        catch(Exception e){
             return Observable.error(e);
        }
    }





}
