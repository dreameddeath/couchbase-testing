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

package com.dreameddeath.infrastructure.plugin.process;

import com.dreameddeath.core.process.model.base.AbstractJob;
import com.dreameddeath.core.process.model.base.AbstractTask;
import com.dreameddeath.core.process.service.factory.impl.ExecutorClientFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 17/03/2016.
 */
public class ProcessorClientPreInit {
    private final ExecutorClientFactory factory;
    private Map<Class<? extends AbstractJob>,Set<Class<? extends AbstractTask>>> fullMap = new ConcurrentHashMap<>();
    public ProcessorClientPreInit(ExecutorClientFactory factory){
        this.factory = factory;
    }


    public Map precreateJobClient(Class<? extends AbstractJob> jobClass){
        fullMap.computeIfAbsent(jobClass,clazz->new HashSet<>());
        return fullMap;
    }

    public Map precreateJobClients(List<Class<? extends  AbstractJob>> jobClasses){
        for(Class<? extends AbstractJob> clazz:jobClasses){
            precreateJobClient(clazz);
        }
        return  fullMap;
    }

    public Map precreateJobAndTasksClients(Map<Class<? extends AbstractJob>,Collection<Class<? extends  AbstractTask>>> taskClassesMap){
        for(Class<? extends AbstractJob> jobClazz:taskClassesMap.keySet()){
            precreateJobClient(jobClazz);
            fullMap.get(jobClazz).addAll(taskClassesMap.get(jobClazz));
        }
        return fullMap;
    }

    public void init(){
        for(Class<? extends AbstractJob> jobClazz:fullMap.keySet()){
            factory.buildJobClient(jobClazz);
            for(Class<? extends AbstractTask> taskClass:fullMap.get(jobClazz)){
                factory.buildTaskClient(jobClazz,taskClass);
            }
        }
    }

    public void cleanup(){
        fullMap.clear();
    }

}
