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

package com.dreameddeath.core.process.services;

import com.dreameddeath.core.dao.exception.DuplicateUniqueKeyDaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.model.TestChildDoc;
import com.dreameddeath.core.process.model.TestDoc;
import com.dreameddeath.core.process.model.TestDocEvent;
import com.dreameddeath.core.process.service.context.*;
import com.dreameddeath.core.process.service.impl.processor.*;
import com.dreameddeath.core.process.services.model.TestDocJobCreate;
import com.google.common.base.Preconditions;
import io.reactivex.Single;

/**
 * Created by Christophe Jeunesse on 04/01/2016.
 */
@JobProcessingForClass(TestDocJobCreate.class)
public class TestJobCreateService extends StandardJobProcessingService<TestDocJobCreate> {
    @Override
    public Single<JobProcessingResult<TestDocJobCreate>> init(JobContext<TestDocJobCreate> context){
        TaskContext<TestDocJobCreate,?> updateTask=context.addTask(new TestDocJobCreate.TestJobCreateTask())
                .chainWith(new TestDocJobCreate.TestJobUpdateTask());
        //First chain ==> Pure create
        updateTask.chainWith(new TestDocJobCreate.TestJobCreateUpdateTaskForNew());
        //Second chain ==> create then update
        updateTask.chainWith(new TestDocJobCreate.TestJobCreateUpdateTaskForExisting(true))
                .chainWith(new TestDocJobCreate.TestJobCreateUpdateTaskForExisting(false));

        //Third chain child create
        TaskContext<TestDocJobCreate, TestDocJobCreate.ChildTestJobCreate> childCreateTask = updateTask.chainWith(new TestDocJobCreate.ChildTestJobCreate());
        //Fourth chain ==> Child create or update
        childCreateTask.chainWith(new TestDocJobCreate.ChildTestJobCreateUpdateTaskForNew());
        childCreateTask.chainWith(new TestDocJobCreate.ChildTestJobCreateUpdateTaskForExisting(true))
                    .chainWith(new TestDocJobCreate.ChildTestJobCreateUpdateTaskForExisting(false));

        return JobProcessingResult.build(context,false);
    }

    @TaskProcessingForClass(TestDocJobCreate.TestJobCreateTask.class)
    public static class TestJobCreateTaskService extends DocumentCreateTaskProcessingService<TestDocJobCreate,TestDoc,TestDocJobCreate.TestJobCreateTask>{
        @Override
        protected Single<ContextAndDocument> buildDocument(TaskContext<TestDocJobCreate, TestDocJobCreate.TestJobCreateTask> ctxt){
            TestDoc newDoc = new TestDoc();
            newDoc.name = ctxt.getParentInternalJob().name;
            newDoc.intValue = ctxt.getParentInternalJob().initIntValue;
            if(newDoc.intValue==null) { newDoc.intValue=0;}
            return buildContextAndDocumentObservable(ctxt,newDoc);
        }

        @Override
        public Single<UpdateJobTaskProcessingResult<TestDocJobCreate, TestDocJobCreate.TestJobCreateTask>> updatejob(TestDocJobCreate job, TestDocJobCreate.TestJobCreateTask task, ICouchbaseSession session) {
            job.createdKey = task.getDocKey();
            return new UpdateJobTaskProcessingResult<>(job,task,true).toSingle();
        }

        @Override
        public Single<TaskNotificationBuildResult<TestDocJobCreate, TestDocJobCreate.TestJobCreateTask>> buildNotifications(TaskContext<TestDocJobCreate, TestDocJobCreate.TestJobCreateTask> ctxt) {
            TestDocEvent event=new TestDocEvent();
            event.sourceTask="Create";
            return super.buildNotifications(ctxt)
                    .flatMap(taskNotifEvent->TaskNotificationBuildResult.build(taskNotifEvent,event));
        }
    }

    @TaskProcessingForClass(TestDocJobCreate.TestJobUpdateTask.class)
    public static class TestJobUpdateTaskService extends DocumentUpdateTaskProcessingService<TestDocJobCreate,TestDoc,TestDocJobCreate.TestJobUpdateTask> {
        @Override
        public Single<TaskProcessingResult<TestDocJobCreate, TestDocJobCreate.TestJobUpdateTask>> preprocess(TaskContext<TestDocJobCreate, TestDocJobCreate.TestJobUpdateTask> context) {
            return context.getDependentTask(TestDocJobCreate.TestJobCreateTask.class)
                    .singleOrError()
                    .flatMap(createTask->{
                        context.getInternalTask().setDocKey(createTask.getDocKey());
                        return TaskProcessingResult.build(context,false);
                    });
        }

