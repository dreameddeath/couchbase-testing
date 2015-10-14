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

package com.dreameddeath.core.exception.process;

import com.dreameddeath.core.process.common.AbstractJob;
import com.dreameddeath.core.process.common.AbstractTask;

/**
 * Created by Christophe Jeunesse on 08/10/2014.
 */
public class DuplicateTaskException extends JobExecutionException {
    AbstractTask task;

    public DuplicateTaskException(AbstractTask task, AbstractJob job){
        super(job,job.getJobState(),"The task <"+task.getUid()+"> is already existing in job <"+job.getBaseMeta().getKey()+">");
        this.task = task;
    }

    public DuplicateTaskException(AbstractTask task, AbstractJob job,String message){
        super(job,job.getJobState(),message);
        this.job = job;
        this.task = task;
    }
}
