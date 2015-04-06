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

package com.dreameddeath.core.storage;


import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.Document;
import com.couchbase.client.java.error.DocumentAlreadyExistsException;
import com.couchbase.client.java.transcoder.Transcoder;
import com.dreameddeath.core.exception.storage.*;
import com.dreameddeath.core.model.document.BaseCouchbaseDocument;
import com.dreameddeath.core.model.document.BucketDocument;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;

//import java.util.concurrent.Future;

/**
*  Class used to perform storage 
*/
public class CouchbaseBucketWrapper {
    private Bucket _bucket;
    private final Cluster _cluster;
    private final String _bucketName;
    private final String _bucketPassword;
    private List<Transcoder<? extends Document, ?>> _transcoders = new ArrayList<Transcoder<? extends Document, ?>>();

    public CouchbaseBucketWrapper(CouchbaseCluster cluster,String bucketName,String bucketPassword){
        _cluster = cluster;
        _bucketName = bucketName;
        _bucketPassword = bucketPassword;
    }

    public CouchbaseBucketWrapper addTranscoder(GenericTranscoder transcoder){
        _transcoders.add(transcoder);
        return this;
    }

    public Bucket getBucket(){
        return _bucket;
    }

    public <T extends BaseCouchbaseDocument> T get(final String key,final Class<BucketDocument<T>> bucketDocClazz) throws StorageException{
        try {
            T result=asyncGet(key,bucketDocClazz).toBlocking().singleOrDefault(null);
            if(result==null){ throw new DocumentNotFoundException(key,"Cannot find document using key <"+key+">");}
            else{ return result; }
        }
        catch(DocumentNotFoundException e){ throw e; }
        catch(Throwable e){ throw new DocumentAccessException(key,"Error during document access attempt of <"+key+">",e); }

    }

    public <T extends BaseCouchbaseDocument> Observable<T> asyncGet(final String id,final Class<BucketDocument<T>> bucketDocClazz){
        return _bucket.async().get(id,bucketDocClazz).map(new Func1<BucketDocument<T>, T>() {
            @Override
            public T call(BucketDocument<T> tBucketDocument) {
                return tBucketDocument.content();
            }
        });
    }

    public <T extends BaseCouchbaseDocument> T add(final BucketDocument<T> bucketDoc) throws StorageException{
        try {
            T result=asyncAdd(bucketDoc).toBlocking().singleOrDefault(null);
            if(result==null){ throw new DuplicateDocumentKeyException(bucketDoc.content());}
            else{ return result; }
        }
        catch(DuplicateDocumentKeyException e){ throw e; }
        catch(DocumentAlreadyExistsException e){ throw new DuplicateDocumentKeyException(bucketDoc.content(),e); }
        catch(Throwable e){ throw new DocumentStorageException(bucketDoc.content(),"Error during fetch execution",e); }
    }

    public <T extends BaseCouchbaseDocument> Observable<T> asyncAdd(final BucketDocument<T> bucketDoc) throws StorageException{
        return _bucket.async().insert(bucketDoc).map(new Func1<BucketDocument<T>, T>() {
            @Override
            public T call(BucketDocument<T> tBucketDocument) {
                bucketDoc.syncCas(tBucketDocument);
                return bucketDoc.content();
            }
        });
    }

    public <T extends BaseCouchbaseDocument> T set(final BucketDocument<T> bucketDoc) throws StorageException{
        try {
            T result=asyncSet(bucketDoc).toBlocking().singleOrDefault(null);
            if(result==null){ throw new DocumentStorageException(bucketDoc.content(),"Cannot apply set method to the document");}
            else{ return result; }
        }
        catch(DuplicateDocumentKeyException e){ throw e; }
        catch(Throwable e){ throw new DocumentStorageException(bucketDoc.content(),"Error during fetch execution",e); }
    }

    public <T extends BaseCouchbaseDocument> Observable<T> asyncSet(final BucketDocument<T> bucketDoc) throws StorageException{
        return _bucket.async().upsert(bucketDoc).map(new Func1<BucketDocument<T>, T>() {
            @Override
            public T call(BucketDocument<T> tBucketDocument) {
                bucketDoc.syncCas(tBucketDocument);
                return bucketDoc.content();
            }
        });
    }

