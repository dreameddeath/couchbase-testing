/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.notification;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.notification.annotation.Listener;
import com.dreameddeath.core.notification.common.IEvent;
import com.dreameddeath.core.notification.listener.impl.AbstractLocalListener;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.google.common.base.Preconditions;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by Christophe Jeunesse on 17/08/2016.
 */
@Listener(forTypes = {"testListenerDiscovery"})
public class NotificationTestListener extends AbstractLocalListener {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationTestListener.class);
    private final String name;
    private final String version;
    private final TestNotificationQueue queue=TestNotificationQueue.INSTANCE();

    public NotificationTestListener(String name) {
        this(name,"1.0");
    }

    public NotificationTestListener(String name,String version) {
        this.name = name;
        this.version=version;
    }

    public NotificationTestListener(String domain,String type,String name,Map<String,String> params){
        this(name);
        Preconditions.checkArgument(type.equals(getType()));
        Preconditions.checkArgument(domain.equals(getDomain()));
    }


    @Override
    public String getType() {
        return "testListenerDiscovery";
    }

    @Override
    public String getDomain() {
        return "test";
    }


    public Notification poll() throws InterruptedException {
        return queue.poll();
    }

    @Override
    protected <T extends IEvent> Single<ProcessingResultInfo> doProcess(T event, Notification notification, ICouchbaseSession session) {
        //LOG.error("Received event {} on thread {}",((TestEvent)event).toAdd,Thread.currentThread());
        try {
            Thread.sleep(new Double(Math.random() * 100).longValue());
        } catch (InterruptedException e) {

        }
        return queue.manageEvent(LOG,event,notification);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public <T extends IEvent> boolean isApplicable(String domain,T event) {
        return domain.equals(getDomain()) && event instanceof TestEvent;
    }

    @Override
    public String getVersion() {
        return version;
    }
}
