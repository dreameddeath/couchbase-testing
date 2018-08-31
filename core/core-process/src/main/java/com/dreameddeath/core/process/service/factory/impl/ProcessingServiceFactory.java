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

import com.dreameddeath.core.depinjection.IDependencyInjector;
import com.dreameddeath.core.depinjection.impl.NotManagedDependencyInjector;
import com.dreameddeath.core.java.utils.ClassUtils;
import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.exception.ProcessingServiceNotFoundException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import com.dreameddeath.core.process.service.IJobProcessingService;
import com.dreameddeath.core.process.service.ITaskProcessingService;
import com.dreameddeath.core.process.service.factory.IProcessingServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
public class ProcessingServiceFactory implements IProcessingServiceFactory {
    private IDependencyInjector dependencyInjector=new NotManagedDependencyInjector();
    private Map<Class<? extends AbstractJob>, IJobProcessingService<?>> jobProcessingServicesMap
            = new ConcurrentHashMap<>();
    private Map<Class<? extends AbstractTask>, ITaskProcessingService<?,?>> taskProcessingServicesMap
            = new ConcurrentHashMap<>();


    @Autowired(required = false)
    public void setDependencyInjector(IDependencyInjector dependencyInjector){
        this.dependencyInjector=dependencyInjector;
    }

    public <T extends AbstractJob> IJobProcessingService<T> addJobProcessingServiceFor(Class<T> entityClass, IJobProcessingService<T> service){
        jobProcessingServicesMap.put(entityClass, service);
        return service;
    }

    protected <T extends IJobProcessingService<? extends AbstractJob>> T  createJobProcessingService(Class<T> serviceClass){
        return dependencyInjector.getBeanOfType(serviceClass); //serviceClass.newInstance();
    }

    public <TJOB extends AbstractJob,T extends IJobProcessingService<TJOB>> T addJobProcessingService(Class<T> serviceClass){
        JobProcessingForClass ann = serviceClass.getAnnotation(JobProcessingForClass.class);
        if(ann==null){
            throw new RuntimeException("Cannot find annotation JobProcessingForClass for job processing service class "+serviceClass.getName());
        }
        T jobProcessingService = createJobProcessingService(serviceClass);
        addJobProcessingServiceFor((Class<TJOB>)ann.value(), jobProcessingService);

        for(Class innerClass:serviceClass.getClasses()){
            if(ITaskProcessingService.class.isAssignableFrom(innerClass)){
                try {
                    getTaskProcessingServiceForClass(ClassUtils.getEffectiveGenericType(innerClass,ITaskProcessingService.class,1));
                }
                catch(ProcessingServiceNotFoundException e){
                    addTaskProcessingService((Class<ITaskProcessingService<TJOB,AbstractTask>>) innerClass);
                }
            }
        }
        return jobProcessingService;
    }


    public <TJOB extends AbstractJob,T extends IJobProcessingService<TJOB>> List<T> addJobProcessingServices(List<Class<T>> serviceClasses){
        List<T> results=new ArrayList<>(serviceClasses.size());
        for(Class<T> serviceClass:serviceClasses) {
            results.add(addJobProcessingService(serviceClass));
        }
        return results;
    }



    public <TJOB extends AbstractJob,T extends AbstractTask> ITaskProcessingService<TJOB, T> addTaskProcessingServiceFor(Class<T> entityClass, ITaskProcessingService<TJOB,T> service){
        return (ITaskProcessingService<TJOB, T>)taskProcessingServicesMap.putIfAbsent(entityClass, service);
    }

    protected <TJOB extends AbstractJob,TTASK extends AbstractTask,T extends ITaskProcessingService<TJOB,TTASK>> T createTaskProcessingService(Class<T> serviceClass){
        return dependencyInjector.getBeanOfType(serviceClass);
    }

    public <TJOB extends AbstractJob,TTASK extends AbstractTask,T extends ITaskProcessingService<TJOB,TTASK>> T addTaskProcessingService(Class<T> serviceClass){
        TaskProcessingForClass ann = serviceClass.getAnnotation(TaskProcessingForClass.class);
        if(ann==null){
            throw new RuntimeException("Cannot find annotation TaskProcessingForClass for processing service class "+serviceClass.getName());
        }
        T service = createTaskProcessingService(serviceClass);
        return (T)addTaskProcessingServiceFor((Class<TTASK>)ann.value(),service);
    }

    public <TJOB extends AbstractJob,TTASK extends AbstractTask,T extends ITaskProcessingService<TJOB,TTASK>> T addTaskProcessingService(T service){
        TaskProcessingForClass ann = service.getClass().getAnnotation(TaskProcessingForClass.class);
        if(ann==null){
            throw new RuntimeException("Cannot find annotation TaskProcessingForClass for processing service class "+service.getClass().getName());
        }
        return (T)addTaskProcessingServiceFor((Class<TTASK>)ann.value(),service);
    }


    public <TJOB extends AbstractJob,TTASK extends AbstractTask,T extends ITaskProcessingService<TJOB,TTASK>> List<T> addTaskProcessingServices(List<T> services){
        List<T> result = new ArrayList<>(services.size());
        for(T service:services) {
            result.add(addTaskProcessingService(service));
        }
        return result;
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

    public void cleanup(){
        jobProcessingServicesMap.clear();
        taskProcessingServicesMap.clear();
    }
}
