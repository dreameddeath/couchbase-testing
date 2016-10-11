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

import com.dreameddeath.core.notification.listener.IEventListener;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;
import com.dreameddeath.core.notification.registrar.ListenerRegistrar;
import com.dreameddeath.core.service.AbstractRestExposableService;
import com.dreameddeath.core.service.annotation.ServiceDef;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.PostConstruct;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * Created by Christophe Jeunesse on 05/10/2016.
 */
public abstract class AbstractRemoteConsumerRest extends AbstractRestExposableService {
    private IEventListener listener;
    private ListenerRegistrar registrar;

    @Required
    public void setEventListener(IEventListener listener){
        this.listener = listener;
    }

    @Autowired
    public void setListenerRegistrar(ListenerRegistrar registrar){
        this.registrar=registrar;
    }

    @PostConstruct
    public void init(){
        ServiceDef annot = this.getClass().getAnnotation(ServiceDef.class);
        Preconditions.checkNotNull(annot,"The service %s should have a the ServiceDef annotation",this.getClass().getName());

        ListenerDescription listenerDescription=new ListenerDescription();
        listenerDescription.setName(listener.getName());
        listenerDescription.setVersion(listener.getVersion());
        listenerDescription.setType("remoteNotificationListener");
        listenerDescription.addParameter("remote.service.domain", annot.domain());
        listenerDescription.addParameter("remote.service.name", annot.name());
        listenerDescription.addParameter("remote.service.version", annot.version());
        try {
            registrar.register(listenerDescription);
        }
        catch(Exception e){

        }
    }

    @Path("{id}/submit")
    @POST
    public Response doProcess(@PathParam("id") String notificationId){
        listener.submit(notificationId);//TODO async
        return Response.ok().build();
    }

}
