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

package com.dreameddeath.core.process.service.impl.processor;

import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.service.IJobProcessingService;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.JobProcessingResult;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 25/11/2014.
 */
public abstract class StandardJobProcessingService<T extends AbstractJob> implements IJobProcessingService<T> {

    @Override
    public Observable<JobProcessingResult<T>> preprocess(JobContext<T> context){
        return JobProcessingResult.build(context,false);
    }

    @Override
    public Observable<JobProcessingResult<T>> postprocess(JobContext<T> context){
        return JobProcessingResult.build(context,false);
    }

    @Override
    public Observable<JobProcessingResult<T>> notify(JobContext<T> context){
        return JobProcessingResult.build(context,false);
    }

    @Override
    public Observable<JobProcessingResult<T>> cleanup(JobContext<T> context){
        return JobProcessingResult.build(context,false);
    }
}
