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
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.IDocumentWithLinkedTasks;
import com.dreameddeath.core.process.model.v1.tasks.DocumentCreateOrUpdateTask;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.context.TaskProcessingResult;
import com.dreameddeath.core.validation.utils.ValidationExceptionUtils;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 16/11/2016.
 */
public abstract class DocumentCreateOrUpdateTaskProcessingService <TJOB extends AbstractJob,TDOC extends CouchbaseDocument & IDocumentWithLinkedTasks,T extends DocumentCreateOrUpdateTask<TDOC>> extends DocumentUpdateTaskProcessingService<TJOB,TDOC,T> {
    @Override
    public final Observable<TaskProcessingResult<TJOB,T>> process(TaskContext<TJOB,T> origCtxt) {
        try {
            return Observable.just(origCtxt)
                    .flatMap(this::findAndGetExistingDocument)
                    .flatMap(this::manageInitEmptyDocument)
                    .flatMap(this::manageFromDocInitialize)
                    .map(result->new TaskProcessingResult<>(result.getCtxt(),false))
                    .onErrorResumeNext(throwable->manageError(throwable,origCtxt,false));
        }
        catch (Throwable e){
            return manageError(e,origCtxt,true);
        }
    }

    private final Observable<ContextAndDocument> manageFromDocInitialize(ContextAndDocument contextAndDocument){
        return manageRetry(contextAndDocument)
                .flatMap(this::buildAndOrSetDocKey)
                .flatMap(this::manageCleanupBeforeRetry)
                .flatMap(this::manageProcessDocument)
                .flatMap(this::managePostProcessing)
                .flatMap(this::saveContext)
                .flatMap(this::saveDoc);
    }

    protected abstract Observable<FindAndGetResult> findAndGetExistingDocument(TaskContext<TJOB, T> taskContext);

    protected abstract Observable<ContextAndDocument> initEmptyDocument(TaskContext<TJOB, T> taskContext);

    protected abstract Observable<DuplicateUniqueKeyCheckResult> onDuplicateUniqueKey(ContextAndDocument ctxt, DuplicateUniqueKeyDaoException e);


    private Observable<ContextAndDocument> manageInitEmptyDocument(FindAndGetResult findAndGetResult){
        if(findAndGetResult.isExisting){
            return buildContextAndDocumentObservable(findAndGetResult.getCtxt(),findAndGetResult.getDoc());
        }
        else{
            return initEmptyDocument(findAndGetResult.getCtxt().getTemporaryReadOnlySessionContext())
                    .map(contextAndDocument -> new ContextAndDocument(contextAndDocument.getCtxt().getStandardSessionContext(),contextAndDocument.getDoc()));
        }
    }


    private Observable<ContextAndDocument> manageRetry(ContextAndDocument contextAndDocument) {
        return manageCleanup(contextAndDocument.getCtxt())
                .map(cleanedCtxt->{
                    return new ContextAndDocument(cleanedCtxt,contextAndDocument.getDoc());
                });
    }

    private Observable<TaskContext<TJOB, T>> manageCleanup(TaskContext<TJOB, T> origCtxt) {
        return cleanupBeforeRetryBuildDocument(origCtxt)
                .map(newCtxt->{
                    newCtxt.getInternalTask().setDocKey(null);
                    return newCtxt;
                })
                .flatMap(TaskContext::asyncSave);
    }

    private Observable<ContextAndDocument> manageDuplicateKey(ContextAndDocument context, DuplicateUniqueKeyDaoException duplicateUniqueKeyDaoException) {
        return onDuplicateUniqueKey(context,duplicateUniqueKeyDaoException)
                .flatMap(checkRes->{
                    if(checkRes.useDuplicateKeyOwnerDoc()){
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
        //return null;
    }

    @Override
    protected Observable<ContextAndDocument> saveDoc(final ContextAndDocument contextAndDocument) {
        return super.saveDoc(contextAndDocument)
                .onErrorResumeNext(throwable -> {
                        DuplicateUniqueKeyDaoException duplicateUniqueKeyDaoException = ValidationExceptionUtils.findUniqueKeyException(throwable);
                        if(duplicateUniqueKeyDaoException!=null){
                            return manageDuplicateKey(contextAndDocument,duplicateUniqueKeyDaoException);
                        }
                        return Observable.error(throwable);
                    });
    }

    private Observable<ContextAndDocument> buildAndOrSetDocKey(ContextAndDocument contextAndDocument) {
        if(contextAndDocument.getDoc().getBaseMeta().getState() == CouchbaseDocument.DocumentState.NEW){
            contextAndDocument.getCtxt().getInternalTask().setIsCreation(true);
            return contextAndDocument.getCtxt().getSession().asyncBuildKey(contextAndDocument.getDoc())
                    .map(docWithKey->{
                        contextAndDocument.getCtxt().getInternalTask().setDocKey(docWithKey.getBaseMeta().getKey());
                        return new ContextAndDocument(contextAndDocument.getCtxt(),docWithKey);
                    });
        }
        else{
            contextAndDocument.getCtxt().getInternalTask().setIsCreation(false);
            contextAndDocument.getCtxt().getInternalTask().setDocKey(contextAndDocument.getDoc().getBaseMeta().getKey());
            return Observable.just(contextAndDocument);
        }

    }

    protected Observable<TaskContext<TJOB,T>> cleanupBeforeRetryBuildDocument(TaskContext<TJOB, T> ctxt) {
        return Observable.just(ctxt);
    }


    protected final Observable<ContextAndDocument> buildContextAndDocumentObservable(TaskContext<TJOB, T> ctxt, TDOC doc){
        return Observable.just(new ContextAndDocument(ctxt,doc));
    }

    
    public class FindAndGetResult {
        private final TaskContext<TJOB,T> ctxt;
        private final boolean isExisting;
        private final TDOC doc;

        public FindAndGetResult(TaskContext<TJOB, T> ctxt, TDOC doc) {
            this.ctxt = ctxt;
            this.doc = doc;
            this.isExisting=(doc!=null);
        }

        public TaskContext<TJOB, T> getCtxt() {
            return ctxt;
        }

        public boolean isExisting() {
            return isExisting;
        }

        public TDOC getDoc() {
            return doc;
        }

        public Observable<FindAndGetResult> toObservable(){
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
            this.useDuplicateKeyOwnerDoc = false;
            this.taskContext = taskContext;
            this.origDocument = origDoc;
            this.targetDuplicateKeyDoc = null;
            this.origDuplicateException=exception;
        }

        public boolean useDuplicateKeyOwnerDoc() {
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
