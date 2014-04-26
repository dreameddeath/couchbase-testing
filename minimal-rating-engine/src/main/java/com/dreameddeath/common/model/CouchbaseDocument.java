package com.dreameddeath.common.model;

import java.util.HashSet;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import com.dreameddeath.common.storage.CouchbaseConstants.DocumentFlag;
import com.dreameddeath.common.dao.CouchbaseSession;

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
    
    public final CouchbaseSession getSession(){ return _session; }
    public final void setSession(CouchbaseSession session){ _session = session; }
    
    public final String getKey(){ return _key.get(); }
    public final void setKey(String key){ _key.set(key); updateReverseLinkKeys(); }
    
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
    
    //Called by setKey
    private void updateReverseLinkKeys(){ for(CouchbaseDocumentLink lnk: _reverseLinks){ lnk.setKey(_key.get()); } }
    
    
    public void setStateDirty(){
        if(_state.equals(State.SYNC)){
            _state=State.DIRTY;
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
