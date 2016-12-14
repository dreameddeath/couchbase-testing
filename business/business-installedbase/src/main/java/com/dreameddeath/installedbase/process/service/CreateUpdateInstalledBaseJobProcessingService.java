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

package com.dreameddeath.installedbase.process.service;

import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.exception.TaskObservableExecutionException;
import com.dreameddeath.core.process.model.v1.tasks.NoOpTask;
import com.dreameddeath.core.process.service.context.*;
import com.dreameddeath.core.process.service.impl.processor.DocumentCreateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.processor.DocumentUpdateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.processor.StandardJobProcessingService;
import com.dreameddeath.installedbase.model.notifications.v1.CreateUpdateInstalledBaseEvent;
import com.dreameddeath.installedbase.model.v1.InstalledBase;
import com.dreameddeath.installedbase.process.model.v1.CreateUpdateInstalledBaseJob;
import com.dreameddeath.installedbase.process.model.v1.CreateUpdateInstalledBaseJob.InitEmptyInstalledBase;
import com.dreameddeath.installedbase.process.model.v1.CreateUpdateInstalledBaseJob.UpdateInstalledBaseTask;
import com.dreameddeath.installedbase.process.model.v1.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.process.model.v1.CreateUpdateInstalledBaseResponse;
import com.dreameddeath.installedbase.process.model.v1.InstalledBaseUpdateResult;
import com.dreameddeath.installedbase.service.ICreateUpdateInstalledBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 25/11/2014.
 */
@JobProcessingForClass(CreateUpdateInstalledBaseJob.class)
public class CreateUpdateInstalledBaseJobProcessingService extends StandardJobProcessingService<CreateUpdateInstalledBaseJob> {
    @Override
    public Observable<JobProcessingResult<CreateUpdateInstalledBaseJob>> init(JobContext<CreateUpdateInstalledBaseJob> context){
        CreateUpdateInstalledBaseJob job = context.getInternalJob();
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
                emptyCreateTaskContext.getInternalTask().setContractTempId(contract.tempId);
                emptyCreateTaskContext.chainWith(
                        new UpdateInstalledBaseTask()
                );
            } else {
                UpdateInstalledBaseTask updateTask = new UpdateInstalledBaseTask();
                updateTask.setContractUid(contract.id);

            }
        }

        return JobProcessingResult.build(context,true);
    }

    @TaskProcessingForClass(InitEmptyInstalledBase.class)
    public static class InitEmptyInstalledBaseProcessingService extends DocumentCreateTaskProcessingService<CreateUpdateInstalledBaseJob,InstalledBase,InitEmptyInstalledBase> {
        @Override
        protected Observable<ContextAndDocument> buildDocument(TaskContext<CreateUpdateInstalledBaseJob, InitEmptyInstalledBase> ctxt) {
            InstalledBase result = ctxt.getSession().newEntity(InstalledBase.class);
            return buildContextAndDocumentObservable(ctxt,result);
        }

        @Override
        public Observable<UpdateJobTaskProcessingResult<CreateUpdateInstalledBaseJob, InitEmptyInstalledBase>> updatejob(CreateUpdateInstalledBaseJob job, InitEmptyInstalledBase task, ICouchbaseSession session) {
            return task.getDocument(session)
                        .map(installedBase -> {
                            final CreateUpdateInstalledBaseResponse.Contract result = new CreateUpdateInstalledBaseResponse.Contract();
                            result.tempId = task.getContractTempId();
                            result.id=installedBase.getUid();
                            job.getResult().contracts.add(result);
                            return new UpdateJobTaskProcessingResult<>(job,task,true);
                        });
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
        public Observable<TaskProcessingResult<CreateUpdateInstalledBaseJob,UpdateInstalledBaseTask>> preprocess(final TaskContext<CreateUpdateInstalledBaseJob,UpdateInstalledBaseTask> ctxt ){
            if(ctxt.getInternalTask().getContractUid()!=null){
                try {
                    ctxt.getInternalTask().setDocKey(ctxt.getSession().getKeyFromUID(ctxt.getInternalTask().getContractUid(), InstalledBase.class));
                    return TaskProcessingResult.build(ctxt,false);
                }
                catch(DaoException e){
                    return Observable.error(new TaskObservableExecutionException(ctxt,"Cannot build key from uid <"+ctxt.getInternalTask().getContractUid()+">"));
                }
            }
            else {
                return ctxt.getDependentTask(InitEmptyInstalledBase.class)
                        .map(creationInstalledBase -> {
                            ctxt.getInternalTask().setDocKey(creationInstalledBase.getDocKey());
                            ctxt.getInternalTask().setTempContractId(creationInstalledBase.getContractTempId());
                            return new TaskProcessingResult<>(ctxt,false);
                        });
            }
        }

        @Override
        protected Observable<ProcessingDocumentResult> processDocument(ContextAndDocument ctxtAndDoc) {
            CreateUpdateInstalledBaseRequest.Contract refContract=null;
            for(CreateUpdateInstalledBaseRequest.Contract currContract:ctxtAndDoc.getCtxt().getParentInternalJob().getRequest().contracts){
                if (ctxtAndDoc.getCtxt().getInternalTask().getTempContractId() != null && ctxtAndDoc.getCtxt().getInternalTask().getTempContractId().equals(currContract.tempId)) {
                    refContract=currContract;
                    break;
                }
                else if(ctxtAndDoc.getCtxt().getInternalTask().getContractUid().equals(currContract.id)){
                    refContract=currContract;
                    break;
                }
            }
            if(refContract==null){
                return Observable.error(new TaskObservableExecutionException(ctxtAndDoc.getCtxt(),"Inconsistent State, cannot find Contract in installed base "+ctxtAndDoc.getCtxt().getInternalTask().getDocKey()));
            }
            InstalledBaseUpdateResult updateResult = service.manageCreateUpdate(ctxtAndDoc.getCtxt().getParentInternalJob().getRequest(),ctxtAndDoc.getDoc(),refContract);
            ctxtAndDoc.getCtxt().getInternalTask().setUpdateResult(updateResult);
            return new ProcessingDocumentResult(ctxtAndDoc,true).toObservable();
        }

        @Override
        public Observable<TaskNotificationBuildResult<CreateUpdateInstalledBaseJob, UpdateInstalledBaseTask>> buildNotifications(TaskContext<CreateUpdateInstalledBaseJob, UpdateInstalledBaseTask> ctxt) {
            CreateUpdateInstalledBaseEvent event = new CreateUpdateInstalledBaseEvent();
            event.setInstalledBaseKey(ctxt.getInternalTask().getDocKey());
            return super.buildNotifications(ctxt)
                    .flatMap(taskNotifRes->TaskNotificationBuildResult.build(taskNotifRes,event));
        }
    }
}