        @Override
        protected Single<ProcessingDocumentResult> processDocument(ContextAndDocument ctxtAndDoc) {
            ctxtAndDoc.getDoc().intValue*=2;
            return new ProcessingDocumentResult(ctxtAndDoc,true).toSingle();
        }

        @Override
        public Single<TaskNotificationBuildResult<TestDocJobCreate, TestDocJobCreate.TestJobUpdateTask>> buildNotifications(TaskContext<TestDocJobCreate, TestDocJobCreate.TestJobUpdateTask> ctxt) {
            TestDocEvent event=new TestDocEvent();
            event.sourceTask="Update";
            return super.buildNotifications(ctxt)
                    .flatMap(taskNotifEvent->TaskNotificationBuildResult.build(taskNotifEvent,event));
        }
    }


    @TaskProcessingForClass(TestDocJobCreate.TestJobCreateUpdateTaskForNew.class)
    public static class TestJobCreateUpdateTaskForNewService extends DocumentCreateOrUpdateTaskProcessingService<TestDocJobCreate,TestDoc,TestDocJobCreate.TestJobCreateUpdateTaskForNew>{
        @Override
        protected Single<FindAndGetResult> findAndGetExistingDocument(TaskContext<TestDocJobCreate, TestDocJobCreate.TestJobCreateUpdateTaskForNew> taskContext) {
            return new FindAndGetResult(taskContext).toSingle();
        }

        @Override
        protected Single<ContextAndDocument> initEmptyDocument(TaskContext<TestDocJobCreate, TestDocJobCreate.TestJobCreateUpdateTaskForNew> taskContext) {
            TestDoc newDoc = new TestDoc();
            newDoc.name = taskContext.getParentInternalJob().name+"ForNew";
            newDoc.intValue = 0;
            return buildContextAndDocumentObservable(taskContext,newDoc);
        }

        @Override
        protected Single<ProcessingDocumentResult> processDocument(ContextAndDocument ctxt) {
            ctxt.getDoc().intValue += ctxt.getCtxt().getParentInternalJob().initIntValue;
            return new ProcessingDocumentResult(false,ctxt).toSingle();
        }

        @Override
        protected Single<DuplicateUniqueKeyCheckResult> onDuplicateUniqueKey(ContextAndDocument ctxt, DuplicateUniqueKeyDaoException e) {
            return ctxt.getCtxt().getSession().asyncGet(e.getOwnerDocumentKey(), TestDoc.class)
                    .map(doc->new DuplicateUniqueKeyCheckResult(ctxt.getCtxt(),doc));
        }
    }


    @TaskProcessingForClass(TestDocJobCreate.TestJobCreateUpdateTaskForExisting.class)
    public static class TestJobCreateUpdateTaskForExistingService extends DocumentCreateOrUpdateTaskProcessingService<TestDocJobCreate,TestDoc,TestDocJobCreate.TestJobCreateUpdateTaskForExisting>{
        @Override
        protected Single<FindAndGetResult> findAndGetExistingDocument(TaskContext<TestDocJobCreate, TestDocJobCreate.TestJobCreateUpdateTaskForExisting> taskContext) {
            if(taskContext.getInternalTask().fromRead){
                return taskContext.getDependentTask(TestDocJobCreate.TestJobCreateTask.class)
                        .singleOrError()
                        .flatMap(testJobCreateTask -> testJobCreateTask.getDocument(taskContext.getSession()))
                        .map(testDoc->new FindAndGetResult(taskContext,testDoc));
            }
            else {
                return new FindAndGetResult(taskContext).toSingle();
            }
        }

        @Override
        protected Single<ContextAndDocument> initEmptyDocument(TaskContext<TestDocJobCreate, TestDocJobCreate.TestJobCreateUpdateTaskForExisting> taskContext) {
            TestDoc newDoc = new TestDoc();
            newDoc.name = taskContext.getParentInternalJob().name;
            newDoc.intValue = 0;
            return buildContextAndDocumentObservable(taskContext,newDoc);
        }

        @Override
        protected Single<ProcessingDocumentResult> processDocument(ContextAndDocument ctxt) {
            ctxt.getDoc().intValue += ctxt.getCtxt().getParentInternalJob().initIntValue;
            return new ProcessingDocumentResult(false,ctxt).toSingle();
        }

