package com.dreameddeath.billing.process.model;

import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.billing.model.cycle.BillingCycle;
import com.dreameddeath.billing.util.BillCycleUtils;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.process.JobExecutionException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.process.AbstractJob;
import com.dreameddeath.core.model.process.EmptyJobResult;
import com.dreameddeath.core.process.business.model.DocumentCreateTask;
import com.dreameddeath.core.process.business.model.DocumentUpdateTask;

/**
 * Created by Christophe Jeunesse on 03/08/2014.
 */
public class CreateBillingCycleJob extends AbstractJob<CreateBillingCycleRequest,EmptyJobResult> {
    @Override
    public CreateBillingCycleRequest newRequest(){return new CreateBillingCycleRequest();}
    @Override
    public EmptyJobResult newResult(){return new EmptyJobResult();}

    public static class CreateBillingCycleTask extends DocumentCreateTask<BillingCycle> { }

    public static class CreateBillingCycleLinkTask extends DocumentUpdateTask<BillingAccount> { }
}
