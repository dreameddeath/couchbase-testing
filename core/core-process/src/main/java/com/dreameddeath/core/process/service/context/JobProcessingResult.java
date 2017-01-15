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

package com.dreameddeath.core.process.service.context;

import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import io.reactivex.Single;

/**
 * Created by Christophe Jeunesse on 27/10/2016.
 */
public class JobProcessingResult<TJOB extends AbstractJob> {
    private final JobContext<TJOB> context;
    private final boolean needSave;

    public JobProcessingResult(JobContext<TJOB> context, boolean needSave) {
        this.context = context;
        this.needSave = needSave;
    }

    public JobContext<TJOB> getContext() {
        return context;
    }

    public boolean isNeedSave() {
        return needSave;
    }

    public static <TJOB extends AbstractJob> Single<JobProcessingResult<TJOB>> build(JobContext<TJOB> context, boolean needSave){
        return Single.just(new JobProcessingResult<>(context,needSave));
    }
}
