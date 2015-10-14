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

package com.dreameddeath.core.model.business;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.BaseCouchbaseDocument;
import com.dreameddeath.core.model.process.CouchbaseDocumentAttachedTaskRef;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.SetProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.HashSetProperty;
import com.dreameddeath.core.process.common.AbstractTask;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public abstract class CouchbaseDocument extends BaseCouchbaseDocument {
    @DocumentProperty("attachedTasks")
    private ListProperty<CouchbaseDocumentAttachedTaskRef> attachedTasks = new ArrayListProperty<CouchbaseDocumentAttachedTaskRef>(CouchbaseDocument.this);
    @DocumentProperty("docRevision")
    private Long revision = 0L;
    @DocumentProperty("docLastModDate")
    private DateTime lastModificationDate;
    /**
     *  docUniqKeys : List of uniqueness Keys attached to this document
     */
    @DocumentProperty("docUniqKeys")
    private SetProperty<String> docUniqKeys = new HashSetProperty<String>(CouchbaseDocument.this);
    private Set<String> inDbUniqKeys = new HashSet<String>();


    public final Long getDocRevision(){ return revision; }
    public final void setDocRevision(Long rev){ revision=rev; }
    public final Long incDocRevision(){ return (++revision); }

    public final DateTime getDocLastModDate(){ return lastModificationDate; }
    public final void setDocLastModDate(DateTime date){ lastModificationDate=date; }
    public final void updateDocLastModDate(){ lastModificationDate=DateTime.now(); }

    // DocUniqKeys Accessors
    public final Set<String> getDocUniqKeys() { return docUniqKeys.get(); }
    public final void setDocUniqKeys(Set<String> vals) { docUniqKeys.set(vals); }
    public final boolean addDocUniqKeys(String key){ return docUniqKeys.add(key); }
    public final boolean removeDocUniqKeys(String key){ return docUniqKeys.remove(key); }


    protected void syncKeyWithDb(){
        inDbUniqKeys.clear();
        inDbUniqKeys.addAll(docUniqKeys.get());
        docUniqKeys.clear();
    }

    public Set<String> getToBeDeletedUniqueKeys(){
        Set<String> toRemoveKeyList=new HashSet<String>(inDbUniqKeys);
        toRemoveKeyList.addAll(docUniqKeys.get());
        return toRemoveKeyList;
    }

    public Set<String> getRemovedUniqueKeys(){
        Set<String> removed=new HashSet<String>(inDbUniqKeys);
        removed.removeAll(docUniqKeys.get());
        return removed;
    }

    public List<CouchbaseDocumentAttachedTaskRef> getAttachedTasks(){return attachedTasks.get();}
    public void setAttachedTasks(Collection<CouchbaseDocumentAttachedTaskRef> tasks){
        attachedTasks.set(tasks);
    }

    public CouchbaseDocumentAttachedTaskRef getAttachedTaskRef(String jobKey,String taskId){
        for(CouchbaseDocumentAttachedTaskRef taskRef: attachedTasks) {
            if (jobKey.equals(taskRef.getJobKey()) && (taskId.equals(taskRef.getTaskId()))) {
                return taskRef;
            }
        }
        return null;
    }

    public void addAttachedTaskRef(CouchbaseDocumentAttachedTaskRef task){
        if(getAttachedTaskRef(task.getJobKey(), task.getTaskId())!=null){
            ///TODO throw error
        }
        attachedTasks.add(task);
    }

    public CouchbaseDocumentAttachedTaskRef getAttachedTaskRef(AbstractTask task){
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
    public void cleanupAttachedTaskRef(AbstractTask task){
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


    public CouchbaseDocument(){
        super(null);
        setBaseMeta(CouchbaseDocument.this.new MetaInfo());
    }

    public MetaInfo getMeta(){
        return (MetaInfo) getBaseMeta();
    }

    public class MetaInfo extends BaseMetaInfo {
        private Collection<CouchbaseDocumentLink> reverseLinks=new HashSet<CouchbaseDocumentLink>();

        public void addReverseLink(CouchbaseDocumentLink lnk){ reverseLinks.add(lnk); }
        public void removeReverseLink(CouchbaseDocumentLink lnk){ reverseLinks.remove(lnk); }

        @Override
        public void setStateDirty(){
            super.setStateDirty();
            for(CouchbaseDocumentLink link: reverseLinks){
                link.syncFields();
            }
        }

        @Override
        public void setStateDeleted(){
            //syncKeyWithDb(); voluntary to key in db values up to date
            super.setStateDeleted();
        }

        @Override
        public void setStateSync(){
            syncKeyWithDb();
            super.setStateSync();
        }
    }
}
