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

package com.dreameddeath.billing.installedbase.process;

import com.dreameddeath.billing.installedbase.process.model.v1.CreateUpdateBillingInstalledBaseJob;
import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.JobProcessingResult;
import com.dreameddeath.core.process.service.impl.processor.StandardJobProcessingService;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 16/11/2016.
 */
@JobProcessingForClass(CreateUpdateBillingInstalledBaseJob.class)
public class CreateUpdateBillingInstalledBaseProcessingService extends StandardJobProcessingService<CreateUpdateBillingInstalledBaseJob> {
    @Override
    public Observable<JobProcessingResult<CreateUpdateBillingInstalledBaseJob>> init(JobContext<CreateUpdateBillingInstalledBaseJob> context) {
        return null;
    }
}
