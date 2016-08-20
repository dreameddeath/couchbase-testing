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

package com.dreameddeath.core.notification.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.notification.bus.EventFireResult;
import com.dreameddeath.core.notification.model.v1.Event;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by Christophe Jeunesse on 14/12/2015.
 */
public class EventBusMetrics implements Closeable {
    protected final Timer totals;
    protected final Timer errors;
    protected final Counter inEvents;
    protected final Timer initialSaves;
    protected final Timer finalSaves;
    protected final Counter notifications;

    protected final String baseName;
    protected final MetricRegistry registry;
    protected final Map<String,NotificationMetrics> notificationMetricsMap = new ConcurrentHashMap<>();

    public EventBusMetrics(String prefix, MetricRegistry registry) {
        this.baseName = prefix;
        this.registry = registry;

        if(registry!=null) {
            totals = registry.timer(baseName + ",Attribute=Totals");
            errors = registry.timer(baseName + ",Attribute=Errors");
            inEvents = registry.counter(baseName + ",Attribute=InEvent");
            initialSaves = registry.timer(baseName + ",Attribute=initSaves");
            finalSaves = registry.timer(baseName + ",Attribute=finalSaves");
            notifications = registry.counter(baseName + ",Attribute=Notifications");
        }
        else{
            totals=null;
            errors=null;
            inEvents =null;
            initialSaves =null;
            finalSaves=null;
            notifications=null;
        }
    }


    @Override
    public void close(){
        if(registry!=null) {
            registry.remove(baseName + ",Attribute=Totals");
            registry.remove(baseName + ",Attribute=InEvent");
            registry.remove(baseName + ",Attribute=Errors");
            registry.remove(baseName + ",Attribute=initSaves");
            registry.remove(baseName + ",Attribute=finalSaves");
            registry.remove(baseName + ",Attribute=Notifications");
        }
        notificationMetricsMap.values().forEach(NotificationMetrics::close);
    }

    public Context start(){
        if(registry!=null) {
            inEvents.inc();
        }
        return new Context();
    }


    protected NotificationMetrics getNotificationMetricsByName(String name){
        return notificationMetricsMap.computeIfAbsent(name,missingName->
            new NotificationMetrics(this.baseName+"!"+missingName,registry)
        );
    }

    public class Context {
        private final Timer.Context total;
        private Timer.Context saveInitial=null;
        private Timer.Context saveFinal=null;

        private Context(){
            total =totals!=null?totals.time():null;
        }

        public <T extends Event> T beforeInitialSave(T event){
            if(event.getBaseMeta().getState()!= CouchbaseDocument.DocumentState.SYNC){
                saveInitial =initialSaves!=null?initialSaves.time():null;
            }
            return event;
        }

        public <T extends Event> T afterInitialSave(T event){
            if(saveInitial!=null) {
                saveInitial.stop();
                saveInitial = null;
            }
            return event;
        }

        public void beforeFinalSave() {
            saveFinal =(finalSaves!=null)?finalSaves.time():null;
        }

        public <T extends Event> EventFireResult<T> afterFinalSave(EventFireResult<T> eventFireResult) {
            if(saveFinal!=null){
                saveFinal.stop();
                saveFinal=null;
            }
            return eventFireResult;
        }

        public void stop(Throwable e){
            if(registry!=null){
                long duration = total.stop();
                if(e!=null) errors.update(duration,TimeUnit.NANOSECONDS);
            }
        }

        public <T extends Event> EventFireResult<T>  stop(EventFireResult<T> eventResult) {
            stop(eventResult.getSaveError());
            return eventResult;
        }

        public NotificationMetrics.Context notificationStart(String name){
            if(notifications!=null)notifications.inc();
            return getNotificationMetricsByName(name).start();
        }

    }
}
