/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.testing.couchbase;

import com.codahale.metrics.MetricRegistry;
import com.couchbase.client.core.lang.Tuple2;
import com.couchbase.client.core.message.ResponseStatus;
import com.couchbase.client.deps.io.netty.buffer.ByteBuf;
import com.couchbase.client.deps.io.netty.buffer.Unpooled;
import com.couchbase.client.java.CouchbaseAsyncBucket;
import com.couchbase.client.java.document.Document;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.CASMismatchException;
import com.couchbase.client.java.error.DocumentAlreadyExistsException;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.couchbase.client.java.transcoder.JacksonTransformers;
import com.couchbase.client.java.transcoder.JsonTranscoder;
import com.couchbase.client.java.transcoder.Transcoder;
import com.couchbase.client.java.view.*;
import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.ICouchbaseTranscoder;
import com.dreameddeath.core.couchbase.exception.DocumentAccessException;
import com.dreameddeath.core.couchbase.exception.DocumentNotFoundException;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.couchbase.exception.ViewCompileException;
import com.dreameddeath.core.couchbase.impl.BlockingCouchbaseBucketWrapper;
import com.dreameddeath.core.couchbase.impl.CouchbaseBucketWrapper;
import com.dreameddeath.core.couchbase.impl.ReadParams;
import com.dreameddeath.core.couchbase.impl.WriteParams;
import com.dreameddeath.core.couchbase.metrics.CouchbaseMetricsContext;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.testing.couchbase.dcp.CouchbaseDCPConnectorSimulator;
import com.google.common.base.Preconditions;
import io.reactivex.Single;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Christophe Jeunesse on 24/11/2014.
 */
public class CouchbaseBucketSimulator extends CouchbaseBucketWrapper {
    private static Logger LOG = LoggerFactory.getLogger(CouchbaseBucketSimulator.class);

    private boolean isStarted=false;
    private final JsonTranscoder couchbaseJsonTranscoder=new JsonTranscoder();
    private final ScriptEngineManager engineFactory = new ScriptEngineManager();
    // create a JavaScript engine
    private final ScriptEngine engine = engineFactory.getEngineByName("JavaScript");
    private Map<String,DocumentSimulator> dbContent = new ConcurrentHashMap<>();
    public Map<Class,Transcoder<? extends Document, ?>> transcoderMap = new HashMap<>();
    public Map<String,Map<String,ScriptObjectMirror>> viewsMaps = new HashMap<>();
    public Set<CouchbaseDCPConnectorSimulator> dcpSimulators= new HashSet<>();
    private List<ICouchbaseOnWriteListener> onWriteListeners = new ArrayList<>();

    private void initTranscoders(){
        transcoderMap.put(CouchbaseAsyncBucket.JSON_OBJECT_TRANSCODER.documentType(), CouchbaseAsyncBucket.JSON_OBJECT_TRANSCODER);
        transcoderMap.put(CouchbaseAsyncBucket.JSON_ARRAY_TRANSCODER.documentType(), CouchbaseAsyncBucket.JSON_ARRAY_TRANSCODER);
        transcoderMap.put(CouchbaseAsyncBucket.JSON_BOOLEAN_TRANSCODER.documentType(), CouchbaseAsyncBucket.JSON_BOOLEAN_TRANSCODER);
        transcoderMap.put(CouchbaseAsyncBucket.JSON_DOUBLE_TRANSCODER.documentType(), CouchbaseAsyncBucket.JSON_DOUBLE_TRANSCODER);
        transcoderMap.put(CouchbaseAsyncBucket.JSON_LONG_TRANSCODER.documentType(), CouchbaseAsyncBucket.JSON_LONG_TRANSCODER);
        transcoderMap.put(CouchbaseAsyncBucket.JSON_STRING_TRANSCODER.documentType(), CouchbaseAsyncBucket.JSON_STRING_TRANSCODER);
        transcoderMap.put(CouchbaseAsyncBucket.RAW_JSON_TRANSCODER.documentType(), CouchbaseAsyncBucket.RAW_JSON_TRANSCODER);
        transcoderMap.put(CouchbaseAsyncBucket.LEGACY_TRANSCODER.documentType(), CouchbaseAsyncBucket.LEGACY_TRANSCODER);
        transcoderMap.put(CouchbaseAsyncBucket.BINARY_TRANSCODER.documentType(), CouchbaseAsyncBucket.BINARY_TRANSCODER);
        transcoderMap.put(CouchbaseAsyncBucket.STRING_TRANSCODER.documentType(), CouchbaseAsyncBucket.STRING_TRANSCODER);
        transcoderMap.put(CouchbaseAsyncBucket.SERIALIZABLE_TRANSCODER.documentType(), CouchbaseAsyncBucket.SERIALIZABLE_TRANSCODER);
    }

