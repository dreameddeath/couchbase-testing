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

package com.dreameddeath.core.notification.listener.impl.crossdomain;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.dto.converter.DtoConverterFactory;
import com.dreameddeath.core.model.dto.converter.IDtoOutputConverter;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.notification.bus.EventFireResult;
import com.dreameddeath.core.notification.common.IEvent;
import com.dreameddeath.core.notification.listener.HasListenerDescription;
import com.dreameddeath.core.notification.listener.IEventListener;
import com.dreameddeath.core.notification.listener.SubmissionResult;
import com.dreameddeath.core.notification.listener.impl.AbstractNotificationProcessor;
import com.dreameddeath.core.notification.model.v1.CrossDomainBridge;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.core.notification.model.v1.listener.ListenedEvent;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 05/09/2017.
 */
public abstract class AbstractCrossDomainListener<T extends IEventListener & HasListenerDescription> extends AbstractNotificationProcessor implements IEventListener {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private String sourceDomain;
    private T parentListener;
    private Map<Class<? extends IEvent>,Converter<? extends IEvent,? extends IEvent>> converterMap = new ConcurrentHashMap<>();
    private DtoConverterFactory dtoConverterFactory;

    @Autowired
    public void setDtoConverterFactory(DtoConverterFactory factory){
        this.dtoConverterFactory = factory;
    }

    public AbstractCrossDomainListener(String sourceDomain, T parentListener) {
        this.sourceDomain = sourceDomain;
        this.parentListener = parentListener;
    }

    @Override
    public String getDomain() {
        return sourceDomain;
    }

    @Override
    public String getName() {
        return parentListener.getName();
    }

    @Override
    public String getType() {
        return parentListener.getType();
    }

    @Override
    public String getVersion() {
        return parentListener.getVersion();
    }

    @Override
    public <T extends IEvent> Single<SubmissionResult> submit(final Notification sourceNotif, final T event) {
        return processIfNeeded(sourceNotif,event);
    }

    @Override
    public <T extends IEvent> Single<SubmissionResult> submit(String domain, String notifId, T event) {
        return processIfNeeded(domain,notifId,event);
    }

    @Override
    public Single<SubmissionResult> submit(String domain,String notifKey) {
        return processIfNeeded(domain,notifKey);
    }

    @Override
    protected <T extends IEvent> Single<ProcessingResultInfo> doProcess(T event, Notification notification, ICouchbaseSession session) {
        final Converter<T,? extends IEvent> converter = getOutputConverter(event);
        return doProcessCrossDomainEvent(converter.convert(event), session)
                .map(res->buildResult(notification,res));
    }

    protected abstract <T extends IEvent> Single<EventFireResult<T, CrossDomainBridge>> doProcessCrossDomainEvent(T event, ICouchbaseSession session);

    private <T extends IEvent> ProcessingResultInfo buildResult(Notification notification, EventFireResult<T,CrossDomainBridge> eventFireResult){
        ProcessingResult result=ProcessingResult.SUBMITTED;
        switch (eventFireResult.getNotificationHolder().getStatus()){
            case NOTIFICATIONS_LIST_NAME_GENERATED: result=ProcessingResult.DEFERRED;break;
            case NOTIFICATIONS_IN_DB:result=ProcessingResult.PROCESSED;break;
        }
        return ProcessingResultInfo.build(notification,false,result);
    }

    private  <T extends IEvent> Converter<T,? extends IEvent> getOutputConverter(T event){
        return (Converter<T,? extends IEvent>)converterMap.computeIfAbsent(event.getClass(),eventClass->this.buildOutputConverter(eventClass));
    }

    private <T extends IEvent> Converter<T,? extends IEvent> buildOutputConverter(Class<T> eventClass){
        EntityModelId modelId=null;
        if(Event.class.isAssignableFrom(eventClass)){
            modelId = CouchbaseDocumentReflection.getReflectionFromClass((Class<Event>)eventClass).getStructure().getEntityModelId();
        }
        for(ListenedEvent listenedEvent: parentListener.getDescription().getListenedEvents()){
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
        throw new RuntimeException("Cannot build converter for class "+eventClass+ " for listened elements "+parentListener.getDescription().getListenedEvents());
    }

    public interface Converter<T extends IEvent,TOUT extends IEvent>{
        TOUT convert(T in);
    }

    @Override
    public <T extends IEvent> boolean isApplicable(String effectiveDomain, T event) {
        return effectiveDomain.equals(getDomain()) && parentListener.isApplicable(effectiveDomain, event);
    }
}
