package com.dreameddeath.common.model;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonAutoDetect(getterVisibility=Visibility.NONE,fieldVisibility=Visibility.NONE)
public abstract class CouchbaseDocumentLink<T extends CouchbaseDocument> extends CouchbaseDocumentElement{
    @JsonProperty("key")
    private Property<String>    _key=new SynchronizedLinkProperty<String,T>(CouchbaseDocumentLink.this){
        @Override
        protected  String getRealValue(T doc){
            return doc.getKey();
        }
    };
    private Property<T>            _docObject=new ImmutableProperty<T>(null);
    
    public final String getKey(){ return _key.get();}
    public final void setKey(String key){ _key.set(key); }
    
    public T getLinkedObject(){
        return getLinkedObject(true);
    }
    
    public T getLinkedObject(boolean fromCache){
        if((_docObject.get()==null) && (fromCache==true)){
            if(_key==null){
                ///TODO throw an error
            }
            else{
                CouchbaseDocument parentDoc = getParentDocument();
                if((parentDoc!=null) && parentDoc.getSession()!=null){
                    _docObject.set((T)parentDoc.getSession().get(_key.get()));
                }
            }
        }
        return _docObject.get();
    }
    
    public void setLinkedObject(T docObj){ 
        _docObject.set(docObj);
        docObj.addReverseLink(this);
    }
    
    
    public CouchbaseDocumentLink(){}
    public CouchbaseDocumentLink(T targetDoc){
        if(targetDoc.getKey()!=null){
            setKey(targetDoc.getKey());
        }
        setLinkedObject(targetDoc);
    }
    
    public CouchbaseDocumentLink(CouchbaseDocumentLink<T> srcLink){
        setKey(srcLink.getKey());
        setLinkedObject(srcLink.getLinkedObject());
    }
    
    
    public boolean equals(CouchbaseDocumentLink<T> target){
        if(target==null){
            return false;
        }
        else if(this == target){
            return true;
        }
        else if((_key!=null) && (target._key!=null)){
            return _key.equals(target._key);
        }
        else if((_docObject!=null)&& (target._docObject!=null)){
            return _docObject.equals(target._docObject);
        }
        else{
            return false;
        }
    }
    
    @Override
    public String toString(){
        return "key : "+getKey();
    }
    
    @Override
    public boolean validate(){
        boolean result = super.validate();
        if(_key.get()==null){
            result&=false;
        }
        return result;
    }
}
