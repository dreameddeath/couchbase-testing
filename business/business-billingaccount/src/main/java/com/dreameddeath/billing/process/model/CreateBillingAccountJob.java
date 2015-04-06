/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.billing.process.model;

import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.core.annotation.DocumentDef;
import com.dreameddeath.core.model.process.AbstractJob;
import com.dreameddeath.core.model.process.SubJobProcessTask;
import com.dreameddeath.core.process.business.model.DocumentCreateTask;
import com.dreameddeath.core.process.business.model.DocumentUpdateTask;
import com.dreameddeath.party.model.base.Party;

/**
 * Created by Christophe Jeunesse on 29/05/2014.
 */
@DocumentDef(domain = "billing",version="1.0.0")
public class CreateBillingAccountJob extends AbstractJob<CreateBillingAccountRequest,CreateBillingAccountResult> {
    @Override
    public CreateBillingAccountRequest newRequest(){return new CreateBillingAccountRequest();}
    @Override
    public CreateBillingAccountResult newResult(){return new CreateBillingAccountResult();}

    @DocumentDef(domain = "billing",version="1.0.0")
    public static class CreateBillingAccountTask extends DocumentCreateTask<BillingAccount> { }

    @DocumentDef(domain = "billing",version="1.0.0")
    public static class CreatePartyRolesTask extends DocumentUpdateTask<Party> { }

    @DocumentDef(domain = "billing",version="1.0.0")
    public static class CreateBillingCycleJobTask extends SubJobProcessTask<CreateBillingCycleJob> { }
}
