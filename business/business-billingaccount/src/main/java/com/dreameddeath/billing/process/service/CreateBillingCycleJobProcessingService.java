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
import com.dreameddeath.billing.model.cycle.BillingCycle;
import com.dreameddeath.billing.process.model.CreateBillingCycleJob;
import com.dreameddeath.billing.process.model.CreateBillingCycleJob.CreateBillingCycleLinkTask;
import com.dreameddeath.billing.process.model.CreateBillingCycleJob.CreateBillingCycleTask;
import com.dreameddeath.billing.process.model.CreateBillingCycleRequest;
import com.dreameddeath.billing.util.BillCycleUtils;
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

/**
 * Created by CEAJ8230 on 25/11/2014.
 */
@JobProcessingForClass(CreateBillingCycleJob.class)
public class CreateBillingCycleJobProcessingService extends StandardJobProcessingService<CreateBillingCycleJob> {
    @Override
    public boolean init(JobContext context, CreateBillingCycleJob job) throws JobExecutionException {
        try {
            job.addTask(new CreateBillingCycleTask())
                    .chainWith(new CreateBillingCycleLinkTask().setDocKey(job.getRequest().baLink.getKey()));
        }
        catch(DuplicateTaskException e){
            throw new JobExecutionException(job,job.getJobState(),"Duplicate Errors",e);
        }
        return false;
    }

    @TaskProcessingForClass(CreateBillingCycleTask.class)
    public static class CreateBillingCycleTaskProcessingService extends DocumentCreateTaskProcessingService<BillingCycle,CreateBillingCycleTask> {
        @Override
        protected BillingCycle buildDocument(TaskContext ctxt, CreateBillingCycleTask task) throws DaoException, StorageException {
            BillingAccount ba = task.getJobRequest(CreateBillingCycleRequest.class).baLink.getLinkedObject(ctxt.getSession());
            CreateBillingCycleJob job = task.getParentJob(CreateBillingCycleJob.class);
            BillingCycle billCycle = ctxt.getSession().newEntity(BillingCycle.class);
            billCycle.setStartDate(job.getRequest().startDate);
            billCycle.setEndDate(BillCycleUtils.CalcCycleEndDate(job.getRequest().startDate, ba.getBillDay(), ba.getBillCycleLength()));
            billCycle.setBillingAccountLink(ba.newLink());
            return billCycle;
        }
    }

    @TaskProcessingForClass(CreateBillingCycleLinkTask.class)
    public static class CreateBillingCycleLinkTaskProcessingService extends DocumentUpdateTaskProcessingService<BillingAccount,CreateBillingCycleLinkTask> {
        @Override
        protected void processDocument(TaskContext ctxt, CreateBillingCycleLinkTask task) throws DaoException, StorageException {
            BillingAccount ba = task.getDocument(ctxt.getSession());
            ba.addBillingCycleLink(task.getDependentTask(CreateBillingCycleTask.class).getDocument(ctxt.getSession()).newLink());
        }
    }
}
