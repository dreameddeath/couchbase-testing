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

import com.dreameddeath.billing.model.v1.account.BillingAccount;
import com.dreameddeath.billing.model.v1.account.PartyRoleLink;
import com.dreameddeath.billing.process.model.v1.CreateBillingAccountJob;
import com.dreameddeath.billing.process.model.v1.CreateBillingAccountJob.CreateBillingAccountTask;
import com.dreameddeath.billing.process.model.v1.CreateBillingAccountJob.CreateBillingCycleJobTask;
import com.dreameddeath.billing.process.model.v1.CreateBillingAccountJob.CreatePartyRolesTask;
import com.dreameddeath.billing.process.model.v1.CreateBillingCycleJob;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.impl.processor.DocumentCreateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.processor.DocumentUpdateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.processor.StandardJobProcessingService;
import com.dreameddeath.core.process.service.impl.processor.StandardSubJobProcessTaskProcessingService;
import com.dreameddeath.couchbase.core.process.remote.RemoteJobTaskProcessing;
import com.dreameddeath.couchbase.core.process.remote.model.rest.RemoteJobResultWrapper;
import com.dreameddeath.party.process.model.v1.roles.published.BillingAccountCreateUpdateRoleRequestRequest;
import com.dreameddeath.party.process.model.v1.roles.published.CreateUpdatePartyRolesJobRequest;
import com.dreameddeath.party.process.model.v1.roles.published.CreateUpdatePartyRolesJobResponse;
import com.dreameddeath.party.process.model.v1.roles.published.RoleTypePublished;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.ArrayList;

/**
 * Created by Christophe Jeunesse on 25/11/2014.
 */
@JobProcessingForClass(CreateBillingAccountJob.class)
public class CreateBillingAccountJobProcessingService extends StandardJobProcessingService<CreateBillingAccountJob> {
    @Override
    public boolean init(JobContext<CreateBillingAccountJob> context) throws JobExecutionException {
        context.addTask(new CreateBillingAccountJob.CreateBillingAccountTask())
                .chainWith(new CreateBillingAccountJob.CreatePartyRolesTask())
                .chainWith(new CreateBillingAccountJob.UpdateBaPartyRolesTask())
                .chainWith(new CreateBillingCycleJobTask());

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
            return newBa;
        }

        @Override
        public boolean updatejob(TaskContext<CreateBillingAccountJob, CreateBillingAccountTask> context) throws TaskExecutionException {
            try {
                context.getParentJob().baLink = context.getTask().getDocument(context.getSession()).newLink();
            }
            catch(DaoException|StorageException e){
                throw new TaskExecutionException(context,"Cannot read back object "+context.getTask().getDocKey(),e);
            }
            return super.updatejob(context);
        }
    }

    @TaskProcessingForClass(CreatePartyRolesTask.class)
    public static class CreatePartyRolesTaskProcessingService extends RemoteJobTaskProcessing<CreateUpdatePartyRolesJobRequest,CreateUpdatePartyRolesJobResponse,CreateBillingAccountJob,CreatePartyRolesTask> {
        public static class Wrapper extends RemoteJobResultWrapper<CreateUpdatePartyRolesJobResponse>{
            @JsonCreator
            public Wrapper(CreateUpdatePartyRolesJobResponse result) {
                super(result);
            }
        }
        @Override
        protected Class<Wrapper> getResponseClass() {
            return Wrapper.class;
        }

        @Override
        protected CreateUpdatePartyRolesJobRequest getRequest(TaskContext<CreateBillingAccountJob, CreatePartyRolesTask> ctxt) throws TaskExecutionException{
            CreateUpdatePartyRolesJobRequest request = new CreateUpdatePartyRolesJobRequest();
            request.setRoleRequests(new ArrayList<>());
            try {
                BillingAccountCreateUpdateRoleRequestRequest newPartyRole = new BillingAccountCreateUpdateRoleRequestRequest();
                request.getRoleRequests().add(newPartyRole);
                newPartyRole.setPartyId(ctxt.getParentJob().partyId);
                newPartyRole.setBaId(ctxt.getDependentTask(CreateBillingAccountTask.class).getDocument(ctxt.getSession()).getUid());
                newPartyRole.setTypes(new ArrayList<>());
                newPartyRole.getTypes().add(RoleTypePublished.BILL_RECEIVER);
                newPartyRole.getTypes().add(RoleTypePublished.HOLDER);
                newPartyRole.getTypes().add(RoleTypePublished.PAYER);
            }
            catch(StorageException|DaoException e){
                throw new TaskExecutionException(ctxt,"Cannot access data",e);
            }
            return request;
        }

        @Override
        protected void updateTaskWithResponse(CreatePartyRolesTask task, CreateUpdatePartyRolesJobResponse resp) {
            if(resp.getResults().size()>0) {
                task.roleUid = resp.getResults().get(0).getRoleUid();
            }
        }
    }


    @TaskProcessingForClass(CreateBillingAccountJob.UpdateBaPartyRolesTask.class)
    public static class CreateUpdateBaPartyRolesService extends DocumentUpdateTaskProcessingService<CreateBillingAccountJob,BillingAccount,CreateBillingAccountJob.UpdateBaPartyRolesTask>{
        @Override
        public boolean preprocess(TaskContext<CreateBillingAccountJob, CreateBillingAccountJob.UpdateBaPartyRolesTask> context) throws TaskExecutionException {
            context.getTask().setDocKey(context.getParentJob().baLink.getKey());
            return true;
        }

        @Override
        protected boolean processDocument(TaskContext<CreateBillingAccountJob, CreateBillingAccountJob.UpdateBaPartyRolesTask> ctxt, BillingAccount doc) throws DaoException, StorageException, TaskExecutionException {
            PartyRoleLink roleLink = new PartyRoleLink();
            roleLink.setPid(ctxt.getParentJob().partyId);
            roleLink.setRoleUid(ctxt.getDependentTask(CreatePartyRolesTask.class).roleUid);
            ctxt.getTask().getDocument(ctxt.getSession()).addPartyRoles(roleLink);
            return false;
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
