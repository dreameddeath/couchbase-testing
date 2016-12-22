/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
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
import com.dreameddeath.core.couchbase.IBlockingCouchbaseBucket;
import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.ICouchbaseTranscoder;
import com.dreameddeath.core.couchbase.config.CouchbaseConfigProperties;
import com.dreameddeath.core.couchbase.exception.DocumentNotFoundException;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.couchbase.exception.StorageObservableException;
import com.dreameddeath.core.couchbase.exception.TranscoderNotFoundException;
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
    private final IBlockingCouchbaseBucket blockingWrapper;

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

    protected BlockingCouchbaseBucketWrapper createBlockingSimulatorWrapper(){
        return new BlockingCouchbaseBucketWrapper(this);
    }

    public CouchbaseBucketWrapper(CouchbaseCluster cluster, String bucketName, String bucketPassword){
        this(cluster, bucketName, bucketPassword,null);
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
        //TODO manage views
        buildTranscoders(bucketName);
        this.blockingWrapper = createBlockingSimulatorWrapper();
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
        if((bucket!=null) && (!transcoders.contains(transcoder))){
            throw new IllegalStateException("Cannot add transcoder "+transcoder.getClass().getName()+" after client initialization");
        }
        if(bucket==null) {
            transcoders.add(transcoder);
            transcoderMap.put(transcoder.getTranscoder().getBaseClass(), transcoder);
        }
        return this;
    }

    @Override
    public IBlockingCouchbaseBucket toBlocking() {
        return this.blockingWrapper;
    }

    public Bucket getBucket(){
        return bucket;
    }



    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncGet(final String id,Class<T> entity){
        CouchbaseMetricsContext.MetricsContext mCtxt = getContext.start();
        return bucket.async().get(id,getTranscoder(entity).documentType())
                .doOnEach(notif->mCtxt.stop(notif))
                .doOnError(mCtxt::stopWithError)
                .map(BucketDocument::content)
                .onErrorResumeNext(throwable-> ICouchbaseBucket.Utils.mapObservableAccessException(id,throwable,entity))
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

        return result.doOnEach(notif->mCtxt.stop(notif))
                .doOnError(mCtxt::stopWithError)
                .map(BucketDocument::content)
                .onErrorResumeNext(throwable-> ICouchbaseBucket.Utils.mapObservableAccessException(id,throwable,entity))
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
        final ICouchbaseTranscoder<T> transcoder = getTranscoder((Class<T>)bucketDoc.getDocument().getClass());
        return obs.doOnEach(notif->mCtxt.stop(notif))
                .doOnError(mCtxt::stopWithError)
                .map(new DocumentResync<>(bucketDoc,transcoder))
                .onErrorResumeNext(throwable -> ICouchbaseBucket.Utils.mapObservableStorageException(bucketDoc.getDocument(),throwable));
    }




    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncAdd(final T doc){
        BucketDocument<T> bucketDoc = buildBucketDocument(doc);
        CouchbaseMetricsContext.MetricsContext mCtxt = createContext.start();
        final ICouchbaseTranscoder<T> transcoder = getTranscoder((Class<T>)doc.getClass());
        return bucket.async().insert(bucketDoc)
                .doOnEach(notif->mCtxt.stop(notif))
                .doOnError(mCtxt::stopWithError)
                .map(new DocumentResync<>(bucketDoc,transcoder))
                .onErrorResumeNext(throwable -> ICouchbaseBucket.Utils.mapObservableStorageException(doc,throwable));
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
    public <T extends CouchbaseDocument> Observable<T> asyncSet(final T doc){
        final BucketDocument<T> bucketDoc = buildBucketDocument(doc);
        final CouchbaseMetricsContext.MetricsContext mCtxt = updateContext.start();
        final ICouchbaseTranscoder<T> transcoder = getTranscoder((Class<T>)doc.getClass());
        return bucket.async().upsert(bucketDoc)
                .doOnEach(notif->mCtxt.stop(notif))
                .doOnError(mCtxt::stopWithError)
                .map(new DocumentResync<>(bucketDoc,transcoder))
                .onErrorResumeNext(throwable-> ICouchbaseBucket.Utils.mapObservableStorageException(doc,throwable));
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
    public <T extends CouchbaseDocument> Observable<T> asyncReplace(final T doc){
        final BucketDocument<T> bucketDoc = buildBucketDocument(doc);
        CouchbaseMetricsContext.MetricsContext mCtxt = updateContext.start();
        final ICouchbaseTranscoder<T> transcoder = getTranscoder((Class<T>)doc.getClass());
        return bucket.async().replace(bucketDoc)
                .doOnEach(notif->mCtxt.stop(notif))
                .doOnError(mCtxt::stopWithError)
                .map(new DocumentResync<>(bucketDoc,transcoder))
                .onErrorResumeNext(throwable-> ICouchbaseBucket.Utils.mapObservableStorageException(doc,throwable));
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
    public <T extends CouchbaseDocument> Observable<T> asyncDelete(final T doc){
        final BucketDocument<T> bucketDoc = buildBucketDocument(doc);
        CouchbaseMetricsContext.MetricsContext mCtxt = deleteContext.start();
        final ICouchbaseTranscoder<T> transcoder = getTranscoder((Class<T>)doc.getClass());
        return bucket.async().remove(bucketDoc)
                .doOnEach(notif->mCtxt.stop(notif))
                .doOnError(mCtxt::stopWithError)
                .map(new DocumentResync<>(bucketDoc,transcoder))
                .onErrorResumeNext(throwable-> ICouchbaseBucket.Utils.mapObservableStorageException(doc,throwable));
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
    public <T extends CouchbaseDocument> Observable<T> asyncAppend(final T doc){
        final BucketDocument<T> bucketDoc = buildBucketDocument(doc);
        CouchbaseMetricsContext.MetricsContext mCtxt = deltaContext.start();
        final ICouchbaseTranscoder<T> transcoder = getTranscoder((Class<T>)doc.getClass());
        return bucket.async().append(bucketDoc)
                .doOnEach(notif->mCtxt.stop(notif))
                .doOnError(mCtxt::stopWithError)
                .map(new DocumentResync<>(bucketDoc,transcoder))
                .onErrorResumeNext(throwable-> ICouchbaseBucket.Utils.mapObservableStorageException(doc,throwable));
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
    public <T extends CouchbaseDocument> Observable<T> asyncPrepend(final T doc){
        final BucketDocument<T> bucketDoc = buildBucketDocument(doc);
        CouchbaseMetricsContext.MetricsContext mCtxt = deltaContext.start();
        final ICouchbaseTranscoder<T> transcoder = getTranscoder((Class<T>)doc.getClass());
        return bucket.async().prepend(bucketDoc)
                .doOnEach(notif->mCtxt.stop(notif))
                .doOnError(mCtxt::stopWithError)
                .map(new DocumentResync<>(bucketDoc,transcoder))
                .onErrorResumeNext(throwable-> ICouchbaseBucket.Utils.mapObservableStorageException(doc,throwable));
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


    @Override
    public Observable<Long> asyncCounter(String key, Long by, Long defaultValue, Integer expiry){
        CouchbaseMetricsContext.MetricsContext mCtxt = counterContext.start();
        return bucket.async().counter(key, by, defaultValue, expiry).doOnEach(mCtxt::stopCounter)
                .doOnError(mCtxt::stopWithError)
                .map(JsonLongDocument::content)
                .onErrorResumeNext(throwable -> ICouchbaseBucket.Utils.mapObservableStorageException(key,throwable));
    }

    @Override
    public Observable<Long> asyncCounter(String origKey, Long by, Long defaultValue, Integer expiration, WriteParams params) {
        CouchbaseMetricsContext.MetricsContext mCtxt = counterContext.start();

        final String key = ICouchbaseBucket.Utils.buildKey(params.getKeyPrefix(),origKey);
        Observable<JsonLongDocument> result = bucket.async().counter(key, by, defaultValue, expiration);

        if(params.getTimeOutUnit()!=null){
            result = result.timeout(params.getTimeOut(),params.getTimeOutUnit());
        }

        return result.doOnEach(mCtxt::stopCounter)
                .doOnError(mCtxt::stopWithError)
                .map(JsonLongDocument::content)
                .onErrorResumeNext(throwable->ICouchbaseBucket.Utils.mapObservableStorageException(key,throwable));
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
    public Observable<Long> asyncCounter(String key, Long by) {
        return asyncCounter(key,by,by);
    }

    @Override
    public Observable<Long> asyncCounter(String key, Long by,WriteParams params){
        return asyncCounter(key, by, by, params);
    }

    public static class DocumentResync<T extends CouchbaseDocument> implements Func1<BucketDocument<T>, T>{
        private final BucketDocument<T> origBucketDoc;
        private final ICouchbaseTranscoder<T> transcoder;

        public DocumentResync(final BucketDocument<T> doc,final ICouchbaseTranscoder<T> transcoder){
            this.origBucketDoc = doc;
            this.transcoder=transcoder;
        }

        @Override
        public T call(BucketDocument<T> tBucketDocument) {
            origBucketDoc.syncMeta(tBucketDocument);//Resync original object
            return tBucketDocument.content();
        }
    }

    @Override
    public Observable<AsyncViewResult> asyncQuery(ViewQuery query){
        return bucket.async().query(query);
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