    public CouchbaseBucketSimulator(String bucketName){
        this(bucketName,null);
    }

    public CouchbaseBucketSimulator(String bucketName, MetricRegistry registry){
        super(null, bucketName, null,registry);
    }

    @Override
    protected BlockingCouchbaseBucketWrapper createBlockingSimulatorWrapper(){
        return new BlockingCouchbaseBucketSimulatorWrapper(this);
    }


    public ScriptEngine getJavaScriptEngine(){
        return engine;
    }



    @Override
    public void start(long timeout,TimeUnit unit){
        try {
            engine.eval("dateToArray = function(date){\n" +
                    "   return (date != null && date != undefined && date instanceof Date)?\n" +
                    "              [date.getYear(), date.getMonth(), date.getDay(),date.getHours(),date.getMinutes(), date.getSeconds()]\n" +
                    "              :[];\n" +
                    "}\n"
            );
        }
        catch(ScriptException e){
            LOG.error("Cannot compile script ",e);
        }
        initTranscoders();
        for(Transcoder transcoder:getTranscoders()){
            if(transcoder instanceof  ICouchbaseTranscoder) {
                transcoderMap.put(transcoder.documentType(), transcoder);
            }
        }
        isStarted=true;
    }

    @Override
    public void start(){
        start(0, null);
    }

    @Override
    public boolean isStarted() {
        return isStarted;
    }

    @Override
    public boolean shutdown(long timeout,TimeUnit unit){
        isStarted=false;
        return true;
    }
    @Override
    public void shutdown(){
        shutdown(0,null);
    }

    @Override
    public ICouchbaseBucket addTranscoder(ICouchbaseTranscoder transcoder) {
        super.addTranscoder(transcoder);
        return this;
    }

    public synchronized Long updateCacheCounter(String key,Long by,Long defaultValue,Integer expiration) throws StorageException{
        DocumentSimulator foundDoc = dbContent.get(key);

        if(foundDoc==null){
            if(defaultValue!=null){
                foundDoc = new DocumentSimulator(this);
                foundDoc.setFlags(0);
                foundDoc.setCas(1);
                foundDoc.setExpiry(expiration);
                foundDoc.setKey(key);
                foundDoc.setData(Unpooled.wrappedBuffer(defaultValue.toString().getBytes(Charset.forName("utf-8"))));
                notifyListenerBeforeUpdateCounter(ImpactMode.ADD,key,0L,defaultValue);
                dbContent.put(foundDoc.getKey(), foundDoc);
                notifyDcpUpdate(ImpactMode.ADD,foundDoc);
                notifyListenerAfterUpdateCounter(ImpactMode.ADD,key,0L,defaultValue);
                LOG.trace("Returning {} for counter {}",defaultValue,key);
                return defaultValue;
            }
            else{
                throw new DocumentNotFoundException(key,"Not found in couchbase simulator");
            }
        }
        try {
            Long result = Long.parseLong(new String(foundDoc.getData().array()));
            notifyListenerBeforeUpdateCounter(ImpactMode.UPDATE,key,result,result+by);
            foundDoc.setCas(foundDoc.getCas()+1);
            result+=by;
            foundDoc.setData(Unpooled.wrappedBuffer(result.toString().getBytes()));
            notifyDcpUpdate(ImpactMode.UPDATE,foundDoc);
            notifyListenerAfterUpdateCounter(ImpactMode.UPDATE,key,result,result+by);
            LOG.trace("Returning {} for counter {}",result,key);
            return result;
        }
        catch(NumberFormatException e){
            throw new DocumentAccessException(key,"Error during document access attempt of <"+key+">",e);
        }
    }

    private void notifyListenerBeforeUpdateCounter(ImpactMode mode, String key, Long before, long after)throws StorageException {
        for(ICouchbaseOnWriteListener listener:onWriteListeners){
            listener.onBeforeCounterWrite(mode,key,before,after);
        }
    }

    private void notifyListenerAfterUpdateCounter(ImpactMode mode, String key, Long before, long after)throws StorageException {
        for(ICouchbaseOnWriteListener listener:onWriteListeners){
            listener.onAfterCounterWrite(mode,key,before,after);
        }
    }


    private void notifyDcpUpdate(ImpactMode mode, DocumentSimulator doc){
        for(CouchbaseDCPConnectorSimulator simulator:dcpSimulators){
            simulator.notifyUpdate(mode,doc);
        }
    }

    private <T extends CouchbaseDocument> void notifyListenerBeforeUpdate(ImpactMode mode, BucketDocument<T> bucketDoc) throws  StorageException{
        for(ICouchbaseOnWriteListener listener:onWriteListeners){
            listener.onBeforeWrite(mode,bucketDoc.content());
        }
    }

