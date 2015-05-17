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

package com.dreameddeath.billing.process.service;

import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.billing.model.account.BillingAccountPartyRole;
import com.dreameddeath.billing.process.model.CreateBillingAccountJob;
import com.dreameddeath.billing.process.model.CreateBillingAccountJob.CreateBillingAccountTask;
import com.dreameddeath.billing.process.model.CreateBillingAccountJob.CreateBillingCycleJobTask;
import com.dreameddeath.billing.process.model.CreateBillingAccountJob.CreatePartyRolesTask;
import com.dreameddeath.billing.process.model.CreateBillingCycleJob;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.exception.DuplicateTaskException;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.service.JobContext;
import com.dreameddeath.core.process.service.TaskContext;
import com.dreameddeath.core.process.service.impl.DocumentCreateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.DocumentUpdateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.StandardJobProcessingService;
import com.dreameddeath.core.process.service.impl.StandardSubJobProcessTaskProcessingService;
import com.dreameddeath.party.model.base.Party;

/**
 * Created by CEAJ8230 on 25/11/2014.
 */
@JobProcessingForClass(CreateBillingAccountJob.class)
public class CreateBillingAccountJobProcessingService extends StandardJobProcessingService<CreateBillingAccountJob> {
    @Override
    public boolean init(JobContext context, CreateBillingAccountJob job) throws JobExecutionException {
        try {
            job.addTask(new CreateBillingAccountJob.CreateBillingAccountTask())
                    .chainWith(new CreateBillingAccountJob.CreatePartyRolesTask().setDocKey(context.getSession().getKeyFromUID(job.getRequest().partyId, Party.class)))
                    .chainWith(new CreateBillingCycleJobTask());
        }
        catch(DaoException|DuplicateTaskException e){
            throw new JobExecutionException(job,job.getJobState(),"Cannot find Key from party id <"+job.getRequest().partyId+">",e);
        }
        return false;
    }



    @TaskProcessingForClass(CreateBillingAccountTask.class)
    public static class CreateBillingAccountTaskProcessingService extends DocumentCreateTaskProcessingService<BillingAccount,CreateBillingAccountTask> {

        @Override
        protected BillingAccount buildDocument(TaskContext ctxt, CreateBillingAccountTask task) throws DaoException, StorageException {
            BillingAccount newBa = ctxt.getSession().newEntity(BillingAccount.class);
            CreateBillingAccountJob job= task.getParentJob(CreateBillingAccountJob.class);
            newBa.setBillDay((job.getRequest().billDay!=null)?job.getRequest().billDay:1);
            newBa.setBillCycleLength((job.getRequest().cycleLength != null) ? job.getRequest().cycleLength : 1);

            Party party = ctxt.getSession().getFromUID(task.getParentJob(CreateBillingAccountJob.class).getRequest().partyId, Party.class);
            job.getResult().partyLink = party.newLink();
            newBa.addPartyLink(job.getResult().partyLink);

            job.getResult().baLink = newBa.newLink();
            return newBa;
        }
    }

    @TaskProcessingForClass(CreatePartyRolesTask.class)
    public static class CreatePartyRolesTaskProcessingService extends DocumentUpdateTaskProcessingService<Party,CreatePartyRolesTask>{
        @Override
        protected void processDocument(TaskContext ctxt, CreatePartyRolesTask task) throws DaoException, StorageException {
            BillingAccountPartyRole newPartyRole = new BillingAccountPartyRole();
            newPartyRole.setBa(task.getDependentTask(CreateBillingAccountTask.class).getDocument(ctxt.getSession()).newLink());
            newPartyRole.addRole(BillingAccountPartyRole.RoleType.HOLDER);
            newPartyRole.addRole(BillingAccountPartyRole.RoleType.PAYER);
            task.getDocument(ctxt.getSession()).addPartyRole(newPartyRole);
        }
    }

    @TaskProcessingForClass(CreateBillingCycleJobTask.class)
    public static class CreateBillingCycleTaskProcessingService extends StandardSubJobProcessTaskProcessingService<CreateBillingCycleJob,CreateBillingCycleJobTask> {

        @Override
        protected CreateBillingCycleJob buildSubJob(TaskContext ctxt, CreateBillingCycleJobTask task) throws DaoException, StorageException {
            CreateBillingCycleJob job = ctxt.getSession().newEntity(CreateBillingCycleJob.class);
            BillingAccount ba = task.getDependentTask(CreateBillingAccountTask.class).getDocument(ctxt.getSession());

            job.getRequest().baLink = ba.newLink();
            job.getRequest().startDate = ba.getCreationDate();
            return job;
        }
    }
}
