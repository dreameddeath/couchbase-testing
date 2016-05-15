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

package com.dreameddeath.billing.process.service;

import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.billing.process.model.CreateBillingAccountJob;
import com.dreameddeath.billing.process.model.CreateBillingAccountJob.CreateBillingAccountTask;
import com.dreameddeath.billing.process.model.CreateBillingAccountJob.CreateBillingCycleJobTask;
import com.dreameddeath.billing.process.model.CreateBillingAccountJob.CreatePartyRolesTask;
import com.dreameddeath.billing.process.model.CreateBillingCycleJob;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.impl.DocumentCreateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.DocumentUpdateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.StandardJobProcessingService;
import com.dreameddeath.core.process.service.impl.StandardSubJobProcessTaskProcessingService;
import com.dreameddeath.party.model.v1.Party;
import com.dreameddeath.party.model.v1.roles.BillingAccountPartyRole;

/**
 * Created by Christophe Jeunesse on 25/11/2014.
 */
@JobProcessingForClass(CreateBillingAccountJob.class)
public class CreateBillingAccountJobProcessingService extends StandardJobProcessingService<CreateBillingAccountJob> {
    @Override
    public boolean init(JobContext<CreateBillingAccountJob> context) throws JobExecutionException {
        try{
            context.addTask(new CreateBillingAccountJob.CreateBillingAccountTask())
                    .chainWith(new CreateBillingAccountJob.CreatePartyRolesTask().setDocKey(context.getSession().getKeyFromUID(context.getJob().partyId, Party.class)))
                    .chainWith(new CreateBillingCycleJobTask());
        }
        catch(DaoException e){
            throw new JobExecutionException(context,"Cannot find Key from party id <"+context.getJob().partyId+">",e);
        }
        return false;
    }



    @TaskProcessingForClass(CreateBillingAccountTask.class)
    public static class CreateBillingAccountTaskProcessingService extends DocumentCreateTaskProcessingService<CreateBillingAccountJob,BillingAccount,CreateBillingAccountTask> {

        @Override
        protected BillingAccount buildDocument(TaskContext<CreateBillingAccountJob,CreateBillingAccountTask> ctxt) throws DaoException, StorageException {
            BillingAccount newBa = ctxt.getSession().newEntity(BillingAccount.class);
            CreateBillingAccountJob job= ctxt.getParentJob();
            newBa.setBillDay((job.billDay!=null)?job.billDay:1);
            newBa.setBillCycleLength((job.cycleLength != null) ? job.cycleLength : 1);

            Party party = ctxt.getSession().getFromUID(job.partyId, Party.class);
            job.partyLink = party.newLink();
            newBa.addPartyLink(job.partyLink);
            job.baLink = newBa.newLink();
            return newBa;
        }
    }

    @TaskProcessingForClass(CreatePartyRolesTask.class)
    public static class CreatePartyRolesTaskProcessingService extends DocumentUpdateTaskProcessingService<CreateBillingAccountJob,Party,CreatePartyRolesTask>{
        @Override
        protected void processDocument(TaskContext<CreateBillingAccountJob,CreatePartyRolesTask> ctxt,Party party) throws DaoException, StorageException {
            BillingAccountPartyRole newPartyRole = new BillingAccountPartyRole();
            newPartyRole.setBaUid(ctxt.getDependentTask(CreateBillingAccountTask.class).getDocument(ctxt.getSession()).getUid());
            newPartyRole.addRole(BillingAccountPartyRole.RoleType.HOLDER);
            newPartyRole.addRole(BillingAccountPartyRole.RoleType.PAYER);
            party.addPartyRole(newPartyRole);
        }
    }

    @TaskProcessingForClass(CreateBillingCycleJobTask.class)
    public static class CreateBillingCycleTaskProcessingService extends StandardSubJobProcessTaskProcessingService<CreateBillingAccountJob,CreateBillingCycleJob,CreateBillingCycleJobTask> {
        @Override
        protected CreateBillingCycleJob buildSubJob(TaskContext<CreateBillingAccountJob,CreateBillingCycleJobTask> ctxt) throws DaoException, StorageException {
            CreateBillingCycleJob job = ctxt.getSession().newEntity(CreateBillingCycleJob.class);
            BillingAccount ba = ctxt.getDependentTask(CreateBillingAccountTask.class).getDocument(ctxt.getSession());

            job.baLink = ba.newLink();
            job.startDate = ba.getCreationDate();
            return job;
        }
    }
}
