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
public class CouchbaseBucketFactory implements ICouchbaseBucketFactory,AutoCloseable {
    private final ICouchbaseClusterFactory clusterFactory;
    private final Map<String,ICouchbaseBucket> couchbaseBucketMap = new HashMap<>();
    private boolean autoStart;

    public CouchbaseBucketFactory(ICouchbaseClusterFactory clusterFactory){
        this.clusterFactory=clusterFactory;
        autoStart=false;
    }

    public CouchbaseBucketFactory(Builder builder){
        this.clusterFactory=builder.clusterFactory;
        autoStart=false;
    }


    public ICouchbaseClusterFactory getClusterFactory(){
        return clusterFactory;
    }

    protected ICouchbaseBucket buildCouchbaseBucket(final String name){
        try {
            String clusterName = CouchbaseConfigProperties.COUCHBASE_BUCKET_CLUSTER_NAME.getProperty(name).getMandatoryValue("Cannot find cluster name for bucket <{}>", name);
            String password = CouchbaseConfigProperties.COUCHBASE_BUCKET_PASSWORD_NAME.getProperty(name).getMandatoryValue("Cannot find password for bucket <{}>", name);
            CouchbaseCluster cluster = clusterFactory.getCluster(clusterName);
            ICouchbaseBucket bucket = new CouchbaseBucketWrapper(cluster, name, password);
            return bucket;
        }
        catch (ConfigPropertyValueNotFoundException e) {
            throw new RuntimeException("Cannot init bucket", e);
        }
    }


    @Override
    synchronized public ICouchbaseBucket getBucket(final String name) throws ConfigPropertyValueNotFoundException{
        try {
            ICouchbaseBucket bucket = couchbaseBucketMap.computeIfAbsent(name, name1 -> buildCouchbaseBucket(name));
            if(autoStart && !bucket.isStarted()){
                bucket.start();
            }
            return bucket;
        }
        catch(RuntimeException e){
            if(e.getCause() instanceof ConfigPropertyValueNotFoundException){
                throw (ConfigPropertyValueNotFoundException)e.getCause();
            }
            throw e;
        }
    }

    synchronized public void start() throws Exception {
        for(ICouchbaseBucket bucket : couchbaseBucketMap.values()){
            if(!bucket.isStarted()) {
                bucket.start();
            }
        }
    }



    @Override
    synchronized public void close() throws Exception {
        for(ICouchbaseBucket bucket : couchbaseBucketMap.values()){
            bucket.shutdown();
        }
        couchbaseBucketMap.clear();
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder{
        private ICouchbaseClusterFactory clusterFactory;

        public Builder(){
            clusterFactory = new CouchbaseClusterFactory();
        }

        public Builder withClusterFactory(ICouchbaseClusterFactory clusterFactory) {
            this.clusterFactory = clusterFactory;
            return this;
        }

        public CouchbaseBucketFactory build(){
            return new CouchbaseBucketFactory(this);
        }
    }
}
