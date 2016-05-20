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

package com.dreameddeath.core.process.service.factory.impl;

import com.dreameddeath.core.process.exception.ExecutorServiceNotFoundException;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import com.dreameddeath.core.process.service.IJobExecutorService;
import com.dreameddeath.core.process.service.ITaskExecutorService;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.factory.IExecutorServiceFactory;
import com.dreameddeath.core.process.service.impl.BasicJobExecutorServiceImpl;
import com.dreameddeath.core.process.service.impl.BasicTaskExecutorServiceImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
public class ExecutorServiceFactory implements IExecutorServiceFactory {
    private Map<Class<? extends AbstractJob>, IJobExecutorService<?>> jobExecutorServicesMap
            = new ConcurrentHashMap<>();
    private Map<Class<? extends AbstractTask>, ITaskExecutorService<?,?>> taskExecutorServicesMap
            = new ConcurrentHashMap<>();


    public ExecutorServiceFactory(){
        addJobExecutorServiceFor(AbstractJob.class, new BasicJobExecutorServiceImpl<>());
        addTaskExecutorServiceFor(AbstractTask.class, new BasicTaskExecutorServiceImpl<>());
    }


    public <TJOB extends AbstractJob,T extends AbstractTask> ITaskExecutorService<TJOB,T>  addTaskExecutorService(Class<T> entityClass, Class<ITaskExecutorService<TJOB,T>> serviceClass){
        try {
            ITaskExecutorService<TJOB,T> service =serviceClass.newInstance();
            addTaskExecutorServiceFor(entityClass,service);
            return service;
        }
        catch(InstantiationException|IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }


    public <TJOB extends AbstractJob,T extends AbstractTask> ITaskExecutorService<TJOB,T>  addTaskExecutorServiceFor(Class<T> entityClass, ITaskExecutorService<TJOB,T> service){
        taskExecutorServicesMap.put(entityClass, service);
        return service;
    }

    private <TJOB extends AbstractJob,T extends AbstractTask> ITaskExecutorService<TJOB,T> getTaskExecutorServiceForClass(Class<T> entityClass,Class<?> origClass) throws ExecutorServiceNotFoundException{
        ITaskExecutorService<TJOB,T> result = (ITaskExecutorService<TJOB,T>) taskExecutorServicesMap.get(entityClass);
        if (result == null) {
            Class parentClass = entityClass.getSuperclass();
            if (AbstractTask.class.isAssignableFrom(parentClass)) {
                result = getTaskExecutorServiceForClass(parentClass.asSubclass(AbstractTask.class),origClass);
                if (result != null) {
                    addTaskExecutorServiceFor(entityClass, result);
                }
            }
        }
        if(result==null){
            throw new ExecutorServiceNotFoundException("Cannot find execution class for job <"+origClass.getName()+">");
        }
        return result;
    }

    public <TJOB extends AbstractJob,T extends AbstractTask> ITaskExecutorService<TJOB,T> getTaskExecutorServiceForClass(Class<T> entityClass) throws ExecutorServiceNotFoundException{
        return getTaskExecutorServiceForClass(entityClass,entityClass);
    }

    public <T extends AbstractJob> IJobExecutorService<T> addJobExecutorService(Class<T> entityClass, Class<IJobExecutorService<T>> serviceClass){
        try {
            IJobExecutorService<T> service =serviceClass.newInstance();
            addJobExecutorServiceFor(entityClass,service);
            return service;
        }
        catch(InstantiationException|IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }


    public <T extends AbstractJob> IJobExecutorService<T> addJobExecutorServiceFor(Class<T> entityClass, IJobExecutorService<T> service){
        jobExecutorServicesMap.put(entityClass, service);
        return service;
    }

    private <T extends AbstractJob> IJobExecutorService<T> getJobExecutorServiceForClass(Class<T> entityClass,Class<?>origClass) throws ExecutorServiceNotFoundException {
        IJobExecutorService<T> result = (IJobExecutorService<T>) jobExecutorServicesMap.get(entityClass);
        if (result == null) {
            Class parentClass = entityClass.getSuperclass();
            if (AbstractJob.class.isAssignableFrom(parentClass)) {
                result = getJobExecutorServiceForClass(parentClass.asSubclass(AbstractJob.class),origClass);
                if (result != null) {
                    jobExecutorServicesMap.put(entityClass, result);
                }
            }
        }
        if(result==null){
            throw new ExecutorServiceNotFoundException("Cannot find execution class for job <"+origClass.getName()+">");
        }
        return result;
    }

    public <T extends AbstractJob> IJobExecutorService<T> getJobExecutorServiceForClass(Class<T> entityClass) throws ExecutorServiceNotFoundException {
        return getJobExecutorServiceForClass(entityClass,entityClass);
    }


    public <T extends AbstractJob> JobContext<T> execute(JobContext<T> ctxt) throws JobExecutionException,ExecutorServiceNotFoundException {
        getJobExecutorServiceForClass((Class<T>)ctxt.getJob().getClass()).execute(ctxt);
        return ctxt;
    }

    public <T extends AbstractTask> TaskContext<? extends AbstractJob,T> execute(TaskContext<? extends AbstractJob,T> ctxt) throws TaskExecutionException,ExecutorServiceNotFoundException {
        getTaskExecutorServiceForClass((Class<T>)ctxt.getTask().getClass()).execute((TaskContext<AbstractJob,T>)ctxt);
        return ctxt;
    }

    public void cleanup(){
        jobExecutorServicesMap.clear();
        taskExecutorServicesMap.clear();
    }
}
