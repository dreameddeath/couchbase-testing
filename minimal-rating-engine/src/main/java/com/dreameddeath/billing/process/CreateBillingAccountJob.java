package com.dreameddeath.billing.process;

import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.billing.model.account.BillingAccountLink;
import com.dreameddeath.billing.model.account.BillingAccountPartyRole;
import com.dreameddeath.billing.model.process.CreateBillingAccountRequest;
import com.dreameddeath.billing.model.process.CreateBillingAccountResult;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.event.TaskProcessEvent;
import com.dreameddeath.core.model.process.AbstractJob;
import com.dreameddeath.core.model.process.DocumentCreateTask;
import com.dreameddeath.core.model.process.DocumentUpdateTask;
import com.dreameddeath.core.model.process.SubJobProcessTask;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.StandardProperty;
import com.dreameddeath.party.model.*;

/**
 * Created by Christophe Jeunesse on 29/05/2014.
 */
public class CreateBillingAccountJob extends AbstractJob<CreateBillingAccountRequest,CreateBillingAccountResult> {

    @Override
    public boolean init(){
        addTask(new CreateBillingAccountTask()); return false;
    }

    @Override
    public boolean when(TaskProcessEvent evt){
        if(evt.getTask() instanceof CreateBillingAccountTask){
            addTask((new CreatePartyRolesTask()).setDocId(partyLink.getKey()));
            return false;
        }
        if(evt.getTask() instanceof CreatePartyRolesTask){
            addTask(new CreateBillingCycleTask());//.setDocId(partyLink.getKey()));
            return false;
        }

        return false; //can be retried without saving
    }


    /**
     * Created by Christophe Jeunesse on 27/07/2014.
     */
    public static class CreateBillingAccountTask extends DocumentCreateTask<BillingAccount> {
        @Override
        protected BillingAccount buildDocument() {
            BillingAccount newBa = newEntity(BillingAccount.class);
            CreateBillingAccountJob job= getParentJob(CreateBillingAccountJob.class);
            newBa.setBillDay((job.request.billDay!=null)?job.request.billDay:1);
            newBa.setBillingCycleLength((job.request.cycleLength!=null)?job.request.cycleLength:1);

            Party party = getParentJob().getSession().getFromUID(getParentJob(CreateBillingAccountJob.class).request.partyId, Party.class);
            getParentJob(CreateBillingAccountJob.class).partyLink = party.newLink();
            newBa.addPartyLink(getParentJob(CreateBillingAccountJob.class).partyLink);

            getParentJob(CreateBillingAccountJob.class).baLink = newBa.newLink();
            return newBa;
        }
    }

    /**
     * Created by Christophe Jeunesse on 27/07/2014.
     */
    public static class CreatePartyRolesTask extends DocumentUpdateTask<Party> {
        @Override
        protected void processDocument() {
            BillingAccountPartyRole newPartyRole = new BillingAccountPartyRole();
            newPartyRole.setBa(getParentJob(CreateBillingAccountJob.class).baLink.getLinkedObject().newLink());
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
        protected CreateBillingCycleJob buildSubJob() {
            CreateBillingCycleJob job = newEntity(CreateBillingCycleJob.class);
            BillingAccount ba = getParentJob(CreateBillingAccountJob.class).baLink.getLinkedObject();

            job.baLink = ba.newLink();
            job.startDate = ba.getCreationDate();
            return job;
        }
    }
}
