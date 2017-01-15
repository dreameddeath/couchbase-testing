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

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.tasks.ChildDocumentCreateTask;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.context.TaskProcessingResult;
import io.reactivex.Single;

/**
 * Created by Christophe Jeunesse on 23/02/2016.
 */
public abstract class ChildDocumentCreateTaskProcessingService<TJOB extends AbstractJob,TDOC extends CouchbaseDocument,TPARENT extends CouchbaseDocument,T extends ChildDocumentCreateTask<TDOC,TPARENT>> extends DocumentCreateTaskProcessingService<TJOB,TDOC,T> {
    @Override
    final public Single<TaskProcessingResult<TJOB,T>> postprocess(TaskContext<TJOB,T> context) {
        try {
            return Single.zip(
                            context.getInternalTask().getDocument(context.getSession()),
                            context.getInternalTask().getParentDocument(context.getSession()),
                            (child,parent)->new ChildParentAndContext(context,parent,child)
                    )
                    .flatMap(this::manageParentUpdate)
                    .map(result->new TaskProcessingResult<>(result.getCtxt(),true))
                    .onErrorResumeNext(throwable->this.manageError(throwable,context));
        }
        catch(Throwable e) {
            return Single.error(new TaskExecutionException(context,"Unexpected error",e));
        }
    }

    private Single<TaskProcessingResult<TJOB, T>> manageError(Throwable throwable, TaskContext<TJOB, T> context) {
        if(throwable instanceof TaskExecutionException) {
            return Single.error(throwable);
        }
        else{
            return Single.error(new TaskExecutionException(context,"Error during execution",throwable));
        }
    }

    private Single<ChildParentAndContext> manageParentUpdate(ChildParentAndContext childParentAndContext) {
        if (needParentUpdate(childParentAndContext.getParent(), childParentAndContext.getChild())) {
            updateParent(childParentAndContext.getParent(), childParentAndContext.getChild());
            return childParentAndContext.getCtxt().getSession()
                    .asyncSave(childParentAndContext.getParent())
                    .map(savedParent -> new ChildParentAndContext(childParentAndContext.getCtxt(), savedParent, childParentAndContext.getChild()));
        }
        else {
            return Single.just(childParentAndContext);
        }
    }


    protected abstract boolean needParentUpdate(TPARENT parent,TDOC child);
    protected abstract void updateParent(TPARENT parent,TDOC child);

    protected class ChildParentAndContext{
        private final TaskContext<TJOB,T> ctxt;
        private final TPARENT parent;
        private final TDOC child;

        public ChildParentAndContext(TaskContext<TJOB, T> ctxt, TPARENT parent, TDOC child) {
            this.ctxt = ctxt;
            this.parent = parent;
            this.child = child;
        }

        public TaskContext<TJOB, T> getCtxt() {
            return ctxt;
        }

        public TPARENT getParent() {
            return parent;
        }

        public TDOC getChild() {
            return child;
        }
    }
}