    private <T extends CouchbaseDocument> void notifyListenerAfterUpdate(ImpactMode mode,Document<T> document)throws  StorageException{
        for(ICouchbaseOnWriteListener listener:onWriteListeners){
            listener.onAfterWrite(mode,document.content());
        }
    }

    synchronized public void addCouchbaseDcpSimulator(CouchbaseDCPConnectorSimulator simulator){
        dcpSimulators.add(simulator);
        for(DocumentSimulator doc :dbContent.values()){
            simulator.notifyUpdate(ImpactMode.ADD,doc);
        }
    }

    synchronized public void removeCouchbaseDcpSimulator(CouchbaseDCPConnectorSimulator simulator){
        dcpSimulators.remove(simulator);
    }

    public synchronized Document getFromCache(String key,Class docType) throws StorageException{
        Transcoder transcoder = transcoderMap.get(docType);
        DocumentSimulator foundDoc = dbContent.get(key);
        if(foundDoc==null){
            throw new DocumentDoesNotExistException("The document <"+key+"> is not ot found in couchbase simulator");
        }
        if(transcoder==null){
            throw new DocumentAccessException(key,"Error during document access attempt of <"+key+">");
        }
        foundDoc.getData().retain();
        return transcoder.decode(foundDoc.getKey(), foundDoc.getData(), foundDoc.getCas(), foundDoc.getExpiry(), foundDoc.getFlags(), ResponseStatus.SUCCESS);
    }

    public enum ImpactMode {
        ADD,
        UPDATE,
        REPLACE,
        APPEND,
        PREPEND,
        DELETE
    }

    public synchronized  <T extends CouchbaseDocument> Single<BucketDocument<T>> asyncPerformImpact(BucketDocument<T> bucketDoc, Class docType, ImpactMode mode, int expiry) throws StorageException{
        try {
            return Single.just((BucketDocument<T>)performImpact(bucketDoc, docType, mode, expiry));
        }
        catch (Throwable e){
            return Single.error(e);
        }
    }

    public synchronized  <T extends CouchbaseDocument> Document<T> performImpact(BucketDocument<T> bucketDoc, Class docType, ImpactMode mode, int expiry) throws StorageException{
        Transcoder<BucketDocument<?>,?> transcoder = (Transcoder)transcoderMap.get(docType);
        DocumentSimulator foundDoc = dbContent.get(bucketDoc.id());
        if((foundDoc==null) && !mode.equals(ImpactMode.ADD) && !mode.equals(ImpactMode.UPDATE) ){
            throw new DocumentDoesNotExistException("Key <"+bucketDoc.id()+"> isn't found in couchbase simulator");
        }
        if((foundDoc!=null) && mode.equals(ImpactMode.ADD)){
            throw new DocumentAlreadyExistsException("Key <"+bucketDoc.id()+"> already existing in couchbase simulator");
        }

        notifyListenerBeforeUpdate(mode,bucketDoc);

        if(foundDoc==null){
            foundDoc = new DocumentSimulator(this);
            foundDoc.setKey(bucketDoc.id());
            foundDoc.setCas(0L);
            dbContent.put(foundDoc.getKey(),foundDoc);
        }
        else{
            if((bucketDoc.cas()!=0) && (bucketDoc.cas()!=foundDoc.getCas())){
                throw new CASMismatchException("Key <"+bucketDoc.id()+"> has already been modified in the mean time with cas check (given:"+bucketDoc.cas()+",found:"+foundDoc.getCas());
            }
        }

        if(transcoder==null){
            throw new DocumentAccessException(bucketDoc.id(),"Error during document access attempt of <"+bucketDoc.id()+">");
        }

        Tuple2<ByteBuf, Integer> encodedResult = transcoder.encode(bucketDoc);
        foundDoc.setExpiry(expiry);
        foundDoc.setCas(foundDoc.getCas() + 1);
        foundDoc.setFlags(encodedResult.value2());

        switch (mode){
            case ADD:
            case REPLACE:
            case UPDATE:
                foundDoc.setData(encodedResult.value1());
                break;
            case APPEND:
                foundDoc.appendData(encodedResult.value1());
                break;
            case PREPEND:
                foundDoc.prependData(encodedResult.value1());
                break;
            case DELETE:
                //Do nothing
                break;
        }

        notifyDcpUpdate(mode,foundDoc);

        Document<T> result = getFromCache(bucketDoc.id(),docType);
        if(mode==ImpactMode.DELETE){
            dbContent.remove(bucketDoc.id());
        }
        notifyListenerAfterUpdate(mode,result);
        return result;
    }


    @Override
    public <T extends CouchbaseDocument> Single<T> asyncGet(String id,Class<T> entity) {
        return asyncGet(id,entity,null);
    }

