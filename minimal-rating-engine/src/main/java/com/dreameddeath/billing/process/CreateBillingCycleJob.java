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

package com.dreameddeath.billing.process;

import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.billing.model.cycle.BillingCycle;
import com.dreameddeath.billing.model.process.CreateBillingCycleRequest;
import com.dreameddeath.billing.util.BillCycleUtils;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.dao.DaoException;
import com.dreameddeath.core.exception.process.JobExecutionException;
import com.dreameddeath.core.model.process.EmptyJobResult;
import com.dreameddeath.core.process.business.DocumentCreateTask;
import com.dreameddeath.core.process.business.DocumentUpdateTask;
import com.dreameddeath.core.process.common.AbstractJob;

/**
 * Created by Christophe Jeunesse on 03/08/2014.
 */
public class CreateBillingCycleJob extends AbstractJob<CreateBillingCycleRequest,EmptyJobResult>{
    @Override
    public CreateBillingCycleRequest newRequest(){return new CreateBillingCycleRequest();}
    @Override
    public EmptyJobResult newResult(){return new EmptyJobResult();}

    @Override
    public boolean init() throws JobExecutionException{
        addTask(new CreateBillingCycleTask())
            .chainWith(new CreateBillingCycleLinkTask().setDocKey(getRequest().baLink.getKey()));
        return false;
    }

    /*@Override
    public boolean when(TaskProcessEvent evt){
        /*if(evt.getTask() instanceof CreateBillingCycleTask){
            .setDocKey(getRequest().baLink.getKey()));
            return false;
        }
        return false; //can be retried without saving
    }*/

    public static class CreateBillingCycleTask extends DocumentCreateTask<BillingCycle>{
        @Override
        public BillingCycle buildDocument() throws DaoException,StorageException{
            BillingAccount ba = getJobRequest(CreateBillingCycleRequest.class).baLink.getLinkedObject();
            CreateBillingCycleJob job = getParentJob(CreateBillingCycleJob.class);
            BillingCycle billCycle = newEntity(BillingCycle.class);
            billCycle.setStartDate(job.getRequest().startDate);
            billCycle.setEndDate(BillCycleUtils.CalcCycleEndDate(job.getRequest().startDate,ba.getBillDay(),ba.getBillCycleLength()));
            billCycle.setBillingAccountLink(ba.newLink());
            return billCycle;

        }
    }

    public static class CreateBillingCycleLinkTask extends DocumentUpdateTask<BillingAccount> {
        @Override
        public void processDocument() throws DaoException,StorageException{
            BillingAccount ba = getDocument();
            ba.addBillingCycleLink(getParentJob(CreateBillingCycleJob.class).getTask(0, CreateBillingCycleTask.class).getDocument().newLink());
        }
    }
}
