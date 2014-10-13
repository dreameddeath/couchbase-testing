package com.dreameddeath.core.model.common;

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
public abstract class BaseCouchbaseDocumentElement implements HasParentDocumentElement{
    private Property<BaseCouchbaseDocumentElement> _parentElt=new StandardProperty<BaseCouchbaseDocumentElement>(null);
    private List<BaseCouchbaseDocumentElement> _childElementList=new ArrayList<BaseCouchbaseDocumentElement>();

    protected void addChildElement(BaseCouchbaseDocumentElement elt){
        _childElementList.add(elt);
    }
    protected void removeChildElement(BaseCouchbaseDocumentElement elt){
        _childElementList.remove(elt);
    }

    public <T extends BaseCouchbaseDocumentElement> List<T> getChildElementsOfType(Class<T> clazz){
        List<T> res=new ArrayList<T>();
        for(BaseCouchbaseDocumentElement child : _childElementList){
            if(clazz.isAssignableFrom(child.getClass())){
                res.add((T)child);
            }
            res.addAll(child.getChildElementsOfType(clazz));
        }
        return res;
    }
    
    public BaseCouchbaseDocumentElement getParentDocumentElement() { return _parentElt.get();}
    public void setParentDocumentElement(BaseCouchbaseDocumentElement parentElt) {
        if(_parentElt.get()!=null){
            _parentElt.get().removeChildElement(this);
        }
        _parentElt.set(parentElt);
        if(parentElt!=null) {
            parentElt.addChildElement(this);
        }
    }
    
    public  BaseCouchbaseDocument getParentDocument() {
        if(this instanceof BaseCouchbaseDocument){
            return (BaseCouchbaseDocument)this;
        }
        else if(_parentElt.get() !=null){
            return _parentElt.get().getParentDocument();
        }
        return null;
    }

    public void dirtyDocument(){
        BaseCouchbaseDocument doc = getParentDocument();
        if(doc!=null){
            doc.getBaseMeta().setStateDirty();
        }
    }

    public <T extends BaseCouchbaseDocumentElement> T getFirstParentOfClass(Class<T> clazz){
        if(_parentElt.get()!=null){
            if(clazz.isAssignableFrom(_parentElt.get().getClass())){
                return (T) (_parentElt.get());
            }
            else{
                return _parentElt.get().getFirstParentOfClass(clazz);
            }
        }
        return null;
    }
}