    public <T extends BaseCouchbaseDocument> T replace(final BucketDocument<T> bucketDoc) throws StorageException{
        try {
            T result=asyncReplace(bucketDoc).toBlocking().singleOrDefault(null);
            if(result==null){ throw new DocumentNotFoundException(bucketDoc.content(),"Cannot apply replace method");}
            else{ return result; }
        }
        catch(DocumentNotFoundException e){ throw e; }
        catch(Throwable e){ throw new DocumentStorageException(bucketDoc.content(),"Error during fetch execution",e); }
    }

    public <T extends BaseCouchbaseDocument> Observable<T> asyncReplace(final BucketDocument<T> bucketDoc) throws StorageException{
        return _bucket.async().upsert(bucketDoc).map(new Func1<BucketDocument<T>, T>() {
            @Override
            public T call(BucketDocument<T> tBucketDocument) {
                bucketDoc.syncCas(tBucketDocument);
                return bucketDoc.content();
            }
        });
    }

    public <T extends BaseCouchbaseDocument> T delete(final BucketDocument<T> bucketDoc) throws StorageException{
        try {
            T result=asyncDelete(bucketDoc).toBlocking().singleOrDefault(null);
            if(result==null){ throw new DocumentNotFoundException(bucketDoc.content(),"Cannot apply replace method");}
            else{ return result; }
        }
        catch(DocumentNotFoundException e){ throw e; }
        catch(Throwable e){  throw new DocumentStorageException(bucketDoc.content(),"Error during fetch execution",e); }
    }

    public <T extends BaseCouchbaseDocument> Observable<T> asyncDelete(final BucketDocument<T> bucketDoc) throws StorageException{
        return _bucket.async().remove(bucketDoc).map(new Func1<BucketDocument<T>, T>() {
            @Override
            public T call(BucketDocument<T> tBucketDocument) {
                bucketDoc.syncCas(tBucketDocument);
                return bucketDoc.content();
            }
        });
    }


    public <T extends BaseCouchbaseDocument> T append(final BucketDocument<T> bucketDoc) throws StorageException{
        try {
            T result=asyncAppend(bucketDoc).toBlocking().singleOrDefault(null);
            if(result==null){ throw new DocumentNotFoundException(bucketDoc.content(),"Cannot apply replace method");}
            else{ return result; }
        }
        catch(DocumentNotFoundException e){ throw e; }
        catch(Throwable e){  throw new DocumentStorageException(bucketDoc.content(),"Error during fetch execution",e); }
    }

    public <T extends BaseCouchbaseDocument> Observable<T> asyncAppend(final BucketDocument<T> bucketDoc) throws StorageException{
        return _bucket.async().append(bucketDoc).map(new Func1<BucketDocument<T>, T>() {
            @Override
            public T call(BucketDocument<T> tBucketDocument) {
                bucketDoc.syncCas(tBucketDocument);
                return bucketDoc.content();
            }
        });
    }

    public <T extends BaseCouchbaseDocument> T prepend(final BucketDocument<T> bucketDoc) throws StorageException{
        try {
            T result=asyncAppend(bucketDoc).toBlocking().singleOrDefault(null);
            if(result==null){ throw new DocumentNotFoundException(bucketDoc.content(),"Cannot apply replace method");}
            else{ return result; }
        }
        catch(DocumentNotFoundException e){ throw e; }
        catch(Throwable e){  throw new DocumentStorageException(bucketDoc.content(),"Error during fetch execution",e); }
    }

    public <T extends BaseCouchbaseDocument> Observable<T> asyncPrepend(final BucketDocument<T> bucketDoc) throws StorageException{
        return _bucket.async().prepend(bucketDoc).map(new Func1<BucketDocument<T>, T>() {
            @Override
            public T call(BucketDocument<T> tBucketDocument) {
                bucketDoc.syncCas(tBucketDocument);
                return bucketDoc.content();
            }
        });
    }

    public void start(long timeout,java.util.concurrent.TimeUnit unit){
        _bucket = _cluster.openBucket(_bucketName,_bucketPassword,_transcoders,timeout,unit);
    }

    public void start(){
        _bucket = _cluster.openBucket(_bucketName,_bucketPassword,_transcoders);
    }

    public boolean shutdown(long timeout,java.util.concurrent.TimeUnit unit){
        return _bucket.close(timeout,unit);
    }
    public void shutdown(){
        _bucket.close();
    }
}
