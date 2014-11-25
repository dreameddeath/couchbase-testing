package com.dreameddeath.party.process.service;

import com.dreameddeath.core.annotation.process.JobProcessingForClass;
import com.dreameddeath.core.annotation.process.TaskProcessingForClass;
import com.dreameddeath.core.exception.model.DuplicateTaskException;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.process.JobExecutionException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.process.business.service.DocumentCreateTaskProcessingService;
import com.dreameddeath.core.process.business.service.StandardJobProcessingService;
import com.dreameddeath.core.process.service.IJobProcessingService;
import com.dreameddeath.core.process.service.JobContext;
import com.dreameddeath.core.process.service.TaskContext;
import com.dreameddeath.party.model.base.Organization;
import com.dreameddeath.party.model.base.Party;
import com.dreameddeath.party.model.base.Person;
import com.dreameddeath.party.process.model.CreatePartyRequest;
import com.dreameddeath.party.process.model.CreatePartyJob;
import com.dreameddeath.party.process.model.CreatePartyJob.CreatePartyTask;

/**
 * Created by ceaj8230 on 23/11/2014.
 */
@JobProcessingForClass(CreatePartyJob.class)
public class CreatePartyJobProcessingService extends StandardJobProcessingService<CreatePartyJob> {
    @Override
    public boolean init(JobContext context, CreatePartyJob job) throws JobExecutionException {
        try {
            job.addTask(new CreatePartyJob.CreatePartyTask());
        }
        catch(DuplicateTaskException e){
            throw new JobExecutionException(job,job.getJobState(),e);
        }

        return false;
    }

    @TaskProcessingForClass(CreatePartyTask.class)
    public static class CreatePartyTaskProcessingService extends DocumentCreateTaskProcessingService<Party,CreatePartyTask>{
        @Override
        protected Party buildDocument(TaskContext ctxt,CreatePartyJob.CreatePartyTask task) throws DaoException, StorageException {
            Party result;
            CreatePartyRequest req = task.getParentJob(CreatePartyJob.class).getRequest();
            if(req.type == CreatePartyRequest.Type.person){
                Person person=ctxt.getSession().newEntity(Person.class);
                person.setFirstName(req.person.firstName);
                person.setLastName(req.person.lastName);
                result = person;
            }
            else{
                Organization organization = ctxt.getSession().newEntity(Organization.class);
                organization.setBrand(req.organization.brand);
                organization.setTradingName(req.organization.tradingName);
                result = organization;
            }

            return result;
        }
    }
}
