package com.dreameddeath.core.process.model;

import com.dreameddeath.core.process.exception.DuplicateAttachedTaskException;

/**
 * Created by CEAJ8230 on 16/05/2015.
 */
public interface IDocumentWithLinkedTasks {
    CouchbaseDocumentAttachedTaskRef getAttachedTaskRef(String jobKey, String taskId);
    void addAttachedTaskRef(CouchbaseDocumentAttachedTaskRef task)throws DuplicateAttachedTaskException;
    CouchbaseDocumentAttachedTaskRef getAttachedTaskRef(AbstractTask task);
    void cleanupAttachedTaskRef(AbstractTask task);
}
