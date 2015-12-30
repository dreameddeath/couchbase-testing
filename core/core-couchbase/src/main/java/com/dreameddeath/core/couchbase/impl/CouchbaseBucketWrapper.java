/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.couchbase.impl;


import com.codahale.metrics.MetricRegistry;
import com.couchbase.client.java.*;
import com.couchbase.client.java.document.Document;
import com.couchbase.client.java.document.JsonLongDocument;
import com.couchbase.client.java.transcoder.Transcoder;
import com.couchbase.client.java.view.*;
import com.dreameddeath.core.config.exception.ConfigPropertyValueNotFoundException;
import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.ICouchbaseTranscoder;
import com.dreameddeath.core.couchbase.config.CouchbaseConfigProperties;
import com.dreameddeath.core.couchbase.exception.*;
import com.dreameddeath.core.couchbase.metrics.CouchbaseMetricsContext;
import com.dreameddeath.core.couchbase.utils.CouchbaseUtils;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.entity.EntityDefinitionManager;
import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.model.transcoder.ITranscoder;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import rx.Observable;
import rx.functions.Func1;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.TimeUnit;

//import java.util.concurrent.Future;

/**
*  Class used to perform storage 
*/
public class CouchbaseBucketWrapper implements ICouchbaseBucket {
    private Bucket bucket;
    private int replicatCount;
    private Random replicateRandom = new Random();
    private PersistTo persistToAllValue;
    private ReplicateTo replicateToAllValue;
    private final Cluster cluster;
    private final String bucketName;
    private final String bucketPassword;
    protected final CouchbaseMetricsContext getContext;
    protected final CouchbaseMetricsContext updateContext;
    protected final CouchbaseMetricsContext createContext;
    protected final CouchbaseMetricsContext deleteContext;
    protected final CouchbaseMetricsContext deltaContext;
    protected final CouchbaseMetricsContext counterContext;

    private final List<Transcoder<? extends Document, ?>> transcoders = new ArrayList<>();
    private final Map<Class<? extends CouchbaseDocument>,ICouchbaseTranscoder<?>> transcoderMap =new HashMap<>();

    protected List<Transcoder<? extends Document,?>> getTranscoders(){
        return Collections.unmodifiableList(transcoders);
    }

    protected <T extends CouchbaseDocument> ICouchbaseTranscoder<T> getTranscoder(Class<T> clazz){
        ICouchbaseTranscoder<T> transcoder =  (ICouchbaseTranscoder<T>)transcoderMap.get(clazz);
        if(transcoder==null){
            synchronized (transcoderMap){
                transcoder = (ICouchbaseTranscoder<T>)transcoderMap.computeIfAbsent(clazz,
                        aClass->
                                CouchbaseDocument.class.isAssignableFrom(aClass.getSuperclass())?getTranscoder((Class<? extends CouchbaseDocument>)aClass.getSuperclass()):null
                );
            }
        }
        return transcoder;
    }

    protected <T extends CouchbaseDocument> BucketDocument<T> buildBucketDocument(T document){
        ICouchbaseTranscoder<T> transcoder =  getTranscoder((Class<T>)document.getClass());
        if(transcoder==null){
            throw new RuntimeException("Cannot find transcoder class for class "+document.getClass());
        }
        return transcoder.newDocument(document);
    }

    protected <T extends CouchbaseDocument> BucketDocument<T> buildBucketDocument(T document,String keyPrefix){
        BucketDocument<T> result =buildBucketDocument(document);
        result.setKeyPrefix(keyPrefix);
        return result;
    }

