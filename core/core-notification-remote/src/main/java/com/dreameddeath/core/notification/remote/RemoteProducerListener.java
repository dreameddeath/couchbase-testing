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

package com.dreameddeath.core.notification.remote;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.dto.converter.DtoConverterFactory;
import com.dreameddeath.core.model.dto.converter.IDtoOutputConverter;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.notification.annotation.Listener;
import com.dreameddeath.core.notification.common.IEvent;
import com.dreameddeath.core.notification.listener.impl.AbstractDiscoverableListener;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.core.notification.model.v1.listener.ListenedEvent;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;
import com.dreameddeath.core.notification.remote.model.RemoteProcessingRequest;
import com.dreameddeath.core.notification.remote.model.RemoteProcessingResult;
import com.dreameddeath.core.service.client.rest.IRestServiceClient;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.ws.rs.client.Entity;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 05/10/2016.
 */
@Listener(forTypes = {AbstractRemoteConsumerRest.REMOTE_NOTIFICATION_LISTENER_TYPE},matcherRank = 10)
public class RemoteProducerListener extends AbstractDiscoverableListener {
    private static final Logger LOG = LoggerFactory.getLogger(RemoteProducerListener.class);

    private IRemoteNotificationClientServiceFactory remoteNotificationClientServiceFactory;
    private Map<Class<? extends IEvent>,Converter<? extends IEvent,? extends IEvent>> converterMap = new ConcurrentHashMap<>();
    private IRestServiceClient remoteClient;
    private DtoConverterFactory dtoConverterFactory;

    public RemoteProducerListener(ListenerDescription description) {
        super(description);
    }

    @Autowired
    public void setClientFactory(IRemoteNotificationClientServiceFactory factory){
        this.remoteNotificationClientServiceFactory = factory;
    }

    @Autowired
    public void setDtoConverterFactory(DtoConverterFactory factory){
        this.dtoConverterFactory = factory;
    }



    @PostConstruct
    public void init(){
        remoteClient = remoteNotificationClientServiceFactory.getClient(getDescription());

    }

    @Override
    protected void incrementAttemptsManagement(Notification sourceNotif) {
        sourceNotif.incNbRemoteAttempts();
    }

    @Override
    protected <T extends IEvent> Single<ProcessingResultInfo> doProcess(final T event, final Notification notification, final ICouchbaseSession session) {
        final Converter<T,? extends IEvent> converter = getOutputConverter(event);
        return session.asyncSave(notification)
                .flatMap(savedNotif->
                    remoteClient.getInstance()
                    .path("{domain}")
                    .resolveTemplate("domain",notification.getDomain())
                    .request()
                    .post(Entity.json(new RemoteProcessingRequest<>(converter.convert(event),notification.getBaseMeta().getKey())), RemoteProcessingResult.class)
                    .flatMap(remoteProcessingResult -> mapResult(remoteProcessingResult,session))
                )
                .onErrorReturn(throwable -> ProcessingResultInfo.build(notification,false,ProcessingResult.DEFERRED))
                ;
    }

    public Single<ProcessingResultInfo> mapResult(final RemoteProcessingResult result, ICouchbaseSession session){
        return session.asyncGet(result.getNotificationKey(),Notification.class)
                .map(notif->ProcessingResultInfo.build(notif,true,result.isSuccess()?ProcessingResult.PROCESSED:ProcessingResult.DEFERRED));
    }

    private  <T extends IEvent> Converter<T,? extends IEvent> getOutputConverter(T event){
        return (Converter<T,? extends IEvent>)converterMap.computeIfAbsent(event.getClass(),eventClass->this.buildOutputConverter(eventClass));
    }

    private <T extends IEvent> Converter<T,? extends IEvent> buildOutputConverter(Class<T> eventClass){
        EntityModelId modelId=null;
        if(Event.class.isAssignableFrom(eventClass)){
            modelId = CouchbaseDocumentReflection.getReflectionFromClass((Class<Event>)eventClass).getStructure().getEntityModelId();
        }
        for(ListenedEvent listenedEvent: getDescription().getListenedEvents()){
            try {
                Class<? extends IEvent> targetClass=null;
                if(listenedEvent.getPublishedClassName()!=null){
                    targetClass =(Class<? extends IEvent>)this.getClass().getClassLoader().loadClass(listenedEvent.getPublishedClassName());
                }

                if(modelId!=null){
                    if(modelId.equals(listenedEvent.getType())) {
                        if(targetClass!=null) {
                            final IDtoOutputConverter<T, ? extends IEvent> converter = dtoConverterFactory.getDtoOutputConverter(eventClass, targetClass);
                            if (converter != null) {
                                return converter::convertToOutput;
                            }
                        }
                        else{
                            return (in)->in;
                        }
                    }
                }
                else if(targetClass!=null && eventClass.isAssignableFrom(targetClass)){
                    return (in)->in;
                }

            }
            catch (ClassNotFoundException e){
                LOG.error("Cannot find class {}",listenedEvent.getPublishedClassName());
            }
        }
        throw new RuntimeException("Cannot build converter for class "+eventClass+ " for listened elements "+getDescription().getListenedEvents());
    }

    public interface Converter<T extends IEvent,TOUT extends IEvent>{
        TOUT convert(T in);
    }
}
