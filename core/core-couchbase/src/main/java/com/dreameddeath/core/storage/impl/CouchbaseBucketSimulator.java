package com.dreameddeath.core.storage.impl;

import com.couchbase.client.core.lang.Tuple2;
import com.couchbase.client.core.message.ResponseStatus;
import com.couchbase.client.deps.io.netty.buffer.ByteBuf;
import com.couchbase.client.deps.io.netty.buffer.Unpooled;
import com.couchbase.client.java.document.Document;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.DocumentAlreadyExistsException;
import com.couchbase.client.java.transcoder.JacksonTransformers;
import com.couchbase.client.java.transcoder.JsonTranscoder;
import com.couchbase.client.java.view.*;
import com.dreameddeath.core.exception.storage.DocumentAccessException;
import com.dreameddeath.core.exception.storage.DocumentNotFoundException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.exception.storage.ViewCompileException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.storage.BucketDocument;
import com.dreameddeath.core.storage.ICouchbaseBucket;
import com.dreameddeath.core.storage.ICouchbaseTranscoder;
import com.dreameddeath.core.storage.impl.simulator.DocumentSimulator;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by CEAJ8230 on 24/11/2014.
 */
public class CouchbaseBucketSimulator extends CouchbaseBucketWrapper {
    private final static JsonTranscoder _couchbaseJsonTranscoder=new JsonTranscoder();
    private final static ScriptEngineManager _enginefactory = new ScriptEngineManager();
    // create a JavaScript engine
    private final static ScriptEngine _engine = _enginefactory.getEngineByName("JavaScript");

    private static Logger LOG = LoggerFactory.getLogger(CouchbaseBucketSimulator.class);

    public CouchbaseBucketSimulator(String bucketName){
        super(null,bucketName,null);
    }

    public CouchbaseBucketSimulator(String bucketName,String prefix){
        super(null,bucketName,null,prefix);
    }

    private Map<String,DocumentSimulator> _dbContent = new ConcurrentHashMap<String,DocumentSimulator>();

