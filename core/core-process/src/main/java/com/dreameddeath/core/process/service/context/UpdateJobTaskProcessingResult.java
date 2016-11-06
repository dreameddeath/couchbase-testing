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

package com.dreameddeath.core.process.service.context;

import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 02/11/2016.
 */
public class UpdateJobTaskProcessingResult<TJOB extends AbstractJob,TTASK extends AbstractTask> {
    private final TJOB job;
    private final TTASK task;
    private final boolean isJobUpdate;

    public UpdateJobTaskProcessingResult(TJOB job, TTASK task, boolean isJobUpdate) {
        this.job = job;
        this.task = task;
        this.isJobUpdate = isJobUpdate;
    }

    public TJOB getJob() {
        return job;
    }

    public TTASK getTask() {
        return task;
    }

    public boolean isJobUpdate() {
        return isJobUpdate;
    }

    public Observable<UpdateJobTaskProcessingResult<TJOB,TTASK>> toObservable(){
        return Observable.just(this);
    }
}