    @Override
    public <T extends CouchbaseDocument> Single<T> asyncGet(final String id,Class<T> entity,ReadParams params) {
        CouchbaseMetricsContext.DocumentMetricContext<T> mCtxt = getContext.startDocument();
        try{
            String effectiveId=id;
            if((params!=null) && (params.getKeyPrefix()!=null)){
                effectiveId = ICouchbaseBucket.Utils.buildKey(params.getKeyPrefix(),id);
            }
            BucketDocument<T> result=(BucketDocument<T>)getFromCache(effectiveId,getTranscoder(entity).documentType());
            if((params!=null) && (params.getKeyPrefix()!=null)) {
                result.setKeyPrefix(params.getKeyPrefix());
            }
            mCtxt.stopWithSize(true,result.getDocument().getBaseMeta().getDbSize().longValue());
            return Single.just(result.content());
        }
        catch(Throwable e){
            mCtxt.stopWithSize(false,null);
            return Single.<T>error(e)
                    .onErrorResumeNext(throwable-> ICouchbaseBucket.Utils.mapObservableAccessException(id,throwable,entity));
        }
    }

    @Override
    public <T extends CouchbaseDocument> Single<T> asyncAdd(T doc)  {
        return asyncAdd(doc,null);
    }

    @Override
    public <T extends CouchbaseDocument> Single<T> asyncAdd(T doc,WriteParams params){
        CouchbaseMetricsContext.DocumentMetricContext<T> mCtxt = createContext.startDocument();
        try{
            final ICouchbaseTranscoder<T> transcoder = getTranscoder((Class<T>)doc.getClass());
            final BucketDocument<T> bucketDoc = (params!=null)?buildBucketDocument(doc,params.getKeyPrefix()):buildBucketDocument(doc);
           return asyncPerformImpact(bucketDoc,transcoder.documentType(), ImpactMode.ADD, 0)
                   .doOnEvent(mCtxt)
                   .map(new DocumentResync<>(bucketDoc,transcoder))
                   .onErrorResumeNext(throwable -> ICouchbaseBucket.Utils.mapObservableStorageException(doc,throwable));
        }
        catch(Exception e){
            mCtxt.stopWithSize(false,null);
            return Single.error(e);
        }
    }

    @Override
    public <T extends CouchbaseDocument> Single<T> asyncSet(T doc){
        return asyncSet(doc,null);
    }

    @Override
    public <T extends CouchbaseDocument> Single<T> asyncSet(T doc,WriteParams params) {
        CouchbaseMetricsContext.DocumentMetricContext<T> mCtxt = updateContext.startDocument();
        try{
            final ICouchbaseTranscoder<T> transcoder = getTranscoder((Class<T>)doc.getClass());
            final BucketDocument<T> bucketDoc = (params!=null)?buildBucketDocument(doc,params.getKeyPrefix()):buildBucketDocument(doc);
            return asyncPerformImpact(bucketDoc, transcoder.documentType(), ImpactMode.UPDATE, 0)
                    .doOnEvent(mCtxt)
                    .map(new DocumentResync<>(bucketDoc,transcoder))
                    .onErrorResumeNext(throwable -> ICouchbaseBucket.Utils.mapObservableStorageException(doc,throwable));
        }
        catch(Exception e){
            mCtxt.stopWithSize(false,null);
            return Single.error(e);
        }
    }


    @Override
    public <T extends CouchbaseDocument> Single<T> asyncReplace(T doc){
        return asyncReplace(doc, null);
    }

    @Override
    public <T extends CouchbaseDocument> Single<T> asyncReplace(T doc,WriteParams params) {
        CouchbaseMetricsContext.DocumentMetricContext<T> mCtxt = updateContext.startDocument();
        try{
            final ICouchbaseTranscoder<T> transcoder = getTranscoder((Class<T>)doc.getClass());
            final BucketDocument<T> bucketDoc = (params!=null)?buildBucketDocument(doc,params.getKeyPrefix()):buildBucketDocument(doc);
            return asyncPerformImpact(bucketDoc, transcoder.documentType(), ImpactMode.REPLACE, 0)
                    .doOnEvent(mCtxt)
                    .map(new DocumentResync<>(bucketDoc,transcoder))
                    .onErrorResumeNext(throwable -> ICouchbaseBucket.Utils.mapObservableStorageException(doc,throwable));
        }
        catch(Exception e){
            mCtxt.stopWithSize(false,null);
            return Single.error(e);
        }
    }


    @Override
    public <T extends CouchbaseDocument> Single<T> asyncDelete(T doc) {
        return asyncDelete(doc,null);
    }

