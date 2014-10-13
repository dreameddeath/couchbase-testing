package com.dreameddeath.core.model.document;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.common.BaseCouchbaseDocument;
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
    private ListProperty<CouchbaseDocumentAttachedTaskRef> _attachedTasks = new ArrayListProperty<CouchbaseDocumentAttachedTaskRef>(CouchbaseDocument.this);
    @DocumentProperty("docRevision")
    private Long _revision = 0L;
    @DocumentProperty("docLastModDate")
    private DateTime _lastModificationDate;
    /**
     *  docUniqKeys : List of uniqueness Keys attached to this document
     */
    @DocumentProperty("docUniqKeys")
    private SetProperty<String> _docUniqKeys = new HashSetProperty<String>(CouchbaseDocument.this);
    private Set<String> _inDbUniqKeys = new HashSet<String>();


    public final Long getDocRevision(){ return _revision; }
    public final void setDocRevision(Long rev){ _revision=rev; }
    public final Long incDocRevision(){ return (++_revision); }

    public final DateTime getDocLastModDate(){ return _lastModificationDate; }
    public final void setDocLastModDate(DateTime date){ _lastModificationDate=date; }
    public final void updateDocLastModDate(){ _lastModificationDate=DateTime.now(); }

    // DocUniqKeys Accessors
    public final Set<String> getDocUniqKeys() { return _docUniqKeys.get(); }
    public final void setDocUniqKeys(Set<String> vals) { _docUniqKeys.set(vals); }
    public final boolean addDocUniqKeys(String key){ return _docUniqKeys.add(key); }
    public final boolean removeDocUniqKeys(String key){ return _docUniqKeys.remove(key); }


    protected void syncKeyWithDb(){
        _inDbUniqKeys.clear();
        _inDbUniqKeys.addAll(_docUniqKeys.get());
        _docUniqKeys.clear();
    }

    public Set<String> getToBeDeletedUniqueKeys(){
        Set<String> toRemoveKeyList=new HashSet<String>(_inDbUniqKeys);
        toRemoveKeyList.addAll(_docUniqKeys.get());
        return toRemoveKeyList;
    }

    public Set<String> getRemovedUniqueKeys(){
        Set<String> removed=new HashSet<String>(_inDbUniqKeys);
        removed.removeAll(_docUniqKeys.get());
        return removed;
    }

    public List<CouchbaseDocumentAttachedTaskRef> getAttachedTasks(){return _attachedTasks.get();}
    public void setAttachedTasks(Collection<CouchbaseDocumentAttachedTaskRef> tasks){
        _attachedTasks.set(tasks);
    }

    public CouchbaseDocumentAttachedTaskRef getAttachedTaskRef(String jobKey,String taskId){
        for(CouchbaseDocumentAttachedTaskRef taskRef: _attachedTasks) {
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
        _attachedTasks.add(task);
    }

    public CouchbaseDocumentAttachedTaskRef getAttachedTaskRef(AbstractTask task){
        for(CouchbaseDocumentAttachedTaskRef taskRef: _attachedTasks){
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
        for(CouchbaseDocumentAttachedTaskRef taskRef: _attachedTasks){
            if(taskRef.isForTask(task)) {
                result = taskRef;
                break;
            }
        }
        if(result!=null){
            _attachedTasks.remove(result);
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
        private Collection<CouchbaseDocumentLink> _reverseLinks=new HashSet<CouchbaseDocumentLink>();

        public void addReverseLink(CouchbaseDocumentLink lnk){ _reverseLinks.add(lnk); }
        public void removeReverseLink(CouchbaseDocumentLink lnk){ _reverseLinks.remove(lnk); }

        @Override
        public void setStateDirty(){
            super.setStateDirty();
            for(CouchbaseDocumentLink link: _reverseLinks){
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
