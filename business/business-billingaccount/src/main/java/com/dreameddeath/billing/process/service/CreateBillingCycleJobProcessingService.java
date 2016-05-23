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
import com.dreameddeath.billing.model.v1.cycle.BillingCycle;
import com.dreameddeath.billing.model.v1.cycle.BillingCycleLink;
import com.dreameddeath.billing.process.model.v1.CreateBillingCycleJob;
import com.dreameddeath.billing.process.model.v1.CreateBillingCycleJob.CreateBillingCycleTask;
import com.dreameddeath.billing.util.BillCycleUtils;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.impl.processor.ChildDocumentCreateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.processor.StandardJobProcessingService;

/**
 * Created by Christophe Jeunesse on 25/11/2014.
 */
@JobProcessingForClass(CreateBillingCycleJob.class)
public class CreateBillingCycleJobProcessingService extends StandardJobProcessingService<CreateBillingCycleJob> {
    @Override
    public boolean init(JobContext<CreateBillingCycleJob> context) throws JobExecutionException {
        context.addTask(new CreateBillingCycleTask(context.getJob().baLink.getKey()));
                //.chainWith(new CreateBillingCycleLinkTask().setDocKey(context.getJob().baLink.getKey()));

        return false;
    }

    @TaskProcessingForClass(CreateBillingCycleTask.class)
    public static class CreateBillingCycleTaskProcessingService extends ChildDocumentCreateTaskProcessingService<CreateBillingCycleJob,BillingCycle,BillingAccount,CreateBillingCycleTask> {
        @Override
        protected BillingCycle buildDocument(TaskContext<CreateBillingCycleJob,CreateBillingCycleTask> ctxt) throws DaoException, StorageException {
            BillingAccount ba = ctxt.getParentJob().baLink.getLinkedObject(ctxt.getSession());
            CreateBillingCycleJob job = ctxt.getParentJob();
            BillingCycle billCycle = ctxt.getSession().newEntity(BillingCycle.class);
            billCycle.setStartDate(job.startDate);
            billCycle.setEndDate(BillCycleUtils.CalcCycleEndDate(job.startDate, ba.getBillDay(), ba.getBillCycleLength()));
            billCycle.setBillingAccountLink(ba.newLink());
            return billCycle;
        }

        @Override
        protected boolean needParentUpdate(BillingAccount parent, BillingCycle child) {
            for(BillingCycleLink link:parent.getBillingCycleLinks()){
                if(link.isLinkTo(child)){
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void updateParent(BillingAccount parent, BillingCycle child) {
            parent.addBillingCycleLink(child.newLink());
        }
    }
}