    @Override
    public <T extends CouchbaseDocument> Single<T> asyncDelete(T doc,WriteParams params) {
        CouchbaseMetricsContext.DocumentMetricContext<T> mCtxt = deleteContext.startDocument();
        try{
            final ICouchbaseTranscoder<T> transcoder = getTranscoder((Class<T>)doc.getClass());
            final BucketDocument<T> bucketDoc = (params!=null)?buildBucketDocument(doc,params.getKeyPrefix()):buildBucketDocument(doc);
            return asyncPerformImpact(bucketDoc, transcoder.documentType(), ImpactMode.DELETE, 0)
                    .doOnEvent(mCtxt)
                    .map(new DocumentResync<>(bucketDoc,transcoder))
                    .onErrorResumeNext(throwable -> ICouchbaseBucket.Utils.mapObservableStorageException(doc,throwable));
        }
        catch(Exception e){
            mCtxt.stopWithSize(false,null);
            return Single.error(e);
        }
    }


    @Override
    public <T extends CouchbaseDocument> Single<T> asyncAppend(T doc,WriteParams params){
        CouchbaseMetricsContext.DocumentMetricContext<T> mCtxt = deltaContext.startDocument();
        try{
            final ICouchbaseTranscoder<T> transcoder = getTranscoder((Class<T>)doc.getClass());
            final BucketDocument<T> bucketDoc = (params!=null)?buildBucketDocument(doc,params.getKeyPrefix()):buildBucketDocument(doc);
            return asyncPerformImpact(bucketDoc, transcoder.documentType(), ImpactMode.APPEND, 0)
                    .doOnEvent(mCtxt)
                    .map(new DocumentResync<>(bucketDoc,transcoder))
                    .onErrorResumeNext(throwable -> ICouchbaseBucket.Utils.mapObservableStorageException(doc,throwable));
        }
        catch(Exception e){
            mCtxt.stopWithSize(false,null);
            return Single.error(e);
        }
    }


        @Override
    public <T extends CouchbaseDocument> Single<T> asyncAppend(T doc){
        return asyncAppend(doc, null);
    }


    @Override
    public <T extends CouchbaseDocument> Single<T> asyncPrepend(T doc) {
        return asyncPrepend(doc,null);
    }

    @Override
    public <T extends CouchbaseDocument> Single<T> asyncPrepend(T doc,WriteParams params) {
        CouchbaseMetricsContext.DocumentMetricContext<T> mCtxt = deltaContext.startDocument();
        try{
            final ICouchbaseTranscoder<T> transcoder = getTranscoder((Class<T>)doc.getClass());
            final BucketDocument<T> bucketDoc = (params!=null)?buildBucketDocument(doc,params.getKeyPrefix()):buildBucketDocument(doc);
            return asyncPerformImpact(bucketDoc, transcoder.documentType(), ImpactMode.PREPEND, 0)
                    .doOnEvent(mCtxt)
                    .map(new DocumentResync<>(bucketDoc,transcoder))
                    .onErrorResumeNext(throwable -> ICouchbaseBucket.Utils.mapObservableStorageException(doc,throwable));
        }
        catch(Exception e){
            mCtxt.stopWithSize(false,null);
            return Single.error(e);
        }
    }


    @Override
    public Single<Long> asyncCounter(String key, Long by, Long defaultValue, Integer expiration,WriteParams params)  {
        CouchbaseMetricsContext.CounterMetricContext mCtxt = counterContext.startCounter();

        try{
            if((params!=null)&& (params.getKeyPrefix()!=null)) {
                key=ICouchbaseBucket.Utils.buildKey(params.getKeyPrefix(), key);
            }
            Long result = updateCacheCounter(key,by,defaultValue,expiration);
            mCtxt.stopWithSize(true,(long)result.toString().length());
            return Single.just(result);
        }
        catch(Exception e){
            mCtxt.stopWithSize(false,null);
            return Single.error(e);
        }


    }

    @Override
    public Single<Long> asyncCounter(String key, Long by, Long defaultValue, Integer expiration){
        return asyncCounter(key,by,defaultValue,expiration,null);
    }


