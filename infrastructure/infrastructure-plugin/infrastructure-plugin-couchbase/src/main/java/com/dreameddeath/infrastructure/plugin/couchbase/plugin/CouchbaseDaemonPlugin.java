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

package com.dreameddeath.infrastructure.plugin.couchbase.plugin;

import com.dreameddeath.core.couchbase.impl.CouchbaseBucketFactory;
import com.dreameddeath.core.couchbase.impl.CouchbaseClusterFactory;
import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.model.DaemonMetricsInfo;
import com.dreameddeath.infrastructure.daemon.plugin.AbstractDaemonPlugin;
import com.dreameddeath.infrastructure.daemon.plugin.IDaemonPluginBuilder;
import com.dreameddeath.infrastructure.plugin.couchbase.lifecycle.CouchbaseDaemonLifeCycle;
import com.dreameddeath.infrastructure.plugin.couchbase.metrics.CouchbaseMetricsSubscriber;

/**
 * Created by Christophe Jeunesse on 20/12/2015.
 */
public class CouchbaseDaemonPlugin extends AbstractDaemonPlugin {
    private final CouchbaseClusterFactory clusterFactory;
    private final CouchbaseBucketFactory bucketFactory;
    private CouchbaseMetricsSubscriber metricSubscriber=null;

    public CouchbaseDaemonPlugin(AbstractDaemon daemon,Builder builder) {
        super(daemon);
        if(builder.clusterFactory==null){
            builder.withClusterFactory(CouchbaseClusterFactory.builder().withMetricSubscriber(getMetricEventSubscriber()).build());
        }
        if(builder.bucketFactory==null){
            builder.withBucketFactory(
                    CouchbaseBucketFactory.builder()
                            .withClusterFactory(builder.clusterFactory)
                            .withMetricRegistry(daemon.getDaemonMetrics().getMetricRegistry())
                            .build()
            );
        }
        this.clusterFactory =builder.clusterFactory;
        this.bucketFactory= builder.bucketFactory;

        //daemonCouchbaseFactories = new DaemonCouchbaseFactories(builder.clusterFactory,builder.bucketFactory);
        daemon.getDaemonLifeCycle().addLifeCycleListener(new CouchbaseDaemonLifeCycle(this));
    }

    public CouchbaseMetricsSubscriber getMetricEventSubscriber(){
        if(metricSubscriber==null){
            synchronized (this){
                if(metricSubscriber==null){
                    metricSubscriber = new CouchbaseMetricsSubscriber();
                }
            }
        }
        return metricSubscriber;
    }


    public CouchbaseBucketFactory getBucketFactory() {
        return bucketFactory;
    }

    public CouchbaseClusterFactory getClusterFactory() {
        return clusterFactory;
    }

    @Override
    public void enrichMetrics(DaemonMetricsInfo info) {
        getMetricEventSubscriber().enrich(info);
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder implements IDaemonPluginBuilder<CouchbaseDaemonPlugin>{
        private CouchbaseClusterFactory clusterFactory =null;
        private CouchbaseBucketFactory bucketFactory =null;

        public Builder withClusterFactory(CouchbaseClusterFactory withClusterFactory) {
            this.clusterFactory = withClusterFactory;
            return this;
        }

        public Builder withBucketFactory(CouchbaseBucketFactory withBucketFactory) {
            this.bucketFactory = withBucketFactory;
            return this;
        }

        @Override
        public CouchbaseDaemonPlugin build(AbstractDaemon parentDeaemon) {
            return new CouchbaseDaemonPlugin(parentDeaemon,this);
        }
    }
}
