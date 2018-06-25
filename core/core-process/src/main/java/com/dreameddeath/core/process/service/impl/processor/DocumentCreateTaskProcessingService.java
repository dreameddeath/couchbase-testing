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

package com.dreameddeath.core.process.service.impl.processor;

import com.dreameddeath.core.couchbase.exception.DocumentNotFoundException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.process.exception.AlreadyCreatedDocumentException;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.notification.CreateDocumentTaskEvent;
import com.dreameddeath.core.process.model.v1.tasks.DocumentCreateTask;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.context.TaskNotificationBuildResult;
import com.dreameddeath.core.process.service.context.TaskProcessingResult;
import io.reactivex.Single;

/**
 * Created by Christophe Jeunesse on 23/11/2014.
 */
public abstract class DocumentCreateTaskProcessingService<TJOB extends AbstractJob,TDOC extends CouchbaseDocument,T extends DocumentCreateTask<TDOC>> extends StandardTaskProcessingService<TJOB,T> {

    @Override
    public final Single<TaskProcessingResult<TJOB,T>> process(TaskContext<TJOB,T> origCtxt) {
        try {
            return Single.just(origCtxt)
                    .flatMap(this::manageRetry)
                    .doOnError(throwable -> logError(origCtxt,"process.manageRetry",throwable))
                    .map(this::toTemporarySession)
                    .flatMap(this::buildDocument)
                    .doOnError(throwable -> logError(origCtxt,"process.buildDocument",throwable))
                    .map(this::toStandardSession)
                    .flatMap(this::buildAndSetDocKey)
                    .doOnError(throwable -> logError(origCtxt,"process.buildAndSetDocKey",throwable))
                    .flatMap(this::saveContext)
                    .doOnError(throwable -> logError(origCtxt,"process.saveContext",throwable))
                    .flatMap(this::saveDoc)
                    .doOnError(throwable -> logError(origCtxt,"process.saveDoc",throwable))
                    .map(result -> new TaskProcessingResult<>(result.ctxt, false))
                    .onErrorResumeNext(throwable -> manageError(throwable, origCtxt,false));
        }
        catch(Throwable e){
            return manageError(e,origCtxt,true);
        }
    }

    @Override
    public Single<TaskNotificationBuildResult<TJOB, T>> buildNotifications(TaskContext<TJOB, T> ctxt) {
        CreateDocumentTaskEvent event = new CreateDocumentTaskEvent();
        event.setDocKey(ctxt.getInternalTask().getDocKey());

        return super.buildNotifications(ctxt)
                .flatMap(parentRes->TaskNotificationBuildResult.build(parentRes, TaskNotificationBuildResult.DuplicateMode.ERROR, event));
    }

    private Single<TaskProcessingResult<TJOB,T>> manageError(Throwable e, TaskContext<TJOB,T> origCtxt,boolean isPreparation){
        if(e instanceof AlreadyCreatedDocumentException){
            AlreadyCreatedDocumentException processedObservableException = (AlreadyCreatedDocumentException) e;
            return TaskProcessingResult.build(processedObservableException.getCtxt(),false);
        }
        else if(e instanceof TaskExecutionException) {
            return Single.error(e);
        }
        else{
            return Single.error(new TaskExecutionException(origCtxt,isPreparation?"Error during setup":"Error during execution",e));
        }
    }

    private TaskContext<TJOB, T> toTemporarySession(TaskContext<TJOB, T> taskContext) {
        return taskContext.getTemporaryReadOnlySessionContext();
    }

    private ContextAndDocument toStandardSession(ContextAndDocument contextAndDocument) {
        return new ContextAndDocument(contextAndDocument.ctxt.getStandardSessionContext(),contextAndDocument.doc);
    }

    private Single<TaskContext<TJOB,T>> manageRetry(TaskContext<TJOB, T> origCtxt) {
        if(origCtxt.getInternalTask().getDocKey()!=null) {
            return origCtxt.getSession().asyncGet(origCtxt.getInternalTask().getDocKey())
                    .flatMap(foundDoc->Single.<TaskContext<TJOB,T>>error(new AlreadyCreatedDocumentException(origCtxt,foundDoc)))
                    .onErrorResumeNext(throwable -> {
                        if(! (throwable instanceof DocumentNotFoundException)){
                            return Single.error(throwable);
                        }
                        return manageCleanup(origCtxt);
                    });
        }
        else {
            return Single.just(origCtxt);
        }
    }

    private Single<TaskContext<TJOB, T>> manageCleanup(TaskContext<TJOB, T> origCtxt) {
        return cleanupBeforeRetryBuildDocument(origCtxt)
                .map(newCtxt->{
                    newCtxt.getInternalTask().setDocKey(null);
                    return newCtxt;
                })
                .flatMap(TaskContext::asyncSave);
    }

    private Single<ContextAndDocument> saveContext(final ContextAndDocument contextAndDocument) {
        return contextAndDocument.ctxt
                .asyncSave()
                .map(savedContext->new ContextAndDocument(savedContext,contextAndDocument.doc));
    }

    private Single<ContextAndDocument> saveDoc(final ContextAndDocument contextAndDocument) {
        return contextAndDocument.ctxt.getSession()
                .asyncSave(contextAndDocument.doc)
                .map(savedDoc->new ContextAndDocument(contextAndDocument.ctxt,savedDoc));
    }

    private Single<ContextAndDocument> buildAndSetDocKey(ContextAndDocument contextAndDocument) {
        return contextAndDocument.ctxt
                .getSession()
                .asyncBuildKey(contextAndDocument.doc)
                .map(docWithKey->{
                    contextAndDocument.ctxt.getInternalTask().setDocKey(docWithKey.getBaseMeta().getKey());
                    return new ContextAndDocument(contextAndDocument.ctxt,docWithKey);
                });
    }

    protected Single<TaskContext<TJOB,T>> cleanupBeforeRetryBuildDocument(TaskContext<TJOB, T> ctxt) {
        return Single.just(ctxt);
    }

    protected abstract Single<ContextAndDocument> buildDocument(TaskContext<TJOB,T> ctxt);


    protected final Single<ContextAndDocument> buildContextAndDocumentObservable(TaskContext<TJOB, T> ctxt, TDOC doc){
        return Single.just(new ContextAndDocument(ctxt,doc));
    }

    protected class ContextAndDocument{
        private final TaskContext<TJOB,T> ctxt;
        private final TDOC doc;

        protected ContextAndDocument(TaskContext<TJOB, T> ctxt, TDOC doc) {
            this.ctxt = ctxt;
            this.doc = doc;
        }

        public Single<ContextAndDocument> toSingle(){
            return Single.just(this);
        }
    }
}