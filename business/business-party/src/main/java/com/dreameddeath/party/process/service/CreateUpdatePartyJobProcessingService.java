/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.party.process.service;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.JobProcessingResult;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.context.UpdateJobTaskProcessingResult;
import com.dreameddeath.core.process.service.impl.processor.DocumentCreateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.processor.StandardJobProcessingService;
import com.dreameddeath.party.model.v1.Party;
import com.dreameddeath.party.process.model.v1.CreateUpdatePartyJob;
import com.dreameddeath.party.process.model.v1.CreateUpdatePartyJob.CreatePartyTask;
import com.dreameddeath.party.process.model.v1.roles.tasks.PartyUpdateResult;
import com.dreameddeath.party.service.IPartyManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 23/11/2014.
 */
@JobProcessingForClass(CreateUpdatePartyJob.class)
public class CreateUpdatePartyJobProcessingService extends StandardJobProcessingService<CreateUpdatePartyJob> {
    @Override
    public Observable<JobProcessingResult<CreateUpdatePartyJob>> init(JobContext<CreateUpdatePartyJob> context) {
        context.addTask(new CreateUpdatePartyJob.CreatePartyTask());
        return JobProcessingResult.build(context,false);
    }

    @TaskProcessingForClass(CreatePartyTask.class)
    public static class CreatePartyTaskProcessingService extends DocumentCreateTaskProcessingService<CreateUpdatePartyJob,Party,CreatePartyTask>{
        IPartyManagementService partyManagementService;

        @Autowired
        public void setPartyManagementService(IPartyManagementService partyManagementService) {
            this.partyManagementService = partyManagementService;
        }


        @Override
        protected Observable<ContextAndDocument> buildDocument(TaskContext<CreateUpdatePartyJob,CreatePartyTask> ctxt){
            Party result=partyManagementService.managePartyCreation(ctxt.getSession(),ctxt.getParentInternalJob().getRequest());
            return buildContextAndDocumentObservable(ctxt,result);
        }

        @Override
        public Observable<UpdateJobTaskProcessingResult<CreateUpdatePartyJob, CreatePartyTask>> updatejob(CreateUpdatePartyJob job, CreatePartyTask task, ICouchbaseSession session) {
            return task.getDocument(session)
                    .map(doc->{
                        PartyUpdateResult result=new PartyUpdateResult();
                        result.setUid(doc.getUid());
                        job.setResponse(result);
                        return new UpdateJobTaskProcessingResult<>(job,task,true);
                    });
        }
    }
}
