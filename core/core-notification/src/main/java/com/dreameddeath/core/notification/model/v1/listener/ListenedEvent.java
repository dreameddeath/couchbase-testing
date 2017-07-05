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

package com.dreameddeath.core.notification.model.v1.listener;

import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.notification.common.IEvent;
import com.dreameddeath.core.notification.model.v1.Event;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

/**
 * Created by Christophe Jeunesse on 25/05/2016.
 */
public class ListenedEvent {
    private final EntityModelId modelId;
    private final String publishedClassName;

    @JsonCreator
    public ListenedEvent(@JsonProperty("modelId") EntityModelId modelId, @JsonProperty("publishedClassName")String className){
        this.modelId=modelId;
        this.publishedClassName = className;
    }

    public ListenedEvent(EntityModelId modelId){
        this(modelId,null);
    }

    @JsonGetter("modelId")
    public EntityModelId getType() {
        return modelId;
    }

    @JsonGetter("publishedClassName")
    public String getPublishedClassName() {
        return publishedClassName;
    }

    public static <T extends Event> ListenedEvent build(Class<T> eventClazz){
        return new ListenedEvent(EntityDef.build(CouchbaseDocumentReflection.getReflectionFromClass(eventClazz).getStructure()).getModelId());
    }

    public static <T extends IEvent> ListenedEvent build(Class<T> eventClazz,EntityModelId modelId){
        Preconditions.checkArgument(!Event.class.isAssignableFrom(eventClazz),"The class musn't be an event id");
        return new ListenedEvent(modelId,eventClazz.getCanonicalName());
    }

    @Override
    public String toString() {
        return "ListenedEvent{" +
                "modelId=" + modelId +
                ", publishedClassName='" + publishedClassName + '\'' +
                '}';
    }
}
