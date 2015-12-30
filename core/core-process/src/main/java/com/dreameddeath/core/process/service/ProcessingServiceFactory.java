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

import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.exception.ProcessingServiceNotFoundException;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.model.AbstractJob;
import com.dreameddeath.core.process.model.AbstractTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
public class ProcessingServiceFactory {
    private Map<Class<? extends AbstractJob>, IJobProcessingService<?>> jobProcessingServicesMap
            = new ConcurrentHashMap<>();
    private Map<Class<? extends AbstractTask>, ITaskProcessingService<?,?>> taskProcessingServicesMap
            = new ConcurrentHashMap<>();


    public <T extends AbstractJob> void addJobProcessingServiceFor(Class<T> entityClass, IJobProcessingService<T> service){
        jobProcessingServicesMap.put(entityClass, service);
    }

    public ProcessingServiceFactory addJobProcessingService(Class<? extends IJobProcessingService> serviceClass){
        JobProcessingForClass ann = serviceClass.getAnnotation(JobProcessingForClass.class);
        try {
            addJobProcessingServiceFor(ann.value(), serviceClass.newInstance());
        }
        catch(IllegalAccessException|InstantiationException e){
            throw new RuntimeException("Cannot instantiate class <"+serviceClass.getName()+">",e);
        }


        for(Class innerClass:serviceClass.getClasses()){
            if(ITaskProcessingService.class.isAssignableFrom(innerClass)){
                addTaskProcessingService((Class<ITaskProcessingService>)innerClass);
            }
        }
        return this;
    }

    public <TJOB extends AbstractJob,T extends AbstractTask> void addTaskProcessingServiceFor(Class<T> entityClass, ITaskProcessingService<TJOB,T> service){
        taskProcessingServicesMap.put(entityClass, service);
    }

    public void addTaskProcessingService(Class<? extends ITaskProcessingService> serviceClass){
        TaskProcessingForClass ann = serviceClass.getAnnotation(TaskProcessingForClass.class);
        try {
            addTaskProcessingServiceFor(ann.value(), serviceClass.newInstance());
        }
        catch(IllegalAccessException|InstantiationException e){
            throw new RuntimeException("Cannot instantiate class <"+serviceClass.getName()+">",e);
        }
    }


    public <TJOB extends AbstractJob,T extends AbstractTask> ITaskProcessingService<TJOB,T> getTaskProcessingServiceForClass(Class<T> entityClass) throws ProcessingServiceNotFoundException {
        ITaskProcessingService<TJOB,T> result = (ITaskProcessingService<TJOB,T>) taskProcessingServicesMap.get(entityClass);
        if (result == null) {
            Class parentClass = entityClass.getSuperclass();
            if (AbstractTask.class.isAssignableFrom(parentClass)) {
                result = getTaskProcessingServiceForClass(parentClass.asSubclass(AbstractTask.class));
                if (result != null) {
                    taskProcessingServicesMap.put(entityClass, result);
                }
            }
        }
        if(result==null){
            throw new ProcessingServiceNotFoundException("Cannot find execution class for task <"+entityClass.getName()+">");
        }
        return result;
    }



    public <T extends AbstractJob> IJobProcessingService<T> getJobProcessingServiceForClass(Class<T> entityClass) throws ProcessingServiceNotFoundException{
        IJobProcessingService<T> result = (IJobProcessingService<T>) jobProcessingServicesMap.get(entityClass);
        if (result == null) {
            Class parentClass = entityClass.getSuperclass();
            if (AbstractJob.class.isAssignableFrom(parentClass)) {
                result = getJobProcessingServiceForClass(parentClass.asSubclass(AbstractJob.class));
                if (result != null) {
                    jobProcessingServicesMap.put(entityClass, result);
                }
            }
        }
        if(result==null){
            throw new ProcessingServiceNotFoundException("Cannot find execution class for job <"+entityClass.getName()+">");
        }
        return result;
    }


    public <T extends AbstractJob> boolean init(JobContext ctxt) throws JobExecutionException,ProcessingServiceNotFoundException {
        return getJobProcessingServiceForClass(ctxt.getJob().getClass()).init(ctxt);
    }

    public <T extends AbstractJob> boolean preprocess(JobContext ctxt) throws JobExecutionException,ProcessingServiceNotFoundException {
        return getJobProcessingServiceForClass(ctxt.getJob().getClass()).preprocess(ctxt);
    }

    public <T extends AbstractJob> boolean postprocess(JobContext ctxt,T job) throws JobExecutionException,ProcessingServiceNotFoundException {
        return getJobProcessingServiceForClass(job.getClass()).postprocess(ctxt);
    }

    public <T extends AbstractJob> boolean cleanup(JobContext ctxt,T job) throws JobExecutionException,ProcessingServiceNotFoundException {
        return getJobProcessingServiceForClass(job.getClass()).cleanup(ctxt);
    }


    public <T extends AbstractTask> boolean init(TaskContext ctxt) throws TaskExecutionException,ProcessingServiceNotFoundException {
        return getTaskProcessingServiceForClass(ctxt.getTask().getClass()).init(ctxt);
    }

    public <T extends AbstractTask> boolean preprocess(TaskContext ctxt) throws TaskExecutionException,ProcessingServiceNotFoundException {
        return getTaskProcessingServiceForClass(ctxt.getTask().getClass()).preprocess(ctxt);
    }

    public <T extends AbstractTask> boolean process(TaskContext ctxt) throws TaskExecutionException,ProcessingServiceNotFoundException {
        return getTaskProcessingServiceForClass(ctxt.getTask().getClass()).process(ctxt);
    }

    public <T extends AbstractTask> boolean postprocess(TaskContext ctxt) throws TaskExecutionException,ProcessingServiceNotFoundException {
        return getTaskProcessingServiceForClass(ctxt.getTask().getClass()).postprocess(ctxt);
    }

    public <T extends AbstractTask> boolean finish(TaskContext ctxt) throws TaskExecutionException,ProcessingServiceNotFoundException {
        return getTaskProcessingServiceForClass(ctxt.getTask().getClass()).finish(ctxt);
    }
    public <T extends AbstractTask> boolean cleanup(TaskContext ctxt) throws TaskExecutionException,ProcessingServiceNotFoundException {
        return getTaskProcessingServiceForClass(ctxt.getTask().getClass()).cleanup(ctxt);
    }
}
