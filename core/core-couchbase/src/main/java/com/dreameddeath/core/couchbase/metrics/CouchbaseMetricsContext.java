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

package com.dreameddeath.core.couchbase.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.couchbase.client.java.document.JsonLongDocument;
import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import rx.Notification;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

/**
 * Created by Christophe Jeunesse on 14/12/2015.
 */
public class CouchbaseMetricsContext implements Closeable {
    protected final Timer totals;
    protected final Timer errors;
    protected final Counter inRequests;
    protected final Meter exchangedData;

    protected final String baseName;
    protected final MetricRegistry registry;

    public CouchbaseMetricsContext(String prefix, MetricRegistry registry) {
        this.baseName = prefix;
        this.registry = registry;

        if(registry!=null) {
            totals = registry.timer(baseName + ",Attribute=Totals");
            inRequests = registry.counter(baseName + ",Attribute=In Requests");
            exchangedData = registry.meter(baseName + ",Attribute=Data Exchanged");
            errors = registry.timer(baseName + ",Attribute=Errors");
        }
        else{
            totals=null;
            errors=null;
            inRequests=null;
            exchangedData=null;
        }
    }

    @Override
    public void close() {
        if(registry!=null) {
            registry.remove(baseName + ",Attribute=Totals");
            registry.remove(baseName + ",Attribute=In Requests");
            registry.remove(baseName + ",Attribute=Data Exchanged");
            registry.remove(baseName + ",Attribute=Errors");
        }
    }

    public MetricsContext start(){
        return new MetricsContext();
    }


    public class MetricsContext {
        private final Timer.Context total;

        public MetricsContext(){
            if(registry!=null) {
                inRequests.inc();
                total=totals.time();
            }
            else{
                total=null;
            }
        }

        public void stopWithSize(boolean success, Long size){
            if(registry!=null) {
                long duration = total.stop();
                if(size!=null){
                    exchangedData.mark(size);
                }
                if(!success){
                    errors.update(duration,TimeUnit.NANOSECONDS);
                }
            }
        }

        public <T extends CouchbaseDocument> void stop(Notification<? super BucketDocument<T>> notif){
            Long exchangedSize = null;
            if((notif.getValue()!=null) && (notif.getValue() instanceof BucketDocument)){
                Integer size =((BucketDocument)notif.getValue()).getDocument().getBaseMeta().getDbSize();
                if(size!=null) {
                    exchangedSize = size.longValue();
                }
            }
            stopWithSize(true,exchangedSize);
        }

        public void stopCounter(Notification<? super JsonLongDocument> notif){
            Long exchangedSize = null;
            if((notif.getValue()!=null) && (notif.getValue() instanceof JsonLongDocument)){
                exchangedSize =(long)((JsonLongDocument)notif.getValue()).content().toString().length();
            }
            stopWithSize(true,exchangedSize);
        }

        public void stopWithError(Throwable e){
            stopWithSize(false,null);
        }
    }
}
