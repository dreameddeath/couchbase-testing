/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.installedbase.process.service;

import com.dreameddeath.core.annotation.process.JobProcessingForClass;
import com.dreameddeath.core.annotation.process.TaskProcessingForClass;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.model.DuplicateTaskException;
import com.dreameddeath.core.exception.process.JobExecutionException;
import com.dreameddeath.core.exception.process.TaskExecutionException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.process.NoOpTask;
import com.dreameddeath.core.process.business.service.DocumentCreateTaskProcessingService;
import com.dreameddeath.core.process.business.service.DocumentUpdateTaskProcessingService;
import com.dreameddeath.core.process.business.service.StandardJobProcessingService;
import com.dreameddeath.core.process.service.JobContext;
import com.dreameddeath.core.process.service.TaskContext;
import com.dreameddeath.installedbase.model.common.InstalledBase;
import com.dreameddeath.installedbase.model.process.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.model.process.CreateUpdateInstalledBaseResponse;
import com.dreameddeath.installedbase.process.model.CreateUpdateInstalledBaseJob;
import com.dreameddeath.installedbase.process.model.CreateUpdateInstalledBaseJob.InitEmptyInstalledBase;
import com.dreameddeath.installedbase.process.model.CreateUpdateInstalledBaseJob.UpdateInstalledBase;

/**
 * Created by CEAJ8230 on 25/11/2014.
 */
@JobProcessingForClass(CreateUpdateInstalledBaseJob.class)
public class CreateUpdateInstalledBaseJobProcessingService extends StandardJobProcessingService<CreateUpdateInstalledBaseJob> {
    @Override
    public boolean init(JobContext context, CreateUpdateInstalledBaseJob job) throws JobExecutionException {
        try {
            for (CreateUpdateInstalledBaseRequest.Contract contract : job.getRequest().contracts) {
                UpdateInstalledBase updateTask = new UpdateInstalledBase();

                if (contract.comOp.equals(CreateUpdateInstalledBaseRequest.CommercialOperation.ADD)) {
                    InitEmptyInstalledBase emptyCreateTask = job.addTask(new InitEmptyInstalledBase());
                    emptyCreateTask.setContractTempId(contract.tempId);
                    emptyCreateTask.chainWith(new UpdateInstalledBase());
                } else {
                    updateTask.setContractUid(contract.id);
                }
            }
        }
        catch(DuplicateTaskException e){
            throw new JobExecutionException(job,job.getJobState(),"Duplicate Errors",e);
        }

        return true;
    }

    @TaskProcessingForClass(InitEmptyInstalledBase.class)
    public static class InitEmptyInstalledBaseProcessingService extends DocumentCreateTaskProcessingService<InstalledBase,InitEmptyInstalledBase> {

        @Override
        public InstalledBase buildDocument(TaskContext ctxt,InitEmptyInstalledBase task){
            InstalledBase result = ctxt.getSession().newEntity(InstalledBase.class);
            return result;
        }

        @Override
        public boolean postprocess(TaskContext ctxt,InitEmptyInstalledBase task)throws TaskExecutionException {
            CreateUpdateInstalledBaseResponse.Contract result = new CreateUpdateInstalledBaseResponse.Contract();
            result.tempId = task.getContractTempId();
            try {
                result.id = task.getDocument(ctxt.getSession()).getUid();
            }
            catch(DaoException e){
                throw new TaskExecutionException(task,task.getState(),"Dao error during retrieval of doc "+task.getDocKey(),e);
            }
            catch(StorageException e){
                throw new TaskExecutionException(task,task.getState(),"Storage error during retrieval of doc "+task.getDocKey(),e);
            }
            task.getJobResult(CreateUpdateInstalledBaseResponse.class).contracts.add(result);
            return true;
        }
    }

    public static class AfterPreCreateSyncTaskPr extends NoOpTask {}

    public static class UpdateInstalledBaseProcessingService extends DocumentUpdateTaskProcessingService<InstalledBase,UpdateInstalledBase> {
        @Override
        public boolean preprocess(TaskContext ctxt,UpdateInstalledBase task) throws TaskExecutionException{
            if(task.getContractUid()!=null){
                try {
                    task.setDocKey(ctxt.getSession().getKeyFromUID(task.getContractUid(), InstalledBase.class));
                }
                catch(DaoException e){
                    throw new TaskExecutionException(task,task.getState(),"Cannot build key from uid <"+task.getContractUid()+">");
                }
            }
            else {
                InitEmptyInstalledBase creationInstalledBase = task.getDependentTask(InitEmptyInstalledBase.class);
                if (creationInstalledBase != null) {
                    task.setDocKey(creationInstalledBase.getDocKey());
                }
                else{
                    throw new TaskExecutionException(task,task.getState(),"Inconsistent State : neither existing InstalledBase nor InitTask found");
                }
            }
            return false;
        }

        @Override
        public void processDocument(TaskContext ctxt,UpdateInstalledBase task){

        }
    }
}

