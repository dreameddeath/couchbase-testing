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
import com.dreameddeath.core.process.service.JobContext;
import com.dreameddeath.core.process.service.TaskContext;
import com.dreameddeath.core.process.service.impl.DocumentCreateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.StandardJobProcessingService;
import com.dreameddeath.party.model.base.Organization;
import com.dreameddeath.party.model.base.Party;
import com.dreameddeath.party.model.base.Person;
import com.dreameddeath.party.process.model.CreatePartyJob;
import com.dreameddeath.party.process.model.CreatePartyJob.CreatePartyTask;

/**
 * Created by Christophe Jeunesse on 23/11/2014.
 */
@JobProcessingForClass(CreatePartyJob.class)
public class CreatePartyJobProcessingService extends StandardJobProcessingService<CreatePartyJob> {
    @Override
    public boolean init(JobContext<CreatePartyJob> context) throws JobExecutionException {
        context.addTask(new CreatePartyJob.CreatePartyTask());
        return false;
    }

    @TaskProcessingForClass(CreatePartyTask.class)
    public static class CreatePartyTaskProcessingService extends DocumentCreateTaskProcessingService<CreatePartyJob,Party,CreatePartyTask>{
        @Override
        protected Party buildDocument(TaskContext<CreatePartyJob,CreatePartyTask> ctxt) throws DaoException, StorageException {
            Party result;
            CreatePartyJob req = ctxt.getParentJob();
            if(req.type == CreatePartyJob.Type.person){
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
