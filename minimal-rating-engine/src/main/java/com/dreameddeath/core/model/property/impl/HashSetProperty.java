package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.common.BaseCouchbaseDocument;
import com.dreameddeath.core.model.common.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.property.HasParentDocumentElement;
import com.dreameddeath.core.model.property.SetProperty;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ceaj8230 on 08/08/2014.
 */
public class HashSetProperty<T> extends HashSet<T> implements SetProperty<T>,HasParentDocumentElement {
    BaseCouchbaseDocumentElement _parentElt;

    public HashSetProperty(BaseCouchbaseDocumentElement parentElement){
        _parentElt=parentElement;
    }
    public void setParentDocumentElement(BaseCouchbaseDocumentElement parentElement){ _parentElt=parentElement;}
    public BaseCouchbaseDocumentElement getParentDocumentElement(){return _parentElt;}

    protected boolean dirtyParent(){
        BaseCouchbaseDocument rootDoc = _parentElt.getParentDocument();
        if(rootDoc!=null){ rootDoc.getBaseMeta().setStateDirty();}
        return true;
    }

    @Override
    public void clear(){
        dirtyParent();
        super.clear();
    }

    @Override
    public boolean remove(Object elt){
        boolean res = super.remove(elt);
        if(res){
            dirtyParent();
            if((elt!=null) && (elt instanceof HasParentDocumentElement)){
                ((HasParentDocumentElement) elt).setParentDocumentElement(null);
            }
        }
        return res;
    }

    @Override
    public boolean removeAll(Collection<?> elts){
        for(Object elt:elts) {
            if ((elt != null) && (elt instanceof HasParentDocumentElement)) {
                ((HasParentDocumentElement) elt).setParentDocumentElement(null);
            }
        }
        return super.removeAll(elts);
    }

    @Override
    public boolean add(T elt){
        dirtyParent();
        if((elt!=null) && (elt instanceof HasParentDocumentElement)){
            ((HasParentDocumentElement) elt).setParentDocumentElement(_parentElt);
        }
        return super.add(elt);
    }

    @Override
    public boolean addAll(Collection<? extends T> elts){
        dirtyParent();
        for(T elt:elts) {
            if ((elt != null) && (elt instanceof HasParentDocumentElement)) {
                ((HasParentDocumentElement) elt).setParentDocumentElement(_parentElt);
            }
        }
        return super.addAll(elts);
    }


    public Set<T> get(){
        return Collections.unmodifiableSet(this);
    }

    public boolean set(Collection<T> list){
        clear();
        return addAll(list);
    }

    public boolean set(Set<T> list){return set((Collection<T>)list);    }
}
