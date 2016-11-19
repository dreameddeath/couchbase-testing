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

import com.dreameddeath.core.dao.exception.DuplicateUniqueKeyDaoException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.DuplicateUniqueKeyException;
import com.dreameddeath.core.process.exception.DuplicateUniqueKeyCreateOrUpdateTaskException;
import com.dreameddeath.core.process.exception.TaskObservableExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.tasks.DocumentCreateOrUpdateTask;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.context.TaskProcessingResult;
import com.dreameddeath.core.validation.utils.ValidationExceptionUtils;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 16/11/2016.
 */
public abstract class DocumentCreateOrUpdateTaskProcessingService <TJOB extends AbstractJob,TDOC extends CouchbaseDocument,T extends DocumentCreateOrUpdateTask<TDOC>> extends StandardTaskProcessingService<TJOB,T> {
    @Override
    public final Observable<TaskProcessingResult<TJOB,T>> process(TaskContext<TJOB,T> origCtxt) {
        try {
            return Observable.just(origCtxt)
                    .flatMap(this::findAndGetExistingDocument)
                    .flatMap(this::manageInitEmptyDocument)
                    .flatMap(this::manageFromDocInitialize);
        }
        catch (Throwable e){
            return manageError(e,origCtxt,true);
        }
    }

    private final Observable<TaskProcessingResult<TJOB,T>> manageFromDocInitialize(ContextAndDocument contextAndDocument){
        return manageRetry(contextAndDocument)
                .map(this::toTemporarySession)
                .flatMap(this::processDocument)
                .map(this::toStandardSession)
                .flatMap(this::buildAndSetDocKey)
                .flatMap(this::saveContext)
                .flatMap(this::saveDoc)
                .map(result->new TaskProcessingResult<>(result.ctxt,false))
                .onErrorResumeNext(throwable->manageError(throwable,contextAndDocument.getCtxt(),false));
    }

    protected abstract Observable<FindAndGetResult> findAndGetExistingDocument(TaskContext<TJOB, T> taskContext);

    protected abstract Observable<ContextAndDocument> initEmptyDocument(TaskContext<TJOB, T> taskContext);

    protected abstract Observable<ProcessingDocumentResult> processDocument(ContextAndDocument ctxt);

    protected abstract Observable<DuplicateUniqueKeyCheckResult> onDuplicateUniqueKey(ContextAndDocument ctxt, DuplicateUniqueKeyDaoException e);


    private Observable<ProcessingDocumentResult> manageProcessDocument(ContextAndDocument contextAndDocument) {
        return processDocument(new ContextAndDocument(contextAndDocument.getDoc(), contextAndDocument.getCtxt().getTemporaryReadOnlySessionContext()))
                .map(resProcessing -> new ProcessingDocumentResult(resProcessing.isTaskUpdated(), resProcessing.getDocUpdated(), resProcessing.getCtxt().getStandardSessionContext()));
    }

    private Observable<ContextAndDocument> manageInitEmptyDocument(FindAndGetResult findAndGetResult){
        if(findAndGetResult.isExisting){
            findAndGetResult.ctxt.getInternalTask().setIsCreation(false);
            findAndGetResult.ctxt.getInternalTask().setDocKey(findAndGetResult.doc.getBaseMeta().getKey());
            return buildContextAndDocumentObservable(findAndGetResult.ctxt,findAndGetResult.doc);
        }
        else{
            findAndGetResult.ctxt.getInternalTask().setIsCreation(true);
            return initEmptyDocument(findAndGetResult.ctxt.getTemporaryReadOnlySessionContext())
                    .flatMap(contextAndDocument -> buildContextAndDocumentObservable(contextAndDocument.ctxt.getStandardSessionContext(),contextAndDocument.doc));
        }
    }

    private Observable<TaskProcessingResult<TJOB,T>> manageError(Throwable e, TaskContext<TJOB,T> origCtxt,boolean isPreparation){
        if(e instanceof TaskObservableExecutionException) {
            return Observable.error(e);
        }
        else{
            return Observable.error(new TaskObservableExecutionException(origCtxt,isPreparation?"Error during Preparation":"Error during execution",e));
        }
    }

    private ContextAndDocument toTemporarySession(ContextAndDocument taskContext) {
        return new ContextAndDocument(taskContext.ctxt.getTemporaryReadOnlySessionContext(),taskContext.doc);
    }

    private ContextAndDocument toStandardSession(ProcessingDocumentResult processingDocumentResult) {
        return new ContextAndDocument(processingDocumentResult.ctxtAndDoc.ctxt.getStandardSessionContext(),processingDocumentResult.ctxtAndDoc.doc);
    }

    private Observable<ContextAndDocument> manageRetry(ContextAndDocument contextAndDocument) {
        //TODO
        return Observable.just(contextAndDocument);
    }

    private Observable<TaskContext<TJOB, T>> manageCleanup(TaskContext<TJOB, T> origCtxt) {
        return cleanupBeforeRetryBuildDocument(origCtxt)
                .map(newCtxt->{
                    newCtxt.getInternalTask().setDocKey(null);
                    return newCtxt;
                })
                .flatMap(TaskContext::asyncSave);
    }

    private Observable<ContextAndDocument> saveContext(final ContextAndDocument contextAndDocument) {
        return contextAndDocument.ctxt.asyncSave()
                .map(savedContext->new ContextAndDocument(savedContext,contextAndDocument.doc))
                .onErrorResumeNext(throwable -> {
                    DuplicateUniqueKeyDaoException duplicateUniqueKeyDaoException = ValidationExceptionUtils.findUniqueKeyException(throwable);
                    if(duplicateUniqueKeyDaoException!=null){
                        return manageDuplicateKey(contextAndDocument,duplicateUniqueKeyDaoException);
                    }
                    return Observable.error(throwable);
                });
    }

