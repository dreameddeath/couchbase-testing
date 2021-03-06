/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.process.service.impl.processor;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import com.dreameddeath.core.process.service.ITaskProcessingService;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.context.TaskNotificationBuildResult;
import com.dreameddeath.core.process.service.context.TaskProcessingResult;
import com.dreameddeath.core.process.service.context.UpdateJobTaskProcessingResult;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Christophe Jeunesse on 29/12/2015.
 */
public abstract class StandardTaskProcessingService<TJOB extends AbstractJob,T extends AbstractTask> implements ITaskProcessingService<TJOB,T> {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Override
    public Single<TaskProcessingResult<TJOB,T>> init(TaskContext<TJOB,T> context) {
        return TaskProcessingResult.build(context,false);
    }

    @Override
    public Single<TaskProcessingResult<TJOB,T>> preprocess(TaskContext<TJOB,T> context) {
        return TaskProcessingResult.build(context,false);
    }

    @Override
    public Single<TaskProcessingResult<TJOB,T>> postprocess(TaskContext<TJOB,T> context){
        return TaskProcessingResult.build(context,false);
    }

    @Override
    public Single<TaskNotificationBuildResult<TJOB, T>> buildNotifications(TaskContext<TJOB, T> ctxt) {
        return TaskNotificationBuildResult.build(ctxt);
    }

    @Override
    public Single<UpdateJobTaskProcessingResult<TJOB, T>> updatejob(TJOB job, T task, ICouchbaseSession session) {
        return Single.just(new UpdateJobTaskProcessingResult<>(job,task,false));
    }

    @Override
    public Single<TaskProcessingResult<TJOB,T>> cleanup(TaskContext<TJOB,T> context) {
        return TaskProcessingResult.build(context,false);
    }

    protected void logError(TaskContext<TJOB,T> context, String state,Throwable e){
        LOG.error("Task error for context <{}> due with message <{}> with exception <{}>",context,state,e.toString());
    }
}
