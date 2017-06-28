/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.process.service;

import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.user.IUser;

/**
 * Created by christophe jeunesse on 09/01/2017.
 */
public interface IJobBlockingExecutorClient<T extends AbstractJob> {
    JobContext<T> executeJob(T job, IUser user) throws JobExecutionException;
    JobContext<T> submitJob(T job, IUser user) throws JobExecutionException;
    JobContext<T> resumeJob(T job, IUser user) throws JobExecutionException;
    JobContext<T> cancelJob(T job, IUser user) throws JobExecutionException;
}
