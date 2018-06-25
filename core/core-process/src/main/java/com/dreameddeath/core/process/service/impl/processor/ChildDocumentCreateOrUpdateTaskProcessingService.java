package com.dreameddeath.core.process.service.impl.processor;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.IDocumentWithLinkedTasks;
import com.dreameddeath.core.process.model.v1.tasks.ChildDocumentCreateOrUpdateTask;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.context.TaskProcessingResult;
import io.reactivex.Single;

public abstract class ChildDocumentCreateOrUpdateTaskProcessingService<TJOB extends AbstractJob,TDOC extends  CouchbaseDocument & IDocumentWithLinkedTasks,TPARENT extends CouchbaseDocument,T extends ChildDocumentCreateOrUpdateTask<TDOC,TPARENT>> extends DocumentCreateOrUpdateTaskProcessingService<TJOB,TDOC,T> {
    @Override
    public Single<TaskProcessingResult<TJOB,T>> preprocess(TaskContext<TJOB,T> context){
        if(context.getInternalTask().getParentDocKey()==null){
            return Single.error(new TaskExecutionException(context,"The parent key is null"));
        }
        else{
            return super.preprocess(context);
        }
    }


    @Override
    final public Single<TaskProcessingResult<TJOB,T>> postprocess(TaskContext<TJOB,T> context) {
        try {
            if(context.getInternalTask().getIsCreation()) {
                return Single.zip(
                            context.getInternalTask().getDocument(context.getSession()),
                            context.getInternalTask().getParentDocument(context.getSession()),
                            (child, parent) -> new ChildParentAndContext(context, parent, child)
                        )
                        .flatMap(this::manageParentUpdate)
                        .doOnError(throwable -> logError(context,"postprocess.manageParentUpdate",throwable))
                        .map(result -> new TaskProcessingResult<>(result.getCtxt(), true))
                        .onErrorResumeNext(throwable -> this.manageError(throwable, context));
            }
            else{
                return TaskProcessingResult.build(context,false);
            }
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
