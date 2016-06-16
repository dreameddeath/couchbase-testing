package com.dreameddeath.core.notification.model.v1.listener;

import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.notification.model.v1.Event;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Created by Christophe Jeunesse on 25/05/2016.
 */
public class ListenedEvent {
    private EntityModelId modelId;

    @JsonCreator
    public ListenedEvent(EntityModelId modelId){
        this.modelId=modelId;
    }

    public EntityModelId getType() {
        return modelId;
    }

    public static <T extends Event> ListenedEvent build(Class<T> eventClazz){
        return new ListenedEvent(EntityDef.build(CouchbaseDocumentReflection.getReflectionFromClass(eventClazz).getStructure()).getModelId());
    }
}
