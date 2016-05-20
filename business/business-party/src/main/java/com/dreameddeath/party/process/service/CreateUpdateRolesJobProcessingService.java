package com.dreameddeath.party.process.service;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.impl.DocumentUpdateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.StandardJobProcessingService;
import com.dreameddeath.party.model.v1.Party;
import com.dreameddeath.party.process.model.v1.roles.CreateUpdatePartyRolesJob;
import com.dreameddeath.party.process.model.v1.roles.CreateUpdateRoleRequest;
import com.dreameddeath.party.process.model.v1.roles.tasks.CreateUpdatePartyRolesTask;
import com.dreameddeath.party.process.model.v1.roles.tasks.PartyRolesUpdateResult;
import com.dreameddeath.party.service.IPartyManagementService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Christophe Jeunesse on 10/05/2016.
 */
@JobProcessingForClass(CreateUpdatePartyRolesJob.class)
public class CreateUpdateRolesJobProcessingService extends StandardJobProcessingService<CreateUpdatePartyRolesJob>{
    @Override
    public boolean init(JobContext<CreateUpdatePartyRolesJob> context) throws JobExecutionException {
        Set<String> impactedPartys=new TreeSet<>();
        for(CreateUpdateRoleRequest request:context.getJob().getRoleRequests()){
            if(!impactedPartys.contains(request.getPartyId())){
                CreateUpdatePartyRolesTask newTask = new CreateUpdatePartyRolesTask();
                newTask.setPartyId(request.getPartyId());
                try {
                    newTask.setDocKey(context.getSession().getKeyFromUID(request.getPartyId(), Party.class));
                }
                catch(DaoException e){
                    throw new JobExecutionException(context,"Cannot find key for uid "+request.getPartyId()+" for class "+Party.class.getName());
                }
                context.addTask(newTask);
                impactedPartys.add(newTask.getPartyId());
            }
        }
        return false;
    }

    @TaskProcessingForClass(CreateUpdatePartyRolesTask.class)
    public static class CreateUpdateRolesTaskProcessingService extends DocumentUpdateTaskProcessingService<CreateUpdatePartyRolesJob,Party,CreateUpdatePartyRolesTask> {
        private IPartyManagementService service;

        @Autowired
        public void setService(IPartyManagementService service) {
            this.service=service;
        }

        @Override
        protected void processDocument(TaskContext<CreateUpdatePartyRolesJob, CreateUpdatePartyRolesTask> ctxt, Party doc) throws DaoException, StorageException, TaskExecutionException {
            PartyRolesUpdateResult result = service.managePartyRolesUpdate(ctxt.getParentJob().getRoleRequests(),doc);
            //TODO manage task update in //
        }
    }
}
