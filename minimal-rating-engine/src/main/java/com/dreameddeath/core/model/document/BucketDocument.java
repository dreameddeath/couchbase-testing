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

import com.couchbase.client.java.document.Document;

/**
 * Created by Christophe Jeunesse on 10/10/2014.
 */
public class BucketDocument<T extends BaseCouchbaseDocument> implements Document<T> {
    final private T _doc;

    public BucketDocument(T doc){ _doc = doc;}
    @Override
    public String id() {
        return _doc.getBaseMeta().getKey();
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

    public void syncCas(BucketDocument<T> ref){
        _doc.getBaseMeta().setCas(ref._doc.getBaseMeta().getCas());
        _doc.getBaseMeta().setExpiry(ref._doc.getBaseMeta().getExpiry());
        _doc.getBaseMeta().setDbSize(ref._doc.getBaseMeta().getDbSize());
    }

    public T getDocument(){
        return _doc;
    }
}
