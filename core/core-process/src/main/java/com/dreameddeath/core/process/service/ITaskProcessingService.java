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

package com.dreameddeath.core.process.service;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.context.TaskProcessingResult;
import com.dreameddeath.core.process.service.context.UpdateJobTaskProcessingResult;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 23/11/2014.
 */
public interface ITaskProcessingService<TJOB extends AbstractJob,T extends AbstractTask> {
    Observable<TaskProcessingResult<TJOB,T>> init(final TaskContext<TJOB,T> ctxt);
    Observable<TaskProcessingResult<TJOB,T>> preprocess(final TaskContext<TJOB,T> ctxt);
    Observable<TaskProcessingResult<TJOB,T>> process(final TaskContext<TJOB,T> ctxt);
    Observable<TaskProcessingResult<TJOB,T>> postprocess(final TaskContext<TJOB,T> ctxt);
    Observable<TaskProcessingResult<TJOB,T>> notify(final TaskContext<TJOB,T> ctxt);
    Observable<TaskProcessingResult<TJOB,T>> finish(final TaskContext<TJOB,T> ctxt);
    Observable<TaskProcessingResult<TJOB,T>> cleanup(final TaskContext<TJOB,T> ctxt);
    Observable<UpdateJobTaskProcessingResult<TJOB,T>> updatejob(final TJOB job, final T task, final ICouchbaseSession session);
}
