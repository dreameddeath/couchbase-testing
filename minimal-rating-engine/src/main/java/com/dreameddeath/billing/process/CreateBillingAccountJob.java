package com.dreameddeath.billing.process;

import com.dreameddeath.billing.model.BillingAccount;
import com.dreameddeath.billing.model.BillingAccountLink;
import com.dreameddeath.billing.model.BillingAccountPartyRole;
import com.dreameddeath.common.annotation.DocumentProperty;
import com.dreameddeath.common.event.TaskProcessEvent;
import com.dreameddeath.common.model.process.AbstractJob;
import com.dreameddeath.common.model.process.DocumentCreateTask;
import com.dreameddeath.common.model.process.DocumentUpdateTask;
import com.dreameddeath.common.model.process.SubJobProcessTask;
import com.dreameddeath.party.model.*;
import org.joda.time.DateTime;

import java.util.ArrayList;

/**
 * Created by ceaj8230 on 29/05/2014.
 */
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
     * Created by ceaj8230 on 27/07/2014.
     */
    public static class CreateBillingAccountTask extends DocumentCreateTask<BillingAccount> {
        @Override
        protected BillingAccount buildDocument() {
            BillingAccount newBa = newEntity(BillingAccount.class);
            CreateBillingAccountJob job= getParentJob(CreateBillingAccountJob.class);
            newBa.setBillDay((job.billDay!=null)?job.billDay:1);
            newBa.setBillingCycleLength((job.cycleLength!=null)?job.cycleLength:1);

            Party party = getParentJob().getSession().getFromUID(getParentJob(CreateBillingAccountJob.class).partyId, Party.class);
            getParentJob(CreateBillingAccountJob.class).partyLink = party.newPartyLink();
            newBa.addPartyLink(getParentJob(CreateBillingAccountJob.class).partyLink);

            getParentJob(CreateBillingAccountJob.class).baLink = newBa.newBillingAccountLink();
            return newBa;
        }
    }

    /**
     * Created by ceaj8230 on 27/07/2014.
     */
    public static class CreatePartyRolesTask extends DocumentUpdateTask<Party> {
        @Override
        protected void processDocument() {
            BillingAccountPartyRole newPartyRole = new BillingAccountPartyRole();
            newPartyRole.setBa(getParentJob(CreateBillingAccountJob.class).baLink.getLinkedObject().newBillingAccountLink());
            newPartyRole.addRole(BillingAccountPartyRole.RoleType.HOLDER);
            newPartyRole.addRole(BillingAccountPartyRole.RoleType.PAYER);
            getDocument().addPartyRole(newPartyRole);
        }
    }


    /**
     * Created by ceaj8230 on 27/07/2014.
     */
    public static class CreateBillingCycleTask extends SubJobProcessTask<CreateBillingCycleJob> {
        @Override
        protected CreateBillingCycleJob buildSubJob() {
            CreateBillingCycleJob job = newEntity(CreateBillingCycleJob.class);
            BillingAccount ba = getParentJob(CreateBillingAccountJob.class).baLink.getLinkedObject();

            job.baLink = ba.newBillingAccountLink();
            job.startDate = ba.getCreationDate();
            return job;
        }
    }
}
