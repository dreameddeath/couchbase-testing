package com.dreameddeath.common.model;

import java.util.HashSet;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonAutoDetect(getterVisibility=Visibility.NONE,fieldVisibility=Visibility.NONE)
public abstract class CouchbaseDocumentLink<T extends CouchbaseDocument>{
    @JsonProperty("key")
    private String _key;
    private T      _docObject;
    private CouchbaseDocument _docSrc;
    
    public final String getKey(){ return _key;}
    public final void setKey(String key){ this._key = key; }
    
    public T getLinkedObject(){ return _docObject; }
    public void setLinkedObject(T docObj){ _docObject=docObj; docObj.addReverseLink(this);}
    
    
    public CouchbaseDocumentLink(){}
    public CouchbaseDocumentLink(T targetDoc){
        setKey(targetDoc.getKey());
        setLinkedObject(targetDoc);
    }
    
    public void setSourceObject(CouchbaseDocument srcDoc){
        _docSrc=srcDoc;
    }
    
    public CouchbaseDocument getSourceObject(){
        return _docSrc;
    }
    
    
    @Override
    public String toString(){
        return "key : "+getKey();
    }
}
