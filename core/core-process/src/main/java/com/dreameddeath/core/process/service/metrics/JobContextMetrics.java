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

package com.dreameddeath.core.process.service.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

/**
 * Created by Christophe Jeunesse on 03/01/2016.
 */
public class JobContextMetrics implements Closeable {
    protected final Timer totals;
    protected final Timer errors;
    protected final Counter inRequests;
    protected final Meter exchangedData;
    protected final String baseName;
    protected final MetricRegistry registry;

    public JobContextMetrics(String prefix, MetricRegistry registry) {
        this.baseName = prefix;
        this.registry = registry;

        if (registry != null) {
            totals = registry.timer(baseName + ",Attribute=Totals");
            inRequests = registry.counter(baseName + ",Attribute=In Requests");
            exchangedData = registry.meter(baseName + ",Attribute=Data Exchanged");
            errors = registry.timer(baseName + ",Attribute=Errors");
        }
        else {
            totals = null;
            errors = null;
            inRequests = null;
            exchangedData = null;
        }
    }

    @Override
    public void close() {
        if (registry != null) {
            registry.remove(baseName + ",Attribute=Totals");
            registry.remove(baseName + ",Attribute=In Requests");
            registry.remove(baseName + ",Attribute=Data Exchanged");
            registry.remove(baseName + ",Attribute=Errors");
        }
    }

    public MetricsContext start() {
        return new MetricsContext();
    }

    public class MetricsContext {
        private final long startTime = System.nanoTime();

        public MetricsContext() {
            if (registry != null) {
                inRequests.inc();
            }
        }

        public void stop(boolean success, Long size) {
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            if (registry != null) {
                totals.update(duration, TimeUnit.NANOSECONDS);
                if (size != null) {
                    exchangedData.mark(size);
                }
                if (!success) {
                    errors.update(duration, TimeUnit.NANOSECONDS);
                }
            }
        }

        public void stop(Throwable e){
            stop(false,null);
        }

    }
}