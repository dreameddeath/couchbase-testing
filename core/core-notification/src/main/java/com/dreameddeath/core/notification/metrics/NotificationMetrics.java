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
import com.dreameddeath.core.notification.listener.SubmissionResult;
import com.dreameddeath.core.notification.model.v1.Notification;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

/**
 * Created by Christophe Jeunesse on 19/08/2016.
 */
public class NotificationMetrics implements Closeable {

    protected final Counter inRequests;
    protected final Timer totals;
    protected final Timer errors;
    protected final Timer saves;
    protected final Timer queueDuration;
    protected final Timer submitDuration;


    protected final String baseName;
    protected final MetricRegistry registry;

    public NotificationMetrics(String prefix, MetricRegistry registry) {
        this.baseName = prefix;
        this.registry = registry;

        if(registry!=null) {
            inRequests = registry.counter(baseName + ",Attribute=InEvent");
            totals = registry.timer(baseName + ",Attribute=Totals");
            errors = registry.timer(baseName + ",Attribute=Errors");
            saves = registry.timer(baseName + ",Attribute=Saves");
            queueDuration = registry.timer(baseName + ",Attribute=Queue");
            submitDuration = registry.timer(baseName + ",Attribute=Submit");
        }
        else{
            totals=null;
            errors=null;
            inRequests=null;
            saves=null;
            queueDuration=null;
            submitDuration=null;
        }
    }

    public Context start(){
        if(registry!=null) {
            inRequests.inc();
        }
        return new Context();
    }

    @Override
    public void close(){
        if(registry!=null) {
            registry.remove(baseName + ",Attribute=InEvent");
            registry.remove(baseName + ",Attribute=Totals");
            registry.remove(baseName + ",Attribute=Errors");
            registry.remove(baseName + ",Attribute=Queue");
            registry.remove(baseName + ",Attribute=Saves");
            registry.remove(baseName + ",Attribute=Submit");
        }
    }

    public class Context {
        private final Timer.Context total;
        private Timer.Context save =null;
        private Timer.Context duplicate=null;
        private Timer.Context submitToQueue=null;
        private Timer.Context submitToListener=null;

        private Context(){
            total = totals!=null?totals.time():null;
        }

        public Notification beforeSave(Notification notif){
            save =saves!=null?saves.time():null;
            return notif;
        }

        public Notification afterSave(Notification notification){
            if(save!=null) save.stop();
            return notification;
        }

        public String beforeReadDuplicate(String s) {
            duplicate=null;
            return s;
        }
        public Notification afterReadDuplicate(Notification notificationObservable) {
            if(duplicate!=null) duplicate.stop();
            return notificationObservable;
        }

        public void submitToQueue() {
            submitToQueue=queueDuration!=null?queueDuration.time():null;
        }

        public void submitToListener() {
            if(submitToQueue!=null){
                submitToQueue.stop();
                submitToQueue=null;
            }
            submitToListener=submitDuration!=null?submitDuration.time():null;
        }

        public void submitResult(SubmissionResult result) {
            if(result.isFailure()){
                stop(result.getError());
            }
            else {
                if(submitToListener!=null)submitToListener.stop();
                stop(null);
            }
        }

        public void stop(Throwable e){
            if(registry!=null){
                long duration= total.stop();
                if(e!=null)errors.update(duration,TimeUnit.NANOSECONDS);
            }
        }

    }
}
