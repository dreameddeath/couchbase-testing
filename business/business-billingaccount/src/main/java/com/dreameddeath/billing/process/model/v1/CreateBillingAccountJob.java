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
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.tasks.DocumentCreateTask;
import com.dreameddeath.core.process.model.v1.tasks.DocumentUpdateTask;
import com.dreameddeath.core.process.model.v1.tasks.SubJobProcessTask;
import com.dreameddeath.party.model.v1.Party;
import com.dreameddeath.party.model.v1.PartyLink;

/**
 * Created by Christophe Jeunesse on 29/05/2014.
 */
@DocumentEntity
public class CreateBillingAccountJob extends AbstractJob {
    @DocumentProperty("partyId")
    public String partyId;
    @DocumentProperty("billDay")
    public Integer billDay;
    @DocumentProperty("cycleLength")
    public Integer cycleLength;

    @DocumentProperty("baCreated")
    public BillingAccountLink baLink;
    @DocumentProperty("partyLink")
    public PartyLink partyLink;

    @DocumentEntity
    public static class CreateBillingAccountTask extends DocumentCreateTask<BillingAccount> { }

    @DocumentEntity
    public static class CreatePartyRolesTask extends DocumentUpdateTask<Party> { }

    @DocumentEntity
    public static class CreateBillingCycleJobTask extends SubJobProcessTask<CreateBillingCycleJob> { }
}
