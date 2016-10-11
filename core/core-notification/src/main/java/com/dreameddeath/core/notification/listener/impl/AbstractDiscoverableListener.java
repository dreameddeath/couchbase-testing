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

package com.dreameddeath.core.notification.listener.impl;

import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.listener.ListenedEvent;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 20/07/2016.
 */
public abstract class AbstractDiscoverableListener extends AbstractLocalListener {
    private volatile ListenerDescription description;
    private final String name;
    private final String type;
    private final String version;
    private final CopyOnWriteArrayList<EntityModelId> listenedEvents = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<EntityModelId,Boolean> modelIdEligibilityMap = new ConcurrentHashMap<>();

    public AbstractDiscoverableListener(ListenerDescription description){
        this.name = description.getName();
        this.type = description.getType();
        this.version = description.getVersion();
        setDescription(description);
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

    public ListenerDescription getDescription() {
        return description;
    }

    @Override
    public final <T extends Event> boolean isApplicable(T event) {
        EntityModelId modelId = CouchbaseDocumentReflection.getReflectionFromClass(event.getClass()).getStructure().getEntityModelId();
        return modelIdEligibilityMap.computeIfAbsent(modelId, this::calcEligibility);
    }

    public Boolean calcEligibility(EntityModelId modelId){
        boolean result=false;
        for(EntityModelId acceptedModelId:listenedEvents){
            result = modelId.getDomain().equals(acceptedModelId.getDomain()) &&
                    modelId.getName().equals(acceptedModelId.getName()) &&
                    (
                            acceptedModelId.getEntityVersion()==null ||
                                    acceptedModelId.getEntityVersion().compareTo(modelId.getEntityVersion())<=0
                    );
            if(result){
                break;
            }
        }
        return result;
    }

    public synchronized void setDescription(ListenerDescription description){
        this.description = description;
        List<EntityModelId> newModelsList = description.getListenedEvents().stream().map(ListenedEvent::getType).collect(Collectors.toList());

        listenedEvents.retainAll(newModelsList);
        listenedEvents.addAllAbsent(newModelsList);
        modelIdEligibilityMap.clear();
    }
}
