package com.dreameddeath.core.model.common;

import com.dreameddeath.core.dao.CouchbaseSession;
import com.dreameddeath.core.exception.dao.InconsistentStateException;
import com.dreameddeath.core.storage.CouchbaseConstants;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by ceaj8230 on 11/09/2014.
 */
public class BaseCouchbaseDocument extends BaseCouchbaseDocumentElement {
    private BaseMetaInfo _meta;

    public BaseMetaInfo getBaseMeta(){return _meta;}
    public void setBaseMeta(BaseMetaInfo meta){ _meta=meta;}

    public BaseCouchbaseDocument(){_meta=this.new BaseMetaInfo();}
    public BaseCouchbaseDocument(BaseMetaInfo meta){_meta=meta;}

    public class BaseMetaInfo {
        private CouchbaseSession _session;
        private String _key;
        private long   _cas;
        private Boolean _isLocked;
        private Integer _dbDocSize;
        private Collection<CouchbaseConstants.DocumentFlag> _flags =new HashSet<CouchbaseConstants.DocumentFlag>();
        private int _expiry;
        private DocumentState _docState = DocumentState.NEW;

        public final CouchbaseSession getSession(){ return _session; }
        public final void setSession(CouchbaseSession session){ _session = session; }

        public final String getKey(){ return _key; }
        public final void setKey(String key){ _key=key;}

        public final long getCas(){ return _cas; }
        public final void setCas(long cas){ this._cas = cas; }

        public final Boolean getIsLocked(){ return _isLocked; }
        public final void setIsLocked(Boolean isLocked){ this._isLocked = isLocked; }

        public final Integer getDbSize(){ return _dbDocSize; }
        public final void setDbSize(Integer docSize){ this._dbDocSize = docSize; }

        public final Collection<CouchbaseConstants.DocumentFlag> getFlags(){ return _flags; }
        public final Integer getEncodedFlags(){ return CouchbaseConstants.DocumentFlag.pack(_flags); }
        public final void setEncodedFlags(Integer encodedFlags){ _flags.clear(); _flags.addAll(CouchbaseConstants.DocumentFlag.unPack(encodedFlags)); }
        public final void setFlags(Collection<CouchbaseConstants.DocumentFlag> flags){ _flags.clear(); _flags.addAll(flags); }
        public final void addEncodedFlags(Integer encodedFlags){ _flags.addAll(CouchbaseConstants.DocumentFlag.unPack(encodedFlags)); }
        public final void addFlag(CouchbaseConstants.DocumentFlag flag){ _flags.add(flag); }
        public final void addFlags(Collection<CouchbaseConstants.DocumentFlag> flags){ _flags.addAll(flags); }
        public final void removeFlag(CouchbaseConstants.DocumentFlag flag){ _flags.remove(flag); }
        public final void removeFlags(Collection<CouchbaseConstants.DocumentFlag> flags){_flags.remove(flags); }
        public boolean hasFlag(CouchbaseConstants.DocumentFlag flag){ return _flags.contains(flag); }


        public int getExpiry(){return _expiry;}
        public void setExpiry(int expiry){ _expiry=expiry;}

        public void setStateDirty(){
            if(_docState.equals(DocumentState.SYNC)){
                _docState = DocumentState.DIRTY;
            }
        }

        public void setStateDeleted(){
            _docState = DocumentState.DELETED;
        }

        public void setStateSync(){ _docState = DocumentState.SYNC; }
        public DocumentState getState(){ return _docState; }



        @Override
        public String toString(){
            return
                    "key   : "+_key+",\n"+
                    "cas   : "+_cas+",\n"+
                    "lock  : "+_isLocked+",\n"+
                    "size  : "+_dbDocSize+",\n"+
                    "state : "+_docState +",\n"+
                    "flags : "+ _flags.toString();
        }

    }

    public boolean equals(BaseCouchbaseDocument doc){
        if     (doc == null){ return false;}
        else if(doc == this){ return true; }
        else if(_meta.getKey()!=null) { return _meta.getKey().equals(doc._meta.getKey()); }
        else                { return false; }
    }

    public enum DocumentState {
        NEW,
        DIRTY,
        SYNC,
        DELETED;
    }

}
