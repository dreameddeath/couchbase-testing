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

import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.model.base.AbstractJob;
import com.dreameddeath.core.process.service.context.JobContext;


/**
 * Created by Christophe Jeunesse on 23/11/2014.
 */
public interface IJobProcessingService<T extends AbstractJob> {
    boolean init(JobContext<T> context) throws JobExecutionException;
    boolean preprocess(JobContext<T> context)throws JobExecutionException;
    boolean postprocess(JobContext<T> context)throws JobExecutionException;
    boolean cleanup(JobContext<T> context)throws JobExecutionException;
}
