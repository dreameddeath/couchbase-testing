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

package com.dreameddeath.infrastructure.daemon.couchbase;

import com.dreameddeath.core.couchbase.impl.CouchbaseBucketFactory;
import com.dreameddeath.core.couchbase.impl.CouchbaseClusterFactory;

/**
 * Created by Christophe Jeunesse on 13/10/2015.
 */
public class DaemonCouchbaseFactories {
    private final CouchbaseClusterFactory clusterFactory;
    private final CouchbaseBucketFactory bucketFactory;

    public DaemonCouchbaseFactories(CouchbaseClusterFactory clusterFactory, CouchbaseBucketFactory bucketFactory) {
        this.clusterFactory = clusterFactory;
        this.bucketFactory = bucketFactory;
    }


    public CouchbaseClusterFactory getClusterFactory() {
        return clusterFactory;
    }

    public CouchbaseBucketFactory getBucketFactory() {
        return bucketFactory;
    }

}
