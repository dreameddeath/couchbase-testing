package com.dreameddeath.core.model.document;

import com.dreameddeath.core.model.property.HasParentDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.ArrayList;
import java.util.List;


@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonAutoDetect(getterVisibility=Visibility.NONE,fieldVisibility=Visibility.NONE,isGetterVisibility = Visibility.NONE,setterVisibility = Visibility.NONE,creatorVisibility = Visibility.NONE)
public abstract class CouchbaseDocumentElement implements HasParentDocumentElement{
    private Property<CouchbaseDocumentElement> _parentElt=new StandardProperty<CouchbaseDocumentElement>(null);
    private List<CouchbaseDocumentElement> _childElementList=new ArrayList<CouchbaseDocumentElement>();
    
    protected void addChildElement(CouchbaseDocumentElement elt){
        _childElementList.add(elt);
    }
    protected void removeChildElement(CouchbaseDocumentElement elt){
        _childElementList.remove(elt);
    }

    public <T extends CouchbaseDocumentElement> List<T> getChildElementsOfType(Class<T> clazz){
        List<T> res=new ArrayList<T>();
        for(CouchbaseDocumentElement child : _childElementList){
            if(clazz.isAssignableFrom(child.getClass())){
                res.add((T)child);
            }
            res.addAll(child.getChildElementsOfType(clazz));
        }
        return res;
    }
    
    public CouchbaseDocumentElement getParentDocumentElement() { return _parentElt.get();}
    public void setParentDocumentElement(CouchbaseDocumentElement parentElt) {
        if(_parentElt.get()!=null){
            _parentElt.get().removeChildElement(this);
        }
        _parentElt.set(parentElt);
        if(parentElt!=null) {
            parentElt.addChildElement(this);
        }
    }
    
    public CouchbaseDocument getParentDocument() { 
        if(this instanceof CouchbaseDocument){
            return (CouchbaseDocument)this;
        }
        else if(_parentElt.get() !=null){
            return _parentElt.get().getParentDocument();
        }
        return null;
    }

    public void dirtyDocument(){
        CouchbaseDocument doc = getParentDocument();
        if(doc!=null){
            doc.setDocStateDirty();
        }
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
}
