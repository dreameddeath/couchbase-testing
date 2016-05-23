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

package com.dreameddeath.party.process.service;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.impl.processor.DocumentCreateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.processor.StandardJobProcessingService;
import com.dreameddeath.party.model.v1.Party;
import com.dreameddeath.party.process.model.v1.CreateUpdatePartyJob;
import com.dreameddeath.party.process.model.v1.CreateUpdatePartyJob.CreatePartyTask;
import com.dreameddeath.party.process.model.v1.roles.tasks.PartyUpdateResult;
import com.dreameddeath.party.service.IPartyManagementService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Christophe Jeunesse on 23/11/2014.
 */
@JobProcessingForClass(CreateUpdatePartyJob.class)
public class CreateUpdatePartyJobProcessingService extends StandardJobProcessingService<CreateUpdatePartyJob> {
    @Override
    public boolean init(JobContext<CreateUpdatePartyJob> context) throws JobExecutionException {
        context.addTask(new CreateUpdatePartyJob.CreatePartyTask());
        return false;
    }

    @TaskProcessingForClass(CreatePartyTask.class)
    public static class CreatePartyTaskProcessingService extends DocumentCreateTaskProcessingService<CreateUpdatePartyJob,Party,CreatePartyTask>{
        IPartyManagementService partyManagementService;

        @Autowired
        public void setPartyManagementService(IPartyManagementService partyManagementService) {
            this.partyManagementService = partyManagementService;
        }


        @Override
        protected Party buildDocument(TaskContext<CreateUpdatePartyJob,CreatePartyTask> ctxt) throws DaoException, StorageException {
            Party result=partyManagementService.managePartyCreation(ctxt.getSession(),ctxt.getParentJob().getRequest());
            return result;
        }

        @Override
        public boolean updatejob(TaskContext<CreateUpdatePartyJob, CreatePartyTask> context) throws TaskExecutionException {
            PartyUpdateResult result = new PartyUpdateResult();
            try {
                result.setUid(context.getTask().getDocument(context.getSession()).getUid());
            }
            catch (DaoException|StorageException e){
                throw new TaskExecutionException(context,"Cannot retrieve created party uid",e);
            }
            context.getParentJob().setResponse(result);
            return true;
        }
    }
}
