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

import com.codahale.metrics.MetricRegistry;
import com.dreameddeath.core.dao.session.ICouchbaseSessionFactory;
import com.dreameddeath.core.process.model.AbstractJob;
import com.dreameddeath.core.process.model.AbstractTask;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.dreameddeath.core.process.service.ITaskExecutorClient;
import com.dreameddeath.core.process.service.factory.IExecutorServiceFactory;
import com.dreameddeath.core.process.service.factory.IJobExecutorClientFactory;
import com.dreameddeath.core.process.service.factory.IProcessingServiceFactory;
import com.dreameddeath.core.process.service.factory.ITaskExecutorClientFactory;
import com.dreameddeath.core.process.service.impl.BasicJobExecutorClient;
import com.dreameddeath.core.process.service.impl.BasicTaskExecutorClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 02/01/2016.
 */
public class ExecutorClientFactory implements IJobExecutorClientFactory,ITaskExecutorClientFactory {
    private final ICouchbaseSessionFactory couchbaseSessionFactory;
    private final IExecutorServiceFactory executorServiceFactory;
    private final IProcessingServiceFactory processingServiceFactory;
    private final MetricRegistry metricRegistry;
    private final Map<Class<? extends AbstractJob>,IJobExecutorClient<? extends AbstractJob>> jobClientMap=new ConcurrentHashMap<>();
    private final Map<TaskClientKey<? extends AbstractJob,? extends AbstractTask>,ITaskExecutorClient<? extends AbstractJob,? extends AbstractTask>> taskClientMap=new ConcurrentHashMap<>();

    public ExecutorClientFactory(ICouchbaseSessionFactory couchbaseSessionFactory, ExecutorServiceFactory executorServiceFactory, ProcessingServiceFactory processingServiceFactory, MetricRegistry registry){
        this.couchbaseSessionFactory = couchbaseSessionFactory;
        this.executorServiceFactory = executorServiceFactory;
        this.processingServiceFactory=processingServiceFactory;
        this.metricRegistry = registry;
    }

    public ExecutorClientFactory(ICouchbaseSessionFactory couchbaseSessionFactory, ExecutorServiceFactory executorServiceFactory, ProcessingServiceFactory processingServiceFactory){
        this(couchbaseSessionFactory,executorServiceFactory,processingServiceFactory,null);
    }

    @SuppressWarnings("unchecked")
    public <TJOB extends AbstractJob> IJobExecutorClient<TJOB> buildJobClient(Class<TJOB> jobClass){
        return (IJobExecutorClient<TJOB>)jobClientMap.computeIfAbsent(jobClass, aClass -> new BasicJobExecutorClient<>(aClass,this, couchbaseSessionFactory,executorServiceFactory,processingServiceFactory,metricRegistry));
    }

    @SuppressWarnings("unchecked")
    public <TJOB extends AbstractJob,TTASK extends AbstractTask> ITaskExecutorClient<TJOB,TTASK> buildTaskClient(Class<TJOB> jobClass, Class<TTASK> taskClass){
        return (ITaskExecutorClient<TJOB,TTASK>) taskClientMap.computeIfAbsent(new TaskClientKey<>(jobClass,taskClass),taskClassKey->new BasicTaskExecutorClient<>(taskClassKey.jobClass,taskClassKey.taskClass,this,couchbaseSessionFactory,executorServiceFactory,processingServiceFactory,metricRegistry));
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

            if (!jobClass.equals(that.jobClass)) return false;
            return taskClass.equals(that.taskClass);
        }

        @Override
        public int hashCode() {
            int result = jobClass.hashCode();
            result = 31 * result + taskClass.hashCode();
            return result;
        }
    }
}
