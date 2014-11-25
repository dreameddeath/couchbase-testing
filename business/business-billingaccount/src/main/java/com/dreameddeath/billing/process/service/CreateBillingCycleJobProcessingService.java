package com.dreameddeath.billing.process.service;

import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.billing.model.cycle.BillingCycle;
import com.dreameddeath.billing.process.model.CreateBillingAccountJob;
import com.dreameddeath.billing.process.model.CreateBillingCycleJob;
import com.dreameddeath.billing.process.model.CreateBillingCycleJob.*;
import com.dreameddeath.billing.process.model.CreateBillingCycleRequest;
import com.dreameddeath.billing.util.BillCycleUtils;
import com.dreameddeath.core.annotation.process.JobProcessingForClass;
import com.dreameddeath.core.annotation.process.TaskProcessingForClass;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.model.DuplicateTaskException;
import com.dreameddeath.core.exception.process.JobExecutionException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.process.business.model.DocumentCreateTask;
import com.dreameddeath.core.process.business.model.DocumentUpdateTask;
import com.dreameddeath.core.process.business.service.DocumentCreateTaskProcessingService;
import com.dreameddeath.core.process.business.service.DocumentUpdateTaskProcessingService;
import com.dreameddeath.core.process.business.service.StandardJobProcessingService;
import com.dreameddeath.core.process.service.JobContext;
import com.dreameddeath.core.process.service.TaskContext;

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