    @Override
    public void createOrUpdateView(String designDoc,Map<String,String> viewList) throws StorageException{
        LOG.debug("Attempt to create design Doc {}",designDoc);

        Map<String,ScriptObjectMirror> newDesignDocMap = new HashMap<>();
        for(Map.Entry<String,String> viewDef:viewList.entrySet()){
            String scriptContent = viewDef.getValue();
            /*scriptContent ="dateToArray = function(date){\n" +
                    "   return (date != null && date != undefined && date instanceof Date)?\n" +
                    "              [date.getYear(), date.getMonth(), date.getDay(),date.getHours(),date.getMinutes(), date.getSeconds()]\n" +
                    "              :[];\n" +
                    "}\n"+*
                    scriptContent ;*/
            scriptContent = scriptContent.replaceAll("^(\\s*function\\s*\\(\\s*doc\\s*,\\s*meta\\s*)", "$1,globalResultEmitter");
            scriptContent = scriptContent.replaceAll("\\bemit\\(", "globalResultEmitter.emit(meta.id,");

            try{
                newDesignDocMap.put(viewDef.getKey(),(ScriptObjectMirror)engine.eval(scriptContent));
            }
            catch(ScriptException e){
                throw new ViewCompileException(
                        String.format("Cannot compile %s/%s with content :%n%s%nEOFCONTENT",
                                designDoc,
                                viewDef.getKey(),
                                scriptContent
                            ),
                        e);
            }
        }
        viewsMaps.put(designDoc,newDesignDocMap);
    }

    public Single<AsyncViewResult> asyncQuery(ViewQuery query){
        return Single.just(InternalViewResult.toAsyncViewResult(buildResult(query)));
    }

    public InternalViewResult buildResult(ViewQuery query){
        String designDoc = query.getDesign();
        String viewName = query.getView();
        Map<String, ScriptObjectMirror> scriptMapping = viewsMaps.get(designDoc);
        Preconditions.checkNotNull(scriptMapping,"The design doc %s isn't existing",designDoc);
        ScriptObjectMirror viewScript = scriptMapping.get(viewName);
        Preconditions.checkNotNull(scriptMapping,"The view %s isn't existing in the design doc doc %s isn't existing",viewName,designDoc);
        EmitSimulator result = new EmitSimulator(this);
        for(Map.Entry<String,DocumentSimulator> docInstance:dbContent.entrySet()){
            try {
                viewScript.call(null, docInstance.getValue().getJavascriptObject(), docInstance.getValue().getMeta(),result);
            }
            catch(Exception e){
                throw new RuntimeException("Error ",e);
            }
        }

        Map<String,Integer> additionalCriteria = new HashMap<>();
        Predicate<InternalRow> filterPredicate=decodeParams(query,additionalCriteria);
        Stream<InternalRow> stream = result.getFullResult().stream();
        if(additionalCriteria.containsKey("reverse") && additionalCriteria.get("reverse").equals(1)){
            stream = stream.sorted((vr1,vr2)->-EmitSimulator.compare(vr1.key(), vr2.key()));
        }
        else{
            stream = stream.sorted((vr1, vr2) -> EmitSimulator.compare(vr1.key(), vr2.key()));
        }
        stream=stream.filter(filterPredicate);
        if(additionalCriteria.containsKey("skip")){
            stream=stream.skip(additionalCriteria.get("skip"));
        }
        if(additionalCriteria.containsKey("limit")){
            stream=stream.limit(additionalCriteria.get("limit"));
        }
        return new InternalViewResult(stream.collect(Collectors.toList()),result.getFullResult().size());
    }

    public Object parseParamKey(String key) throws Exception{
        if(key.startsWith("[") || key.startsWith("{")){
            return couchbaseJsonTranscoder.stringToJsonObject(key);
        }
        else if(key.startsWith("\"")){
            return key.substring(1,key.length()-1);
        }
        else{
            try {
                try {
                    return Long.parseLong(key);
                } catch (Exception e) {
                    return Double.parseDouble(key);
                }
            }
            catch(Exception e){
                return key;
            }

        }
    }

