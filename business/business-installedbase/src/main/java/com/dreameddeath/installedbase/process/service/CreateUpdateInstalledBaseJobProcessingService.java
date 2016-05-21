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

package com.dreameddeath.installedbase.process.service;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.model.v1.tasks.NoOpTask;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.impl.DocumentCreateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.DocumentUpdateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.StandardJobProcessingService;
import com.dreameddeath.installedbase.model.v1.InstalledBase;
import com.dreameddeath.installedbase.process.model.v1.CreateUpdateInstalledBaseJob;
import com.dreameddeath.installedbase.process.model.v1.CreateUpdateInstalledBaseJob.InitEmptyInstalledBase;
import com.dreameddeath.installedbase.process.model.v1.CreateUpdateInstalledBaseJob.UpdateInstalledBaseTask;
import com.dreameddeath.installedbase.process.model.v1.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.process.model.v1.CreateUpdateInstalledBaseResponse;
import com.dreameddeath.installedbase.service.ICreateUpdateInstalledBaseService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Christophe Jeunesse on 25/11/2014.
 */
@JobProcessingForClass(CreateUpdateInstalledBaseJob.class)
public class CreateUpdateInstalledBaseJobProcessingService extends StandardJobProcessingService<CreateUpdateInstalledBaseJob> {
    @Override
    public boolean init(JobContext<CreateUpdateInstalledBaseJob> context) throws JobExecutionException {
        CreateUpdateInstalledBaseJob job = context.getJob();
        //Algorithm :
        // * create all contracts without contents :
        //     * create structure
        //     * attach to party
        //     * attach to billing account
        // * Loop on all contracts :
        //     * Perform all modifications
        //     * Provide ids to all new objects
        //     * init links to all objects
        //     * attach to party
        //     * attach to billing account
        // * Loop on all contracts with inter contracts links toward new items :
        //     * create them


        for (CreateUpdateInstalledBaseRequest.Contract contract : job.getRequest().contracts) {

            //Create target contracts if needed
            if (
                    contract.comOp.equals(CreateUpdateInstalledBaseRequest.CommercialOperation.ADD)
                    ||(StringUtils.isEmpty(contract.id))
               )
            {
                TaskContext<CreateUpdateInstalledBaseJob,InitEmptyInstalledBase> emptyCreateTaskContext = context.addTask(new InitEmptyInstalledBase());
                emptyCreateTaskContext.getTask().setContractTempId(contract.tempId);
                emptyCreateTaskContext.chainWith(
                        new UpdateInstalledBaseTask()
                );
            } else {
                UpdateInstalledBaseTask updateTask = new UpdateInstalledBaseTask();
                updateTask.setContractUid(contract.id);

            }
        }

        return true;
    }

    @TaskProcessingForClass(InitEmptyInstalledBase.class)
    public static class InitEmptyInstalledBaseProcessingService extends DocumentCreateTaskProcessingService<CreateUpdateInstalledBaseJob,InstalledBase,InitEmptyInstalledBase> {

        @Override
        public InstalledBase buildDocument(TaskContext<CreateUpdateInstalledBaseJob,InitEmptyInstalledBase> ctxt){
            InstalledBase result = ctxt.getSession().newEntity(InstalledBase.class);
            return result;
        }

        @Override
        public boolean updatejob(TaskContext<CreateUpdateInstalledBaseJob, InitEmptyInstalledBase> ctxt) throws TaskExecutionException {
            InitEmptyInstalledBase task = ctxt.getTask();
            CreateUpdateInstalledBaseResponse.Contract result = new CreateUpdateInstalledBaseResponse.Contract();
            result.tempId = task.getContractTempId();
            try {
                result.id = task.getDocument(ctxt.getSession()).getUid();
            }
            catch(DaoException e){
                throw new TaskExecutionException(ctxt,"Dao error during retrieval of doc "+task.getDocKey(),e);
            }
            catch(StorageException e){
                throw new TaskExecutionException(ctxt,"Storage error during retrieval of doc "+task.getDocKey(),e);
            }
            ctxt.getParentJob().getResult().contracts.add(result);
            return true;
        }
    }

    public static class AfterPreCreateSyncTaskPr extends NoOpTask {}


    @TaskProcessingForClass(UpdateInstalledBaseTask.class)
    public static class UpdateInstalledBaseProcessingService extends DocumentUpdateTaskProcessingService<CreateUpdateInstalledBaseJob,InstalledBase,UpdateInstalledBaseTask> {
        private ICreateUpdateInstalledBaseService service;


        @Autowired
        public void setService(ICreateUpdateInstalledBaseService service) {
            this.service = service;
        }

        @Override
        public boolean preprocess(TaskContext<CreateUpdateInstalledBaseJob,UpdateInstalledBaseTask> ctxt ) throws TaskExecutionException{
            UpdateInstalledBaseTask task=ctxt.getTask();
            if(task.getContractUid()!=null){
                try {
                    task.setDocKey(ctxt.getSession().getKeyFromUID(task.getContractUid(), InstalledBase.class));
                }
                catch(DaoException e){
                    throw new TaskExecutionException(ctxt,"Cannot build key from uid <"+task.getContractUid()+">");
                }
            }
            else {
                InitEmptyInstalledBase creationInstalledBase = ctxt.getDependentTask(InitEmptyInstalledBase.class);
                if (creationInstalledBase != null) {
                    task.setDocKey(creationInstalledBase.getDocKey());
                    task.setTempContractId(creationInstalledBase.getContractTempId());
                }
                else{
                    throw new TaskExecutionException(ctxt,"Inconsistent State : neither existing InstalledBase nor InitTask found");
                }
            }
            return false;
        }

        @Override
        public boolean processDocument(TaskContext<CreateUpdateInstalledBaseJob,UpdateInstalledBaseTask> ctxt, InstalledBase installBase) throws TaskExecutionException{
            CreateUpdateInstalledBaseRequest.Contract refContract=null;
            for(CreateUpdateInstalledBaseRequest.Contract currContract:ctxt.getParentJob().getRequest().contracts){
                if (ctxt.getTask().getTempContractId() != null && ctxt.getTask().getTempContractId().equals(currContract.tempId)) {
                    refContract=currContract;
                    break;
                }
                else if(ctxt.getTask().getContractUid().equals(currContract.id)){
                    refContract=currContract;
                    break;
                }
            }
            if(refContract==null){
                throw new TaskExecutionException(ctxt,"Inconsitent State, cannot find Contract in installed base "+ctxt.getTask().getDocKey());
            }
            service.manageCreateUpdate(ctxt.getParentJob().getRequest(),installBase,refContract);
            return true;
        }
    }
}