        @Override
        protected Single<DuplicateUniqueKeyCheckResult> onDuplicateUniqueKey(ContextAndDocument ctxt, DuplicateUniqueKeyDaoException e) {
            TestDocJobCreate.TestJobCreateTask testJobCreateTask = ctxt.getCtxt().getDependentTask(TestDocJobCreate.TestJobCreateTask.class).blockingFirst();
            Preconditions.checkArgument(testJobCreateTask.getDocKey().equals(e.getOwnerDocumentKey()));
            return ctxt.getCtxt().getSession().asyncGet(e.getOwnerDocumentKey(), TestDoc.class)
                    .map(doc->new DuplicateUniqueKeyCheckResult(ctxt.getCtxt(),doc));
        }
    }

    @TaskProcessingForClass(TestDocJobCreate.ChildTestJobCreate.class)
    public static class ChildTestJobCreateTask extends ChildDocumentCreateTaskProcessingService<TestDocJobCreate,TestChildDoc,TestDoc,TestDocJobCreate.ChildTestJobCreate> {
        @Override
        public Single<TaskProcessingResult<TestDocJobCreate, TestDocJobCreate.ChildTestJobCreate>> preprocess(TaskContext<TestDocJobCreate, TestDocJobCreate.ChildTestJobCreate> context) {
            return context.getDependentTask(TestDocJobCreate.TestJobCreateTask.class)
                    .singleOrError()
                    .map(task->{
                        context.getInternalTask().setParentDocKey(task.getDocKey());
                        return context.getInternalTask();
                    })
                    .flatMap(task->TaskProcessingResult.build(context,true));
        }

        @Override
        protected boolean needParentUpdate(TestDoc parent, TestChildDoc child) {
            return !parent.childs.contains(child.childId);
        }

        @Override
        protected void updateParent(TestDoc parent, TestChildDoc child) {
            parent.childs.add(child.childId);
        }

        @Override
        protected Single<ContextAndDocument> buildDocument(TaskContext<TestDocJobCreate, TestDocJobCreate.ChildTestJobCreate> ctxt) {
            TestChildDoc childDoc = new TestChildDoc();
            childDoc.childId = ctxt.getParentInternalJob().name+"Create";
            childDoc.parentDocKey = ctxt.getInternalTask().getParentDocKey();
            return buildContextAndDocumentObservable(ctxt,childDoc);
        }
    }


    @TaskProcessingForClass(TestDocJobCreate.ChildTestJobCreateUpdateTaskForNew.class)
    public static class ChildTestJobCreateOrUpdateTask extends ChildDocumentCreateOrUpdateTaskProcessingService<TestDocJobCreate,TestChildDoc,TestDoc,TestDocJobCreate.ChildTestJobCreateUpdateTaskForNew> {
        @Override
        public Single<TaskProcessingResult<TestDocJobCreate, TestDocJobCreate.ChildTestJobCreateUpdateTaskForNew>> preprocess(TaskContext<TestDocJobCreate, TestDocJobCreate.ChildTestJobCreateUpdateTaskForNew> context) {
            return context.getDependentTask(TestDocJobCreate.TestJobCreateTask.class)
                    .singleOrError()
                    .map(task->{
                        context.getInternalTask().setParentDocKey(task.getDocKey());
                        return context.getInternalTask();
                    })
                    .flatMap(task->TaskProcessingResult.build(context,true));
        }

        @Override
        protected boolean needParentUpdate(TestDoc parent, TestChildDoc child) {
            return !parent.childs.contains(child.childId);
        }

        @Override
        protected void updateParent(TestDoc parent, TestChildDoc child) {
            parent.childs.add(child.childId);
        }

        @Override
        protected Single<FindAndGetResult> findAndGetExistingDocument(TaskContext<TestDocJobCreate, TestDocJobCreate.ChildTestJobCreateUpdateTaskForNew> taskContext) {
            return new FindAndGetResult(taskContext).toSingle();
        }

        @Override
        protected Single<ContextAndDocument> initEmptyDocument(TaskContext<TestDocJobCreate, TestDocJobCreate.ChildTestJobCreateUpdateTaskForNew> taskContext) {
            TestChildDoc childDoc = new TestChildDoc();
            childDoc.childId = taskContext.getParentInternalJob().name+"New";
            childDoc.parentDocKey = taskContext.getInternalTask().getParentDocKey();
            return buildContextAndDocumentObservable(taskContext,childDoc);
        }

