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

import com.dreameddeath.core.process.model.base.AbstractJob;
import com.dreameddeath.core.process.service.IJobExecutorClient;

/**
 * Created by Christophe Jeunesse on 02/01/2016.
 */
public interface IJobExecutorClientFactory {
     <T extends AbstractJob> IJobExecutorClient<T> buildJobClient(Class<T> jobClass);
}
