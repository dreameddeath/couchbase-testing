/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.notification.remote;

import com.dreameddeath.core.notification.listener.IEventListener;
import com.dreameddeath.core.notification.listener.SubmissionResult;
import com.dreameddeath.core.notification.model.v1.listener.ListenedEvent;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;
import com.dreameddeath.core.notification.registrar.ListenerRegistrar;
import com.dreameddeath.core.notification.remote.model.RemoteProcessingResult;
import com.dreameddeath.core.service.AbstractRestExposableService;
import com.dreameddeath.core.service.annotation.ServiceDef;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.PostConstruct;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 05/10/2016.
 */
public abstract class AbstractRemoteConsumerRest<T extends IEventListener> extends AbstractRestExposableService {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractRemoteConsumerRest.class);
    public static final String SERVICE_TYPE_LISTENER = "listener";
    public static final String REMOTE_SERVICE_DOMAIN = "remote.service.domain";
    public static final String REMOTE_SERVICE_NAME = "remote.service.name";
    public static final String REMOTE_SERVICE_VERSION = "remote.service.version";
    public static final String REMOTE_NOTIFICATION_LISTENER_TYPE = "remoteNotificationListener";
    private T listener;
    private ListenerRegistrar registrar;

    @Required
    public void setEventListener(T listener){
        this.listener = listener;
    }

    @Autowired
    public void setListenerRegistrar(ListenerRegistrar registrar){
        this.registrar=registrar;
    }

    public abstract List<ListenedEvent> getListenedEvents();

    @PostConstruct
    public void init(){
        ServiceDef annot = this.getClass().getAnnotation(ServiceDef.class);
        Preconditions.checkNotNull(annot,"The service %s should have a the ServiceDef annotation",this.getClass().getName());

        ListenerDescription listenerDescription=new ListenerDescription();
        listenerDescription.setName(listener.getName());
        listenerDescription.setVersion(listener.getVersion());
        listenerDescription.setListenedEvents(getListenedEvents());
        listenerDescription.setType(REMOTE_NOTIFICATION_LISTENER_TYPE);
        listenerDescription.addParameter(REMOTE_SERVICE_DOMAIN, annot.domain());
        listenerDescription.addParameter(REMOTE_SERVICE_NAME, annot.name());
        listenerDescription.addParameter(REMOTE_SERVICE_VERSION, annot.version());
        try {
            registrar.register(listenerDescription);
        }
        catch(Exception e){
            LOG.error("Cannot registar the listener "+listener.getName()+"/"+listener.getVersion(),e);
            throw new RuntimeException(e);
        }
    }

    @Path("{id}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public void doProcess(final @PathParam("id") String notificationKey,@Suspended final AsyncResponse asyncResponse){
        listener.submit(notificationKey)
                .map(this::mapResult)
                .onErrorReturn(throwable -> mapResult(notificationKey,throwable))
                .subscribe(asyncResponse::resume);
    }

    private RemoteProcessingResult mapResult(SubmissionResult result){
        return new RemoteProcessingResult(
                result.getNotificationKey(),
                result.isSuccess(),
                result.getError()!=null?result.getError().getMessage():null,
                result.getError()!=null?result.getError().getClass().toString():null
        );
    }

    private RemoteProcessingResult mapResult(String notificationKey,Throwable error){
        return new RemoteProcessingResult(
                notificationKey,
                false,
                error.getMessage(),
                error.getClass().toString()
        );
    }


}
