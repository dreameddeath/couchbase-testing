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
import com.dreameddeath.core.notification.annotation.ListenerProcessor;
import com.dreameddeath.core.notification.listener.impl.AbstractLocalStandardListener;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.google.common.base.Preconditions;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by Christophe Jeunesse on 30/07/2017.
 */
@Listener(forTypes = "3Params")
public class AutoDiscoveryListener3Params extends AbstractLocalStandardListener {
    private final static Logger LOG = LoggerFactory.getLogger(AutoDiscoveryListener3Params.class);
    private final TestNotificationQueue queue=TestNotificationQueue.INSTANCE();
    private final String name;
    private final String version;

    public AutoDiscoveryListener3Params(String name) {
        this(name,"1.0");
    }

    public AutoDiscoveryListener3Params(String name, String version) {
        this.name = name;
        this.version=version;
    }

    public AutoDiscoveryListener3Params(String domain,String type,String name,Map<String,String> params){
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

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @ListenerProcessor
    public Single<ProcessingResultInfo> test(TestEvent event, Notification notification, ICouchbaseSession session) {
        return queue.manageEvent(LOG,event,notification);
    }
}
