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

package com.dreameddeath.infrastructure.plugin.couchbase.metrics;

import com.couchbase.client.core.event.CouchbaseEvent;
import com.couchbase.client.core.event.metrics.NetworkLatencyMetricsEvent;
import com.couchbase.client.core.event.metrics.RuntimeMetricsEvent;
import com.dreameddeath.infrastructure.daemon.model.DaemonMetricsInfo;
import rx.Subscriber;

/**
 * Created by Christophe Jeunesse on 20/12/2015.
 */
public class CouchbaseMetricsSubscriber extends Subscriber<CouchbaseEvent> {
    private NetworkLatencyMetricsEvent lastCouchbaseLatencyMetricEvent=null;
    private RuntimeMetricsEvent lastCouchbaseRuntimeMetricEvent=null;

    public NetworkLatencyMetricsEvent getLastCouchbaseLatencyMetricEvent() {
        return lastCouchbaseLatencyMetricEvent;
    }

    public RuntimeMetricsEvent getLastCouchbaseRuntimeMetricEvent() {
        return lastCouchbaseRuntimeMetricEvent;
    }

    public void enrich(DaemonMetricsInfo infos){
        if((lastCouchbaseLatencyMetricEvent!=null)||(lastCouchbaseRuntimeMetricEvent!=null)){
            CouchbaseMetrics couchbaseMetrics = new CouchbaseMetrics();
            if(lastCouchbaseLatencyMetricEvent!=null){
                couchbaseMetrics.setLatency(lastCouchbaseLatencyMetricEvent.toMap());
            }
            if(lastCouchbaseRuntimeMetricEvent!=null){
                couchbaseMetrics.setRuntime(lastCouchbaseRuntimeMetricEvent.toMap());
            }
            infos.putPluginMetrics("couchbase",couchbaseMetrics);
        }

    }

    @Override
    public void onCompleted() {}

    @Override
    public void onError(Throwable throwable) {}

    @Override
    public void onNext(CouchbaseEvent couchbaseEvent) {
        if(couchbaseEvent instanceof NetworkLatencyMetricsEvent){
            lastCouchbaseLatencyMetricEvent = (NetworkLatencyMetricsEvent) couchbaseEvent;
        }
        else if(couchbaseEvent instanceof RuntimeMetricsEvent){
            lastCouchbaseRuntimeMetricEvent = (RuntimeMetricsEvent) couchbaseEvent;
        }
    }
}