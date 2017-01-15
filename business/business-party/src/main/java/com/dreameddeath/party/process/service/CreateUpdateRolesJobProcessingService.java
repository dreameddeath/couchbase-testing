/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.party.process.service;

import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.JobProcessingResult;
import com.dreameddeath.core.process.service.context.UpdateJobTaskProcessingResult;
import com.dreameddeath.core.process.service.impl.processor.DocumentUpdateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.processor.StandardJobProcessingService;
import com.dreameddeath.party.model.v1.Party;
import com.dreameddeath.party.process.model.v1.roles.CreateUpdatePartyRolesJob;
import com.dreameddeath.party.process.model.v1.roles.CreateUpdateRoleRequest;
import com.dreameddeath.party.process.model.v1.roles.tasks.CreateUpdatePartyRolesTask;
import com.dreameddeath.party.process.model.v1.roles.tasks.PartyRolesUpdateResult;
import com.dreameddeath.party.service.IPartyManagementService;
import io.reactivex.Single;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Christophe Jeunesse on 10/05/2016.
 */
@JobProcessingForClass(CreateUpdatePartyRolesJob.class)
public class CreateUpdateRolesJobProcessingService extends StandardJobProcessingService<CreateUpdatePartyRolesJob>{
    @Override
    public Single<JobProcessingResult<CreateUpdatePartyRolesJob>> init(JobContext<CreateUpdatePartyRolesJob> context){
        Set<String> impactedPartys=new TreeSet<>();
        for(CreateUpdateRoleRequest request:context.getInternalJob().getRoleRequests()){
            if(!impactedPartys.contains(request.getPartyId())){
                CreateUpdatePartyRolesTask newTask = new CreateUpdatePartyRolesTask();
                newTask.setPartyId(request.getPartyId());
                try {
                    newTask.setDocKey(context.getSession().getKeyFromUID(request.getPartyId(), Party.class));
                }
                catch(DaoException e){
                    return Single.error(new JobExecutionException(context,"Cannot find key for uid "+request.getPartyId()+" for class "+Party.class.getName()));
                }
                context.addTask(newTask);
                impactedPartys.add(newTask.getPartyId());
            }
        }
        return JobProcessingResult.build(context,false);
    }

    @TaskProcessingForClass(CreateUpdatePartyRolesTask.class)
    public static class CreateUpdateRolesTaskProcessingService extends DocumentUpdateTaskProcessingService<CreateUpdatePartyRolesJob,Party,CreateUpdatePartyRolesTask> {
        private IPartyManagementService service;

        @Autowired
        public void setService(IPartyManagementService service) {
            this.service=service;
        }

        @Override
        protected Single<ContextAndDocument> cleanTaskBeforeRetryProcessing(ContextAndDocument ctxtAndDoc) {
            ctxtAndDoc.getCtxt().getInternalTask().getCreateUpdateRoles().clear();
            return Single.just(ctxtAndDoc);
        }

        @Override
        protected Single<ProcessingDocumentResult> processDocument(ContextAndDocument ctxtAndDoc) {
            PartyRolesUpdateResult result = service.managePartyRolesUpdate(ctxtAndDoc.getCtxt().getParentInternalJob().getRoleRequests(),ctxtAndDoc.getDoc());
            result.getRoles().forEach(ctxtAndDoc.getCtxt().getInternalTask()::addCreateUpdateRoles);
            return new ProcessingDocumentResult(ctxtAndDoc,true).toSingle();
        }


        @Override
        public Single<UpdateJobTaskProcessingResult<CreateUpdatePartyRolesJob, CreateUpdatePartyRolesTask>> updatejob(CreateUpdatePartyRolesJob job, CreateUpdatePartyRolesTask task, ICouchbaseSession session) {
            task.getCreateUpdateRoles().forEach(job::addResults);
            return new UpdateJobTaskProcessingResult<>(job,task,true).toSingle();
        }
    }
}