    protected void buildTranscoders(String bucketName){
        EntityDefinitionManager definitionManager = new EntityDefinitionManager();
        //TODO define per bucket name list of entities to load
        for(EntityDef entityDef :definitionManager.getEntities()){
            try {
                CouchbaseDocumentReflection entityDocInfo = CouchbaseDocumentReflection.getClassInfo(entityDef.getClassName());
                if(entityDocInfo==null) {
                    continue;
                }
                CouchbaseUtils.ApplicableBucketDocumentInfo bucketDocumentInfo=CouchbaseUtils.resolveBucketDocumentForClass(entityDocInfo.getClassInfo().getCurrentClass());
                if(bucketDocumentInfo==null){
                    throw new RuntimeException("Cannot find bucket document for class "+entityDef.getClassName());
                }
                //it is a bucket info for parent entity
                else if(!bucketDocumentInfo.getEffectiveCouchbaseDocumentclass().equals(entityDocInfo)){
                    continue;
                }
                ITranscoder transcoder = CouchbaseUtils.resolveTranscoderForClass(entityDocInfo.getClassInfo().getCurrentClass());

                Class<? extends BucketDocument> bucketDocument = bucketDocumentInfo.getBucketDocumentClass().getCurrentClass();
                String cbTranscoderClassName = CouchbaseConfigProperties.COUCHBASE_CBTRANSCODER_CLASS_NAME.getProperty(entityDef.getModelId().getDomain(), entityDef.getModelId().getName()).getMandatoryValue("Please define couchbase CB Transcoder");
                Class<? extends ICouchbaseTranscoder> cbTranscoderClass = (Class<? extends ICouchbaseTranscoder>)this.getClass().getClassLoader().loadClass(cbTranscoderClassName);
                ICouchbaseTranscoder cbTranscoder = cbTranscoderClass.getConstructor(ITranscoder.class,Class.class).newInstance(transcoder, bucketDocument);
                transcoders.add(cbTranscoder);
                transcoderMap.put(entityDocInfo.getClassInfo().getCurrentClass(),cbTranscoder);
            }
            catch(ConfigPropertyValueNotFoundException|NoSuchMethodException|ClassNotFoundException|InstantiationException|IllegalAccessException|InvocationTargetException|TranscoderNotFoundException e){
                throw new RuntimeException(e);
            }
        }
    }


    public CouchbaseBucketWrapper(CouchbaseCluster cluster, String bucketName, String bucketPassword){
        this(cluster,bucketName,bucketPassword,null);
    }


    public CouchbaseBucketWrapper(CouchbaseCluster cluster, String bucketName, String bucketPassword, MetricRegistry registry){
        this.cluster = cluster;
        this.bucketName = bucketName;
        this.bucketPassword = bucketPassword;
        getContext = new CouchbaseMetricsContext("CouchbaseWrapper=\""+bucketName+"\" Operation=\"GET\"", registry);
        updateContext = new CouchbaseMetricsContext("CouchbaseWrapper=\""+bucketName+"\" Operation=\"UPDATE\"", registry);
        createContext = new CouchbaseMetricsContext("CouchbaseWrapper=\""+bucketName+"\" Operation=\"CREATE\"", registry);
        deleteContext = new CouchbaseMetricsContext("CouchbaseWrapper=\""+bucketName+"\" Operation=\"DELETE\"", registry);
        deltaContext = new CouchbaseMetricsContext("CouchbaseWrapper=\""+bucketName+"\" Operation=\"DELTA\"", registry);
        counterContext = new CouchbaseMetricsContext("CouchbaseWrapper=\""+bucketName+"\" Operation=\"COUNTER\"", registry);
        buildTranscoders(bucketName);
    }


    public Bucket getInternalBucket(){
        return bucket;
    }

    @Override
    public void start(long timeout,TimeUnit unit){
        bucket = cluster.openBucket(bucketName,bucketPassword,transcoders,timeout,unit);
        initReplicatInfo();
    }

    @Override
    public void start(){
        bucket = cluster.openBucket(bucketName,bucketPassword,transcoders);
        initReplicatInfo();
    }

    @Override
    public boolean isStarted() {
        return bucket!=null;
    }

    @Override
    public boolean shutdown(long timeout,TimeUnit unit){
        if(bucket!=null) {
            boolean result = bucket.close(timeout, unit);
            if (result) {
                bucket = null;
            }
            return result;
        }
        else{
            return false;
        }
    }

    @Override
    public void shutdown(){
        if(bucket!=null) {
            bucket.close();
            bucket = null;
        }
        getContext.close();
        updateContext.close();
        deleteContext.close();
        createContext.close();
        deltaContext.close();
        counterContext.close();
    }

    @Override
    public String getBucketName(){
        return bucketName;
    }

