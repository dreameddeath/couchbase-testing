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
import com.dreameddeath.core.couchbase.annotation.BucketDocumentForClass;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;

/**
 * Created by Christophe Jeunesse on 27/02/2016.
 */
public class UniqueKeyCouchbaseTranscoder extends GenericCouchbaseTranscoder<CouchbaseUniqueKey> {

    @BucketDocumentForClass(CouchbaseUniqueKey.class)
    public static class LocalBucketDocument extends BucketDocument<CouchbaseUniqueKey>{
        public LocalBucketDocument(CouchbaseUniqueKey doc) {
            super(doc);
        }
    }


    public UniqueKeyCouchbaseTranscoder() {
        super(CouchbaseUniqueKey.class, LocalBucketDocument.class);
    }
}
