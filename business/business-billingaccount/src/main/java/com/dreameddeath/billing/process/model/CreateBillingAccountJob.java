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

package com.dreameddeath.billing.process.model;

import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.billing.model.account.BillingAccountLink;
import com.dreameddeath.core.model.annotation.DocumentDef;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.process.model.AbstractJob;
import com.dreameddeath.core.process.model.DocumentCreateTask;
import com.dreameddeath.core.process.model.DocumentUpdateTask;
import com.dreameddeath.core.process.model.SubJobProcessTask;
import com.dreameddeath.party.model.base.Party;
import com.dreameddeath.party.model.base.PartyLink;

/**
 * Created by Christophe Jeunesse on 29/05/2014.
 */
@DocumentDef(domain = "billing",version="1.0.0")
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


    @DocumentDef(domain = "billing",version="1.0.0")
    public static class CreateBillingAccountTask extends DocumentCreateTask<BillingAccount> { }

    @DocumentDef(domain = "billing",version="1.0.0")
    public static class CreatePartyRolesTask extends DocumentUpdateTask<Party> { }

    @DocumentDef(domain = "billing",version="1.0.0")
    public static class CreateBillingCycleJobTask extends SubJobProcessTask<CreateBillingCycleJob> { }
}
