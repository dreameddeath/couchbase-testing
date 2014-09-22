package com.dreameddeath.billing.process;

import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.billing.model.cycle.BillingCycle;
import com.dreameddeath.billing.model.process.CreateBillingCycleRequest;
import com.dreameddeath.billing.util.BillCycleUtils;
import com.dreameddeath.core.event.TaskProcessEvent;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.process.common.AbstractJob;
import com.dreameddeath.core.process.document.DocumentCreateTask;
import com.dreameddeath.core.process.document.DocumentUpdateTask;
import com.dreameddeath.core.model.process.EmptyJobResult;

/**
 * Created by Christophe Jeunesse on 03/08/2014.
 */
public class CreateBillingCycleJob extends AbstractJob<CreateBillingCycleRequest,EmptyJobResult>{
    @Override
    public CreateBillingCycleRequest newRequest(){return new CreateBillingCycleRequest();}
    @Override
    public EmptyJobResult newResult(){return new EmptyJobResult();}

    @Override
    public boolean init(){
        addTask(new CreateBillingCycleTask()); return false;
    }

    @Override
    public boolean when(TaskProcessEvent evt){
        if(evt.getTask() instanceof CreateBillingCycleTask){
            addTask((new CreateBillingCycleLinkTask()).setDocKey(getRequest().baLink.getKey()));
            return false;
        }
        return false; //can be retried without saving
    }

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
