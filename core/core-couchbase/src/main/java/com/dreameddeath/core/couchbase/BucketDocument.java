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

import com.couchbase.client.core.message.kv.MutationToken;
import com.couchbase.client.java.document.Document;
import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by Christophe Jeunesse on 10/10/2014.
 */
public abstract class BucketDocument<T extends CouchbaseDocument> implements Document<T> {
    final private T doc;
    private String keyPrefix=null;

    public BucketDocument(T doc){
        this.doc = doc;
    }

    @Override
    public String id() {
        return ICouchbaseBucket.Utils.buildKey(keyPrefix,doc.getBaseMeta().getKey());
    }

    @Override
    public T content() {
        return doc;
    }

    @Override
    public long cas() {
        return doc.getBaseMeta().getCas();
    }

    @Override
    public int expiry() {
        return doc.getBaseMeta().getExpiry();
    }

    public void syncMeta(BucketDocument<T> ref){
        doc.getBaseMeta().setCas(ref.doc.getBaseMeta().getCas());
        doc.getBaseMeta().setExpiry(ref.doc.getBaseMeta().getExpiry());
        doc.getBaseMeta().setDbSize(ref.doc.getBaseMeta().getDbSize());
        doc.getBaseMeta().setBucketName(ref.doc.getBaseMeta().getBucketName());
        if(ref.mutationToken()!=null){
            doc.getBaseMeta().setVbucketID(ref.mutationToken().vbucketID());
            doc.getBaseMeta().setVbucketUUID(ref.mutationToken().vbucketUUID());
            doc.getBaseMeta().setSequenceNumber(ref.mutationToken().sequenceNumber());
            doc.getBaseMeta().setBucketName(ref.mutationToken().bucket());
        }
    }

    
    public T getDocument(){
        return doc;
    }

    public MutationToken mutationToken(){
        if(doc.getBaseMeta().getVbucketID()!=0){
            return new MutationToken(doc.getBaseMeta().getVbucketID(),doc.getBaseMeta().getVbucketUUID(),doc.getBaseMeta().getSequenceNumber(),doc.getBaseMeta().getBucketName());
        }
        else{
            return null;
        }
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        if(doc!=null){
            ICouchbaseBucket.Utils.cleanDocKey(keyPrefix,doc);
        }
        this.keyPrefix = keyPrefix;
    }

    public BucketDocument<T> withKeyPrefix(String prefix){
        setKeyPrefix(prefix);
        return this;
    }
}
