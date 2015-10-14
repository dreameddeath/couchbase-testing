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

package com.dreameddeath.core.model.document;

import com.couchbase.client.core.message.kv.MutationToken;
import com.couchbase.client.java.document.Document;

/**
 * Created by Christophe Jeunesse on 10/10/2014.
 */
public class BucketDocument<T extends BaseCouchbaseDocument> implements Document<T> {
    final private T doc;

    public BucketDocument(T doc){ this.doc = doc;}

    @Override
    public String id() {
        return doc.getBaseMeta().getKey();
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

    @Override
    public MutationToken mutationToken() {
        return null;
    }

    public void syncCas(BucketDocument<T> ref){
        doc.getBaseMeta().setCas(ref.doc.getBaseMeta().getCas());
        doc.getBaseMeta().setExpiry(ref.doc.getBaseMeta().getExpiry());
        doc.getBaseMeta().setDbSize(ref.doc.getBaseMeta().getDbSize());
    }

    public T getDocument(){
        return doc;
    }
}
