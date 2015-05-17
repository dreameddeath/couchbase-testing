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

import com.dreameddeath.core.process.exception.ExecutorServiceNotFoundException;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.model.AbstractJob;
import com.dreameddeath.core.process.model.AbstractTask;
import com.dreameddeath.core.process.service.impl.BasicJobExecutorServiceImpl;
import com.dreameddeath.core.process.service.impl.BasicTaskExecutorServiceImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
public class ExecutorServiceFactory {
    private Map<Class<? extends AbstractJob>, IJobExecutorService<?>> _jobExecutorServicesMap
            = new ConcurrentHashMap<Class<? extends AbstractJob>, IJobExecutorService<?>>();
    private Map<Class<? extends AbstractTask>, ITaskExecutorService<?>> _taskExecutorServicesMap
            = new ConcurrentHashMap<Class<? extends AbstractTask>, ITaskExecutorService<?>>();


    public ExecutorServiceFactory(){
        addJobExecutorServiceFor(AbstractJob.class, new BasicJobExecutorServiceImpl());
        addTaskExecutorServiceFor(AbstractTask.class, new BasicTaskExecutorServiceImpl());
    }


    public <T extends AbstractTask> void addTaskExecutorServiceFor(Class<T> entityClass, ITaskExecutorService<T> service){
        _taskExecutorServicesMap.put(entityClass, service);
    }

    public <T extends AbstractTask> ITaskExecutorService<T> getTaskExecutorServiceForClass(Class<T> entityClass) {
        ITaskExecutorService<T> result = (ITaskExecutorService<T>) _taskExecutorServicesMap.get(entityClass);
        if (result == null) {
            Class parentClass = entityClass.getSuperclass();
            if (AbstractTask.class.isAssignableFrom(parentClass)) {
                result = getTaskExecutorServiceForClass(parentClass.asSubclass(AbstractTask.class));
                if (result != null) {
                    _taskExecutorServicesMap.put(entityClass, result);
                }
            }
        }
        ///TODO throw an error if null
        return result;
    }


    public <T extends AbstractJob> void addJobExecutorServiceFor(Class<T> entityClass, IJobExecutorService<T> service){
        _jobExecutorServicesMap.put(entityClass, service);
    }

    public <T extends AbstractJob> IJobExecutorService<T> getJobExecutorServiceForClass(Class<T> entityClass) throws ExecutorServiceNotFoundException {
        IJobExecutorService<T> result = (IJobExecutorService<T>) _jobExecutorServicesMap.get(entityClass);
        if (result == null) {
            Class parentClass = entityClass.getSuperclass();
            if (AbstractJob.class.isAssignableFrom(parentClass)) {
                result = getJobExecutorServiceForClass(parentClass.asSubclass(AbstractJob.class));
                if (result != null) {
                    _jobExecutorServicesMap.put(entityClass, result);
                }
            }
        }
        if(result==null){
            throw new ExecutorServiceNotFoundException("Cannot find execution class for job <"+entityClass.getName()+">");
        }
        ///TODO throw an error if null
        return result;
    }


    public <T extends AbstractJob> void execute(JobContext ctxt,T job) throws JobExecutionException,ExecutorServiceNotFoundException {
        ((IJobExecutorService<T>) getJobExecutorServiceForClass(job.getClass())).execute(ctxt,job);
    }

    public <T extends AbstractTask> void execute(TaskContext ctxt,T task) throws TaskExecutionException,ExecutorServiceNotFoundException {
        ((ITaskExecutorService<T>) getTaskExecutorServiceForClass(task.getClass())).execute(ctxt,task);
    }
}
