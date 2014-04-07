package com.dreameddeath.common.model;

import java.util.HashSet;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.dreameddeath.common.storage.CouchbaseConstants.DocumentFlag;

public abstract class CouchbaseDocument{
    private String _key;
    private Long   _cas;
    private Boolean _isLocked;
    private Integer _dbDocSize;
    private Collection<DocumentFlag> _documentFlags=new HashSet<DocumentFlag>();
    private Collection<CouchbaseDocumentLink> _reverseLinks=new HashSet<CouchbaseDocumentLink>();
    
    @JsonIgnore
    public final String getKey(){ return _key; }
    public final void setKey(String key){ this._key = key; updateReverseLinkKeys(); }

    @JsonIgnore
    public final Long getCas(){ return _cas; }
    public final void setCas(Long cas){ this._cas = cas; }
    
    @JsonIgnore
    public final Boolean getIsLocked(){ return _isLocked; }
    public final void setIsLocked(Boolean isLocked){ this._isLocked = isLocked; }

    @JsonIgnore
    public final Integer getDbDocSize(){ return _dbDocSize; }
    public final void setDbDocSize(Integer docSize){ this._dbDocSize = docSize; }

    @JsonIgnore
    public final Collection<DocumentFlag> getDocumentFlags(){ return _documentFlags; }
    
    @JsonIgnore
    public final Integer getDocumentEncodedFlags(){ return DocumentFlag.pack(_documentFlags); }
    public final void setDocumentEncodedFlags(Integer encodedFlags){ _documentFlags.clear(); _documentFlags.addAll(DocumentFlag.unPack(encodedFlags)); }
    public final void setDocumentFlags(Collection<DocumentFlag> flags){ _documentFlags.clear(); _documentFlags.addAll(flags); }
    public final void addDocumentEncodedFlags(Integer encodedFlags){ _documentFlags.addAll(DocumentFlag.unPack(encodedFlags)); }
    public final void addDocumentFlag(DocumentFlag flag){ _documentFlags.add(flag); }
    public final void addDocumentFlags(Collection<DocumentFlag> flags){ _documentFlags.addAll(flags); }
    public final void removeDocumentFlag(DocumentFlag flag){ _documentFlags.remove(flag); }
    public final void removeDocumentFlags(Collection<DocumentFlag> flags){_documentFlags.remove(flags); }
    public boolean hasDocumentFlag(DocumentFlag flag){ return _documentFlags.contains(flag); }
    
    @JsonIgnore
    public void addReverseLink(CouchbaseDocumentLink lnk){ _reverseLinks.add(lnk); }
    
    //Called by setKey
    private void updateReverseLinkKeys(){ for(CouchbaseDocumentLink lnk: _reverseLinks){ lnk.setKey(_key); } }
    
    @Override
    public String toString(){
        return 
            "key   : "+_key+",\n"+
            "cas   : "+_cas+",\n"+
            "lock  : "+_isLocked+",\n"+
            "size  : "+_dbDocSize+",\n"+
            "flags : "+_documentFlags.toString();
    }
}
