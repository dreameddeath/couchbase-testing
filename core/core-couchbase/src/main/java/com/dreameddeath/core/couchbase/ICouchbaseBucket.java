/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.couchbase;

import com.couchbase.client.java.view.AsyncViewResult;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.couchbase.impl.ReadParams;
import com.dreameddeath.core.couchbase.impl.WriteParams;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import rx.Observable;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by ceaj8230 on 21/11/2014.
 */
public interface ICouchbaseBucket {
    ICouchbaseBucket addTranscoder(ICouchbaseTranscoder transcoder);

    <T extends CouchbaseDocument> T get(final String key, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    <T extends CouchbaseDocument> T get(final String key, final ICouchbaseTranscoder<T> transcoder, ReadParams params) throws StorageException;
    <T extends CouchbaseDocument> Observable<T> asyncGet(final String id, final ICouchbaseTranscoder<T> transcoder);
    <T extends CouchbaseDocument> Observable<T> asyncGet(final String id, final ICouchbaseTranscoder<T> transcoder, ReadParams params);
    //Observable<JsonDocument> getAndTouch(String id, int expiry);
    //<D extends Document<?>> Observable<Boolean> touch(String id, int expiry);
    //<D extends Document<?>> Observable<Boolean> touch(D document);


    //Observable<JsonDocument> getAndLock(String id, int lockTime);
    //Observable<Boolean> unlock(D document);
    //Observable<Boolean> unlock(String id,long cas);

    <T extends CouchbaseDocument> T add(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    <T extends CouchbaseDocument> T add(final T doc, final ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException;
    <T extends CouchbaseDocument> Observable<T> asyncAdd(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    <T extends CouchbaseDocument> Observable<T> asyncAdd(final T doc, final ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException;


    <T extends CouchbaseDocument> T set(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    <T extends CouchbaseDocument> T set(final T doc, final ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException;
    <T extends CouchbaseDocument> Observable<T> asyncSet(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    <T extends CouchbaseDocument> Observable<T> asyncSet(final T doc, final ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException;

    <T extends CouchbaseDocument> T replace(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    <T extends CouchbaseDocument> T replace(final T doc, final ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException;
    <T extends CouchbaseDocument> Observable<T> asyncReplace(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    <T extends CouchbaseDocument> Observable<T> asyncReplace(final T doc, final ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException;

    <T extends CouchbaseDocument> T delete(T bucketDoc, ICouchbaseTranscoder<T> transcoder) throws StorageException;
    <T extends CouchbaseDocument> T delete(T bucketDoc, ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException;
    <T extends CouchbaseDocument> Observable<T> asyncDelete(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    <T extends CouchbaseDocument> Observable<T> asyncDelete(final T doc, final ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException;

    <T extends CouchbaseDocument> T append(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    <T extends CouchbaseDocument> T append(final T doc, final ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException;
    <T extends CouchbaseDocument> Observable<T> asyncAppend(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    <T extends CouchbaseDocument> Observable<T> asyncAppend(final T doc, final ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException;

    <T extends CouchbaseDocument> T prepend(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    <T extends CouchbaseDocument> T prepend(final T doc, final ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException;
    <T extends CouchbaseDocument> Observable<T> asyncPrepend(final T doc, final ICouchbaseTranscoder<T> transcoder) throws StorageException;
    <T extends CouchbaseDocument> Observable<T> asyncPrepend(final T doc, final ICouchbaseTranscoder<T> transcoder, WriteParams params) throws StorageException;

    Long counter(String key, Long by, Long defaultValue, Integer expiration) throws StorageException;
    Long counter(String key, Long by, Long defaultValue, Integer expiration, WriteParams params) throws StorageException;

    Observable<Long> asyncCounter(String key, Long by, Long defaultValue, Integer expiration)throws StorageException;
    Observable<Long> asyncCounter(String key, Long by, Long defaultValue, Integer expiration, WriteParams params)throws StorageException;
    Long counter(String key, Long by, Long defaultValue) throws StorageException;
    Long counter(String key, Long by, Long defaultValue, WriteParams params) throws StorageException;
    Observable<Long> asyncCounter(String key, Long by, Long defaultValue)throws StorageException;
    Observable<Long> asyncCounter(String key, Long by, Long defaultValue, WriteParams params)throws StorageException;
    Long counter(String key, Long by) throws StorageException;
    Long counter(String key, Long by, WriteParams params) throws StorageException;
    Observable<Long> asyncCounter(String key, Long by)throws StorageException;
    Observable<Long> asyncCounter(String key, Long by, WriteParams params)throws StorageException;


    String getPrefix();

    void createOrUpdateView(String designDoc, Map<String, String> viewMap) throws StorageException;

    Observable<AsyncViewResult> asyncQuery(ViewQuery query);
    ViewResult query(ViewQuery query);

    void start(long timeout, TimeUnit unit);
    void start();

    boolean shutdown(long timeout, TimeUnit unit);
    void shutdown();

    class Utils{
        public static final Character KEY_SEP='$';
        public static final String KEY_WITH_PREFIX_FMT="%s"+KEY_SEP+"%s";

        public static String buildDesignDoc(String prefix,String designDocName){
            if(prefix!=null){
                return String.format(KEY_WITH_PREFIX_FMT,prefix,designDocName);
            }
            else{
                return designDocName;
            }
        }

        public static String buildKey(String prefix,String key){
            if(prefix!=null){
                return String.format(KEY_WITH_PREFIX_FMT,prefix,key);
            }
            else{
                return key;
            }
        }

        public static String extractKey(String prefix,String key){
            if((prefix!=null) && key.startsWith(prefix+KEY_SEP)){
                return key.substring(prefix.length()+1);
            }
            else{
                return key;
            }
        }
    }

}
