/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.process.service.impl.processor;

import com.dreameddeath.core.couchbase.exception.DocumentNotFoundException;
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
import io.reactivex.Single;

import java.util.Optional;

/**
 * Created by Christophe Jeunesse on 16/11/2016.
 */
public abstract class DocumentCreateOrUpdateTaskProcessingService <TJOB extends AbstractJob,TDOC extends CouchbaseDocument & IDocumentWithLinkedTasks,T extends DocumentCreateOrUpdateTask<TDOC>> extends DocumentUpdateTaskProcessingService<TJOB,TDOC,T> {
    @Override
    public final Single<TaskProcessingResult<TJOB,T>> process(TaskContext<TJOB,T> origCtxt) {
        try {
            return Single.just(origCtxt)
                    .flatMap(this::manageRetry)//Reuse attached task if any
                    .flatMap(this::manageFindAndGetExistingDocument) //Lookup for existing obj if previous step empty
                    .flatMap(this::manageInitEmptyDocument) //init an empty doc if lookup failed
                    .flatMap(this::manageFromDocInitialize)
                    .map(result->new TaskProcessingResult<>(result.getCtxt(),false))
                    .onErrorResumeNext(throwable->manageError(throwable,origCtxt,false));
        }
        catch (Throwable e){
            return manageError(e,origCtxt,true);
        }
    }

    private final Single<ContextAndDocument> manageFromDocInitialize(ContextAndDocument contextAndDocument){
        return  Single.just(contextAndDocument)
                .flatMap(this::buildAndOrSetDocKey)
                .flatMap(this::manageCleanupBeforeRetry)
                .flatMap(this::manageProcessDocument)
                .flatMap(this::managePostProcessing)
                .flatMap(this::saveContext)
                .flatMap(this::saveDoc);
    }

    protected abstract Single<FindAndGetResult> findAndGetExistingDocument(TaskContext<TJOB, T> taskContext);

    protected abstract Single<ContextAndDocument> initEmptyDocument(TaskContext<TJOB, T> taskContext);

    protected abstract Single<DuplicateUniqueKeyCheckResult> onDuplicateUniqueKey(ContextAndDocument ctxt, DuplicateUniqueKeyDaoException e);

    private Single<ContextAndDocument> manageFindAndGetExistingDocument(final RetryResult retryResult){
        //Retry with attached doc, keep current task data
        if(retryResult.doc!=null){
            return new ContextAndDocument(retryResult.ctxt,retryResult.doc).toSingle();
        }
        else{
            return findAndGetExistingDocument(retryResult.ctxt)
                    .map(findAndGetResult -> {
                        findAndGetResult.ctxt.getInternalTask().setIsCreation(findAndGetResult.doc==null);
                        return new ContextAndDocument(findAndGetResult.ctxt,findAndGetResult.doc);
                    });
        }
    }

    private Single<ContextAndDocument> manageInitEmptyDocument(ContextAndDocument origContextAndDocument){
        if(origContextAndDocument.getDoc()==null){
            return cleanupBeforeRetryInitDocument(origContextAndDocument.getCtxt().getTemporaryReadOnlySessionContext())
                    .flatMap(this::initEmptyDocument)
                    .map(contextAndDocument -> new ContextAndDocument(contextAndDocument.getCtxt().getStandardSessionContext(),contextAndDocument.getDoc()));
        }
        else{
            return origContextAndDocument.toSingle();

        }
    }


    private Single<RetryResult> manageRetry(TaskContext<TJOB,T> origCtxt) {
        if(origCtxt.getInternalTask().getDocKey()!=null){
            return origCtxt.getSession().<TDOC>asyncGet(origCtxt.getInternalTask().getDocKey())
                    .map(foundDoc->new RetryResult(origCtxt,foundDoc))
                    .onErrorResumeNext(throwable -> {
                        if(throwable instanceof DocumentNotFoundException){
                            //Reset
                            origCtxt.getInternalTask().setDocKey(null);
                            origCtxt.getInternalTask().setIsCreation(null);
                            return new RetryResult(origCtxt,null).toSingle();
                        }
                        return Single.error(throwable);
                    });
        }
        else{
            return new RetryResult(origCtxt,null).toSingle();
        }

    }

