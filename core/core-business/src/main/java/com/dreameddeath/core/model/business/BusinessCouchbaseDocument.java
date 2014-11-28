package com.dreameddeath.core.model.business;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.exception.model.DuplicateAttachedTaskException;
import com.dreameddeath.core.model.IHasUniqueKeysRef;
import com.dreameddeath.core.model.IVersionedDocument;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.process.AbstractTask;
import com.dreameddeath.core.model.process.CouchbaseDocumentAttachedTaskRef;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.SetProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.HashSetProperty;
import com.dreameddeath.core.session.ICouchbaseSession;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonTypeInfo(use= JsonTypeInfo.Id.CUSTOM, include= JsonTypeInfo.As.PROPERTY, property="@t",visible = true)
@JsonTypeIdResolver(CouchbaseDocumentTypeIdResolver.class)
public abstract class BusinessCouchbaseDocument extends CouchbaseDocument implements IHasUniqueKeysRef,IVersionedDocument {
    @JsonSetter("@t")
    public void setDocumentFullVersionId(String typeId){getMeta().setTypeId(typeId);}
    public String getDocumentFullVersionId(){return getMeta().getTypeId();}

    @DocumentProperty("attachedTasks")
    private ListProperty<CouchbaseDocumentAttachedTaskRef> _attachedTasks = new ArrayListProperty<CouchbaseDocumentAttachedTaskRef>(BusinessCouchbaseDocument.this);
    @DocumentProperty("docRevision")
    private Long _revision = 0L;
    @DocumentProperty("docLastModDate")
    private DateTime _lastModificationDate;
    /**
     *  docUniqKeys : List of uniqueness Keys attached to this document
     */
    @DocumentProperty("docUniqKeys")
    private SetProperty<String> _docUniqKeys = new HashSetProperty<String>(BusinessCouchbaseDocument.this);
    private Set<String> _inDbUniqKeys = new HashSet<String>();


    public final Long getDocRevision(){ return _revision; }
    public final void setDocRevision(Long rev){ _revision=rev; }
    public final Long incDocRevision(ICouchbaseSession session){ return (++_revision); }

    public final DateTime getDocLastModDate(){ return _lastModificationDate; }
    public final void setDocLastModDate(DateTime date){ _lastModificationDate=date; }
    public final void updateDocLastModDate(ICouchbaseSession session){ _lastModificationDate=session.getCurrentDate(); }

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

    public void addAttachedTaskRef(CouchbaseDocumentAttachedTaskRef task)throws DuplicateAttachedTaskException{
        if(getAttachedTaskRef(task.getJobKey(), task.getTaskId())!=null){
            throw new DuplicateAttachedTaskException(this,task.getJobKey(),task.getTaskId());
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


    public BusinessCouchbaseDocument(){
        super(null);
        setBaseMeta(BusinessCouchbaseDocument.this.new MetaInfo());
    }

    public MetaInfo getMeta(){
        return (MetaInfo) getBaseMeta();
    }

    public class MetaInfo extends BaseMetaInfo {
        private String _typeId;

        public void setTypeId(String typeId){_typeId = typeId;}
        public String getTypeId(){return _typeId;}

        private Collection<BusinessCouchbaseDocumentLink> _reverseLinks=new HashSet<BusinessCouchbaseDocumentLink>();

        public void addReverseLink(BusinessCouchbaseDocumentLink lnk){ _reverseLinks.add(lnk); }
        public void removeReverseLink(BusinessCouchbaseDocumentLink lnk){ _reverseLinks.remove(lnk); }


        @Override
        public void setStateDirty(){
            super.setStateDirty();
            for(BusinessCouchbaseDocumentLink link: _reverseLinks){
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