    private Observable<ContextAndDocument> manageDuplicateKey(ContextAndDocument context, DuplicateUniqueKeyDaoException duplicateUniqueKeyDaoException) {
        onDuplicateUniqueKey(context,duplicateUniqueKeyDaoException)
                .map(checkRes->{
                    if(checkRes.useDuplicateKeyOwnerDoc()){
                        checkRes.getTaskContext().getInternalTask().setDocKey(checkRes.getTargetDuplicateKeyDoc().getBaseMeta().getKey());
                        return manageFromDocInitialize(new ContextAndDocument(checkRes.getTaskContext(),checkRes.getTargetDuplicateKeyDoc()));
                    }
                    else{
                        return Observable.error(
                                new DuplicateUniqueKeyCreateOrUpdateTaskException(
                                        checkRes.getTaskContext(),
                                        "A true duplicate error occurs",
                                        checkRes.getOrigDocument(),
                                        checkRes.origDuplicateException));
                    }
                });
        return null;
    }


    private Observable<ContextAndDocument> saveDoc(final ContextAndDocument contextAndDocument) {
        return contextAndDocument.ctxt.getSession().asyncSave(contextAndDocument.doc).map(savedDoc->new ContextAndDocument(contextAndDocument.ctxt,savedDoc));
    }

    private Observable<ContextAndDocument> buildAndSetDocKey(ContextAndDocument contextAndDocument) {
        if(contextAndDocument.doc.getBaseMeta().getState() == CouchbaseDocument.DocumentState.NEW){
            return contextAndDocument.ctxt.getSession().asyncBuildKey(contextAndDocument.doc)
                    .map(docWithKey->{
                        contextAndDocument.ctxt.getInternalTask().setDocKey(docWithKey.getBaseMeta().getKey());
                        return new ContextAndDocument(contextAndDocument.ctxt,docWithKey);
                    });
        }
        else{
            return Observable.just(contextAndDocument);
        }

    }

    protected Observable<TaskContext<TJOB,T>> cleanupBeforeRetryBuildDocument(TaskContext<TJOB, T> ctxt) {
        return Observable.just(ctxt);
    }



    protected final Observable<ContextAndDocument> buildContextAndDocumentObservable(TaskContext<TJOB, T> ctxt, TDOC doc){
        return Observable.just(new ContextAndDocument(ctxt,doc));
    }

    protected class ContextAndDocument{
        private final TaskContext<TJOB,T> ctxt;
        private final TDOC doc;

        protected ContextAndDocument(TaskContext<TJOB, T> ctxt, TDOC doc) {
            this.ctxt = ctxt;
            this.doc = doc;
        }

        protected ContextAndDocument(TDOC doc,TaskContext<TJOB, T> ctxt) {
            this.ctxt = ctxt;
            this.doc = doc;
        }


        public Observable<ContextAndDocument> toObservable(){
            return Observable.just(this);
        }

        public TDOC getDoc() {
            return doc;
        }

        public TaskContext<TJOB,T> getCtxt() {
            return ctxt;
        }
    }

    public class FindAndGetResult {
        private final TaskContext<TJOB,T> ctxt;
        private final boolean isExisting;
        private final TDOC doc;

        protected FindAndGetResult(TaskContext<TJOB, T> ctxt, TDOC doc) {
            this.ctxt = ctxt;
            this.doc = doc;
            this.isExisting=(doc!=null);
        }

        public Observable<FindAndGetResult> toObservable(){
            return Observable.just(this);
        }
    }

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

        public ProcessingDocumentResult(ContextAndDocument ctxtAndDoc, boolean taskUpdated) {
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

        public Observable<ProcessingDocumentResult> toObservable(){
            return Observable.just(this);
        }
    }

    public class DuplicateUniqueKeyCheckResult {
        private final boolean useDuplicateKeyOwnerDoc;
        private final TaskContext<TJOB,T> taskContext;
        private final TDOC origDocument;
        private final TDOC targetDuplicateKeyDoc;
        private final DuplicateUniqueKeyException origDuplicateException;

        public DuplicateUniqueKeyCheckResult(TaskContext<TJOB, T> taskContext, TDOC targetDuplicateKeyDoc) {
            this.useDuplicateKeyOwnerDoc = true;
            this.taskContext = taskContext;
            this.origDocument = null;
            this.targetDuplicateKeyDoc = targetDuplicateKeyDoc;
            this.origDuplicateException=null;
        }


        public DuplicateUniqueKeyCheckResult(TaskContext<TJOB, T> taskContext, TDOC origDoc,DuplicateUniqueKeyException exception) {
            this.useDuplicateKeyOwnerDoc = true;
            this.taskContext = taskContext;
            this.origDocument = origDoc;
            this.targetDuplicateKeyDoc = null;
            this.origDuplicateException=exception;
        }

        public boolean useDuplicateKeyOwnerDoc() {
            return useDuplicateKeyOwnerDoc;
        }

        public boolean isUseDuplicateKeyOwnerDoc() {
            return useDuplicateKeyOwnerDoc;
        }

        public TaskContext<TJOB, T> getTaskContext() {
            return taskContext;
        }

        public TDOC getOrigDocument() {
            return origDocument;
        }

        public TDOC getTargetDuplicateKeyDoc() {
            return targetDuplicateKeyDoc;
        }
    }
}
