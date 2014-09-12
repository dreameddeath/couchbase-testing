package com.dreameddeath.core.model.common;

import com.dreameddeath.core.dao.CouchbaseSession;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.storage.CouchbaseConstants;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by ceaj8230 on 11/09/2014.
 */
public class BaseCouchbaseDocument extends BaseCouchbaseDocumentElement {
    private CouchbaseSession _session;
    private ImmutableProperty<String> _key=new ImmutableProperty<String>(BaseCouchbaseDocument.this);
    private Long   _cas;
    private Boolean _isLocked;
    private Integer _dbDocSize;
    private Collection<CouchbaseConstants.DocumentFlag> _documentFlags=new HashSet<CouchbaseConstants.DocumentFlag>();
    private DocumentState _docState = DocumentState.NEW;

    public final CouchbaseSession getSession(){ return _session; }
    public final void setSession(CouchbaseSession session){ _session = session; }

    public final String getDocumentKey(){ return _key.get(); }
    public final void setDocumentKey(String key){ _key.set(key);}

    public final Long getDocumentCas(){ return _cas; }
    public final void setDocumentCas(Long cas){ this._cas = cas; }

    public final Boolean getIsLocked(){ return _isLocked; }
    public final void setIsLocked(Boolean isLocked){ this._isLocked = isLocked; }

    public final Integer getDocumentDbSize(){ return _dbDocSize; }
    public final void setDocumentDbSize(Integer docSize){ this._dbDocSize = docSize; }

    public final Collection<CouchbaseConstants.DocumentFlag> getDocumentFlags(){ return _documentFlags; }
    public final Integer getDocumentEncodedFlags(){ return CouchbaseConstants.DocumentFlag.pack(_documentFlags); }
    public final void setDocumentEncodedFlags(Integer encodedFlags){ _documentFlags.clear(); _documentFlags.addAll(CouchbaseConstants.DocumentFlag.unPack(encodedFlags)); }
    public final void setDocumentFlags(Collection<CouchbaseConstants.DocumentFlag> flags){ _documentFlags.clear(); _documentFlags.addAll(flags); }
    public final void addDocumentEncodedFlags(Integer encodedFlags){ _documentFlags.addAll(CouchbaseConstants.DocumentFlag.unPack(encodedFlags)); }
    public final void addDocumentFlag(CouchbaseConstants.DocumentFlag flag){ _documentFlags.add(flag); }
    public final void addDocumentFlags(Collection<CouchbaseConstants.DocumentFlag> flags){ _documentFlags.addAll(flags); }
    public final void removeDocumentFlag(CouchbaseConstants.DocumentFlag flag){ _documentFlags.remove(flag); }
    public final void removeDocumentFlags(Collection<CouchbaseConstants.DocumentFlag> flags){_documentFlags.remove(flags); }
    public boolean hasDocumentFlag(CouchbaseConstants.DocumentFlag flag){ return _documentFlags.contains(flag); }

    public void setDocStateDirty(){
        if(_docState.equals(DocumentState.SYNC)){
            _docState = DocumentState.DIRTY;
        }
    }

    public void setDocStateDeleted(){
        _docState = DocumentState.DELETED;
    }

    public void setDocStateSync(){ _docState = DocumentState.SYNC; }
    public DocumentState getDocState(){ return _docState; }


    public boolean equals(BaseCouchbaseDocument doc){
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
                        "state  : "+ _docState +",\n"+
                        "flags : "+_documentFlags.toString();
    }

    public enum DocumentState {
        NEW,
        DIRTY,
        SYNC,
        DELETED;
    }
}
