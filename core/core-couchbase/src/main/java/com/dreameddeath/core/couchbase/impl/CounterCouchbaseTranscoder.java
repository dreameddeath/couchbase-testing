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

package com.dreameddeath.core.couchbase.impl;

import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.model.counter.CouchbaseCounter;

/**
 * Created by Christophe Jeunesse on 12/06/2015.
 */
public final class CounterCouchbaseTranscoder extends GenericCouchbaseTranscoder<CouchbaseCounter> {
    public static class CounterBucketDocument extends BucketDocument<CouchbaseCounter>{
        public CounterBucketDocument(CouchbaseCounter doc) {
            super(doc);
        }
    }
    
    public CounterCouchbaseTranscoder() {
        super(CouchbaseCounter.class, CounterBucketDocument.class);
    }
}
