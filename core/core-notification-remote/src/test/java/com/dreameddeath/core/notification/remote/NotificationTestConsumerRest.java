/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.notification.remote;

import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.notification.EventTest;
import com.dreameddeath.core.notification.NotificationTestListener;
import com.dreameddeath.core.notification.model.v1.listener.ListenedEvent;
import com.dreameddeath.core.service.annotation.ServiceDef;

import javax.ws.rs.Path;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 25/10/2016.
 */
@Path("/")
@ServiceDef(domain = "test",type=AbstractRemoteConsumerRest.SERVICE_DEF_TYPE,name="testRestListener",version = "1.0")
public class NotificationTestConsumerRest extends AbstractRemoteConsumerRest<NotificationTestListener>{

    @Override
    public List<ListenedEvent> getListenedEvents(){
        return Collections.singletonList(new ListenedEvent(CouchbaseDocumentReflection.getReflectionFromClass(EventTest.class).getStructure().getEntityModelId()));
    }

}
