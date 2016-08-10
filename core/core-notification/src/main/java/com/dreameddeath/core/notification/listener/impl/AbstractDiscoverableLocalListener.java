/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.notification.listener.impl;

import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.listener.ListenedEvent;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 20/07/2016.
 */
public abstract class AbstractDiscoverableLocalListener extends AbstractLocalListener {
    private final ListenerDescription description;
    private final String name;
    private final List<EntityModelId> listenedEvents = new ArrayList<>();

    public AbstractDiscoverableLocalListener(ListenerDescription description){
        this.description = description;
        this.name = description.getGroupName()+"/"+description.getType();
        listenedEvents.addAll(description.getListenedNotification().stream().map(ListenedEvent::getType).collect(Collectors.toList()));
    }

    @Override
    public String getName() {
        return name;
    }


    public ListenerDescription getDescription() {
        return description;
    }

    @Override
    public final <T extends Event> boolean isApplicable(T event) {
        EntityModelId modelId = CouchbaseDocumentReflection.getReflectionFromClass(event.getClass()).getStructure().getEntityModelId();
        for(EntityModelId acceptedModelId:listenedEvents){
            boolean res = modelId.getDomain().equals(acceptedModelId.getDomain()) &&
                    modelId.getName().equals(acceptedModelId.getName()) &&
                    (
                            acceptedModelId.getEntityVersion()==null ||
                            acceptedModelId.getEntityVersion().compareTo(modelId.getEntityVersion())<=0
                    );
            if(res){
                return true;
            }
        }
        return false;
    }
}
