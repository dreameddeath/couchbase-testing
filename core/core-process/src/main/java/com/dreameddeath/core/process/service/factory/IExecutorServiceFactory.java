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

package com.dreameddeath.core.process.service.factory;

import com.dreameddeath.core.process.exception.ExecutorServiceNotFoundException;
import com.dreameddeath.core.process.model.AbstractJob;
import com.dreameddeath.core.process.model.AbstractTask;
import com.dreameddeath.core.process.service.IJobExecutorService;
import com.dreameddeath.core.process.service.ITaskExecutorService;

/**
 * Created by Christophe Jeunesse on 02/01/2016.
 */
public interface IExecutorServiceFactory{
    <T extends AbstractJob> IJobExecutorService<T> getJobExecutorServiceForClass(Class<T> entityClass) throws ExecutorServiceNotFoundException;
    <TJOB extends AbstractJob,T extends AbstractTask> ITaskExecutorService<TJOB,T> getTaskExecutorServiceForClass(Class<T> entityClass) throws ExecutorServiceNotFoundException;
}
