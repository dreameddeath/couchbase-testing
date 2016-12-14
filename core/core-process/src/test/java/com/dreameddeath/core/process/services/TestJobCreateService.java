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

package com.dreameddeath.core.process.services;

import com.dreameddeath.core.dao.exception.DuplicateUniqueKeyDaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.model.TestDoc;
import com.dreameddeath.core.process.model.TestDocEvent;
import com.dreameddeath.core.process.service.context.*;
import com.dreameddeath.core.process.service.impl.processor.DocumentCreateOrUpdateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.processor.DocumentCreateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.processor.DocumentUpdateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.processor.StandardJobProcessingService;
import com.dreameddeath.core.process.services.model.TestDocJobCreate;
import com.google.common.base.Preconditions;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 04/01/2016.
 */
@JobProcessingForClass(TestDocJobCreate.class)
public class TestJobCreateService extends StandardJobProcessingService<TestDocJobCreate> {
    @Override
    public Observable<JobProcessingResult<TestDocJobCreate>> init(JobContext<TestDocJobCreate> context){
        TaskContext<TestDocJobCreate,?> updateTask=context.addTask(new TestDocJobCreate.TestJobCreateTask())
                .chainWith(new TestDocJobCreate.TestJobUpdateTask());
        updateTask.chainWith(new TestDocJobCreate.TestJobCreateUpdateTaskForNew());
        updateTask.chainWith(new TestDocJobCreate.TestJobCreateUpdateTaskForExisting(true))
                .chainWith(new TestDocJobCreate.TestJobCreateUpdateTaskForExisting(false));
        return JobProcessingResult.build(context,false);
    }

    @TaskProcessingForClass(TestDocJobCreate.TestJobCreateTask.class)
    public static class TestJobCreateTaskService extends DocumentCreateTaskProcessingService<TestDocJobCreate,TestDoc,TestDocJobCreate.TestJobCreateTask>{
        @Override
        protected Observable<ContextAndDocument> buildDocument(TaskContext<TestDocJobCreate, TestDocJobCreate.TestJobCreateTask> ctxt){
            TestDoc newDoc = new TestDoc();
            newDoc.name = ctxt.getParentInternalJob().name;
            newDoc.intValue = ctxt.getParentInternalJob().initIntValue;
            if(newDoc.intValue==null) { newDoc.intValue=0;}
            return buildContextAndDocumentObservable(ctxt,newDoc);
        }

        @Override
        public Observable<UpdateJobTaskProcessingResult<TestDocJobCreate, TestDocJobCreate.TestJobCreateTask>> updatejob(TestDocJobCreate job, TestDocJobCreate.TestJobCreateTask task, ICouchbaseSession session) {
            job.createdKey = task.getDocKey();
            return new UpdateJobTaskProcessingResult<>(job,task,true).toObservable();
        }

        @Override
        public Observable<TaskNotificationBuildResult<TestDocJobCreate, TestDocJobCreate.TestJobCreateTask>> buildNotifications(TaskContext<TestDocJobCreate, TestDocJobCreate.TestJobCreateTask> ctxt) {
            TestDocEvent event=new TestDocEvent();
            event.sourceTask="Create";
            return super.buildNotifications(ctxt)
                    .flatMap(taskNotifEvent->TaskNotificationBuildResult.build(taskNotifEvent,event));
        }
    }

    @TaskProcessingForClass(TestDocJobCreate.TestJobUpdateTask.class)
    public static class TestJobUpdateTaskService extends DocumentUpdateTaskProcessingService<TestDocJobCreate,TestDoc,TestDocJobCreate.TestJobUpdateTask> {
        @Override
        public Observable<TaskProcessingResult<TestDocJobCreate, TestDocJobCreate.TestJobUpdateTask>> preprocess(TaskContext<TestDocJobCreate, TestDocJobCreate.TestJobUpdateTask> context) {
            return context.getDependentTask(TestDocJobCreate.TestJobCreateTask.class)
                    .flatMap(createTask->{
                        context.getInternalTask().setDocKey(createTask.getDocKey());
                        return TaskProcessingResult.build(context,false);
                    });
        }

        @Override
        protected Observable<ProcessingDocumentResult> processDocument(ContextAndDocument ctxtAndDoc) {
            ctxtAndDoc.getDoc().intValue*=2;
            return new ProcessingDocumentResult(ctxtAndDoc,true).toObservable();
        }

