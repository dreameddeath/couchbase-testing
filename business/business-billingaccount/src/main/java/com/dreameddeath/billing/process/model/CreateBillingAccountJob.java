package com.dreameddeath.billing.process.model;

import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.billing.model.account.BillingAccountPartyRole;
import com.dreameddeath.core.annotation.DocumentDef;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.process.JobExecutionException;
import com.dreameddeath.core.exception.storage.StorageException;
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