    public void initReplicatInfo(){
        replicatCount = bucket.bucketManager().info().replicaCount();
        switch (replicatCount){
            case 0:
                persistToAllValue = PersistTo.MASTER;
                replicateToAllValue = ReplicateTo.NONE;
                break;
            case 1:
                persistToAllValue = PersistTo.ONE;
                replicateToAllValue = ReplicateTo.ONE;
                break;
            case 2:
                persistToAllValue = PersistTo.TWO;
                replicateToAllValue = ReplicateTo.TWO;
                break;
            case 3:
                persistToAllValue = PersistTo.THREE;
                replicateToAllValue = ReplicateTo.THREE;
                break;
        }
    }

    public ReplicaMode getReplicat(){
        switch(replicateRandom.nextInt(replicatCount)){
            case 0:return ReplicaMode.FIRST;
            case 1:return ReplicaMode.SECOND;
            case 2:return ReplicaMode.THIRD;
        }
        return ReplicaMode.ALL;
    }



    @Override
    public ICouchbaseBucket addTranscoder(ICouchbaseTranscoder transcoder){
        if((bucket!=null) && (transcoders.contains(transcoder))){
            throw new IllegalStateException("Cannot add transcoder "+transcoder.getClass().getName()+" after client initialization");
        }
        transcoders.add(transcoder);
        transcoderMap.put(transcoder.getTranscoder().getBaseClass(),transcoder);
        return this;
    }

    public Bucket getBucket(){
        return bucket;
    }

    @Override
    public <T extends CouchbaseDocument> T get(String key,Class<T> entity) throws StorageException {
        try{
            return asyncGet(key,entity).toBlocking().first();
        }
        catch(Throwable e){
            throw ICouchbaseBucket.Utils.mapAccessException(key,e);
        }
    }

