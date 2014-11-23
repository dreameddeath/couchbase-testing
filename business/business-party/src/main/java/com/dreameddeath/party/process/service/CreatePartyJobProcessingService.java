package com.dreameddeath.party.process.service;

import com.dreameddeath.core.exception.DuplicateTaskException;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.process.JobExecutionException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.process.document.service.DocumentCreateTaskProcessingService;
import com.dreameddeath.core.process.service.IJobProcessingService;
import com.dreameddeath.core.process.service.JobContext;
import com.dreameddeath.core.process.service.TaskContext;
import com.dreameddeath.party.model.base.Organization;
import com.dreameddeath.party.model.base.Party;
import com.dreameddeath.party.model.base.Person;
import com.dreameddeath.party.process.model.CreatePartyRequest;
import com.dreameddeath.party.process.model.CreatePartyJob;

/**
 * Created by ceaj8230 on 23/11/2014.
 */
public class CreatePartyJobProcessingService implements IJobProcessingService<CreatePartyJob> {
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

    @Override
    public boolean preprocess(JobContext context, CreatePartyJob job) throws JobExecutionException {
        return false;
    }

    @Override
    public boolean postprocess(JobContext context, CreatePartyJob job) throws JobExecutionException {
        return false;
    }

    @Override
    public boolean cleanup(JobContext context, CreatePartyJob job) throws JobExecutionException {
        return false;
    }

    public static class CreatePartyTaskProcessingService extends DocumentCreateTaskProcessingService<Party,CreatePartyJob.CreatePartyTask>{

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
