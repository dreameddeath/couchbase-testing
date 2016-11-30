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

package com.dreameddeath.core.process.service.impl.processor;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.process.exception.AlreadyUpdatedTaskObservableException;
import com.dreameddeath.core.process.exception.DuplicateAttachedTaskException;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.exception.TaskObservableExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.CouchbaseDocumentAttachedTaskRef;
import com.dreameddeath.core.process.model.v1.base.IDocumentWithLinkedTasks;
import com.dreameddeath.core.process.model.v1.tasks.DocumentUpdateTask;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.context.TaskProcessingResult;
import com.google.common.base.Preconditions;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 23/11/2014.
 */
public abstract class DocumentUpdateTaskProcessingService<TJOB extends AbstractJob,TDOC extends CouchbaseDocument & IDocumentWithLinkedTasks,T extends DocumentUpdateTask<TDOC>> extends StandardTaskProcessingService<TJOB,T> {

    @Override
    public Observable<TaskProcessingResult<TJOB,T>> process(final TaskContext<TJOB,T> origCtxt){
        Preconditions.checkNotNull(origCtxt.getInternalTask().getDocKey(),"The document to update hasn't key for task %s of type %s",origCtxt.getInternalTask().getId(),origCtxt.getInternalTask().getClass().getName());
        try {
            return buildContextAndDocument(origCtxt)
                    .flatMap(this::manageCleanupBeforeRetry)
                    .flatMap(this::manageProcessDocument)
                    .flatMap(this::managePostProcessing)
                    .flatMap(this::saveDoc)
                    .map(ctxtAndDoc -> new TaskProcessingResult<>(ctxtAndDoc.getCtxt(), false))
                    .onErrorResumeNext(throwable -> this.manageError(throwable,origCtxt,false));
        }
        catch(Throwable e){
            return manageError(e,origCtxt,true);
        }
    }

    protected Observable<TaskProcessingResult<TJOB, T>> manageError(Throwable throwable, TaskContext<TJOB, T> origCtxt,boolean isPreparation) {
        if(throwable instanceof AlreadyUpdatedTaskObservableException){
            AlreadyUpdatedTaskObservableException e=(AlreadyUpdatedTaskObservableException)throwable;
            return TaskProcessingResult.build(e.getCtxt(),true);
        }
        if(throwable instanceof TaskObservableExecutionException) {
            return Observable.error(throwable);
        }
        else{
            return Observable.error(new TaskObservableExecutionException(origCtxt,isPreparation?"Error during Preparation":"Error during execution",throwable));
        }
    }

    protected Observable<ProcessingDocumentResult> manageProcessDocument(ContextAndDocument contextAndDocument) {
        return processDocument(new ContextAndDocument(contextAndDocument.getDoc(),contextAndDocument.getCtxt().getTemporaryReadOnlySessionContext()))
                .map(resProcessing->new ProcessingDocumentResult(resProcessing.isTaskUpdated(),resProcessing.getDocUpdated(),resProcessing.getCtxt().getStandardSessionContext()));
    }

    protected Observable<ContextAndDocument> managePostProcessing(ProcessingDocumentResult processingDocumentResult) {
        if(processingDocumentResult.isTaskUpdated()){
            return saveContext(processingDocumentResult.ctxtAndDoc)
                    .flatMap(this::attachTaskDef);
        }
        else{
            return attachTaskDef(processingDocumentResult.ctxtAndDoc);
        }
    }

    protected Observable<ContextAndDocument> attachTaskDef(ContextAndDocument contextAndDocument){
        try {
            CouchbaseDocumentAttachedTaskRef attachedTaskRef = new CouchbaseDocumentAttachedTaskRef();
            attachedTaskRef.setJobUid(contextAndDocument.getCtxt().getJobContext().getJobId());
            attachedTaskRef.setJobClass(contextAndDocument.getCtxt().getJobContext().getJobClass().getName());
            attachedTaskRef.setTaskId(contextAndDocument.getCtxt().getId());
            attachedTaskRef.setTaskClass(contextAndDocument.getCtxt().getTaskClass().getName());
            contextAndDocument.getDoc().addAttachedTaskRef(attachedTaskRef);
            return Observable.just(contextAndDocument);
        }
        catch(DuplicateAttachedTaskException e){
            return Observable.error(new TaskObservableExecutionException(contextAndDocument.getCtxt(),"DuplicateTask attachement",e));
        }
    }

    /**
     * Allow a clean up of task result before retry the processing of the document
     * @param ctxtAndDoc
     */
    protected Observable<ContextAndDocument> cleanTaskBeforeRetryProcessing(ContextAndDocument ctxtAndDoc) {
        return Observable.just(ctxtAndDoc);
    }

    public Observable<ContextAndDocument> buildContextAndDocument(final TaskContext<TJOB,T> ctxt){
        return ctxt.getSession().<TDOC>asyncGet(ctxt.getInternalTask().getDocKey())
                .map(doc->new ContextAndDocument(doc,ctxt));
    }