    private Single<ContextAndDocument> manageDuplicateKey(ContextAndDocument context, DuplicateUniqueKeyDaoException duplicateUniqueKeyDaoException) {
        return onDuplicateUniqueKey(context,duplicateUniqueKeyDaoException)
                .flatMap(checkRes->{
                    if(checkRes.useDuplicateKeyOwnerDoc()){
                        checkRes.getTaskContext().getInternalTask().setIsCreation(false);
                        checkRes.getTaskContext().getInternalTask().setDocKey(null);
                        //checkRes.getTaskContext().getInternalTask().setDocKey(checkRes.getTargetDuplicateKeyDoc().getBaseMeta().getKey());
                        return manageFromDocInitialize(new ContextAndDocument(checkRes.getTaskContext(),checkRes.getTargetDuplicateKeyDoc()));
                    }
                    else{
                        return Single.error(
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
    protected Single<ContextAndDocument> saveDoc(final ContextAndDocument contextAndDocument) {
        return super.saveDoc(contextAndDocument)
                .onErrorResumeNext(throwable -> {
                        Optional<DuplicateUniqueKeyDaoException> duplicateUniqueKeyDaoException = ValidationExceptionUtils.findUniqueKeyException(throwable);
                        if(duplicateUniqueKeyDaoException.isPresent()){
                            return manageDuplicateKey(contextAndDocument,duplicateUniqueKeyDaoException.get());
                        }
                        return Single.error(throwable);
                    });
    }

    private Single<ContextAndDocument> buildAndOrSetDocKey(ContextAndDocument contextAndDocument) {
        if(contextAndDocument.getDoc().getBaseMeta().getState() == CouchbaseDocument.DocumentState.NEW){
            return contextAndDocument.getCtxt().getSession().asyncBuildKey(contextAndDocument.getDoc())
                    .map(docWithKey->{
                        contextAndDocument.getCtxt().getInternalTask().setDocKey(docWithKey.getBaseMeta().getKey());
                        return new ContextAndDocument(contextAndDocument.getCtxt(),docWithKey);
                    });
        }
        else{
            contextAndDocument.getCtxt().getInternalTask().setDocKey(contextAndDocument.getDoc().getBaseMeta().getKey());
            return Single.just(contextAndDocument);
        }
    }

    protected Single<TaskContext<TJOB,T>> cleanupBeforeRetryInitDocument(TaskContext<TJOB, T> ctxt) {
        return Single.just(ctxt);
    }


    protected final Single<ContextAndDocument> buildContextAndDocumentObservable(TaskContext<TJOB, T> ctxt, TDOC doc){
        return Single.just(new ContextAndDocument(ctxt,doc));
    }


    public class RetryResult {
        private final TaskContext<TJOB,T> ctxt;
        private final TDOC doc;

        public RetryResult(TaskContext<TJOB, T> ctxt, TDOC doc) {
            this.ctxt = ctxt;
            this.doc = doc;
        }

        public Single<RetryResult> toSingle(){
            return Single.just(this);
        }
    }

    public class FindAndGetResult {
        private final TaskContext<TJOB,T> ctxt;
        private final TDOC doc;

        public FindAndGetResult(TaskContext<TJOB, T> ctxt, TDOC doc) {
            this.ctxt = ctxt;
            this.doc = doc;
        }

        public FindAndGetResult(TaskContext<TJOB, T> ctxt) {
            this(ctxt,null);
        }

        public TaskContext<TJOB, T> getCtxt() {
            return ctxt;
        }

        public TDOC getDoc() {
            return doc;
        }

        public Single<FindAndGetResult> toSingle(){
            return Single.just(this);
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
