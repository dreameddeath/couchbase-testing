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

import com.couchbase.client.java.CouchbaseCluster;
import com.dreameddeath.core.config.exception.ConfigPropertyValueNotFoundException;
import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.ICouchbaseBucketFactory;
import com.dreameddeath.core.couchbase.ICouchbaseClusterFactory;
import com.dreameddeath.core.couchbase.config.CouchbaseConfigProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 10/10/2015.
 */
public class CouchbaseBucketFactory implements ICouchbaseBucketFactory {
    private final ICouchbaseClusterFactory _clusterFactory;
    private final Map<String,ICouchbaseBucket> _couchbaseBucketMap = new HashMap<>();
    public CouchbaseBucketFactory(ICouchbaseClusterFactory clusterFactory){
        _clusterFactory=clusterFactory;
    }

    @Override
    public ICouchbaseBucket getBucket(final String name) throws ConfigPropertyValueNotFoundException{
        return getBucket(name,null);
    }

    @Override
    synchronized public ICouchbaseBucket getBucket(final String name,final String prefix) throws ConfigPropertyValueNotFoundException{
        try {
            return _couchbaseBucketMap.computeIfAbsent(name + ((prefix != null) ? "#" + prefix : ""), name1 -> {
                try {
                    String clusterName = CouchbaseConfigProperties.COUCHBASE_BUCKET_CLUSTER_NAME.getProperty(name).getMandatoryValue("Cannot find cluster name for bucket <{}>", name);
                    String password = CouchbaseConfigProperties.COUCHBASE_BUCKET_PASSWORD_NAME.getProperty(name).getMandatoryValue("Cannot find password for bucket <{}>", name);
                    CouchbaseCluster cluster = _clusterFactory.getCluster(clusterName);
                    return new CouchbaseBucketWrapper(cluster, name, password, prefix);
                } catch (ConfigPropertyValueNotFoundException e) {
                    throw new RuntimeException("Cannot init bucket", e);
                }
            });
        }
        catch(RuntimeException e){
            if(e.getCause() instanceof ConfigPropertyValueNotFoundException){
                throw (ConfigPropertyValueNotFoundException)e.getCause();
            }
            throw e;
        }
    }
}
