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

package com.dreameddeath.core.notification.listener.impl.dispatcher;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import com.dreameddeath.core.notification.annotation.EventOrigModelID;
import com.dreameddeath.core.notification.annotation.ListenerProcessor;
import com.dreameddeath.core.notification.common.IEvent;
import com.dreameddeath.core.notification.listener.impl.AbstractNotificationProcessor;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.core.notification.model.v1.listener.ListenedEvent;
import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.common.base.Preconditions;
import io.reactivex.Single;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 19/07/2017.
 */
public class StandardAnnotNotificationProcessorDispatcher {
    private final List<ListenedEvent> listenedEvents=new ArrayList<>();
    private final List<ProcessorMethodWrapper> processorForEvents=new ArrayList<>();
    private final Map<Class<? extends IEvent>,ProcessorMethodWrapper> eventMapper=new ConcurrentHashMap<>();
    private final Map<Class<?>,Boolean> eventApplicable=new ConcurrentHashMap<>();


    private void extractMethods(Object eventListenerInstance){
        for(Method method:eventListenerInstance.getClass().getMethods()){
            ListenerProcessor annot= method.getAnnotation(ListenerProcessor.class);
            if(annot!=null){
                Class<? extends IEvent> eventClass = (Class<? extends IEvent>)method.getParameters()[0].getType();
                //Preconditions.checkArgument();
                ListenedEvent listenedEvent;
                if(Event.class.isAssignableFrom(eventClass)){
                    CouchbaseDocumentStructureReflection documentStructureReflection = CouchbaseDocumentStructureReflection.getReflectionFromClass(eventClass);
                    EntityModelId modelId = documentStructureReflection.getEntityModelId();
                    listenedEvent = new ListenedEvent(modelId);
                }
                else{
                    EventOrigModelID eventOrigModelID = eventClass.getAnnotation(EventOrigModelID.class);
                    Preconditions.checkArgument(eventOrigModelID!=null,"The method %s.%s() doesn't have an entity model id annot for type %s",method.getDeclaringClass().getCanonicalName(),method.getName(),eventClass.getCanonicalName());
                    EntityModelId modelId = EntityModelId.build(eventOrigModelID.value());
                    listenedEvent = new ListenedEvent(modelId,eventClass.getCanonicalName());
                }
                listenedEvents.add(listenedEvent);
                processorForEvents.add(new ProcessorMethodWrapper(eventListenerInstance,method,eventClass));
            }
        }

        processorForEvents.sort((a,b)->{
            if(a.eventType.isAssignableFrom(b.eventType)){
                return -1;
            }
            else if(a.eventType.isAssignableFrom(b.eventType)){
                return 1;
            }
            else{
                return 0;
            }
        });

        for(ProcessorMethodWrapper wrapper:processorForEvents){
            isApplicable(wrapper.eventType);
            eventMapper.put(wrapper.eventType,wrapper);
            eventApplicable.put(wrapper.eventType,true);
        }
    }

    public List<ListenedEvent> getListenedEvents() {
        return listenedEvents;
    }

    public StandardAnnotNotificationProcessorDispatcher(Object parent) {
        extractMethods(parent);
    }

    public boolean isApplicable(Class<?> event){
        return eventApplicable.computeIfAbsent(event,evt->{
            for(ProcessorMethodWrapper wrapper:processorForEvents){
                if(wrapper.eventType.isAssignableFrom(evt)){
                    eventMapper.put((Class<? extends IEvent>)evt,wrapper);
                    return true;
                }
            }
            return false;
        });
    }

    public <T extends IEvent> Single<AbstractNotificationProcessor.ProcessingResultInfo> doProcess(T event, Notification notification, ICouchbaseSession session){
        return eventMapper.get(event.getClass()).invoker.invoke(event,notification,session);
    }

    private static class ProcessorMethodWrapper{
        private final Object processorObject;
        private final MethodAccess access;
        private final Class<? extends IEvent> eventType;
        private final int index;
        private final Invoker invoker;
        private ProcessorMethodWrapper(Object processor,Method method,Class<? extends IEvent> eventType){
            this.processorObject = processor;
            this.access = MethodAccess.get(processor.getClass());
            this.eventType = eventType;
            this.index = this.access.getIndex(method.getName(),method.getParameterTypes());
            if(method.getParameterCount()==2){
                invoker = (evt,notification,session)->
                        ((Single< AbstractNotificationProcessor.ProcessingResult>)access.invoke(processorObject,index,evt,session))
                        .flatMap(processingResult -> AbstractNotificationProcessor.ProcessingResultInfo.buildSingle(notification,false,processingResult));

            }
            else{
                invoker = (evt,notification,session)-> ((Single< AbstractNotificationProcessor.ProcessingResultInfo>)access.invoke(processorObject,index,evt,notification,session));
            }
        }

    }

    private interface Invoker {
        Single<AbstractNotificationProcessor.ProcessingResultInfo> invoke(IEvent event,Notification notification,ICouchbaseSession session);
    }
}
