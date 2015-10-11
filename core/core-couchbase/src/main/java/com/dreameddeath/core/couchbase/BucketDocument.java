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
    final private T _doc;
    private String _keyPrefix=null;

    public BucketDocument(T doc){ _doc = doc;}
    @Override
    public String id() {
        return ICouchbaseBucket.Utils.buildKey(_keyPrefix,_doc.getBaseMeta().getKey());
    }

    @Override
    public T content() {
        return _doc;
    }

    @Override
    public long cas() {
        return _doc.getBaseMeta().getCas();
    }

    @Override
    public int expiry() {
        return _doc.getBaseMeta().getExpiry();
    }

    public void syncMeta(BucketDocument<T> ref){
        _doc.getBaseMeta().setCas(ref._doc.getBaseMeta().getCas());
        _doc.getBaseMeta().setExpiry(ref._doc.getBaseMeta().getExpiry());
        _doc.getBaseMeta().setDbSize(ref._doc.getBaseMeta().getDbSize());
        if(ref.mutationToken()!=null){
            _doc.getBaseMeta().setVbucketID(ref.mutationToken().vbucketID());
            _doc.getBaseMeta().setVbucketUUID(ref.mutationToken().vbucketUUID());
            _doc.getBaseMeta().setSequenceNumber(ref.mutationToken().sequenceNumber());
        }
    }

    
    public T getDocument(){
        return _doc;
    }

    public MutationToken mutationToken(){
        if(_doc.getBaseMeta().getVbucketID()!=0){
            return new MutationToken(_doc.getBaseMeta().getVbucketID(),_doc.getBaseMeta().getVbucketUUID(),_doc.getBaseMeta().getSequenceNumber());
        }
        else{
            return null;
        }
    }

    public String getKeyPrefix() {
        return _keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        _keyPrefix = keyPrefix;
    }
}
