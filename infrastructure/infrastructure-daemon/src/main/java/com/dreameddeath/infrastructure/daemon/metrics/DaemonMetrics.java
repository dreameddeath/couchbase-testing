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

package com.dreameddeath.infrastructure.daemon.metrics;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.jvm.*;
import com.codahale.metrics.logback.InstrumentedAppender;
import com.couchbase.client.core.event.CouchbaseEvent;
import com.couchbase.client.core.event.metrics.NetworkLatencyMetricsEvent;
import com.couchbase.client.core.event.metrics.RuntimeMetricsEvent;
import com.dreameddeath.infrastructure.daemon.model.DaemonMetricsInfo;
import org.slf4j.LoggerFactory;
import rx.Subscriber;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by Christophe Jeunesse on 29/11/2015.
 */
public class DaemonMetrics {
    private final MetricRegistry metricRegistry = new MetricRegistry();
    private NetworkLatencyMetricsEvent lastCouchbaseLatencyMetricEvent=null;
    private RuntimeMetricsEvent lastCouchbaseRuntimeMetricEvent=null;
    private CouchbaseMetricSubscriber metricSubscriber=null;

    public DaemonMetrics() {
        final LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
        final Logger root = factory.getLogger(Logger.ROOT_LOGGER_NAME);

        final InstrumentedAppender metrics = new InstrumentedAppender(metricRegistry);
        metrics.setContext(root.getLoggerContext());
        metrics.start();
        root.addAppender(metrics);

        MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
        metricRegistry.registerAll(new BufferPoolMetricSet(beanServer));
        metricRegistry.registerAll(new CachedThreadStatesGaugeSet(1, TimeUnit.MINUTES));
        metricRegistry.registerAll(new ClassLoadingGaugeSet());
        metricRegistry.register("file.descr.ratio.gauge", new FileDescriptorRatioGauge());
        metricRegistry.registerAll(new GarbageCollectorMetricSet());
        metricRegistry.registerAll(new MemoryUsageGaugeSet());

        final Slf4jReporter reporter = Slf4jReporter.forRegistry(metricRegistry)
                .outputTo(LoggerFactory.getLogger(DaemonMetrics.class))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(1, TimeUnit.MINUTES);
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public DaemonMetricsInfo getMetrics(){
        DaemonMetricsInfo infos = new DaemonMetricsInfo();
        infos.setMetricRegistry(metricRegistry);
        if((lastCouchbaseLatencyMetricEvent!=null)||(lastCouchbaseRuntimeMetricEvent!=null)){
            DaemonMetricsInfo.CouchbaseMetrics couchbaseMetrics = new DaemonMetricsInfo.CouchbaseMetrics();
            infos.setCouchbaseMetrics(couchbaseMetrics);
            if(lastCouchbaseLatencyMetricEvent!=null){
                couchbaseMetrics.setLatency(lastCouchbaseLatencyMetricEvent.toMap());
            }
            if(lastCouchbaseRuntimeMetricEvent!=null){
                couchbaseMetrics.setRuntime(lastCouchbaseRuntimeMetricEvent.toMap());
            }
        }
        return infos;
    }

    public NetworkLatencyMetricsEvent getLastCouchbaseLatencyMetricEvent() {
        return lastCouchbaseLatencyMetricEvent;
    }

    public RuntimeMetricsEvent getLastCouchbaseRuntimeMetricEvent() {
        return lastCouchbaseRuntimeMetricEvent;
    }

    public Subscriber<CouchbaseEvent> getMetricEventSubscriber(){
        if(metricSubscriber==null){
            synchronized (this){
                if(metricSubscriber==null){
                    metricSubscriber = new CouchbaseMetricSubscriber();
                }
            }
        }
        return metricSubscriber;
    }

    public class CouchbaseMetricSubscriber extends Subscriber<CouchbaseEvent>{

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
    };
}
