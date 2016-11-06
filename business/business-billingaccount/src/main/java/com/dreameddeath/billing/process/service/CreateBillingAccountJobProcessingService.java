/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.billing.process.service;

import com.dreameddeath.billing.model.v1.account.BillingAccount;
import com.dreameddeath.billing.model.v1.account.PartyRoleLink;
import com.dreameddeath.billing.process.model.v1.CreateBillingAccountJob;
import com.dreameddeath.billing.process.model.v1.CreateBillingAccountJob.CreateBillingAccountTask;
import com.dreameddeath.billing.process.model.v1.CreateBillingAccountJob.CreateBillingCycleJobTask;
import com.dreameddeath.billing.process.model.v1.CreateBillingAccountJob.CreatePartyRolesTask;
import com.dreameddeath.billing.process.model.v1.CreateBillingCycleJob;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.exception.TaskObservableExecutionException;
import com.dreameddeath.core.process.service.context.*;
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
import com.google.common.base.Preconditions;
import rx.Observable;

import java.util.ArrayList;

/**
 * Created by Christophe Jeunesse on 25/11/2014.
 */
@JobProcessingForClass(CreateBillingAccountJob.class)
public class CreateBillingAccountJobProcessingService extends StandardJobProcessingService<CreateBillingAccountJob> {
    @Override
    public Observable<JobProcessingResult<CreateBillingAccountJob>> init(JobContext<CreateBillingAccountJob> context){
        context.addTask(new CreateBillingAccountJob.CreateBillingAccountTask())
                .chainWith(new CreateBillingAccountJob.CreatePartyRolesTask())
                .chainWith(new CreateBillingAccountJob.UpdateBaPartyRolesTask())
                .chainWith(new CreateBillingCycleJobTask());

        return JobProcessingResult.build(context,false);
    }



    @TaskProcessingForClass(CreateBillingAccountTask.class)
    public static class CreateBillingAccountTaskProcessingService extends DocumentCreateTaskProcessingService<CreateBillingAccountJob,BillingAccount,CreateBillingAccountTask> {

        @Override
        protected Observable<ContextAndDocument> buildDocument(TaskContext<CreateBillingAccountJob,CreateBillingAccountTask> ctxt){
            BillingAccount newBa = ctxt.getSession().newEntity(BillingAccount.class);
            CreateBillingAccountJob job= ctxt.getParentInternalJob();
            newBa.setBillDay((job.billDay!=null)?job.billDay:1);
            newBa.setBillCycleLength((job.cycleLength != null) ? job.cycleLength : 1);
            return buildContextAndDocumentObservable(ctxt,newBa);
        }

        @Override
        public Observable<UpdateJobTaskProcessingResult<CreateBillingAccountJob, CreateBillingAccountTask>> updatejob(CreateBillingAccountJob job, CreateBillingAccountTask task, ICouchbaseSession session) {
            return task.getDocument(session)
                    .map(billingAccount -> {
                        job.baLink = billingAccount.newLink();
                        return new UpdateJobTaskProcessingResult<>(job,task,true);
                    });
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
        protected Observable<CreateUpdatePartyRolesJobRequest> getRequest(TaskContext<CreateBillingAccountJob, CreatePartyRolesTask> ctxt){
            return ctxt.getDependentTask(CreateBillingAccountTask.class)
                    .switchIfEmpty(Observable.error(new TaskObservableExecutionException(ctxt,"Billing account Task not found")))
                    .flatMap(createBillingAccountTask -> createBillingAccountTask.getDocument(ctxt.getSession()))
                    .switchIfEmpty(Observable.error(new TaskObservableExecutionException(ctxt,"Billing account not found")))
                    .map(billingAccount -> {
                        CreateUpdatePartyRolesJobRequest request = new CreateUpdatePartyRolesJobRequest();
                        request.setRoleRequests(new ArrayList<>());
                        BillingAccountCreateUpdateRoleRequestRequest newPartyRole = new BillingAccountCreateUpdateRoleRequestRequest();
                        request.getRoleRequests().add(newPartyRole);
                        newPartyRole.setPartyId(ctxt.getParentInternalJob().partyId);
                        newPartyRole.setBaId(billingAccount.getUid());
                        newPartyRole.setTypes(new ArrayList<>());
                        newPartyRole.getTypes().add(RoleTypePublished.BILL_RECEIVER);
                        newPartyRole.getTypes().add(RoleTypePublished.HOLDER);
                        newPartyRole.getTypes().add(RoleTypePublished.PAYER);
                        return request;
                    });
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
        public Observable<TaskProcessingResult<CreateBillingAccountJob,CreateBillingAccountJob.UpdateBaPartyRolesTask>> preprocess(TaskContext<CreateBillingAccountJob, CreateBillingAccountJob.UpdateBaPartyRolesTask> context){
            Preconditions.checkNotNull(context.getParentInternalJob().baLink,"The ba Link should be set");
            context.getInternalTask().setDocKey(context.getParentInternalJob().baLink.getKey());
            return TaskProcessingResult.build(context,true);
        }

        @Override
        protected Observable<ProcessingDocumentResult> processDocument(ContextAndDocument ctxtAndDoc) {
            return ctxtAndDoc.getCtxt().getDependentTask(CreatePartyRolesTask.class)
                    .map(createPartyRolesTask -> {
                        PartyRoleLink roleLink = new PartyRoleLink();
                        roleLink.setPid(ctxtAndDoc.getCtxt().getParentInternalJob().partyId);
                        roleLink.setRoleUid(createPartyRolesTask.roleUid);
                        ctxtAndDoc.getDoc().addPartyRoles(roleLink);
                        return new ProcessingDocumentResult(ctxtAndDoc,false);
                    });
        }
    }

    @TaskProcessingForClass(CreateBillingCycleJobTask.class)
    public static class CreateBillingCycleTaskProcessingService extends StandardSubJobProcessTaskProcessingService<CreateBillingAccountJob,CreateBillingCycleJob,CreateBillingCycleJobTask> {

        @Override
        protected Observable<BuildSubJobResult> buildSubJob(TaskContext<CreateBillingAccountJob,CreateBillingCycleJobTask> ctxt){
            return ctxt.getDependentTask(CreateBillingAccountTask.class)
                    .flatMap(createBillingAccountTask -> createBillingAccountTask.getDocument(ctxt.getSession()))
                    .map(billingAccount -> {
                        CreateBillingCycleJob job = ctxt.getSession().newEntity(CreateBillingCycleJob.class);
                        job.baLink = billingAccount.newLink();
                        job.startDate = billingAccount.getCreationDate();
                        return new BuildSubJobResult(ctxt,job);
                    });
        }
    }
}
