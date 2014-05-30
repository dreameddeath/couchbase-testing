package com.dreameddeath.common.service;

import com.dreameddeath.common.model.process.AbstractDocumentUpdateTask;
import com.dreameddeath.common.model.process.CouchbaseDocumentAttachedTaskRef;

/**
 * Created by ceaj8230 on 21/05/2014.
 */
public abstract class AbstractDocumentUpdateTaskProcessingServiceImpl<T extends AbstractDocumentUpdateTask> extends AbstractTaskProcessingServiceImpl<T> {
    //public abstract void init(AbstractTask task);

    @Override
    public void cleanup(T task){
        task.getDocument().cleanupAttachedTaskRef(task);
        task.getDocument().save();
    }

    @Override
    public void process(T task){
        CouchbaseDocumentAttachedTaskRef reference = task.getDocument().getAttachedTaskRef(task);
        if(reference==null){
            CouchbaseDocumentAttachedTaskRef resultRef = processDocument(task);
            task.getDocument().addAttachedTaskRef(resultRef);
            task.getDocument().save();
        }
    }

    public abstract CouchbaseDocumentAttachedTaskRef processDocument(T task);
}
