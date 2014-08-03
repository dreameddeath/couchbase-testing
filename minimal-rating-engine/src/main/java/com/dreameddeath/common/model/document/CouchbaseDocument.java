package com.dreameddeath.common.model.document;

import com.dreameddeath.common.annotation.DocumentProperty;
import com.dreameddeath.common.dao.CouchbaseSession;
import com.dreameddeath.common.model.process.AbstractTask;
import com.dreameddeath.common.model.process.CouchbaseDocumentAttachedTaskRef;
import com.dreameddeath.common.model.property.ImmutableProperty;
import com.dreameddeath.common.model.property.Property;
import com.dreameddeath.common.storage.CouchbaseConstants.DocumentFlag;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonAutoDetect(getterVisibility=Visibility.NONE,fieldVisibility=Visibility.NONE)
public abstract class CouchbaseDocument extends CouchbaseDocumentElement {
    private CouchbaseSession _session;
    private ImmutableProperty<String> _key=new ImmutableProperty<String>(CouchbaseDocument.this);
    private Long   _cas;
    private Boolean _isLocked;
    private Integer _dbDocSize;
    private Collection<DocumentFlag> _documentFlags=new HashSet<DocumentFlag>();
    private Collection<CouchbaseDocumentLink> _reverseLinks=new HashSet<CouchbaseDocumentLink>();
    private State _state=State.NEW;

    @DocumentProperty("attachedTasks")
    private List<CouchbaseDocumentAttachedTaskRef> _attachedTasks = new CouchbaseDocumentArrayList<CouchbaseDocumentAttachedTaskRef>(CouchbaseDocument.this);
    @DocumentProperty("docRevision")
    private Long _revision = 0L;
    @DocumentProperty("docLastModDate")
    private DateTime _lastModificationDate;


    public final CouchbaseSession getSession(){ return _session; }
    public final void setSession(CouchbaseSession session){ _session = session; }


    public final Long getDocRevision(){ return _revision; }
    public final void setDocRevision(Long rev){ _revision=rev; }
    public final Long incDocRevision(){ return (++_revision); }

    public final DateTime getDocLastModDate(){ return _lastModificationDate; }
    public final void setDocLastModDate(DateTime date){ _lastModificationDate=date; }
    public final void updateDocLastModDate(){ _lastModificationDate=DateTime.now(); }


    public final String getKey(){ return _key.get(); }
    public final void setKey(String key){ _key.set(key);}
    
    public final Long getCas(){ return _cas; }
    public final void setCas(Long cas){ this._cas = cas; }
    
    public final Boolean getIsLocked(){ return _isLocked; }
    public final void setIsLocked(Boolean isLocked){ this._isLocked = isLocked; }
    
    public final Integer getDbDocSize(){ return _dbDocSize; }
    public final void setDbDocSize(Integer docSize){ this._dbDocSize = docSize; }
    
    public final Collection<DocumentFlag> getDocumentFlags(){ return _documentFlags; }
    public final Integer getDocumentEncodedFlags(){ return DocumentFlag.pack(_documentFlags); }
    public final void setDocumentEncodedFlags(Integer encodedFlags){ _documentFlags.clear(); _documentFlags.addAll(DocumentFlag.unPack(encodedFlags)); }
    public final void setDocumentFlags(Collection<DocumentFlag> flags){ _documentFlags.clear(); _documentFlags.addAll(flags); }
    public final void addDocumentEncodedFlags(Integer encodedFlags){ _documentFlags.addAll(DocumentFlag.unPack(encodedFlags)); }
    public final void addDocumentFlag(DocumentFlag flag){ _documentFlags.add(flag); }
    public final void addDocumentFlags(Collection<DocumentFlag> flags){ _documentFlags.addAll(flags); }
    public final void removeDocumentFlag(DocumentFlag flag){ _documentFlags.remove(flag); }
    public final void removeDocumentFlags(Collection<DocumentFlag> flags){_documentFlags.remove(flags); }
    public boolean hasDocumentFlag(DocumentFlag flag){ return _documentFlags.contains(flag); }
    
    public void addReverseLink(CouchbaseDocumentLink lnk){ _reverseLinks.add(lnk); }
    public void removeReverseLink(CouchbaseDocumentLink lnk){ _reverseLinks.remove(lnk); }
    
    public void setStateDirty(){
        if(_state.equals(State.SYNC)){
            _state=State.DIRTY;
        }
        for(CouchbaseDocumentLink link: _reverseLinks){
            link.syncFields();
        }
    }
    
    public void setStateSync(){ _state = State.SYNC; }
    public State getState(){ return _state; }
    
    
    public boolean equals(CouchbaseDocument doc){
        if     (doc == null){ return false;}
        else if(doc == this){ return true; }
        else if(_key!=null) { return _key.equals(doc._key); }
        else                { return false; }
    }


    public void save(){
        if(_state.equals(State.NEW)) {
            this.getSession().create(this);
        }
        else{
            this.getSession().update(this);
        }
    }

    public List<CouchbaseDocumentAttachedTaskRef> getAttachedTasks(){return Collections.unmodifiableList(_attachedTasks);}
    public void setAttachedTasks(Collection<CouchbaseDocumentAttachedTaskRef> tasks){
        _attachedTasks.clear();
        _attachedTasks.addAll(tasks);
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

    @Override
    public String toString(){
        return 
            "key   : "+_key+",\n"+
            "cas   : "+_cas+",\n"+
            "lock  : "+_isLocked+",\n"+
            "size  : "+_dbDocSize+",\n"+
            "state  : "+_state+",\n"+
            "flags : "+_documentFlags.toString();
    }
    
    public enum State{
        NEW,
        DIRTY,
        SYNC;
    }
    
    @Override
    public boolean validate(){
        boolean result=super.validate();
        if(_key.get()==null){
            ///TODO log
            result&=false;
        }
        return result;
    }
}
