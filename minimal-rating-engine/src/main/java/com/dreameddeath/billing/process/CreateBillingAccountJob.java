package com.dreameddeath.billing.process;

import com.dreameddeath.billing.model.BillingAccount;
import com.dreameddeath.billing.model.BillingAccountLink;
import com.dreameddeath.billing.model.BillingAccountPartyRole;
import com.dreameddeath.common.annotation.DocumentProperty;
import com.dreameddeath.common.event.TaskProcessEvent;
import com.dreameddeath.common.model.process.AbstractJob;
import com.dreameddeath.common.model.process.DocumentCreateTask;
import com.dreameddeath.common.model.process.DocumentUpdateTask;
import com.dreameddeath.party.model.*;

/**
 * Created by ceaj8230 on 29/05/2014.
 */
public class CreateBillingAccountJob extends AbstractJob {
    @DocumentProperty("partyId")
    public String partyId;
    @DocumentProperty("billDay")
    public Integer billDay;
    @DocumentProperty("baCreated")
    public BillingAccountLink baLink;
    @DocumentProperty("partyLink")
    public PartyLink partyLink;


    @Override
    public void init(){
        addTask(new CreateBillingAccountTask());
    }

    @Override
    public void when(TaskProcessEvent evt){
        if(evt.getTask() instanceof CreateBillingAccountTask){
            addTask((new CreatePartyRolesTask()).setDocId(partyLink.getKey()));
        }
    }


    /**
     * Created by ceaj8230 on 27/07/2014.
     */
    public static class CreateBillingAccountTask extends DocumentCreateTask<BillingAccount> {
        @Override
        public BillingAccount buildDocument() {
            BillingAccount newBa = new BillingAccount();
            newBa.setBillDay(getParentJob(CreateBillingAccountJob.class).billDay);

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
        public void processDocument() {
            BillingAccountPartyRole newPartyRole = new BillingAccountPartyRole();
            newPartyRole.setBa(getParentJob(CreateBillingAccountJob.class).baLink.getLinkedObject().newBillingAccountLink());
            getDocument().addPartyRole(newPartyRole);
        }
    }
}
