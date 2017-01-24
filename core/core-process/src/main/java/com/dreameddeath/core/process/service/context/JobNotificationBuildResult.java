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

import com.dreameddeath.core.model.annotation.HasEffectiveDomain;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.entity.model.EntityVersion;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.notification.AbstractJobEvent;
import io.reactivex.Single;

import java.util.*;

/**
 * Created by Christophe Jeunesse on 06/11/2016.
 */
public class JobNotificationBuildResult<TJOB extends AbstractJob> {
    private final JobContext<TJOB> context;
    private final Map<EntityModelId,AbstractJobEvent> eventMap;

    public JobNotificationBuildResult(JobContext<TJOB> context, Collection<? extends AbstractJobEvent> eventList) {
        this.context = context;
        this.eventMap = new HashMap<>(eventList.size());
        addElements(eventList,DuplicateMode.ERROR);
    }

    public JobNotificationBuildResult(final JobNotificationBuildResult<TJOB> previousResult, Collection<? extends AbstractJobEvent> eventList,DuplicateMode duplicateMode) {
        this.context = previousResult.context;
        this.eventMap = new HashMap<>(previousResult.eventMap.size()+eventList.size());
        addElements(previousResult.eventMap.values(),DuplicateMode.ERROR);
        addElements(eventList,duplicateMode);
    }


    private JobNotificationBuildResult<TJOB> addElement(AbstractJobEvent event,DuplicateMode duplicateMode){
        EntityModelId origModelId = CouchbaseDocumentReflection.getReflectionFromClass(event.getClass()).getStructure().getEntityModelId();
        EntityModelId applicableModelId = new EntityModelId(origModelId.getDomain(),origModelId.getName(), EntityVersion.EMPTY_VERSION);
        if(eventMap.containsKey(applicableModelId)){
            if(DuplicateMode.ERROR==duplicateMode) {
                throw new IllegalArgumentException("The list of event of contains duplicated model ids :" + applicableModelId.toString());
            }
            else if(DuplicateMode.IGNORE==duplicateMode){
                return this;
            }
        }
        if(event instanceof HasEffectiveDomain){

        }
        eventMap.put(applicableModelId,event);
        return this;
    }

    private JobNotificationBuildResult<TJOB> addElements(Collection<? extends AbstractJobEvent> events,DuplicateMode duplicateMode){
        events.forEach(event->this.addElement(event,duplicateMode));
        return this;
    }

    public JobContext<TJOB> getContext() {
        return context;
    }

    public IEventBus getEventBus(){
        return context.getEventBus();
    }

    public Map<EntityModelId, AbstractJobEvent> getEventMap() {
        return Collections.unmodifiableMap(eventMap);
    }

    public static <TJOB extends AbstractJob> Single<JobNotificationBuildResult<TJOB>> build(JobContext<TJOB> context,Collection<AbstractJobEvent> events){
        return Single.just(new JobNotificationBuildResult<>(context, events));
    }

    public static <TJOB extends AbstractJob> Single<JobNotificationBuildResult<TJOB>> build(JobNotificationBuildResult<TJOB> previousRes,Collection<AbstractJobEvent> events){
        return build(previousRes,events,DuplicateMode.ERROR);
    }

    public static <TJOB extends AbstractJob> Single<JobNotificationBuildResult<TJOB>> build(JobContext<TJOB> context, AbstractJobEvent ... events){
        return build(context, Arrays.asList(events));
    }

    public static <TJOB extends AbstractJob> Single<JobNotificationBuildResult<TJOB>> build(JobNotificationBuildResult<TJOB> previousRes,AbstractJobEvent ... events){
        return build(previousRes,DuplicateMode.ERROR,events);
    }

    public static <TJOB extends AbstractJob> Single<JobNotificationBuildResult<TJOB>> build(JobNotificationBuildResult<TJOB> previousRes,DuplicateMode mode,AbstractJobEvent ... events){
        return build(previousRes, Arrays.asList(events),mode);
    }


    public static <TJOB extends AbstractJob> Single<JobNotificationBuildResult<TJOB>> build(JobNotificationBuildResult<TJOB> previousRes,Collection<AbstractJobEvent> events,DuplicateMode mode){
        return Single.just(new JobNotificationBuildResult<>(previousRes, events,mode));
    }

    public enum DuplicateMode{
        ERROR,
        IGNORE,
        REPLACE
    };

}