    protected Observable<ContextAndDocument> manageCleanupBeforeRetry(final ContextAndDocument ctxtAndDoc){
        CouchbaseDocumentAttachedTaskRef reference=ctxtAndDoc.getDoc().getAttachedTaskRef(ctxtAndDoc.ctxt.getInternalTask());
        if(reference!=null) {
            return Observable.<ContextAndDocument>error(new AlreadyUpdatedTaskObservableException(ctxtAndDoc.getCtxt(),ctxtAndDoc.getDoc()));
        }
        else{
            if(ctxtAndDoc.getCtxt().isNew()){
                return saveContext(ctxtAndDoc)
                        .flatMap(this::performTaskCleanup);
            }
            else{
                return performTaskCleanup(ctxtAndDoc);
            }
        }
    }


    private Observable<ContextAndDocument> performTaskCleanup(final ContextAndDocument origCtxtAndDoc){
        if(origCtxtAndDoc.getCtxt().getInternalTask().getUpdatedWithDoc()){
            return cleanTaskBeforeRetryProcessing(origCtxtAndDoc)
            .map(ctxtAndDoc->{
                ctxtAndDoc.getCtxt().getInternalTask().setUpdatedWithDoc(false);
                return ctxtAndDoc;
            })
            .flatMap(this::saveContext);
        }
        else{
            return Observable.just(origCtxtAndDoc);
        }
    }

    protected Observable<ContextAndDocument> saveContext(final ContextAndDocument ctxtAndDoc){
        return ctxtAndDoc.getCtxt().asyncSave().map(ctxt->new ContextAndDocument(ctxtAndDoc.getDoc(),ctxt));
    }

    protected Observable<ContextAndDocument> saveDoc(final ContextAndDocument ctxtAndDoc){
        return ctxtAndDoc.getCtxt().getSession().asyncSave(ctxtAndDoc.getDoc()).map(doc->new ContextAndDocument(doc,ctxtAndDoc.getCtxt()));
    }


    @Override
    public Observable<TaskProcessingResult<TJOB,T>> cleanup(TaskContext<TJOB,T> origCtxt) {
        return  buildContextAndDocument(origCtxt)
                .flatMap(ctxtAndDoc->{
                    ctxtAndDoc.getDoc().cleanupAttachedTaskRef(ctxtAndDoc.getCtxt().getInternalTask());
                    return saveDoc(ctxtAndDoc);
                })
                .map(ctxtAndDoc->new TaskProcessingResult<>(ctxtAndDoc.getCtxt(),false));
    }

    /**
     * Implement the document update processing
     * @param ctxtAndDoc
     * @return a value telling to save or not the task
     * @throws DaoException
     * @throws StorageException
     * @throws TaskExecutionException
     */
    protected abstract Observable<ProcessingDocumentResult> processDocument(ContextAndDocument ctxtAndDoc);

    public  class ProcessingDocumentResult{
        private final boolean taskUpdated;
        private final ContextAndDocument ctxtAndDoc;

        public  ProcessingDocumentResult(boolean taskUpdated, TDOC docUpdated, TaskContext<TJOB, T> ctxt) {
            this(taskUpdated,new ContextAndDocument(docUpdated,ctxt));
        }

        public  ProcessingDocumentResult(boolean taskUpdated, ContextAndDocument ctxtAndDoc) {
            this.taskUpdated = taskUpdated;
            this.ctxtAndDoc = ctxtAndDoc;
        }

        public ProcessingDocumentResult(ContextAndDocument ctxtAndDoc,boolean taskUpdated) {
            this.ctxtAndDoc = ctxtAndDoc;
            this.taskUpdated = taskUpdated;
        }

        public boolean isTaskUpdated() {
            return taskUpdated;
        }

        public TDOC getDocUpdated() {
            return ctxtAndDoc.doc;
        }

        public TaskContext<TJOB, T> getCtxt() {
            return ctxtAndDoc.getCtxt();
        }

        public ContextAndDocument getCtxtAndDoc(){
            return ctxtAndDoc;
        }

        public Observable<ProcessingDocumentResult> toObservable(){
            return Observable.just(this);
        }
    }

    protected class ContextAndDocument {
        private final TDOC doc;
        private final TaskContext<TJOB,T> ctxt;

        protected ContextAndDocument(TDOC doc, TaskContext<TJOB, T> ctxt) {
            this.doc = doc;
            this.ctxt = ctxt;
        }

        protected ContextAndDocument(TaskContext<TJOB, T> ctxt,TDOC doc) {
            this.doc = doc;
            this.ctxt = ctxt;
        }


        public TDOC getDoc() {
            return doc;
        }

        public TaskContext<TJOB, T> getCtxt() {
            return ctxt;
        }
    }
}
