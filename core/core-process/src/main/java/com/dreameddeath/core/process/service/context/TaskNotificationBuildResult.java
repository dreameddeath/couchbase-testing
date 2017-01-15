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

package com.dreameddeath.core.process.service.context;

import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.entity.model.EntityVersion;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import com.dreameddeath.core.process.model.v1.notification.AbstractTaskEvent;
import com.google.common.base.Preconditions;
import io.reactivex.Single;

import java.util.*;

/**
 * Created by Christophe Jeunesse on 06/11/2016.
 */
public class TaskNotificationBuildResult<TJOB extends AbstractJob,TTASK extends AbstractTask> {
    private final TaskContext<TJOB,TTASK> context;
    private final Map<EntityModelId,AbstractTaskEvent> eventMap;

    public TaskNotificationBuildResult(TaskContext<TJOB,TTASK> context, Collection<? extends AbstractTaskEvent> eventList) {
        this.context = context;
        this.eventMap = new HashMap<>(eventList.size());
        addElements(eventList, DuplicateMode.ERROR);
    }

    public TaskNotificationBuildResult(final TaskNotificationBuildResult<TJOB,TTASK> previousResult, Collection<? extends AbstractTaskEvent> eventList, DuplicateMode duplicateMode) {
        this.context = previousResult.context;
        this.eventMap = new HashMap<>(previousResult.eventMap.size()+eventList.size());
        addElements(previousResult.eventMap.values(), DuplicateMode.ERROR);
        addElements(eventList,duplicateMode);
    }


    private TaskNotificationBuildResult<TJOB,TTASK> addElement(AbstractTaskEvent event, DuplicateMode duplicateMode){
        String taskKey = context.getInternalTask().getBaseMeta().getKey();
        Preconditions.checkArgument(StringUtils.isNotEmpty(taskKey),"The key must be defined");
        EntityModelId origModelId = CouchbaseDocumentReflection.getReflectionFromClass(event.getClass()).getStructure().getEntityModelId();
        EntityModelId applicableModelId = new EntityModelId(origModelId.getDomain(),origModelId.getName(), (EntityVersion)null);
        if(eventMap.containsKey(applicableModelId)){
            if(DuplicateMode.ERROR==duplicateMode) {
                throw new IllegalArgumentException("The list of event of contains duplicated model ids :" + applicableModelId.toString());
            }
            else if(DuplicateMode.IGNORE==duplicateMode){
                return this;
            }
        }
        event.setTaskKey(taskKey);
        eventMap.put(applicableModelId,event);
        return this;
    }

    private TaskNotificationBuildResult<TJOB,TTASK> addElements(Collection<? extends AbstractTaskEvent> events, DuplicateMode duplicateMode){
        events.forEach(event->this.addElement(event,duplicateMode));
        return this;
    }

    public TaskContext<TJOB,TTASK> getContext() {
        return context;
    }

    public IEventBus getEventBus(){
        return context.getEventBus();
    }

    public Map<EntityModelId, AbstractTaskEvent> getEventMap() {
        return Collections.unmodifiableMap(eventMap);
    }

    public static <TJOB extends AbstractJob,TTASK extends AbstractTask> Single<TaskNotificationBuildResult<TJOB,TTASK>> build(TaskContext<TJOB,TTASK> context, Collection<AbstractTaskEvent> events){
        return Single.just(new TaskNotificationBuildResult<>(context, events));
    }

    public static <TJOB extends AbstractJob,TTASK extends AbstractTask> Single<TaskNotificationBuildResult<TJOB,TTASK>> build(TaskNotificationBuildResult<TJOB,TTASK> previousRes, Collection<AbstractTaskEvent> events){
        return build(previousRes,events, DuplicateMode.ERROR);
    }

    public static <TJOB extends AbstractJob,TTASK extends AbstractTask> Single<TaskNotificationBuildResult<TJOB,TTASK>>  build(TaskContext<TJOB,TTASK> context, AbstractTaskEvent ... events){
        return build(context, Arrays.asList(events));
    }

    public static <TJOB extends AbstractJob,TTASK extends AbstractTask> Single<TaskNotificationBuildResult<TJOB,TTASK>> build(TaskNotificationBuildResult<TJOB,TTASK> previousRes, AbstractTaskEvent ... events){
        return build(previousRes, DuplicateMode.ERROR,events);
    }

    public static <TJOB extends AbstractJob,TTASK extends AbstractTask> Single<TaskNotificationBuildResult<TJOB,TTASK>> build(TaskNotificationBuildResult<TJOB,TTASK> previousRes, DuplicateMode mode, AbstractTaskEvent ... events){
        return build(previousRes, Arrays.asList(events),mode);
    }


    public static <TJOB extends AbstractJob,TTASK extends AbstractTask> Single<TaskNotificationBuildResult<TJOB,TTASK>> build(TaskNotificationBuildResult<TJOB,TTASK> previousRes, Collection<AbstractTaskEvent> events, DuplicateMode mode){
        return Single.just(new TaskNotificationBuildResult<>(previousRes, events,mode));
    }

    public enum DuplicateMode{
        ERROR,
        IGNORE,
        REPLACE
    };

}
