package com.dreameddeath.billing.process.model;

import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.billing.model.account.BillingAccountPartyRole;
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
public class CreateBillingAccountJob extends AbstractJob<CreateBillingAccountRequest,CreateBillingAccountResult> {
    @Override
    public CreateBillingAccountRequest newRequest(){return new CreateBillingAccountRequest();}
    @Override
    public CreateBillingAccountResult newResult(){return new CreateBillingAccountResult();}

    public static class CreateBillingAccountTask extends DocumentCreateTask<BillingAccount> { }

    public static class CreatePartyRolesTask extends DocumentUpdateTask<Party> { }

    public static class CreateBillingCycleTask extends SubJobProcessTask<CreateBillingCycleJob> { }
}
