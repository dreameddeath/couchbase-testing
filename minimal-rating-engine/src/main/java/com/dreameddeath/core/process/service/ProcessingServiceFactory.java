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

package com.dreameddeath.core.process.service;

import com.dreameddeath.core.exception.process.ExecutionServiceNotFoundException;
import com.dreameddeath.core.exception.process.JobExecutionException;
import com.dreameddeath.core.exception.process.TaskExecutionException;
import com.dreameddeath.core.process.common.AbstractJob;
import com.dreameddeath.core.process.common.AbstractTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
public class ProcessingServiceFactory {
    private Map<Class<? extends AbstractJob>, JobProcessingService<?>> jobServicesMap
            = new ConcurrentHashMap<Class<? extends AbstractJob>, JobProcessingService<?>>();


    private Map<Class<? extends AbstractTask>, TaskProcessingService<?>> taskServicesMap
            = new ConcurrentHashMap<Class<? extends AbstractTask>, TaskProcessingService<?>>();


    public ProcessingServiceFactory(){
        addJobServiceFor(AbstractJob.class, new BasicJobProcessingServiceImpl(this));
        addTaskServiceFor(AbstractTask.class, new BasicTaskProcessingServiceImpl(this));
    }


    public <T extends AbstractTask> void addTaskServiceFor(Class<T> entityClass,TaskProcessingService<T> service){
        taskServicesMap.put(entityClass, service);
    }

    public <T extends AbstractTask> TaskProcessingService<T> getTaskServiceForClass(Class<T> entityClass) {
        TaskProcessingService<T> result = (TaskProcessingService<T>) taskServicesMap.get(entityClass);
        if (result == null) {
            Class parentClass = entityClass.getSuperclass();
            if (AbstractTask.class.isAssignableFrom(parentClass)) {
                result = getTaskServiceForClass(parentClass.asSubclass(AbstractTask.class));
                if (result != null) {
                    taskServicesMap.put(entityClass, result);
                }
            }
        }
        ///TODO throw an error if null
        return result;
    }


    public <T extends AbstractJob> void addJobServiceFor(Class<T> entityClass,JobProcessingService<T> service){
        jobServicesMap.put(entityClass,service);
    }

    public <T extends AbstractJob> JobProcessingService<T> getJobServiceForClass(Class<T> entityClass) throws ExecutionServiceNotFoundException{
        JobProcessingService<T> result = (JobProcessingService<T>) jobServicesMap.get(entityClass);
        if (result == null) {
            Class parentClass = entityClass.getSuperclass();
            if (AbstractJob.class.isAssignableFrom(parentClass)) {
                result = getJobServiceForClass(parentClass.asSubclass(AbstractJob.class));
                if (result != null) {
                    jobServicesMap.put(entityClass, result);
                }
            }
        }
        if(result==null){
            throw new ExecutionServiceNotFoundException("Cannot find execution class for job <"+entityClass.getName()+">");
        }
        ///TODO throw an error if null
        return result;
    }


    public <T extends AbstractJob> void execute(T job) throws JobExecutionException,ExecutionServiceNotFoundException {
        ((JobProcessingService<T>)getJobServiceForClass(job.getClass())).execute(job);
    }

    public <T extends AbstractTask> void execute(T task) throws TaskExecutionException,ExecutionServiceNotFoundException {
        ((TaskProcessingService<T>)getTaskServiceForClass(task.getClass())).execute(task);
    }
}
