/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.process.service.factory.impl;

import com.codahale.metrics.MetricRegistry;
import com.dreameddeath.core.dao.session.ICouchbaseSessionFactory;
import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.process.model.discovery.JobExecutorClientInfo;
import com.dreameddeath.core.process.model.discovery.TaskExecutorClientInfo;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import com.dreameddeath.core.process.registrar.JobExecutorClientRegistrar;
import com.dreameddeath.core.process.registrar.TaskExecutorClientRegistrar;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.dreameddeath.core.process.service.ITaskExecutorClient;
import com.dreameddeath.core.process.service.factory.IExecutorServiceFactory;
import com.dreameddeath.core.process.service.factory.IJobExecutorClientFactory;
import com.dreameddeath.core.process.service.factory.IProcessingServiceFactory;
import com.dreameddeath.core.process.service.factory.ITaskExecutorClientFactory;
import com.dreameddeath.core.process.service.impl.client.BasicJobExecutorClientImpl;
import com.dreameddeath.core.process.service.impl.client.BasicTaskExecutorClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 02/01/2016.
 */
public class ExecutorClientFactory implements IJobExecutorClientFactory,ITaskExecutorClientFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ExecutorClientFactory.class);
    private final JobExecutorClientRegistrar jobRegistrar;
    private final TaskExecutorClientRegistrar taskRegistrar;
    private final IEventBus bus;
    private final ICouchbaseSessionFactory couchbaseSessionFactory;
    private final IExecutorServiceFactory executorServiceFactory;
    private final IProcessingServiceFactory processingServiceFactory;
    private final MetricRegistry metricRegistry;
    private final Map<Class<? extends AbstractJob>,IJobExecutorClient<? extends AbstractJob>> jobClientMap=new ConcurrentHashMap<>();
    private final Map<TaskClientKey<? extends AbstractJob,? extends AbstractTask>,ITaskExecutorClient<? extends AbstractJob,? extends AbstractTask>> taskClientMap=new ConcurrentHashMap<>();

    public ExecutorClientFactory(ICouchbaseSessionFactory couchbaseSessionFactory, ExecutorServiceFactory executorServiceFactory, ProcessingServiceFactory processingServiceFactory,IEventBus bus, MetricRegistry registry,JobExecutorClientRegistrar jobRegistrar,TaskExecutorClientRegistrar taskRegistrar){
        this.couchbaseSessionFactory = couchbaseSessionFactory;
        this.executorServiceFactory = executorServiceFactory;
        this.processingServiceFactory=processingServiceFactory;
        this.metricRegistry = registry;
        this.jobRegistrar = jobRegistrar;
        this.taskRegistrar = taskRegistrar;
        this.bus=bus;
    }


    public ExecutorClientFactory(ICouchbaseSessionFactory couchbaseSessionFactory, ExecutorServiceFactory executorServiceFactory, ProcessingServiceFactory processingServiceFactory,IEventBus bus, MetricRegistry registry){
        this(couchbaseSessionFactory,executorServiceFactory,processingServiceFactory,bus,registry,null,null);
    }

    public ExecutorClientFactory(ICouchbaseSessionFactory couchbaseSessionFactory, ExecutorServiceFactory executorServiceFactory, ProcessingServiceFactory processingServiceFactory,IEventBus bus){
        this(couchbaseSessionFactory,executorServiceFactory,processingServiceFactory,bus,null);
    }

    private <TJOB extends AbstractJob> IJobExecutorClient<TJOB> createJobClient(Class<TJOB> jobClass){
        IJobExecutorClient<TJOB> jobClient = new BasicJobExecutorClientImpl<>(jobClass, this, couchbaseSessionFactory, executorServiceFactory, processingServiceFactory,bus, metricRegistry);
        if(jobRegistrar!=null) {
            try {
                JobExecutorClientInfo info = new JobExecutorClientInfo(jobClient);
                jobRegistrar.enrich(info);
                jobRegistrar.register(info);
            }
            catch(Exception e){
                LOG.error("Error during unregistrar of job client "+jobClient.getInstanceUUID(),e);
                throw new RuntimeException(e);
            }
        }
        return jobClient;

    }

    @SuppressWarnings("unchecked")
    public <TJOB extends AbstractJob> IJobExecutorClient<TJOB> buildJobClient(Class<TJOB> jobClass){
        return (IJobExecutorClient<TJOB>)jobClientMap.computeIfAbsent(jobClass, this::createJobClient);
    }


    private <TJOB extends AbstractJob,TTASK extends AbstractTask> ITaskExecutorClient<TJOB,TTASK> createTaskClient(TaskClientKey<TJOB,TTASK> clientKey){
        ITaskExecutorClient<TJOB,TTASK> taskClient = new BasicTaskExecutorClientImpl<>(clientKey.jobClass,clientKey.taskClass,this,couchbaseSessionFactory,executorServiceFactory,processingServiceFactory,metricRegistry);
        if(taskRegistrar!=null) {
            try {
                TaskExecutorClientInfo info = new TaskExecutorClientInfo(taskClient);
                taskRegistrar.enrich(info);
                taskRegistrar.register(info);
            }
            catch(Exception e){
                LOG.error("Error during registrar of task client "+taskClient.getInstanceUUID(),e);
                throw new RuntimeException(e);
            }
        }
        return taskClient;

    }

    @SuppressWarnings("unchecked")
    public <TJOB extends AbstractJob,TTASK extends AbstractTask> ITaskExecutorClient<TJOB,TTASK> buildTaskClient(Class<TJOB> jobClass, Class<TTASK> taskClass){
        return (ITaskExecutorClient<TJOB,TTASK>) taskClientMap.computeIfAbsent(new TaskClientKey<>(jobClass,taskClass),this::createTaskClient);
    }


    public void cleanup(){
        if(taskRegistrar!=null){
            for(ITaskExecutorClient client:taskClientMap.values()){
                try {
                    TaskExecutorClientInfo info = new TaskExecutorClientInfo(client);
                    taskRegistrar.deregister(info);
                }
                catch(Exception e){
                    LOG.error("Error during unregistrar of task client "+client.getInstanceUUID(),e);
                }
            }
        }
        taskClientMap.clear();
        if(jobRegistrar!=null){
            for(IJobExecutorClient client:jobClientMap.values()){
                try {
                    JobExecutorClientInfo info = new JobExecutorClientInfo(client);
                    jobRegistrar.deregister(info);
                }
                catch(Exception e){
                    LOG.error("Error during unregistrar of job client "+client.getInstanceUUID(),e);
                }
            }
        }
        jobClientMap.clear();
    }


    protected static class TaskClientKey<TJOB extends AbstractJob,TTASK extends AbstractTask>{
        private final Class<TJOB> jobClass;
        private final Class<TTASK> taskClass;
        public TaskClientKey(Class<TJOB> jobClass,Class<TTASK> taskClass){
            this.jobClass = jobClass;
            this.taskClass = taskClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TaskClientKey<?, ?> that = (TaskClientKey<?, ?>) o;

            return jobClass.equals(that.jobClass) && taskClass.equals(that.taskClass);
        }

        @Override
        public int hashCode() {
            int result = jobClass.hashCode();
            result = 31 * result + taskClass.hashCode();
            return result;
        }
    }
}
