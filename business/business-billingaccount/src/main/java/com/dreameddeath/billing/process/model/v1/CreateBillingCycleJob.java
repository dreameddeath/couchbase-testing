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

package com.dreameddeath.billing.process.model.v1;

import com.dreameddeath.billing.model.v1.account.BillingAccount;
import com.dreameddeath.billing.model.v1.account.BillingAccountLink;
import com.dreameddeath.billing.model.v1.cycle.BillingCycle;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.tasks.ChildDocumentCreateTask;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

/**
 * Created by Christophe Jeunesse on 03/08/2014.
 */
@DocumentEntity
public class CreateBillingCycleJob extends AbstractJob {
    @DocumentProperty("ba")
    public BillingAccountLink baLink;
    @DocumentProperty("startDate")
    public DateTime startDate;


    @DocumentEntity
    public static class CreateBillingCycleTask extends ChildDocumentCreateTask<BillingCycle,BillingAccount> {
        @JsonCreator
        public CreateBillingCycleTask(@JsonProperty("parent") String parentKey) {
            super(parentKey);
        }
    }
}
