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
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.dreameddeath.core.config.exception.ConfigPropertyValueNotFoundException;
import com.dreameddeath.core.couchbase.ICouchbaseClusterFactory;
import com.dreameddeath.core.couchbase.config.CouchbaseConfigProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 10/10/2015.
 */
public class CouchbaseClusterFactory implements ICouchbaseClusterFactory,AutoCloseable {
    private final Boolean autoCreatedEnv;
    private final CouchbaseEnvironment env;
    private Map<String,CouchbaseCluster> couchbaseClusterMap = new HashMap<>();


    public CouchbaseClusterFactory(CouchbaseEnvironment env){
        this(builder().withCouchbaseEnv(env));
    }

    public CouchbaseClusterFactory(Builder builder){
        if(builder.couchbaseEnv==null){
            this.env = DefaultCouchbaseEnvironment.create();
            this.autoCreatedEnv = true;
        }
        else{
            this.env = builder.couchbaseEnv;
            this.autoCreatedEnv=false;
        }
    }


    public CouchbaseClusterFactory(){
        this(builder());
    }

    @Override
    synchronized public CouchbaseCluster getCluster(final String name)throws ConfigPropertyValueNotFoundException{
            try {
                return couchbaseClusterMap.computeIfAbsent(name, name1 -> {
                    try {
                        List<String> clusterAddresses = CouchbaseConfigProperties.COUCHBASE_CLUSTER_ADDRESSES.getProperty(name).getMandatoryValue("Please define the addresses of cluster <{}>", name);
                        return CouchbaseCluster.create(env, clusterAddresses);
                    } catch (ConfigPropertyValueNotFoundException e) {
                        throw new RuntimeException("Cluster setup failure", e);
                    }
                });
            }
            catch(RuntimeException e){
                if(e.getCause() instanceof ConfigPropertyValueNotFoundException){
                    throw (ConfigPropertyValueNotFoundException)e.getCause();
                }
                else{
                    throw e;
                }
            }
    }

    synchronized public void stop(){
        for(CouchbaseCluster cluster:couchbaseClusterMap.values()){
            cluster.disconnect();
        }
    }

    @Override
    synchronized public void close(){
        stop();
        if(autoCreatedEnv){
            env.shutdown();
        }
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder{
        private CouchbaseEnvironment couchbaseEnv =null;

        public Builder withCouchbaseEnv(CouchbaseEnvironment couchbaseEnv) {
            this.couchbaseEnv = couchbaseEnv;
            return this;
        }

        public CouchbaseClusterFactory build(){
            return new CouchbaseClusterFactory(this);
        }
    }
}
