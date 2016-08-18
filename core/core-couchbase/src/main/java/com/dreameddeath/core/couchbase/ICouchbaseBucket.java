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

package com.dreameddeath.core.couchbase;

import com.couchbase.client.core.CouchbaseException;
import com.couchbase.client.java.error.CASMismatchException;
import com.couchbase.client.java.error.DocumentAlreadyExistsException;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.couchbase.client.java.view.AsyncViewResult;
import com.couchbase.client.java.view.ViewQuery;
import com.dreameddeath.core.couchbase.exception.*;
import com.dreameddeath.core.couchbase.impl.ReadParams;
import com.dreameddeath.core.couchbase.impl.WriteParams;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import rx.Observable;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Christophe Jeunesse on 21/11/2014.
 */
public interface ICouchbaseBucket {
    ICouchbaseBucket addTranscoder(ICouchbaseTranscoder transcoder);
    IBlockingCouchbaseBucket toBlocking();
    <T extends CouchbaseDocument> Observable<T> asyncGet(final String id,Class<T> entity);
    <T extends CouchbaseDocument> Observable<T> asyncGet(final String id,Class<T> entity, ReadParams params);

    <T extends CouchbaseDocument> Observable<T> asyncAdd(final T doc);
    <T extends CouchbaseDocument> Observable<T> asyncAdd(final T doc, WriteParams params);


    <T extends CouchbaseDocument> Observable<T> asyncSet(final T doc);
    <T extends CouchbaseDocument> Observable<T> asyncSet(final T doc, WriteParams params);

    <T extends CouchbaseDocument> Observable<T> asyncReplace(final T doc);
    <T extends CouchbaseDocument> Observable<T> asyncReplace(final T doc, WriteParams params);

    <T extends CouchbaseDocument> Observable<T> asyncDelete(final T doc);
    <T extends CouchbaseDocument> Observable<T> asyncDelete(final T doc, WriteParams params);

    <T extends CouchbaseDocument> Observable<T> asyncAppend(final T doc);
    <T extends CouchbaseDocument> Observable<T> asyncAppend(final T doc, WriteParams params);

    <T extends CouchbaseDocument> Observable<T> asyncPrepend(final T doc);
    <T extends CouchbaseDocument> Observable<T> asyncPrepend(final T doc, WriteParams params);


    Observable<Long> asyncCounter(String key, Long by, Long defaultValue, Integer expiration);
    Observable<Long> asyncCounter(String key, Long by, Long defaultValue, Integer expiration, WriteParams params);
    Observable<Long> asyncCounter(String key, Long by, Long defaultValue);
    Observable<Long> asyncCounter(String key, Long by, Long defaultValue, WriteParams params);
    Observable<Long> asyncCounter(String key, Long by);
    Observable<Long> asyncCounter(String key, Long by, WriteParams params);

    void createOrUpdateView(String designDoc, Map<String, String> viewMap) throws StorageException;

    Observable<AsyncViewResult> asyncQuery(ViewQuery query);

    void start(long timeout, TimeUnit unit);
    void start();
    boolean isStarted();
    boolean shutdown(long timeout, TimeUnit unit);
    void shutdown();

    String getBucketName();

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
                return key.substring(prefix.length() + 1);
            }
            else{
                return key;
            }
        }

        public static <T extends CouchbaseDocument> T cleanDocKey(String prefix,T doc){
            if((prefix!=null) && doc.getBaseMeta().getKey().startsWith(prefix+KEY_SEP)){
                doc.getBaseMeta().setKey(doc.getBaseMeta().getKey().substring(prefix.length()+1));
            }
            return doc;
        }


        public static <T extends CouchbaseDocument> Observable<T> mapObservableStorageException(T doc,Throwable e){
            throw new StorageObservableException(mapStorageException(doc,e));
        }


        private static <T extends CouchbaseDocument> StorageException mapStorageException(T doc,Throwable e){
            if(e instanceof StorageException){
                return (StorageException)e;
            }
            else if(e instanceof StorageObservableException){
                return (StorageException)e.getCause();
            }

            Throwable rootException;
            if(e instanceof CouchbaseException){
                rootException = e;
            }
            else {
                rootException = e.getCause();
                if (rootException == null) {
                    rootException = e;
                }
            }
            if(rootException instanceof InterruptedException){
                return new DocumentStorageTimeOutException(doc,"Interruption occurs",rootException);
            }
            else if(rootException instanceof CASMismatchException){
                return new DocumentConcurrentUpdateException(doc,"Concurrent access append",rootException);
            }
            else if(rootException instanceof DocumentAlreadyExistsException){
                return new DuplicateDocumentKeyException(doc,rootException);
            }
            else if(rootException instanceof DocumentDoesNotExistException){
                return new DocumentNotFoundException(doc,rootException);
            }
            return new DocumentStorageException(doc,"Error during storage attempt execution",e);
        }

        public static <T extends CouchbaseDocument> Observable<Long> mapObservableStorageException(String key,Throwable e){
            throw new StorageObservableException(mapStorageException(key,e));
        }


        private static StorageException mapStorageException(String key,Throwable e){
            if(e instanceof StorageException){
                return (StorageException)e;
            }
            else if(e instanceof StorageObservableException){
                return (StorageException)e.getCause();
            }
            Throwable rootException =e.getCause();
            if(rootException==null){
                rootException=e;
            }
            if(rootException instanceof InterruptedException){
                return new DocumentStorageTimeOutException(key,"Interruption occurs",rootException);
            }
            else if(rootException instanceof CASMismatchException){
                return new DocumentConcurrentUpdateException(key,"Concurrent access append",rootException);
            }
            else if(rootException instanceof DocumentDoesNotExistException){
                return new DocumentNotFoundException(key,rootException);
            }
            return new DocumentStorageException(key,"Error during storage attempt execution",e);
        }


        public static <T extends CouchbaseDocument> Observable<T>  mapObservableAccessException(String key,Throwable e,Class<T> clazz){
            throw new StorageObservableException(mapAccessException(key,e));
        }

        private static StorageException mapAccessException(String key,Throwable e){
            Throwable rootException =e.getCause();
            if(rootException==null){
                rootException=e;
            }
            if(e instanceof StorageException){
                return (StorageException)e;
            }
            else if(e instanceof StorageObservableException){
                return (StorageException)e.getCause();
            }
            else if(rootException instanceof InterruptedException){
                return new DocumentStorageTimeOutException(key,"Interruption occurs",rootException);
            }
            else if(rootException instanceof DocumentDoesNotExistException){
                return new DocumentNotFoundException(key,rootException);
            }
            return new DocumentAccessException(key,"Error during storage attempt execution",e);
        }


    }

}