        @Override
        public Observable<TaskNotificationBuildResult<TestDocJobCreate, TestDocJobCreate.TestJobUpdateTask>> buildNotifications(TaskContext<TestDocJobCreate, TestDocJobCreate.TestJobUpdateTask> ctxt) {
            TestDocEvent event=new TestDocEvent();
            event.sourceTask="Update";
            return super.buildNotifications(ctxt)
                    .flatMap(taskNotifEvent->TaskNotificationBuildResult.build(taskNotifEvent,event));
        }
    }


    @TaskProcessingForClass(TestDocJobCreate.TestJobCreateUpdateTaskForNew.class)
    public static class TestJobCreateUpdateTaskForNewService extends DocumentCreateOrUpdateTaskProcessingService<TestDocJobCreate,TestDoc,TestDocJobCreate.TestJobCreateUpdateTaskForNew>{
        @Override
        protected Observable<FindAndGetResult> findAndGetExistingDocument(TaskContext<TestDocJobCreate, TestDocJobCreate.TestJobCreateUpdateTaskForNew> taskContext) {
            return new FindAndGetResult(taskContext).toObservable();
        }

        @Override
        protected Observable<ContextAndDocument> initEmptyDocument(TaskContext<TestDocJobCreate, TestDocJobCreate.TestJobCreateUpdateTaskForNew> taskContext) {
            TestDoc newDoc = new TestDoc();
            newDoc.name = taskContext.getParentInternalJob().name+"ForNew";
            newDoc.intValue = 0;
            return buildContextAndDocumentObservable(taskContext,newDoc);
        }

        @Override
        protected Observable<ProcessingDocumentResult> processDocument(ContextAndDocument ctxt) {
            ctxt.getDoc().intValue += ctxt.getCtxt().getParentInternalJob().initIntValue;
            return new ProcessingDocumentResult(false,ctxt).toObservable();
        }

        @Override
        protected Observable<DuplicateUniqueKeyCheckResult> onDuplicateUniqueKey(ContextAndDocument ctxt, DuplicateUniqueKeyDaoException e) {
            return ctxt.getCtxt().getSession().asyncGet(e.getOwnerDocumentKey(), TestDoc.class)
                    .map(doc->new DuplicateUniqueKeyCheckResult(ctxt.getCtxt(),doc));
        }
    }


    @TaskProcessingForClass(TestDocJobCreate.TestJobCreateUpdateTaskForExisting.class)
    public static class TestJobCreateUpdateTaskForExistingService extends DocumentCreateOrUpdateTaskProcessingService<TestDocJobCreate,TestDoc,TestDocJobCreate.TestJobCreateUpdateTaskForExisting>{
        @Override
        protected Observable<FindAndGetResult> findAndGetExistingDocument(TaskContext<TestDocJobCreate, TestDocJobCreate.TestJobCreateUpdateTaskForExisting> taskContext) {
            if(taskContext.getInternalTask().fromRead){
                return taskContext.getDependentTask(TestDocJobCreate.TestJobCreateTask.class)
                        .flatMap(testJobCreateTask -> testJobCreateTask.getDocument(taskContext.getSession()))
                        .map(testDoc->new FindAndGetResult(taskContext,testDoc));
            }
            else {
                return new FindAndGetResult(taskContext).toObservable();
            }
        }

        @Override
        protected Observable<ContextAndDocument> initEmptyDocument(TaskContext<TestDocJobCreate, TestDocJobCreate.TestJobCreateUpdateTaskForExisting> taskContext) {
            TestDoc newDoc = new TestDoc();
            newDoc.name = taskContext.getParentInternalJob().name;
            newDoc.intValue = 0;
            return buildContextAndDocumentObservable(taskContext,newDoc);
        }

        @Override
        protected Observable<ProcessingDocumentResult> processDocument(ContextAndDocument ctxt) {
            ctxt.getDoc().intValue += ctxt.getCtxt().getParentInternalJob().initIntValue;
            return new ProcessingDocumentResult(false,ctxt).toObservable();
        }

        @Override
        protected Observable<DuplicateUniqueKeyCheckResult> onDuplicateUniqueKey(ContextAndDocument ctxt, DuplicateUniqueKeyDaoException e) {
            TestDocJobCreate.TestJobCreateTask testJobCreateTask = ctxt.getCtxt().getDependentTask(TestDocJobCreate.TestJobCreateTask.class).toBlocking().first();
            Preconditions.checkArgument(testJobCreateTask.getDocKey().equals(e.getOwnerDocumentKey()));
            return ctxt.getCtxt().getSession().asyncGet(e.getOwnerDocumentKey(), TestDoc.class)
                    .map(doc->new DuplicateUniqueKeyCheckResult(ctxt.getCtxt(),doc));
        }
    }


}
