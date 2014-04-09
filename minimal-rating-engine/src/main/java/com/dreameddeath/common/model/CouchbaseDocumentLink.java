package com.dreameddeath.common.model;

import java.util.HashSet;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonAutoDetect(getterVisibility=Visibility.NONE,fieldVisibility=Visibility.NONE)
public abstract class CouchbaseDocumentLink<T extends CouchbaseDocument> extends CouchbaseDocumentElement{
    @JsonProperty("key")
    private String _key;
    private T      _docObject;
    
    public final String getKey(){ return _key;}
    public final void setKey(String key){ this._key = key; }
    
    public T getLinkedObject(){ return _docObject; }
    public void setLinkedObject(T docObj){ 
        if(_docObject!=null){
            _docObject.removeReverseLink(this);
        }
        _docObject=docObj;
        docObj.addReverseLink(this);
    }
    
    
    public CouchbaseDocumentLink(){}
    public CouchbaseDocumentLink(T targetDoc){
        if(targetDoc.getKey()!=null){
            setKey(targetDoc.getKey());
        }
        setLinkedObject(targetDoc);
    }
    
    @Override
    public String toString(){
        return "key : "+getKey();
    }
}
