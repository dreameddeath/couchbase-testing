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

package com.dreameddeath.testing.couchbase;

import com.dreameddeath.core.config.exception.ConfigPropertyValueNotFoundException;
import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.ICouchbaseClusterFactory;
import com.dreameddeath.core.couchbase.config.CouchbaseConfigProperties;
import com.dreameddeath.core.couchbase.impl.CouchbaseBucketFactory;

/**
 * Created by Christophe Jeunesse on 11/10/2015.
 */
public class CouchbaseBucketFactorySimulator extends CouchbaseBucketFactory{
    public CouchbaseBucketFactorySimulator(){
        this((ICouchbaseClusterFactory)null);
    }

    public CouchbaseBucketFactorySimulator(ICouchbaseClusterFactory factory){
        super(factory);
    }

    public CouchbaseBucketFactorySimulator(Builder builder){
        super(builder);
    }


    @Override
    protected ICouchbaseBucket buildCouchbaseBucket(final String name){
        if(getClusterFactory()!=null){
            try {
                String clusterName = CouchbaseConfigProperties.COUCHBASE_BUCKET_CLUSTER_NAME.getProperty(name).getMandatoryValue("Cannot find cluster name for bucket <{}>", name);
                getClusterFactory().getCluster(clusterName);
            }
            catch (ConfigPropertyValueNotFoundException e) {
                throw new RuntimeException("Cannot init bucket", e);
            }
        }

        return new CouchbaseBucketSimulator(name);
    }
}
