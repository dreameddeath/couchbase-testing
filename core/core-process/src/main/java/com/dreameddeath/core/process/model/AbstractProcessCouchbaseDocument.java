/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.process.model;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.process.exception.DuplicateAttachedTaskException;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 04/01/2016.
 */
public class AbstractProcessCouchbaseDocument extends CouchbaseDocument implements IDocumentWithLinkedTasks {

    public AbstractProcessCouchbaseDocument() {
        super();
    }

    public AbstractProcessCouchbaseDocument(BaseMetaInfo meta) {
        super(meta);
    }

    @DocumentProperty("attachedTasks")
    private ListProperty<CouchbaseDocumentAttachedTaskRef> attachedTasks = new ArrayListProperty<>(AbstractProcessCouchbaseDocument.this);

    public final List<CouchbaseDocumentAttachedTaskRef> getAttachedTasks(){return attachedTasks.get();}
    public final void setAttachedTasks(Collection<CouchbaseDocumentAttachedTaskRef> tasks){
        attachedTasks.set(tasks);
    }

    @Override
    public final CouchbaseDocumentAttachedTaskRef getAttachedTaskRef(UUID jobKey, String taskId){
        for(CouchbaseDocumentAttachedTaskRef taskRef: attachedTasks) {
            if (jobKey.equals(taskRef.getJobUid()) && (taskId.equals(taskRef.getTaskId()))) {
                return taskRef;
            }
        }
        return null;
    }
    @Override
    public final void addAttachedTaskRef(CouchbaseDocumentAttachedTaskRef task)throws DuplicateAttachedTaskException {
        if(getAttachedTaskRef(task.getJobUid(), task.getTaskId())!=null){
            throw new DuplicateAttachedTaskException(this,task.getJobUid().toString(),task.getTaskId());
        }
        attachedTasks.add(task);
    }

    @Override
    public final CouchbaseDocumentAttachedTaskRef getAttachedTaskRef(AbstractTask task){
        for(CouchbaseDocumentAttachedTaskRef taskRef: attachedTasks){
            if(taskRef.isForTask(task)){
                return taskRef;
            }
        }
        return null;
    }

    /**
     * Detach the task from the document as it has been processed
     *
     * @param task the task to be detached
     */
    @Override
    public final void cleanupAttachedTaskRef(AbstractTask task){
        CouchbaseDocumentAttachedTaskRef result=null;
        for(CouchbaseDocumentAttachedTaskRef taskRef: attachedTasks){
            if(taskRef.isForTask(task)) {
                result = taskRef;
                break;
            }
        }
        if(result!=null){
            attachedTasks.remove(result);
        }
    }

}
