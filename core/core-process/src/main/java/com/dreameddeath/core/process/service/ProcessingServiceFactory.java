/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
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
    private Map<Class<? extends AbstractJob>, IJobProcessingService<?>> _jobProcessingServicesMap
            = new ConcurrentHashMap<Class<? extends AbstractJob>, IJobProcessingService<?>>();
    private Map<Class<? extends AbstractTask>, ITaskProcessingService<?>> _taskProcessingServicesMap
            = new ConcurrentHashMap<Class<? extends AbstractTask>, ITaskProcessingService<?>>();


    public <T extends AbstractJob> void addJobProcessingServiceFor(Class<T> entityClass, IJobProcessingService<T> service){
        _jobProcessingServicesMap.put(entityClass, service);
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

    public <T extends AbstractTask> void addTaskProcessingServiceFor(Class<T> entityClass, ITaskProcessingService<T> service){
        _taskProcessingServicesMap.put(entityClass, service);
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


    public <T extends AbstractTask> ITaskProcessingService<T> getTaskProcessingServiceForClass(Class<T> entityClass) throws ProcessingServiceNotFoundException {
        ITaskProcessingService<T> result = (ITaskProcessingService<T>) _taskProcessingServicesMap.get(entityClass);
        if (result == null) {
            Class parentClass = entityClass.getSuperclass();
            if (AbstractTask.class.isAssignableFrom(parentClass)) {
                result = getTaskProcessingServiceForClass(parentClass.asSubclass(AbstractTask.class));
                if (result != null) {
                    _taskProcessingServicesMap.put(entityClass, result);
                }
            }
        }
        if(result==null){
            throw new ProcessingServiceNotFoundException("Cannot find execution class for task <"+entityClass.getName()+">");
        }
        return result;
    }



    public <T extends AbstractJob> IJobProcessingService<T> getJobProcessingServiceForClass(Class<T> entityClass) throws ProcessingServiceNotFoundException{
        IJobProcessingService<T> result = (IJobProcessingService<T>) _jobProcessingServicesMap.get(entityClass);
        if (result == null) {
            Class parentClass = entityClass.getSuperclass();
            if (AbstractJob.class.isAssignableFrom(parentClass)) {
                result = getJobProcessingServiceForClass(parentClass.asSubclass(AbstractJob.class));
                if (result != null) {
                    _jobProcessingServicesMap.put(entityClass, result);
                }
            }
        }
        if(result==null){
            throw new ProcessingServiceNotFoundException("Cannot find execution class for job <"+entityClass.getName()+">");
        }
        return result;
    }


    public <T extends AbstractJob> boolean init(JobContext ctxt,T job) throws JobExecutionException,ProcessingServiceNotFoundException {
        return ((IJobProcessingService<T>) getJobProcessingServiceForClass(job.getClass())).init(ctxt,job);
    }

    public <T extends AbstractJob> boolean preprocess(JobContext ctxt,T job) throws JobExecutionException,ProcessingServiceNotFoundException {
        return ((IJobProcessingService<T>) getJobProcessingServiceForClass(job.getClass())).preprocess(ctxt,job);
    }

    public <T extends AbstractJob> boolean postprocess(JobContext ctxt,T job) throws JobExecutionException,ProcessingServiceNotFoundException {
        return ((IJobProcessingService<T>) getJobProcessingServiceForClass(job.getClass())).postprocess(ctxt,job);
    }

    public <T extends AbstractJob> boolean cleanup(JobContext ctxt,T job) throws JobExecutionException,ProcessingServiceNotFoundException {
        return ((IJobProcessingService<T>) getJobProcessingServiceForClass(job.getClass())).cleanup(ctxt,job);
    }


    public <T extends AbstractTask> boolean init(TaskContext ctxt,T task) throws TaskExecutionException,ProcessingServiceNotFoundException {
        return ((ITaskProcessingService<T>) getTaskProcessingServiceForClass(task.getClass())).init(ctxt,task);
    }

    public <T extends AbstractTask> boolean preprocess(TaskContext ctxt,T task) throws TaskExecutionException,ProcessingServiceNotFoundException {
        return ((ITaskProcessingService<T>) getTaskProcessingServiceForClass(task.getClass())).preprocess(ctxt,task);
    }

    public <T extends AbstractTask> boolean process(TaskContext ctxt,T task) throws TaskExecutionException,ProcessingServiceNotFoundException {
        return ((ITaskProcessingService<T>) getTaskProcessingServiceForClass(task.getClass())).process(ctxt, task);
    }

    public <T extends AbstractTask> boolean postprocess(TaskContext ctxt,T task) throws TaskExecutionException,ProcessingServiceNotFoundException {
        return ((ITaskProcessingService<T>) getTaskProcessingServiceForClass(task.getClass())).postprocess(ctxt, task);
    }

    public <T extends AbstractTask> boolean finish(TaskContext ctxt,T task) throws TaskExecutionException,ProcessingServiceNotFoundException {
        return ((ITaskProcessingService<T>) getTaskProcessingServiceForClass(task.getClass())).finish(ctxt, task);
    }
    public <T extends AbstractTask> boolean cleanup(TaskContext ctxt,T task) throws TaskExecutionException,ProcessingServiceNotFoundException {
        return ((ITaskProcessingService<T>) getTaskProcessingServiceForClass(task.getClass())).cleanup(ctxt, task);
    }
}
