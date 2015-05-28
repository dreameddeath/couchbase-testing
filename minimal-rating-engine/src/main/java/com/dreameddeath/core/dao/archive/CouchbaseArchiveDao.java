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

package com.dreameddeath.core.dao.archive;

import com.couchbase.client.java.transcoder.Transcoder;
import com.dreameddeath.core.couchbase.CouchbaseBucketWrapper;
import com.dreameddeath.core.model.business.CouchbaseDocument;
import com.dreameddeath.core.model.document.BucketDocument;

/**
 * Created by Christophe Jeunesse on 17/09/2014.
 */
public abstract class CouchbaseArchiveDao<T extends CouchbaseDocument> {
    private CouchbaseBucketWrapper _client;

    public abstract Transcoder<BucketDocument<T>,T> getTranscoder();

    protected CouchbaseBucketWrapper getClientWrapper(){
        return _client;
    }

    public CouchbaseArchiveDao(CouchbaseBucketWrapper client,String key, Integer expiration){
        _client = client;
        Integer _expiration = expiration;
    }
}