        @Override
        protected Single<DuplicateUniqueKeyCheckResult> onDuplicateUniqueKey(ContextAndDocument ctxt, DuplicateUniqueKeyDaoException e) {
            TestDocJobCreate.ChildTestJobCreateUpdateTaskForNew childTestJobCreateTask = ctxt.getCtxt().getDependentTask(TestDocJobCreate.ChildTestJobCreateUpdateTaskForNew.class).blockingFirst();
            Preconditions.checkArgument(childTestJobCreateTask.getDocKey().equals(e.getOwnerDocumentKey()));
            return ctxt.getCtxt().getSession().asyncGet(e.getOwnerDocumentKey(), TestChildDoc.class)
                    .map(doc->new DuplicateUniqueKeyCheckResult(ctxt.getCtxt(),doc));
        }

        @Override
        protected Single<ProcessingDocumentResult> processDocument(ContextAndDocument ctxtAndDoc) {
            ctxtAndDoc.getDoc().counter += ctxtAndDoc.getCtxt().getParentInternalJob().initIntValue;
            return new ProcessingDocumentResult(false,ctxtAndDoc).toSingle();
        }
    }


    @TaskProcessingForClass(TestDocJobCreate.ChildTestJobCreateUpdateTaskForExisting.class)
    public static class ChildTestJobCreateOrUpdateTaskForExisting extends ChildDocumentCreateOrUpdateTaskProcessingService<TestDocJobCreate,TestChildDoc,TestDoc,TestDocJobCreate.ChildTestJobCreateUpdateTaskForExisting> {
        @Override
        public Single<TaskProcessingResult<TestDocJobCreate, TestDocJobCreate.ChildTestJobCreateUpdateTaskForExisting>> preprocess(TaskContext<TestDocJobCreate, TestDocJobCreate.ChildTestJobCreateUpdateTaskForExisting> context) {
            return context.getDependentTask(TestDocJobCreate.TestJobCreateTask.class)
                    .singleOrError()
                    .map(task->{
                        context.getInternalTask().setParentDocKey(task.getDocKey());
                        return context.getInternalTask();
                    })
                    .flatMap(task->TaskProcessingResult.build(context,true));
        }

        @Override
        protected boolean needParentUpdate(TestDoc parent, TestChildDoc child) {
            return !parent.childs.contains(child.childId);
        }

        @Override
        protected void updateParent(TestDoc parent, TestChildDoc child) {
            parent.childs.add(child.childId);
        }

        @Override
        protected Single<FindAndGetResult> findAndGetExistingDocument(TaskContext<TestDocJobCreate, TestDocJobCreate.ChildTestJobCreateUpdateTaskForExisting> taskContext) {
            if(taskContext.getInternalTask().fromRead){
                return taskContext.getDependentTask(TestDocJobCreate.ChildTestJobCreate.class)
                        .singleOrError()
                        .flatMap(testJobCreateTask -> testJobCreateTask.getDocument(taskContext.getSession()))
                        .map(testDoc->new FindAndGetResult(taskContext,testDoc));
            }
            else {
                return new FindAndGetResult(taskContext).toSingle();
            }
        }

        @Override
        protected Single<ContextAndDocument> initEmptyDocument(TaskContext<TestDocJobCreate, TestDocJobCreate.ChildTestJobCreateUpdateTaskForExisting> taskContext) {
            TestChildDoc childDoc = new TestChildDoc();
            childDoc.childId = taskContext.getParentInternalJob().name;
            childDoc.parentDocKey = taskContext.getInternalTask().getParentDocKey();
            return buildContextAndDocumentObservable(taskContext,childDoc);
        }

        @Override
        protected Single<DuplicateUniqueKeyCheckResult> onDuplicateUniqueKey(ContextAndDocument ctxt, DuplicateUniqueKeyDaoException e) {
            TestDocJobCreate.ChildTestJobCreateUpdateTaskForExisting childTestJobCreateTask = ctxt.getCtxt().getDependentTask(TestDocJobCreate.ChildTestJobCreateUpdateTaskForExisting.class).blockingFirst();
            Preconditions.checkArgument(childTestJobCreateTask.getDocKey().equals(e.getOwnerDocumentKey()));
            return ctxt.getCtxt().getSession()
                    .asyncGet(e.getOwnerDocumentKey(), TestChildDoc.class)
                    .map(doc->new DuplicateUniqueKeyCheckResult(ctxt.getCtxt(),doc));
        }

        @Override
        protected Single<ProcessingDocumentResult> processDocument(ContextAndDocument ctxtAndDoc) {
            ctxtAndDoc.getDoc().counter += ctxtAndDoc.getCtxt().getParentInternalJob().initIntValue;
            return new ProcessingDocumentResult(false,ctxtAndDoc).toSingle();
        }
    }


}
