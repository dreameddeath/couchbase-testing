package com.dreameddeath.common.storage;


import com.fasterxml.jackson.annotation.JsonIgnore;
import net.spy.memcached.transcoders.Transcoder;

public abstract class CouchbaseDocument{
    private String _key;
    private Long   _cas;
    private Boolean _isLocked;
    private Integer _dbDocSize;
    
    @JsonIgnore
    public final String getKey(){ return _key;}
    public final void setKey(String key){ this._key = key; }

    @JsonIgnore
    public final Long getCas(){ return _cas;}
    public final void setCas(Long cas){ this._cas = cas; }
    
    @JsonIgnore
    public final Boolean getIsLocked(){ return _isLocked;}
    public final void setIsLocked(Boolean isLocked){ this._isLocked = isLocked; }

    @JsonIgnore
    public final Integer getDbDocSize(){ return _dbDocSize;}
    public final void setDbDocSize(Integer docSize){ this._dbDocSize = docSize; }

    @JsonIgnore
    public abstract Transcoder<? extends CouchbaseDocument> getTranscoder();
}