    protected  Predicate<InternalRow> decodeParams(ViewQuery query,Map<String,Integer> streamComplements){
        Map<String,String> params = new HashMap<>();
        //can use split as it is enconded
        for(String paramElem:query.toQueryString().split("&")){
            String[] paramParts=paramElem.split("=");
            try {
                params.put(paramParts[0], URLDecoder.decode(paramParts[1],"UTF-8"));
            }
            catch(Exception e){
                throw new RuntimeException(String.format("Decoding error of param <%s>/<%s>",paramParts[0],paramParts[1]),e);
            }
        }
        boolean inclusive_end = "true".equals(params.get("inclusive_end"));
        Predicate<InternalRow> resultPredicate=(InternalRow vr)->true;
        try {
            if (params.containsKey("key")) {
                final Object keyPredicateCriteria = parseParamKey(params.get("key"));
                resultPredicate = resultPredicate.and((InternalRow vr)->vr.key().equals(keyPredicateCriteria));
            }
            if (params.containsKey("startkey_docid")) {
                resultPredicate = resultPredicate.and((InternalRow vr)->vr.id().compareTo(params.get("startkey_docid"))>0);
            }
            if (params.containsKey("endkey_docid")) {
                if(inclusive_end) { resultPredicate = resultPredicate.and((InternalRow vr) -> vr.id().compareTo(params.get("endkey_docid"))<=0); }
                else{ resultPredicate = resultPredicate.and((InternalRow vr) ->
                        vr.id().compareTo(params.get("endkey_docid"))<0
                );}
            }
            if (params.containsKey("startkey")) {
                final Object keyPredicateCriteria = parseParamKey(params.get("startkey"));
                resultPredicate = resultPredicate.and(
                        (InternalRow vr)->
                                EmitSimulator.compare(vr.key(), keyPredicateCriteria)>=0
                );
            }
            if (params.containsKey("endkey")) {
                final Object keyPredicateCriteria = parseParamKey(params.get("endkey"));
                if(inclusive_end) { resultPredicate = resultPredicate.and((InternalRow vr) ->
                        EmitSimulator.compare(vr.key(), keyPredicateCriteria) <= 0
                );}
                else{ resultPredicate = resultPredicate.and((InternalRow vr) ->
                        EmitSimulator.compare(vr.key(), keyPredicateCriteria) < 0
                );}
            }
            if(params.containsKey("keys")){
                final Object keyPredicateCriteria = parseParamKey(params.get("keys"));
                final List<Object> keyList = ((JsonArray)keyPredicateCriteria).toList();
                resultPredicate = resultPredicate.and((InternalRow vr) -> keyList.stream().anyMatch(elem-> EmitSimulator.compare(elem, vr.key())==0));
            }
        }
        catch(Exception e){
            throw new RuntimeException("Unexpected issue",e);
        }

        if (params.containsKey("skip")) {
            streamComplements.put("skip",Integer.parseInt(params.get("skip")));
        }
        if (params.containsKey("limit")) {
            streamComplements.put("limit",Integer.parseInt(params.get("limit")));
        }
        if (params.containsKey("reverse") && params.get("reverse").equals("true") ) {
            streamComplements.put("reverse",1);
        }
        if (params.containsKey("debug") && params.get("debug").equals("true") ) {
            streamComplements.put("debug",1);
        }

        return resultPredicate;
    }

    public static class EmitSimulator{
        private CouchbaseBucketSimulator wrapper;
        public EmitSimulator(CouchbaseBucketSimulator wrapper){
            this.wrapper = wrapper;
        }
        private List<InternalRow> resultList = new ArrayList<>();

        public List<InternalRow> getFullResult(){
            return resultList;
        }

        public Object toNormalizedObject(Object obj){
            if(obj ==null){return null;}
            else if(obj instanceof String){return new String((String)obj);}
            else if(obj instanceof Integer){return new Long((Integer)obj); }
            else if(obj instanceof Long){return new Long((Long)obj); }
            else if(obj instanceof Double){return new Double((Double) obj); }
            else if(obj instanceof Boolean){return new Boolean((Boolean)obj);}
            else if(obj instanceof ScriptObjectMirror){
                if(((ScriptObjectMirror)obj).isArray()){
                    JsonArray result = JsonArray.create();
                    for(Object elem:((ScriptObjectMirror)obj).values()){
                        result.add(toNormalizedObject(elem));
                    }
                    return result;
                }
                else {
                    JsonObject result = JsonObject.create();
                    for (Map.Entry<String, ?> elem : ((Map<String, ?>) obj).entrySet()) {
                        result.put(elem.getKey(), toNormalizedObject(elem.getValue()));
                    }
                    return result;
                }
            }

            return null;
        }

        public void emit(final String docKey,Object key,Object value){
            LOG.debug("Emitting {}/{} for key {}",key,value,docKey);
            final Object realKey= toNormalizedObject(key);
            final Object realValue =toNormalizedObject(value);
            resultList.add(new InternalRow(docKey,realKey,realValue,EmitSimulator.this.wrapper));
        }

        public static int compare(Object key1, Object key2) {
            if(key1 instanceof JsonArray){
                if(key2 instanceof JsonArray){
                    Iterator it1 = ((JsonArray)key1).iterator();
                    Iterator it2 = ((JsonArray)key2).iterator();

                    while(it1.hasNext() && it2.hasNext()){
                        int result = compare(it1.next(),it2.next());
                        if(result!=0){ return result; }
                    }
                    if(it1.hasNext() && !it2.hasNext()){return 1;}
                    else if(!it1.hasNext() && it2.hasNext()){return -1;}
                    else{ return 0;}
                }
                else{ return 1;}
            }
            else{
                if(key1==null){
                    if(key2==null){ return 0;}
                    else{return -1;}
                }
                else if(key2 instanceof JsonArray){ return -1;}
                else {
                    if(key2==null){ return 1;}
                    else{return key1.toString().compareTo(key2.toString());}
                }
            }
        }

    }

    public static class InternalRow{
        private final CouchbaseBucketSimulator wrapper;
        private final String docKey;
        private final Object key;
        private final Object value;