    @Override
    public <T extends CouchbaseDocument> T get(String key, Class<T> entity,ReadParams params) throws StorageException {
        try{
            return asyncGet(key,entity,params).toBlocking().single();
        }
        catch(Throwable e){
            throw ICouchbaseBucket.Utils.mapAccessException(key,e);
        }
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncGet(final String id,Class<T> entity){
        CouchbaseMetricsContext.MetricsContext mCtxt = getContext.start();
        return bucket.async().get(id,getTranscoder(entity).documentType())
                .doOnEach(mCtxt::stop)
                .doOnError(mCtxt::stop)
                .map(BucketDocument::content)
                .map(val-> {
                    if (val == null) {
                        throw new StorageObservableException(new DocumentNotFoundException(id, "Cannot find document using key <" + id + ">"));
                    }
                    return val;
                });
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncGet(String oldId,Class<T>entity, ReadParams params) {
        final String id = ICouchbaseBucket.Utils.buildKey(params.getKeyPrefix(),oldId);
        Observable<BucketDocument<T>> result;
        CouchbaseMetricsContext.MetricsContext mCtxt = getContext.start();
        switch (params.getReadMode()){
            case FROM_REPLICATE:result=bucket.async().getFromReplica(id, getReplicat(),getTranscoder(entity).documentType());break;
            case FROM_MASTER_THEN_REPLICATE:
            {
                final Observable<BucketDocument<T>> fromReplicatResult = bucket.async().getFromReplica(id, getReplicat(), getTranscoder(entity).documentType());
                result=bucket.async().get(id, getTranscoder(entity).documentType()).onErrorResumeNext(fromReplicatResult);
            }
            break;
            default:
                result=bucket.async().get(id,getTranscoder(entity).documentType());
        }
        if(params.getTimeOutUnit()!=null){
            result = result.timeout(params.getTimeOut(),params.getTimeOutUnit());
        }
        if(params.getKeyPrefix()!=null){
            result.map(doc->doc.withKeyPrefix(params.getKeyPrefix()));
        }

        return result.doOnEach(mCtxt::stop)
                .doOnError(mCtxt::stop)
                .map(BucketDocument::content)
                .map(val-> {
                    if (val == null) {
                        throw new StorageObservableException(new DocumentNotFoundException(id, "Cannot find document using key <" + id + ">"));
                    }
                    return val;
                });
    }

    protected PersistTo getPersistToFromParam(WriteParams params){
        switch(params.getWritePersistMode()) {
            case MASTER:return PersistTo.MASTER;
            case MASTER_AND_ALL_SLAVES:return persistToAllValue;
            default:return PersistTo.NONE;
        }
    }

    protected ReplicateTo getReplicateToFromParam(WriteParams params){
        switch (params.getWriteReplicateMode()) {
            case ALL_SLAVES:return replicateToAllValue;
            default:return ReplicateTo.NONE;
        }
    }

    protected <T extends CouchbaseDocument> Observable<T> asyncWritePostProcess(Observable<BucketDocument<T>> obs, CouchbaseMetricsContext.MetricsContext mCtxt, final BucketDocument<T> bucketDoc, WriteParams params){
        if(params.getTimeOutUnit()!=null){
            obs = obs.timeout(params.getTimeOut(),params.getTimeOutUnit());
        }
        return obs.doOnEach(mCtxt::stop)
                .doOnError(mCtxt::stop)
                .map(new DocumentResync<>(bucketDoc));
    }

    protected <T extends CouchbaseDocument> T syncDocumentObserverManage(final T doc, Observable<T> obj) throws StorageException{
        try{
            return obj.toBlocking().single();
        }
        catch(Throwable e) {
            throw ICouchbaseBucket.Utils.mapStorageException(doc,e);
        }
    }


    @Override
    public <T extends CouchbaseDocument> T add(final T doc) throws StorageException{
        return syncDocumentObserverManage(doc, asyncAdd(doc));
    }

    @Override
    public <T extends CouchbaseDocument> T add(T doc , WriteParams params) throws StorageException {
        return syncDocumentObserverManage(doc, asyncAdd(doc, params));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncAdd(final T doc){
        BucketDocument<T> bucketDoc = buildBucketDocument(doc);
        CouchbaseMetricsContext.MetricsContext mCtxt = createContext.start();
        return bucket.async().insert(bucketDoc).doOnEach(mCtxt::stop)
                .doOnError(mCtxt::stop)
                .map(new DocumentResync<>(bucketDoc));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncAdd(final T doc , WriteParams params) {
        final BucketDocument<T> bucketDoc = buildBucketDocument(doc,params.getKeyPrefix());
        CouchbaseMetricsContext.MetricsContext mCtxt = createContext.start();
        return asyncWritePostProcess(
                bucket.async().insert(bucketDoc, getPersistToFromParam(params), getReplicateToFromParam(params)),
                mCtxt,
                bucketDoc,
                params);
    }

    @Override
    public <T extends CouchbaseDocument> T set(final T doc) throws StorageException{
        return syncDocumentObserverManage(doc, asyncSet(doc));
    }

    @Override
    public <T extends CouchbaseDocument> T set(T doc, WriteParams params) throws StorageException {
        return syncDocumentObserverManage(doc, asyncSet(doc, params));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncSet(final T doc){
        final BucketDocument<T> bucketDoc = buildBucketDocument(doc);
        CouchbaseMetricsContext.MetricsContext mCtxt = updateContext.start();
        return bucket.async().upsert(bucketDoc)
                .doOnEach(mCtxt::stop)
                .doOnError(mCtxt::stop)
                .map(new DocumentResync<>(bucketDoc));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncSet(T doc, WriteParams params) {
        final BucketDocument<T> bucketDoc = buildBucketDocument(doc,params.getKeyPrefix());
        CouchbaseMetricsContext.MetricsContext mCtxt = updateContext.start();
        return asyncWritePostProcess(
                bucket.async().upsert(bucketDoc, getPersistToFromParam(params), getReplicateToFromParam(params)),
                mCtxt,
                bucketDoc,
                params);
    }

    @Override
    public <T extends CouchbaseDocument> T replace(final T doc) throws StorageException{
        return syncDocumentObserverManage(doc,asyncReplace(doc));
    }

    @Override
    public <T extends CouchbaseDocument> T replace(T doc, WriteParams params) throws StorageException {
        return syncDocumentObserverManage(doc, asyncReplace(doc, params));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncReplace(final T doc){
        final BucketDocument<T> bucketDoc = buildBucketDocument(doc);
        CouchbaseMetricsContext.MetricsContext mCtxt = updateContext.start();
        return bucket.async().replace(bucketDoc)
                .doOnEach(mCtxt::stop)
                .doOnError(mCtxt::stop)
                .map(new DocumentResync<>(bucketDoc));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncReplace(T doc, WriteParams params) {
        final BucketDocument<T> bucketDoc = buildBucketDocument(doc,params.getKeyPrefix());
        CouchbaseMetricsContext.MetricsContext mCtxt = updateContext.start();
        return asyncWritePostProcess(
                bucket.async().replace(bucketDoc, getPersistToFromParam(params), getReplicateToFromParam(params)),
                mCtxt,
                bucketDoc,
                params);
    }

    @Override
    public <T extends CouchbaseDocument> T delete(final T doc) throws StorageException{
        try {
            T result = asyncDelete(doc).toBlocking().single();
            if(result==null){ throw new DocumentNotFoundException(doc,"Cannot apply delete method");}
            else{ return result; }
        }
        catch(DocumentNotFoundException e){ throw e; }
        catch(Throwable e){  throw new DocumentStorageException(doc,"Error during delete execution",e); }
    }

    @Override
    public <T extends CouchbaseDocument> T delete(T doc, WriteParams params) throws StorageException {
        try {
            T result = asyncDelete(doc,params).toBlocking().single();
            if(result==null){ throw new DocumentNotFoundException(doc,"Cannot apply replace method");}
            else{ return result; }
        }
        catch(DocumentNotFoundException e){ throw e; }
        catch(Throwable e){  throw new DocumentStorageException(doc,"Error during fetch execution",e); }
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncDelete(final T doc){
        final BucketDocument<T> bucketDoc = buildBucketDocument(doc);
        CouchbaseMetricsContext.MetricsContext mCtxt = deleteContext.start();
        return bucket.async().remove(bucketDoc)
                .doOnEach(mCtxt::stop)
                .doOnError(mCtxt::stop)
                .map(new DocumentResync<>(bucketDoc));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncDelete(T doc, WriteParams params){
        final BucketDocument<T> bucketDoc = buildBucketDocument(doc,params.getKeyPrefix());
        CouchbaseMetricsContext.MetricsContext mCtxt = deleteContext.start();
        return asyncWritePostProcess(
                bucket.async().remove(bucketDoc, getPersistToFromParam(params), getReplicateToFromParam(params)),
                mCtxt,
                bucketDoc,
                params);
    }


    @Override
    public <T extends CouchbaseDocument> T append(final T doc) throws StorageException{
        return syncDocumentObserverManage(doc,asyncAppend(doc));
    }

    @Override
    public <T extends CouchbaseDocument> T append(T doc, WriteParams params) throws StorageException {
        return syncDocumentObserverManage(doc, asyncAppend(doc, params));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncAppend(final T doc){
        final BucketDocument<T> bucketDoc = buildBucketDocument(doc);
        CouchbaseMetricsContext.MetricsContext mCtxt = deltaContext.start();
        return bucket.async().append(bucketDoc)
                .doOnEach(mCtxt::stop)
                .doOnError(mCtxt::stop)
                .map(new DocumentResync<>(bucketDoc));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncAppend(T doc, WriteParams params) {
        final BucketDocument<T> bucketDoc = buildBucketDocument(doc,params.getKeyPrefix());
        CouchbaseMetricsContext.MetricsContext mCtxt = deltaContext.start();
        return asyncWritePostProcess(bucket.async().append(bucketDoc),
                mCtxt,
                bucketDoc,
                params);
    }

    @Override
    public <T extends CouchbaseDocument> T prepend(final T doc) throws StorageException{
        return  syncDocumentObserverManage(doc,asyncPrepend(doc));
    }

    @Override
    public <T extends CouchbaseDocument> T prepend(T doc, WriteParams params) throws StorageException {
        return  syncDocumentObserverManage(doc, asyncPrepend(doc, params));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncPrepend(final T doc){
        final BucketDocument<T> bucketDoc = buildBucketDocument(doc);
        CouchbaseMetricsContext.MetricsContext mCtxt = deltaContext.start();
        return bucket.async().prepend(bucketDoc)
                .doOnEach(mCtxt::stop)
                .doOnError(mCtxt::stop)
                .map(new DocumentResync<>(bucketDoc));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncPrepend(T doc, WriteParams params){
        final BucketDocument<T> bucketDoc = buildBucketDocument(doc,params.getKeyPrefix());
        CouchbaseMetricsContext.MetricsContext mCtxt = deltaContext.start();
        return  asyncWritePostProcess(
                bucket.async().prepend(bucketDoc),
                mCtxt,
                bucketDoc,
                params);
    }

    protected Long syncCounterObserverManage(final String key, Observable<Long> obj) throws StorageException{
        try{
            return obj.toBlocking().single();
        }
        catch(Throwable e) {
            throw ICouchbaseBucket.Utils.mapStorageException(key,e);
        }
    }


    @Override
    public Long counter(String key, Long by, Long defaultValue, Integer expiry) throws StorageException{
        return syncCounterObserverManage(key,asyncCounter(key, by, defaultValue, expiry));
    }

    @Override
    public Long counter(String key, Long by, Long defaultValue, Integer expiration, WriteParams params) throws StorageException {
        return syncCounterObserverManage(key, asyncCounter(key, by, defaultValue, 0, params));
    }

    @Override
    public Observable<Long> asyncCounter(String key, Long by, Long defaultValue, Integer expiry){
        CouchbaseMetricsContext.MetricsContext mCtxt = counterContext.start();
        return bucket.async().counter(key, by, defaultValue, expiry).doOnEach(mCtxt::stopCounter)
                .doOnError(mCtxt::stop)
                .map(JsonLongDocument::content);
    }

    @Override
    public Observable<Long> asyncCounter(String key, Long by, Long defaultValue, Integer expiration, WriteParams params) {
        CouchbaseMetricsContext.MetricsContext mCtxt = counterContext.start();

        key = ICouchbaseBucket.Utils.buildKey(params.getKeyPrefix(),key);
        Observable<JsonLongDocument> result = bucket.async().counter(key, by, defaultValue, expiration);

        if(params.getTimeOutUnit()!=null){
            result = result.timeout(params.getTimeOut(),params.getTimeOutUnit());
        }

        return result.doOnEach(mCtxt::stopCounter)
                .doOnError(mCtxt::stop)
                .map(JsonLongDocument::content);
    }

    @Override
    public Long counter(String key, Long by, Long defaultValue) throws StorageException{
        return counter(key,by,defaultValue,0);
    }
    @Override
    public Long counter(String key, Long by, Long defaultValue,WriteParams params) throws StorageException{
        return counter(key, by, defaultValue, 0, params);
    }
    @Override
    public Observable<Long> asyncCounter(String key, Long by, Long defaultValue) {
        return asyncCounter(key,by,defaultValue,0);
    }
    @Override
    public Observable<Long> asyncCounter(String key, Long by, Long defaultValue,WriteParams params) {
        return asyncCounter(key, by, defaultValue, 0, params);
    }

    @Override
    public Long counter(String key, Long by) throws StorageException{
        return counter(key, by, by);
    }

    @Override
    public Long counter(String key, Long by,WriteParams params) throws StorageException{
        return counter(key, by, by, params);
    }

    @Override
    public Observable<Long> asyncCounter(String key, Long by) {
        return asyncCounter(key,by,by);
    }

    @Override
    public Observable<Long> asyncCounter(String key, Long by,WriteParams params){
        return asyncCounter(key, by, by, params);
    }

    public class DocumentResync<T extends CouchbaseDocument> implements Func1<BucketDocument<T>, T>{
        private final BucketDocument<T> bucketDoc;

        public DocumentResync(final BucketDocument<T> doc){
            bucketDoc = doc;
        }

        @Override
        public T call(BucketDocument<T> tBucketDocument) {
            bucketDoc.syncMeta(tBucketDocument);
            return bucketDoc.content();
        }
    }

    @Override
    public Observable<AsyncViewResult> asyncQuery(ViewQuery query){
        return bucket.async().query(query);
    }

    @Override
    public ViewResult query(ViewQuery query){
        return bucket.query(query);
    }

    @Override
    public void createOrUpdateView(String designDoc,Map<String,String> viewsMap) throws StorageException{
        ///TODO check if already up to date
        List<View> viewList = new ArrayList<>();
        for(Map.Entry<String,String> view:viewsMap.entrySet()){
            viewList.add(DefaultView.create(view.getKey(),view.getValue()));
        }
        DesignDocument designDocument = DesignDocument.create(designDoc,viewList);

        bucket.bucketManager().upsertDesignDocument(designDocument);
    }
}
