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

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

/**
 * Created by Christophe Jeunesse on 14/12/2015.
 */
public class DcpFlowHandlerMetrics implements Closeable {
    protected final Timer totals;
    protected final Timer errors;
    protected final Counter inRequests;
    protected final Meter exchangedData;

    protected final String baseName;
    protected final MetricRegistry registry;

    public DcpFlowHandlerMetrics(String prefix, MetricRegistry registry) {
        this.baseName = prefix;
        this.registry = registry;

        if(registry!=null) {
            totals = registry.timer(baseName + ",Attribute=Totals");
            inRequests = registry.counter(baseName + ",Attribute=InEvent");
            exchangedData = registry.meter(baseName + ",Attribute=DataExchanged");
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
    public void close(){
        if(registry!=null) {
            registry.remove(baseName + ",Attribute=Totals");
            registry.remove(baseName + ",Attribute=InEvent");
            registry.remove(baseName + ",Attribute=DataExchanged");
            registry.remove(baseName + ",Attribute=Errors");
        }
    }

    public Context start(long size){
        if(registry!=null) {
            inRequests.inc();
            exchangedData.mark(size);
        }
        return new Context();
    }

    public Context start(){
        return start(0);
    }


    public class Context {
        private final Timer.Context total;
        private Context(){
            total = totals!=null?totals.time():null;
        }

        public void stop(){
            stop(null);
        }

        public void stop(Throwable e){
            if(registry!=null){
                long duration = total.stop();
                if(e!=null) errors.update(duration,TimeUnit.NANOSECONDS);
            }
        }
    }
}