        public InternalRow(String docKey,Object key,Object value,CouchbaseBucketSimulator wrapper){
            this.wrapper = wrapper;
            this.key = key;
            this.docKey = docKey;
            this.value = value;
        }

        public static ViewRow toViewRow(final InternalRow row){
            return new ViewRow() {
                private InternalRow internalRow=row;
                @Override public String id() {return internalRow.id();}
                @Override public Object key() {return internalRow.key();}
                @Override public Object value() {return internalRow.value();}
                @Override public JsonDocument document() {return internalRow.document();}
                @Override public JsonDocument document(long timeout, TimeUnit timeUnit) {return document();}
                @Override public <D extends Document<?>> D document(Class<D> target) {return internalRow.document(target);}
                @Override public <D extends Document<?>> D document(Class<D> target, long timeout, TimeUnit timeUnit) {return document(target);}
            };
        }

        public static AsyncViewRow toAsyncViewRow(final InternalRow row){
            return new AsyncViewRow() {
                private InternalRow internalRow = row;
                @Override public String id() {return internalRow.id();}
                @Override public Object key() {return internalRow.key();}
                @Override public Object value() {return internalRow.value();}
                @Override public rx.Observable<JsonDocument> document() {return rx.Observable.from(new JsonDocument[]{internalRow.document()});}
                @Override public <D extends Document<?>> rx.Observable<D> document(Class<D> target) {return rx.Observable.from((D[])new JsonDocument[]{(JsonDocument) internalRow.document(target)});}
            };
        }

        public String id() {return docKey;}
        public Object key() {return key;}
        public Object value() {return value;}
        public JsonDocument document() {return document(JsonDocument.class);}
        public <D extends Document<?>> D document(Class<D> target) {
            try {
                DocumentSimulator documentSimulator;
                synchronized (wrapper) {
                    documentSimulator=wrapper.dbContent.get(id());
                }
                if(documentSimulator!=null) {
                    return JacksonTransformers.MAPPER.readValue(documentSimulator.getData().array(), target);
                }
                else{
                    return null;
                }
            }
            catch(Exception e){ throw new RuntimeException("Unexpected exception",e);}
        }
    }

    public static class InternalViewResult{
        public List<InternalRow> results;
        int totalRows;

        public static AsyncViewResult toAsyncViewResult(final InternalViewResult result){
            return new AsyncViewResult() {
                private InternalViewResult internalResult=result;
                @Override
                public rx.Observable<AsyncViewRow> rows() {
                    return rx.Observable.from(internalResult.allRows().stream().map(InternalRow::toAsyncViewRow).collect(Collectors.toList()));
                }

                @Override public int totalRows() {return internalResult.totalRows();}
                @Override public boolean success() {return true;}
                @Override public rx.Observable<JsonObject> error() {return null;}
                @Override public JsonObject debug() {return null;}
            };
        }

        public static ViewResult toViewResult(final InternalViewResult result){
            return new ViewResult() {

                private InternalViewResult internalResult=result;

                @Override
                public List<ViewRow> allRows() {
                    return internalResult.allRows().stream().map(InternalRow::toViewRow).collect(Collectors.toList());
                }

                @Override public List<ViewRow> allRows(long timeout, TimeUnit timeUnit) {return allRows();}
                @Override public Iterator<ViewRow> rows() {return allRows().iterator();}
                @Override public Iterator<ViewRow> rows(long timeout, TimeUnit timeUnit) {return allRows().iterator();}
                @Override public int totalRows() {return internalResult.totalRows();}
                @Override public boolean success() {return true;}
                @Override public JsonObject error() {return null;}
                @Override public Iterator<ViewRow> iterator() { return allRows().iterator(); }
                @Override public JsonObject error(long l, TimeUnit timeUnit) { return null; }
                @Override public JsonObject debug() {return null;}
            };
        }

        public InternalViewResult(List<InternalRow> results,int totalRows){
            this.totalRows = totalRows;
            this.results = results;
        }

        public List<InternalRow> allRows() {return Collections.unmodifiableList(results);}

        public int totalRows() {
            return totalRows;
        }
    }


    public class BlockingCouchbaseBucketSimulatorWrapper extends BlockingCouchbaseBucketWrapper{

        public BlockingCouchbaseBucketSimulatorWrapper(ICouchbaseBucket asyncWrapper){
            super(asyncWrapper);
        }

        @Override
        public ViewResult query(ViewQuery query){
            return InternalViewResult.toViewResult(buildResult(query));
        }

    }


    public boolean addOnWriteListener(ICouchbaseOnWriteListener listener){
        return this.onWriteListeners.add(listener);
    }

    public boolean removeOnWriteListener(ICouchbaseOnWriteListener listener){
        return this.onWriteListeners.remove(listener);
    }
}