    public Map<Class,ICouchbaseTranscoder> _transcoderMap = new HashMap<Class,ICouchbaseTranscoder>();
    public Map<String,Map<String,ScriptObjectMirror>> _viewsMaps = new HashMap<>();

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
            Long result = Long.parseLong(new String(foundDoc.getData().array()));
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
            id=ICouchbaseBucket.Utils.buildKey(_keyPrefix,id);
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
            key = ICouchbaseBucket.Utils.buildKey(_keyPrefix,key);
            return Observable.just(updateCacheCounter(key,by,defaultValue,expiration));
        }
        catch(Exception e){
             return Observable.error(e);
        }
    }


    @Override
    public void createOrUpdateView(String designDoc,Map<String,String> viewList) throws StorageException{
        designDoc = ICouchbaseBucket.Utils.buildDesignDoc(_keyPrefix,designDoc);

        LOG.debug("Attempt to create design Doc {}",designDoc);

        Map<String,ScriptObjectMirror> newDesignDocMap = new HashMap<>();
        for(Map.Entry<String,String> viewDef:viewList.entrySet()){
            Compilable compilator = (Compilable)_engine;
            String scriptContent = viewDef.getValue();

            scriptContent = scriptContent.replaceAll("^(\\s*function\\s*\\(\\s*doc\\s*,\\s*meta\\s*)", "$1,globalResultEmitter");
            scriptContent = scriptContent.replaceAll("\\bemit\\(", "globalResultEmitter.emit(meta.id,");

            try{
                //Object result = ;
                //newDesignDocMap.put(viewDef.getKey(),compilator.compile(scriptContent));
                newDesignDocMap.put(viewDef.getKey(),(ScriptObjectMirror)_engine.eval(scriptContent));
            }
            catch(ScriptException e){
                throw new ViewCompileException(
                        String.format("Cannot compile %s/%s with content :\n%s\nEOFCONTENT",
                                designDoc,
                                viewDef.getKey(),
                                scriptContent
                            ),
                        e);
            }
        }
        _viewsMaps.put(designDoc,newDesignDocMap);
    }

    public Observable<AsyncViewResult> asyncQuery(ViewQuery query){
        return Observable.<AsyncViewResult>from(new AsyncViewResult[]{InternalViewResult.toAsyncViewResult(buildResult(query))});
    }

    @Override
    public ViewResult query(ViewQuery query){
        return InternalViewResult.toViewResult(buildResult(query));
    }

    public InternalViewResult buildResult(ViewQuery query){
        String designDoc = query.getDesign();
        String viewName = query.getView();

        ScriptObjectMirror viewScript = _viewsMaps.get(designDoc).get(viewName);
        EmitSimulator result = new EmitSimulator(this);
        for(Map.Entry<String,DocumentSimulator> docInstance:_dbContent.entrySet()){
            try {
                Object callResult = viewScript.call(null,new Object[]{ docInstance.getValue().getJavascriptObject(), docInstance.getValue().getMeta(),result});
            }
            catch(Exception e){
                throw new RuntimeException("Error ",e);
            }
        }

        Map<String,Integer> additionnalCriteria = new HashMap<>();
        Predicate<InternalRow> filterPredicate=decodeParams(query,additionnalCriteria);
        Stream<InternalRow> stream = result.getFullResult().stream();
        if(additionnalCriteria.containsKey("reverse") && additionnalCriteria.get("reverse").equals(1)){
            stream = stream.sorted((vr1,vr2)->-EmitSimulator.compare(vr1.key(),vr2.key()));
        }
        else{
            stream = stream.sorted((vr1, vr2) ->EmitSimulator.compare(vr1.key(), vr2.key()));
        }

        stream=stream.filter(filterPredicate);
        if(additionnalCriteria.containsKey("skip")){
            stream=stream.skip(additionnalCriteria.get("skip"));
        }
        if(additionnalCriteria.containsKey("limit")){
            stream=stream.limit(additionnalCriteria.get("limit"));
        }
        return new InternalViewResult(stream.collect(Collectors.<InternalRow>toList()),result.getFullResult().size());
    }

    public Object parseParamKey(String key) throws Exception{
        if(key.startsWith("[") || key.startsWith("{")){
            return _couchbaseJsonTranscoder.stringToJsonObject(key);
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
        for(String paramElem:query.toString().split("&")){
            String[] paramParts=paramElem.split("=");
            try {
                params.put(paramParts[0], URLDecoder.decode(paramParts[1], "UTF-8"));
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
                resultPredicate = resultPredicate.and((InternalRow vr)->vr.id().compareTo(params.get("startkey_docid").toString())>0);
            }
            if (params.containsKey("endkey_docid")) {
                if(inclusive_end) { resultPredicate = resultPredicate.and((InternalRow vr) -> vr.id().compareTo(params.get("endkey_docid").toString())<=0); }
                else{ resultPredicate = resultPredicate.and((InternalRow vr) ->
                        vr.id().compareTo(params.get("endkey_docid").toString())<0
                );}
            }
            if (params.containsKey("startkey")) {
                final Object keyPredicateCriteria = parseParamKey(params.get("startkey"));
                resultPredicate = resultPredicate.and(
                        (InternalRow vr)->
                                EmitSimulator.compare(vr.key(),keyPredicateCriteria)>=0
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
                resultPredicate = resultPredicate.and((InternalRow vr) -> keyList.stream().anyMatch(elem->EmitSimulator.compare(elem,vr.key())==0));
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
        private CouchbaseBucketSimulator _wrapper;
        public EmitSimulator(CouchbaseBucketSimulator wrapper){
            _wrapper = wrapper;
        }
        private List<InternalRow> _resultList = new ArrayList<>();

        public List<InternalRow> getFullResult(){
            return _resultList;
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
            _resultList.add(new InternalRow(docKey,realKey,realValue,EmitSimulator.this._wrapper));
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
        private final CouchbaseBucketSimulator _wrapper;
        private final String _docKey;
        private final Object _key;
        private final Object _value;

        public InternalRow(String docKey,Object key,Object value,CouchbaseBucketSimulator wrapper){
            _wrapper = wrapper;
            _key = key;
            _docKey = docKey;
            _value = value;
        }

        public static ViewRow toViewRow(final InternalRow row){
            return new ViewRow() {
                private InternalRow _row=row;
                @Override public String id() {return _row.id();}
                @Override public Object key() {return _row.key();}
                @Override public Object value() {return _row.value();}
                @Override public JsonDocument document() {return _row.document();}
                @Override public JsonDocument document(long timeout, TimeUnit timeUnit) {return document();}
                @Override public <D extends Document<?>> D document(Class<D> target) {return _row.document(target);}
                @Override public <D extends Document<?>> D document(Class<D> target, long timeout, TimeUnit timeUnit) {return document(target);}
            };
        }

        public static AsyncViewRow toAsyncViewRow(final InternalRow row){
            return new AsyncViewRow() {
                private InternalRow _row=row;
                @Override public String id() {return _row.id();}
                @Override public Object key() {return _row.key();}
                @Override public Object value() {return _row.value();}
                @Override public Observable<JsonDocument> document() {return Observable.from(new JsonDocument[]{_row.document()});}
                @Override public <D extends Document<?>> Observable<D> document(Class<D> target) {return Observable.from((D[])new JsonDocument[]{(JsonDocument)_row.document(target)});}
            };
        }

        public String id() {return _docKey;}
        public Object key() {return _key;}
        public Object value() {return _value;}
        public JsonDocument document() {return document(JsonDocument.class);}
        public <D extends Document<?>> D document(Class<D> target) {
            try { return JacksonTransformers.MAPPER.readValue(_wrapper._dbContent.get(id()).getData().array(), target);}
            catch(Exception e){ throw new RuntimeException("Unexpected exception",e);}
        }
    }

    public static class InternalViewResult{
        public List<InternalRow> _results;
        int _totalRows;

        public static AsyncViewResult toAsyncViewResult(final InternalViewResult result){
            return new AsyncViewResult() {
                private InternalViewResult _internalResult=result;
                @Override
                public Observable<AsyncViewRow> rows() {
                    return Observable.<AsyncViewRow>from(_internalResult.allRows().stream().map(InternalRow::toAsyncViewRow).collect(Collectors.<AsyncViewRow>toList()));
                }

                @Override public int totalRows() {return _internalResult.totalRows();}
                @Override public boolean success() {return true;}
                @Override public JsonObject error() {return null;}
                @Override public JsonObject debug() {return null;}
            };
        }

        public static ViewResult toViewResult(final InternalViewResult result){
            return new ViewResult() {
                private InternalViewResult _internalResult=result;

                @Override
                public List<ViewRow> allRows() {
                    return _internalResult.allRows().stream().map(InternalRow::toViewRow).collect(Collectors.<ViewRow>toList());
                }

                @Override public List<ViewRow> allRows(long timeout, TimeUnit timeUnit) {return allRows();}
                @Override public Iterator<ViewRow> rows() {return allRows().iterator();}
                @Override public Iterator<ViewRow> rows(long timeout, TimeUnit timeUnit) {return allRows().iterator();}
                @Override public int totalRows() {return _internalResult.totalRows();}
                @Override public boolean success() {return true;}
                @Override public JsonObject error() {return null;}
                @Override public JsonObject debug() {return null;}
            };
        }
        public InternalViewResult(List<InternalRow> results,int totalRows){
            _totalRows = totalRows;
            _results = results;
        }

        public List<InternalRow> allRows() {return Collections.unmodifiableList(_results);}

        public int totalRows() {
            return _totalRows;
        }
    }

}
