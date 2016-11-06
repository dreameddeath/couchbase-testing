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

package com.dreameddeath.core.process.exception;

import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.tasks.SubJobProcessTask;
import com.dreameddeath.core.process.service.context.TaskContext;

/**
 * Created by Christophe Jeunesse on 30/10/2016.
 */
public class AlreadyCreatedSubJobObservableException extends RuntimeException{
    private final TaskContext ctxt;
    private final AbstractJob job;

    public AlreadyCreatedSubJobObservableException(TaskContext ctxt, AbstractJob job){
        this.ctxt=ctxt;
        this.job=job;
    }

    public <TJOB extends AbstractJob,TPARENTJOB extends AbstractJob,TTASK extends SubJobProcessTask<TJOB>> TaskContext<TPARENTJOB, TTASK> getCtxt(Class<TaskContext<TPARENTJOB,TTASK>> clazz) {
        return ctxt;
    }

    public <TJOB extends AbstractJob> TJOB getJob(Class<TJOB> clazz) {
        return (TJOB)job;
    }
}
