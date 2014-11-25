package com.dreameddeath.billing.process;

import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.billing.model.account.BillingAccountPartyRole;
import com.dreameddeath.billing.model.process.CreateBillingAccountRequest;
import com.dreameddeath.billing.model.process.CreateBillingAccountResult;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.process.JobExecutionException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.process.common.AbstractJob;
import com.dreameddeath.core.process.common.SubJobProcessTask;
import com.dreameddeath.core.process.business.DocumentCreateTask;
import com.dreameddeath.core.process.business.DocumentUpdateTask;
import com.dreameddeath.party.model.base.Party;

/**
 * Created by Christophe Jeunesse on 29/05/2014.
 */
public class CreateBillingAccountJob extends AbstractJob<CreateBillingAccountRequest,CreateBillingAccountResult> {
    @Override
    public CreateBillingAccountRequest newRequest(){return new CreateBillingAccountRequest();}
    @Override
    public CreateBillingAccountResult newResult(){return new CreateBillingAccountResult();}
    @Override
    public boolean init() throws JobExecutionException{
        try {
            addTask(new CreateBillingAccountTask())
                .chainWith(new CreatePartyRolesTask().setDocKey(getBaseMeta().getSession().getKeyFromUID(getRequest().partyId, Party.class)))
                .chainWith(new CreateBillingCycleTask());
        }
        catch(DaoException e){
            throw new JobExecutionException(this,this.getJobState(),"Cannot find Key from party id <"+getRequest().partyId+">",e);
        }
        return false;
    }

    /**
     * Created by Christophe Jeunesse on 27/07/2014.
     */
    public static class CreateBillingAccountTask extends DocumentCreateTask<BillingAccount> {
        @Override
        protected BillingAccount buildDocument() throws DaoException,StorageException {
            BillingAccount newBa = newEntity(BillingAccount.class);
            CreateBillingAccountJob job= getParentJob(CreateBillingAccountJob.class);
            newBa.setBillDay((job.getRequest().billDay!=null)?job.getRequest().billDay:1);
            newBa.setBillCycleLength((job.getRequest().cycleLength != null) ? job.getRequest().cycleLength : 1);

            Party party = getParentJob().getBaseMeta().getSession().getFromUID(getParentJob(CreateBillingAccountJob.class).getRequest().partyId, Party.class);
            job.getResult().partyLink = party.newLink();
            newBa.addPartyLink(job.getResult().partyLink);

            job.getResult().baLink = newBa.newLink();
            return newBa;
        }
    }

    /**
     * Created by Christophe Jeunesse on 27/07/2014.
     */
    public static class CreatePartyRolesTask extends DocumentUpdateTask<Party> {
        @Override
        protected void processDocument() throws DaoException,StorageException{
            BillingAccountPartyRole newPartyRole = new BillingAccountPartyRole();
            newPartyRole.setBa(getDependentTask(CreateBillingAccountTask.class).getDocument().newLink());
            newPartyRole.addRole(BillingAccountPartyRole.RoleType.HOLDER);
            newPartyRole.addRole(BillingAccountPartyRole.RoleType.PAYER);
            getDocument().addPartyRole(newPartyRole);
        }
    }


    /**
     * Created by Christophe Jeunesse on 27/07/2014.
     */
    public static class CreateBillingCycleTask extends SubJobProcessTask<CreateBillingCycleJob> {
        @Override
        protected CreateBillingCycleJob buildSubJob() throws DaoException,StorageException{
            CreateBillingCycleJob job = newEntity(CreateBillingCycleJob.class);
            BillingAccount ba = getDependentTask(CreateBillingAccountTask.class).getDocument();

            job.getRequest().baLink = ba.newLink();
            job.getRequest().startDate = ba.getCreationDate();
            return job;
        }
    }
}
