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

package com.dreameddeath.core.notification.listener.impl;

import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.notification.annotation.EventOrigModelID;
import com.dreameddeath.core.notification.common.IEvent;
import com.dreameddeath.core.notification.listener.HasListenerDescription;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.listener.ListenedEvent;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;
import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 20/07/2016.
 */
public abstract class AbstractDiscoverableListener extends AbstractLocalListener implements HasListenerDescription {
    private volatile ListenerDescription description;
    private final String domain;
    private final String name;
    private final String type;
    private final String version;
    private volatile List<EntityModelId> listenedEvents = null;

    public AbstractDiscoverableListener(ListenerDescription description){
        this.domain = description.getDomain();
        this.name = description.getName();
        this.type = description.getType();
        this.version = description.getVersion();
        setDescription(description);
    }

    @Override
    public String getDomain(){
        return domain;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public ListenerDescription getDescription() {
        return description;
    }

    @Override
    public void setDescription(ListenerDescription description){
        this.description = description;
        this.listenedEvents = Collections.unmodifiableList(description.getListenedEvents().stream().map(ListenedEvent::getType).collect(Collectors.toList()));
    }


    @Override
    protected boolean isApplicable(Class<? extends IEvent> clazz) {
        return calcEligibility(clazz);
    }

    public <T extends IEvent> boolean calcEligibility(Class<? extends IEvent> clazz){
        EntityModelId modelId=null;
        if(Event.class.isAssignableFrom(clazz)) {
            modelId = CouchbaseDocumentReflection.getReflectionFromClass((Class<? extends Event>)clazz).getStructure().getEntityModelId();
        }
        if(modelId==null){
            EventOrigModelID modelIDAnnot = clazz.getAnnotation(EventOrigModelID.class);
            if(modelIDAnnot!=null && StringUtils.isNotEmpty(modelIDAnnot.value())){
                modelId = EntityModelId.build(modelIDAnnot.value());
            }
        }

        Preconditions.checkState(modelId!=null,"Not managed event %s because no model id found ",clazz.getCanonicalName());

        boolean result=false;
        List<EntityModelId> modelIdList = listenedEvents;
        if(modelIdList!=null) {
            for (EntityModelId acceptedModelId : modelIdList) {
                result = modelId.getDomain().equals(acceptedModelId.getDomain()) &&
                        modelId.getName().equals(acceptedModelId.getName()) &&
                        (
                                acceptedModelId.getEntityVersion() == null ||
                                        acceptedModelId.getEntityVersion().compareTo(modelId.getEntityVersion()) <= 0
                        );
                if (result) {
                    break;
                }
            }
        }
        return result;
    }
}
