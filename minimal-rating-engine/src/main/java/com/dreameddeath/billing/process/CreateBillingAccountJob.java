package com.dreameddeath.billing.process;

import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.billing.model.account.BillingAccountPartyRole;
import com.dreameddeath.billing.model.process.CreateBillingAccountRequest;
import com.dreameddeath.billing.model.process.CreateBillingAccountResult;
import com.dreameddeath.core.event.TaskProcessEvent;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.process.common.AbstractJob;
import com.dreameddeath.core.process.document.DocumentCreateTask;
import com.dreameddeath.core.process.document.DocumentUpdateTask;
import com.dreameddeath.core.process.common.SubJobProcessTask;
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
    public boolean init(){
        addTask(new CreateBillingAccountTask());
        return false;
    }

    @Override
    public boolean when(TaskProcessEvent evt){
        if(evt.getTask() instanceof CreateBillingAccountTask){
            addTask((new CreatePartyRolesTask()).setDocKey(getResult().partyLink.getKey()));
            return false;
        }
        if(evt.getTask() instanceof CreatePartyRolesTask){
            addTask(new CreateBillingCycleTask());//.setDocId(partyLink.getDocumentKey()));
            return false;
        }

        return false; //can be retried without saving
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

            Party party = getParentJob().getSession().getFromUID(getParentJob(CreateBillingAccountJob.class).getRequest().partyId, Party.class);
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
            newPartyRole.setBa(getParentJob(CreateBillingAccountJob.class).getResult().baLink.getLinkedObject().newLink());
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
            BillingAccount ba = getParentJob(CreateBillingAccountJob.class).getResult().baLink.getLinkedObject();

            job.getRequest().baLink = ba.newLink();
            job.getRequest().startDate = ba.getCreationDate();
            return job;
        }
    }
}
