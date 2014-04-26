package com.dreameddeath.common.model;

import java.util.HashSet;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;


@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(Include.NON_EMPTY)
@JsonAutoDetect(getterVisibility=Visibility.NONE,fieldVisibility=Visibility.NONE)
public abstract class CouchbaseDocumentElement{
    private ImmutableProperty<CouchbaseDocumentElement> _parentElt=new ImmutableProperty<CouchbaseDocumentElement>(null);
    
    public CouchbaseDocumentElement getParentElement() { return _parentElt.get();}
    public void setParentElement(CouchbaseDocumentElement parentElt) {  _parentElt.set(parentElt); }
    
    public CouchbaseDocument getParentDocument() { 
        if(this instanceof CouchbaseDocument){
            return (CouchbaseDocument)this;
        }
        else if(_parentElt.get() !=null){
            return _parentElt.get().getParentDocument();
        }
        return null;
    }
    
    public <T extends CouchbaseDocumentElement> T getFirstParentOfClass(Class<T> clazz){
        if(_parentElt!=null){
            if(_parentElt.getClass().equals(clazz)){
                return (T) (_parentElt.get());
            }
            else{
                return _parentElt.get().getFirstParentOfClass(clazz);
            }
        }
        return null;
    }
    
    public boolean validate(){
        return true;
    }
    
}
